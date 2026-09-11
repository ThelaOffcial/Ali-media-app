package com.example.data.repository

import com.example.R
import com.example.data.model.Elephant
import com.example.data.model.ElephantPost
import com.example.data.model.ElephantType
import com.example.data.model.FeedbackType
import com.example.data.model.Notice
import com.example.data.model.PlatformFeedback
import com.example.data.model.PostComment
import com.example.data.model.Story
import com.example.data.model.UserProfile
import com.example.data.remote.FirebaseRtdbService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AliMediaRepository {

    private val rtdbService = FirebaseRtdbService()
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Elephants list (fetched live from Firebase RTDB)
    private val _elephants = MutableStateFlow<List<Elephant>>(emptyList())
    val elephants: StateFlow<List<Elephant>> = _elephants.asStateFlow()

    // Posts list (fetched live from Firebase RTDB)
    private val _posts = MutableStateFlow<List<ElephantPost>>(emptyList())
    val posts: StateFlow<List<ElephantPost>> = _posts.asStateFlow()

    // Stories list (fetched live from Firebase RTDB)
    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    // Notices list
    private val _notices = MutableStateFlow<List<Notice>>(getSeedNotices())
    val notices: StateFlow<List<Notice>> = _notices.asStateFlow()

    // Feedback list
    private val _feedbacks = MutableStateFlow<List<PlatformFeedback>>(getSeedFeedbacks())
    val feedbacks: StateFlow<List<PlatformFeedback>> = _feedbacks.asStateFlow()

    // Current User Profile (Owner & Developer of AliMedia)
    private val _currentUser = MutableStateFlow(
        UserProfile(
            uid = "Y2YgwbSQAtSHdAmBuJa9AxQ2cdc2",
            displayName = "Samithu",
            username = "samithudinildewapriya",
            email = "samithudinildewapriya@gmail.com",
            photoURL = "https://res.cloudinary.com/drmmn0xp3/image/upload/v1787848751/alimedia_uploads/afxycvbkqox1j653fuhh.jpg",
            bio = "AliMedia Creator & Wildlife Conservationist. Dedicated to Sri Lankan tuskers and elephants. 🐘🇱🇰",
            bioSinhala = "අලිMedia නිර්මාතෘ සහ වනජීවී සංරක්ෂණවේදී. ශ්‍රී ලාංකේය ඇතුන් සහ අලි රැකගැනීම සඳහා. 🐘🇱🇰",
            followedElephants = listOf("-P02YqMogpGcK14IRT3q", "-P02_XSyX_5LQ3Mu3T_J", "-P02eXxxyNbRDEbnfR4a"),
            postsCount = 7,
            followersCount = 820,
            followingCount = 14
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    init {
        // Ensure Firebase Auth session exists (required for RTDB writes)
        // then automatically sync with the live Firebase Realtime Database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                com.example.data.remote.FirebaseAuthService.instance.ensureSignedIn()
            } catch (e: Exception) {
                // Reads still work without auth; writes will retry later
            }
            refreshFromDatabase()
        }
    }

    suspend fun syncWithRealtimeDatabase() {
        _isSyncing.value = true
        try {
            val currentUid = _currentUser.value.uid
            val liveElephants = rtdbService.fetchElephants()
            val liveElephantLikes = rtdbService.fetchElephantLikes(currentUid)
            val livePostLikes = rtdbService.fetchPostLikes(currentUid)
            val liveComments = rtdbService.fetchComments()
            val livePosts = rtdbService.fetchPosts(liveComments, livePostLikes)

            if (liveElephants.isNotEmpty()) {
                val updatedElephants = liveElephants.map { el ->
                    val likeInfo = liveElephantLikes[el.id]
                    val likes = likeInfo?.first ?: 0
                    val isFollowed = likeInfo?.second ?: el.isFollowedByMe
                    el.copy(
                        followerCount = maxOf(el.followerCount, likes),
                        isFollowedByMe = isFollowed
                    )
                }
                _elephants.value = updatedElephants
            } else {
                _elephants.value = getSeedElephants()
            }

            if (livePosts.isNotEmpty()) {
                _posts.value = livePosts

                // Extract active stories from live posts
                val liveStories = livePosts
                    .filter { it.isStory && it.photoUrl.isNotBlank() }
                    .map { p ->
                        Story(
                            id = "story_${p.id}",
                            authorUid = p.authorUid,
                            authorName = p.authorName,
                            authorUsername = p.authorUsername,
                            authorPhotoUrl = p.authorPhotoURL,
                            mediaUrl = p.photoUrl,
                            caption = p.caption,
                            createdAt = p.createdAt,
                            elephantId = p.elephantId,
                            elephantName = p.elephantName,
                            elephantSinhalaName = p.elephantSinhalaName
                        )
                    }

                if (liveStories.isNotEmpty()) {
                    _stories.value = liveStories
                } else {
                    _stories.value = getSeedStories()
                }
            } else {
                _posts.value = getSeedPosts()
                _stories.value = getSeedStories()
            }
        } catch (e: Exception) {
            android.util.Log.e("AliMediaRepository", "Error syncing with RTDB", e)
            if (_elephants.value.isEmpty()) _elephants.value = getSeedElephants()
            if (_posts.value.isEmpty()) _posts.value = getSeedPosts()
            if (_stories.value.isEmpty()) _stories.value = getSeedStories()
        } finally {
            _isSyncing.value = false
        }
    }

    fun refreshFromDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            syncWithRealtimeDatabase()
        }
    }

    fun toggleLikePost(postId: String) {
        val post = _posts.value.find { it.id == postId } ?: return
        val currentlyLiked = post.isLikedByMe
        // Optimistic UI update
        _posts.value = _posts.value.map {
            if (it.id == postId) {
                val newLiked = !currentlyLiked
                val newCount = if (newLiked) it.likesCount + 1 else maxOf(0, it.likesCount - 1)
                it.copy(isLikedByMe = newLiked, likesCount = newCount)
            } else it
        }
        // Persist to exact RTDB path used by the website
        CoroutineScope(Dispatchers.IO).launch {
            val user = _currentUser.value
            val (newLiked, newCount) = rtdbService.togglePostLike(postId, user.uid, currentlyLiked)
            // Reconcile with server result
            _posts.value = _posts.value.map {
                if (it.id == postId) it.copy(isLikedByMe = newLiked, likesCount = newCount)
                else it
            }
        }
    }

    fun addComment(postId: String, text: String) {
        if (text.isBlank()) return
        val user = _currentUser.value
        val optimistic = PostComment(
            id = "temp_${UUID.randomUUID()}",
            authorName = user.displayName,
            text = text.trim()
        )
        // Optimistic UI
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(
                    commentsCount = post.commentsCount + 1,
                    comments = listOf(optimistic) + post.comments
                )
            } else post
        }
        // Persist to comments/{postId}/{id} – same path as website
        CoroutineScope(Dispatchers.IO).launch {
            val remote = rtdbService.addComment(
                postId = postId,
                authorUid = user.uid,
                authorName = user.displayName,
                authorUsername = user.username,
                authorPhotoURL = user.photoURL,
                text = text.trim()
            )
            if (remote != null) {
                _posts.value = _posts.value.map { post ->
                    if (post.id == postId) {
                        val updatedComments = post.comments.map {
                            if (it.id == optimistic.id) remote else it
                        }
                        post.copy(comments = updatedComments)
                    } else post
                }
            }
        }
    }

    fun toggleFollowElephant(elephantId: String) {
        val elephant = _elephants.value.find { it.id == elephantId } ?: return
        val currentlyFollowed = elephant.isFollowedByMe
        // Optimistic UI
        _elephants.value = _elephants.value.map {
            if (it.id == elephantId) {
                val newFollow = !currentlyFollowed
                val newCount = if (newFollow) it.followerCount + 1 else maxOf(0, it.followerCount - 1)
                it.copy(isFollowedByMe = newFollow, followerCount = newCount)
            } else it
        }
        val currentFollowed = _currentUser.value.followedElephants.toMutableList()
        if (currentlyFollowed) currentFollowed.remove(elephantId) else currentFollowed.add(elephantId)
        _currentUser.value = _currentUser.value.copy(
            followedElephants = currentFollowed,
            followingCount = currentFollowed.size
        )
        // Persist to elephant_likes/{id} + users/{uid}/followedElephants
        CoroutineScope(Dispatchers.IO).launch {
            val user = _currentUser.value
            val (newFollowed, newCount) = rtdbService.toggleFollowElephant(
                elephantId, user.uid, currentlyFollowed
            )
            _elephants.value = _elephants.value.map {
                if (it.id == elephantId) it.copy(isFollowedByMe = newFollowed, followerCount = newCount)
                else it
            }
        }
    }

    fun createPost(
        photoUrl: String,
        drawableRes: Int?,
        caption: String,
        elephantId: String?,
        isStoryOnly: Boolean,
        aspectRatio: String
    ) {
        val user = _currentUser.value
        val targetElephant = _elephants.value.find { it.id == elephantId }
        val tempId = "temp_${UUID.randomUUID()}"

        val newPost = ElephantPost(
            id = tempId,
            elephantId = elephantId,
            elephantName = targetElephant?.name,
            elephantSinhalaName = targetElephant?.sinhalaName,
            photoUrl = photoUrl,
            drawableResId = drawableRes ?: R.drawable.img_hero_tuskers,
            caption = caption,
            authorUid = user.uid,
            authorName = user.displayName,
            authorUsername = user.username,
            authorPhotoURL = user.photoURL,
            authorIsAliMedia = false,
            likesCount = 0,
            isLikedByMe = false,
            commentsCount = 0,
            isStory = true,
            isStoryOnly = isStoryOnly,
            aspectRatio = aspectRatio,
            createdAt = System.currentTimeMillis()
        )

        if (!isStoryOnly) {
            _posts.value = listOf(newPost) + _posts.value
            _currentUser.value = user.copy(postsCount = user.postsCount + 1)
        }

        // Add to stories tray
        val newStory = Story(
            id = "story_$tempId",
            authorUid = user.uid,
            authorName = user.displayName,
            authorUsername = user.username,
            authorPhotoUrl = user.photoURL,
            elephantId = elephantId,
            elephantName = targetElephant?.name,
            elephantSinhalaName = targetElephant?.sinhalaName,
            mediaUrl = photoUrl,
            drawableResId = drawableRes ?: R.drawable.img_hero_tuskers,
            caption = caption,
            createdAt = System.currentTimeMillis()
        )
        _stories.value = listOf(newStory) + _stories.value

        // Persist to elephant_posts/{id} – exact same path as website
        CoroutineScope(Dispatchers.IO).launch {
            val remoteId = rtdbService.createPost(
                authorUid = user.uid,
                authorName = user.displayName,
                authorUsername = user.username,
                authorPhotoURL = user.photoURL,
                photoUrl = photoUrl,
                caption = caption,
                elephantId = elephantId,
                elephantName = targetElephant?.name,
                elephantSinhalaName = targetElephant?.sinhalaName,
                isStory = true,
                isStoryOnly = isStoryOnly,
                aspectRatio = aspectRatio
            )
            if (remoteId != null && !isStoryOnly) {
                _posts.value = _posts.value.map {
                    if (it.id == tempId) it.copy(id = remoteId) else it
                }
            }
        }
    }

    fun submitFeedback(type: FeedbackType, message: String) {
        if (message.isBlank()) return
        val user = _currentUser.value
        val item = PlatformFeedback(
            id = UUID.randomUUID().toString(),
            type = type,
            message = message.trim(),
            authorUid = user.uid,
            authorName = user.displayName,
            authorPhotoURL = user.photoURL,
            createdAt = System.currentTimeMillis(),
            likesCount = 0,
            isLikedByMe = false,
            status = "open"
        )
        _feedbacks.value = listOf(item) + _feedbacks.value
    }

    fun toggleLikeFeedback(feedbackId: String) {
        _feedbacks.value = _feedbacks.value.map { fb ->
            if (fb.id == feedbackId) {
                val newLiked = !fb.isLikedByMe
                val count = if (newLiked) fb.likesCount + 1 else maxOf(0, fb.likesCount - 1)
                fb.copy(isLikedByMe = newLiked, likesCount = count)
            } else fb
        }
    }

    private fun getSeedElephants(): List<Elephant> = listOf(
        Elephant(
            id = "nadungamuwa_raja",
            name = "Nadungamuwa Raja",
            sinhalaName = "නැදුන්ගමුවේ රාජා",
            location = "Gampaha, Western Province",
            locationSinhala = "ගම්පහ, බස්නාහිර පළාත",
            type = ElephantType.TUSKER,
            isLive = false,
            drawableResId = R.drawable.img_nadungamuwa_raja,
            description = "The most revered tusker of contemporary Sri Lanka. For nearly two decades, Nadungamuwa Raja carried the Sacred Tooth Relic casket (Dalada Karanduwa) in the historic Esala Perahera in Kandy.",
            descriptionSinhala = "ශ්‍රී ලංකාවේ වර්තමාන ඉතිහාසයේ විසූ උත්තම ගණයේ හස්තිරාජයෙකි. දශක දෙකකට ආසන්න කාලයක් මහනුවර ඓතිහාසික ඇසළ පෙරහැරේ ශ්‍රී දන්ත ධාතු කරඬුව වැඩමවීමේ පූජනීය භාග්‍යය ලැබුවේය.",
            age = 68,
            templeOwner = "Dr. Harsha Dharmavijaya (Nadungamuwa)",
            templeOwnerSinhala = "වෛද්‍ය හර්ෂ ධර්මවිජය (නැදුන්ගමුව)",
            followerCount = 14200,
            isFollowedByMe = true
        ),
        Elephant(
            id = "wasana_tusker",
            name = "Kataragama Wasana",
            sinhalaName = "රුහුණු කතරගම වාසනා ඇතා",
            location = "Kataragama, Southern Province",
            locationSinhala = "කතරගම, දකුණු පළාත",
            type = ElephantType.TUSKER,
            isLive = true,
            drawableResId = R.drawable.img_hero_tuskers,
            description = "Chief casket bearer tusker of the Ruhunu Maha Kataragama Devalaya. Known for calm demeanor and grand arched tusks.",
            descriptionSinhala = "රුහුණු මහා කතරගම දේවාලයේ පෙරහැර කරඬුව වැඩමවන ප්‍රධාන හස්තියා. ඉතා ශාන්ත ගතිපැවතුම් සහ දිගු දළ යුගලක් හිමි ගෞරවනීය ඇතෙකි.",
            age = 52,
            templeOwner = "Ruhunu Maha Kataragama Devalaya",
            templeOwnerSinhala = "රුහුණු මහා කතරගම දේවාලය",
            followerCount = 8900,
            isFollowedByMe = true
        ),
        Elephant(
            id = "indiraja",
            name = "Kandy Indiraja",
            sinhalaName = "ශ්‍රී දළදා මාලිගාවේ ඉන්දිරාජා",
            location = "Kandy, Central Province",
            locationSinhala = "මහනුවර, මධ්‍යම පළාත",
            type = ElephantType.TUSKER,
            isLive = true,
            drawableResId = R.drawable.img_hero_tuskers,
            description = "Majestic royal tusker of Sri Dalada Maligawa. Gifted by the Government of India in 1987, now one of the senior relic-bearing tuskers in Kandy.",
            descriptionSinhala = "ශ්‍රී දළදා මාලිගාවේ ප්‍රධාන හස්තිරාජයෙකි. 1987 වසරේදී ඉන්දීය රජය විසින් පූජා කරන ලද අතර දළදා පෙරහැරේ ප්‍රධාන කාර්යභාරයක් ඉටු කරයි.",
            age = 45,
            templeOwner = "Sri Dalada Maligawa, Kandy",
            templeOwnerSinhala = "ශ්‍රී දළදා මාලිගාව, මහනුවර",
            followerCount = 7450,
            isFollowedByMe = false
        ),
        Elephant(
            id = "millangoda_raja",
            name = "Millangoda Raja",
            sinhalaName = "මිල්ලන්ගොඩ රාජා",
            location = "Kegalle, Sabaragamuwa",
            locationSinhala = "කෑගල්ල, සබරගමුව",
            type = ElephantType.TUSKER,
            isLive = false,
            drawableResId = R.drawable.img_nadungamuwa_raja,
            description = "Celebrated as having had the longest tusks in Asia during his lifetime (over 7.5 feet). Carried the Sacred Tooth Relic with supreme dignity.",
            descriptionSinhala = "ආසියාවේ විසූ දිගම දළ යුගලක් හිමිවූ හස්තියා ලෙස ඉතිහාසයට එක්වූ මිල්ලන්ගොඩ රාජා. දළදා කරඬුව ගෞරවාන්විතව වැඩමවීය.",
            age = 73,
            templeOwner = "Millangoda Family",
            templeOwnerSinhala = "මිල්ලන්ගොඩ පරපුර",
            followerCount = 11200,
            isFollowedByMe = false
        ),
        Elephant(
            id = "kandula_bath",
            name = "Pinnawala Kandula",
            sinhalaName = "පින්නවල කණ්ඩුල",
            location = "Pinnawala, Rambukkana",
            locationSinhala = "පින්නවල, රඹුක්කන",
            type = ElephantType.ELEPHANT,
            isLive = true,
            drawableResId = R.drawable.img_kandula_bath,
            description = "Gentle giant resident at the famous Pinnawala river sanctuary. Beloved by visitors from around the globe for playful river baths.",
            descriptionSinhala = "පින්නවල අලි අනාථාගාරයේ සිටින ජනප්‍රිය හීලෑ අලියා. මා ඔයේ දිය කෙළින ආකාරය දෙස් විදෙස් සංචාරකයන්ගේ නෙත් සිත් ඇදගනී.",
            age = 31,
            templeOwner = "Department of National Zoological Gardens",
            templeOwnerSinhala = "ජාතික සත්වෝද්‍යාන දෙපාර්තමේන්තුව",
            followerCount = 5300,
            isFollowedByMe = false
        )
    )

    private fun getSeedPosts(): List<ElephantPost> = listOf(
        ElephantPost(
            id = "post_1",
            elephantId = "wasana_tusker",
            elephantName = "Kataragama Wasana",
            elephantSinhalaName = "රුහුණු කතරගම වාසනා ඇතා",
            drawableResId = R.drawable.img_hero_tuskers,
            caption = "රුහුණු මහා කතරගම ඓතිහාසික පෙරහැර මංගල්‍යයේදී වාසනා ඇතා දේවාභරණ කරඬුව වැඩමවූ අසිරිමත් මොහොත. සැබැවින්ම දර්ශනීය ගාම්භීර දසුනක්! 🐘✨🙏 #AliMedia #Tusker #Kataragama #SriLanka",
            authorUid = "user_kataragama_fan",
            authorName = "Sudath Wickramasinghe",
            authorUsername = "sudath_w",
            authorIsAliMedia = true,
            likesCount = 482,
            isLikedByMe = true,
            commentsCount = 24,
            comments = listOf(
                PostComment(
                    id = "c1",
                    authorName = "Nalaka Silva",
                    text = "සාදු සාදු! ඉතාම ගාම්භීර දසුනක්. වාසනා ඇතාට දීර්ඝායුෂ ලැබේවා!"
                ),
                PostComment(
                    id = "c2",
                    authorName = "Chamari Fernando",
                    text = "A true national treasure of Sri Lanka ❤️"
                )
            ),
            createdAt = System.currentTimeMillis() - (2 * 60 * 60 * 1000)
        ),
        ElephantPost(
            id = "post_2",
            elephantId = "nadungamuwa_raja",
            elephantName = "Nadungamuwa Raja",
            elephantSinhalaName = "නැදුන්ගමුවේ රාජා",
            drawableResId = R.drawable.img_nadungamuwa_raja,
            caption = "Remembering the pride of the nation: Nadungamuwa Raja on his peaceful morning walk. The unmatched grace and gentleness of this royal tusker will live forever in our hearts. 👑🐘 #NadungamuwaRaja #Legend #SriLankaElephants",
            authorUid = "user_gemini_sl",
            authorName = "Kavindu Perera",
            authorUsername = "kavindu_elephants",
            authorIsAliMedia = false,
            likesCount = 829,
            isLikedByMe = false,
            commentsCount = 41,
            comments = listOf(
                PostComment(
                    id = "c3",
                    authorName = "Ruwan Dissanayake",
                    text = "නැදුන්ගමුවේ රාජා වැනි තවත් ඇතෙකු මෙලොව පහළ නොවේ. නිවන් සුව ලැබේවා හස්තිරාජයාණෙනි."
                )
            ),
            createdAt = System.currentTimeMillis() - (6 * 60 * 60 * 1000)
        ),
        ElephantPost(
            id = "post_3",
            elephantId = "kandula_bath",
            elephantName = "Pinnawala Kandula",
            elephantSinhalaName = "පින්නවල කණ්ඩුල",
            drawableResId = R.drawable.img_kandula_bath,
            caption = "Cooling down in the waters of Maha Oya river this afternoon! Water bathing is an essential daily ritual for their skin health and happiness. 🌊☀️🌿 #Pinnawala #ElephantCare #SriLanka #Conservation",
            authorUid = "user_pinnawala_guide",
            authorName = "Dinesh Bandara",
            authorUsername = "dinesh_wildlife",
            authorIsAliMedia = false,
            likesCount = 312,
            isLikedByMe = true,
            commentsCount = 12,
            comments = emptyList(),
            createdAt = System.currentTimeMillis() - (12 * 60 * 60 * 1000)
        )
    )

    private fun getSeedStories(): List<Story> = listOf(
        Story(
            id = "story_1",
            authorUid = "admin_alimedia",
            authorName = "AliMedia Official",
            authorUsername = "alimedia",
            elephantId = "wasana_tusker",
            elephantName = "Kataragama Wasana",
            elephantSinhalaName = "වාසනා ඇතා",
            drawableResId = R.drawable.img_hero_tuskers,
            caption = "Live from Kandy Dalada Perahera rehearsals! 🛕",
            createdAt = System.currentTimeMillis() - (1 * 60 * 60 * 1000)
        ),
        Story(
            id = "story_2",
            authorUid = "user_gemini_sl",
            authorName = "Kavindu Perera",
            authorUsername = "kavindu_elephants",
            elephantId = "nadungamuwa_raja",
            elephantName = "Nadungamuwa Raja",
            elephantSinhalaName = "නැදුන්ගමුවේ රාජා",
            drawableResId = R.drawable.img_nadungamuwa_raja,
            caption = "Honoring our timeless legends today 🐘🌿",
            createdAt = System.currentTimeMillis() - (3 * 60 * 60 * 1000)
        ),
        Story(
            id = "story_3",
            authorUid = "user_pinnawala_guide",
            authorName = "Dinesh Bandara",
            authorUsername = "dinesh_wildlife",
            elephantId = "kandula_bath",
            elephantName = "Pinnawala Kandula",
            elephantSinhalaName = "පින්නවල කණ්ඩුල",
            drawableResId = R.drawable.img_kandula_bath,
            caption = "Afternoon river bath time! 💦",
            createdAt = System.currentTimeMillis() - (5 * 60 * 60 * 1000)
        )
    )

    private fun getSeedNotices(): List<Notice> = listOf(
        Notice(
            id = "notice_1",
            title = "Esala Perahera Schedule & Tusker Participation 2026",
            titleSinhala = "2026 ඓතිහාසික මහනුවර ඇසළ පෙරහැර මංගල්‍යය සහ ඇතුන් සහභාගීත්වය",
            message = "The Sri Dalada Maligawa has published the official dates and health guidelines for all domesticated elephants participating in the upcoming Esala Perahera. Medical inspections will take place next week.",
            messageSinhala = "මෙවර මහනුවර ඓතිහාසික ඇසළ මහා පෙරහැර සඳහා සහභාගී වන හීලෑ අලි ඇතුන්ගේ සෞඛ්‍ය පරීක්ෂණ සහ ආරක්ෂක මාර්ගෝපදේශ පිළිබඳ සම්පූර්ණ නිවේදනය නිකුත් කර ඇත.",
            tag = "Perahera Update",
            tagSinhala = "පෙරහැර පුවත්",
            date = "Today",
            isUrgent = true
        ),
        Notice(
            id = "notice_2",
            title = "Veterinary Care & Nutrition Workshop for Mahouts",
            titleSinhala = "ඇත්ගොව්වන් සඳහා නවීන පශු වෛද්‍ය සහ පෝෂණ වැඩමුළුව",
            message = "A special 3-day practical training workshop on modern elephant dietetics and foot care will be held at Pinnawala for all registered elephant owners and caretakers.",
            messageSinhala = "ලියාපදිංචි ඇත් හිමියන් සහ ඇත්ගොව්වන් සඳහා පාද සත්කාරය සහ පෝෂණ කළමනාකරණය පිළිබඳ තෙදින පුහුණු වැඩමුළුවක් පින්නවලදී පැවැත්වේ.",
            tag = "Welfare & Health",
            tagSinhala = "සුබසාධන හා සෞඛ්‍ය",
            date = "2 days ago",
            isUrgent = false
        )
    )

    private fun getSeedFeedbacks(): List<PlatformFeedback> = listOf(
        PlatformFeedback(
            id = "fb_1",
            type = FeedbackType.SUGGESTION,
            message = "It would be great to have an interactive map showing where the major Peraheras and temple processions are happening across Sri Lanka this month!",
            authorUid = "user_anuradhapura",
            authorName = "Kasun Jayasuriya",
            likesCount = 18,
            isLikedByMe = true,
            status = "reviewed"
        ),
        PlatformFeedback(
            id = "fb_2",
            type = FeedbackType.SUGGESTION,
            message = "කරුණාකර මෙරට විසූ ඓතිහාසික මාලිගාවේ රාජා සහ අනෙකුත් පැරණි ඇතුන්ගේ පැරණි කළු-සුදු ඡායාරූප එකතුවක්ද ඩිජිටල් සංරක්ෂණය සඳහා එක්කරන්න.",
            authorUid = "user_kandy",
            authorName = "Wasantha Senanayake",
            likesCount = 27,
            isLikedByMe = false,
            status = "open"
        ),
        PlatformFeedback(
            id = "fb_3",
            type = FeedbackType.COMPLAINT,
            message = "Photo upload preview on slower mobile connections took a few seconds. Client-side compression helped a lot though!",
            authorUid = "user_matara",
            authorName = "Nuwantha Perera",
            likesCount = 5,
            isLikedByMe = false,
            status = "reviewed"
        )
    )

    companion object {
        val instance by lazy { AliMediaRepository() }
    }
}
