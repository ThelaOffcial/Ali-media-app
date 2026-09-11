package com.example.data.remote

import android.util.Log
import com.example.data.model.Elephant
import com.example.data.model.ElephantPost
import com.example.data.model.ElephantType
import com.example.data.model.PostComment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class FirebaseRtdbService(
    private val databaseUrl: String = "https://aliapp-e5196-default-rtdb.firebaseio.com"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun fetchElephants(): List<Elephant> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Elephant>()
        try {
            val request = Request.Builder()
                .url("$databaseUrl/elephants.json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Failed to fetch elephants: HTTP ${response.code}")
                    return@withContext emptyList()
                }

                val bodyStr = response.body?.string() ?: return@withContext emptyList()
                if (bodyStr == "null" || bodyStr.isBlank()) return@withContext emptyList()

                val json = JSONObject(bodyStr)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val obj = json.optJSONObject(key) ?: continue

                    val name = obj.optString("name", "Elephant")
                    val sinhalaName = obj.optString("sinhalaName", name)
                    val location = obj.optString("location", "Sri Lanka")
                    val rawType = obj.optString("type", "tusker").lowercase()
                    val type = if (rawType == "tusker") ElephantType.TUSKER else ElephantType.ELEPHANT
                    val status = obj.optString("status", "living")
                    val isLive = status != "memorial"
                    val desc = obj.optString("description", "")
                    val organization = obj.optString("organization", obj.optString("mahout", "Sri Lanka"))
                    val customBadge = obj.optString("customBadge", "")
                    val gender = obj.optString("gender", "male")

                    // Age parsing (could be number or string or empty)
                    val age = when (val rawAge = obj.opt("age")) {
                        is Number -> rawAge.toInt()
                        is String -> rawAge.toIntOrNull() ?: 45
                        else -> 45
                    }

                    val followerCount = when (val rawF = obj.opt("followerCount")) {
                        is Number -> rawF.toInt()
                        is String -> rawF.toIntOrNull() ?: 0
                        else -> 0
                    }

                    // Photos
                    val photos = mutableListOf<String>()
                    val photoArr = obj.optJSONArray("photos")
                    if (photoArr != null) {
                        for (i in 0 until photoArr.length()) {
                            val url = photoArr.optString(i)
                            if (url.isNotBlank() && url.startsWith("http")) photos.add(url)
                        }
                    }
                    if (photos.isEmpty()) {
                        val cPhotoArr = obj.optJSONArray("cloudinaryPhotos")
                        if (cPhotoArr != null) {
                            for (i in 0 until cPhotoArr.length()) {
                                val cObj = cPhotoArr.optJSONObject(i)
                                val url = cObj?.optString("url") ?: ""
                                if (url.isNotBlank() && url.startsWith("http")) photos.add(url)
                            }
                        }
                    }

                    // Photos come only from RTDB / Cloudinary — no local image fallbacks
                    result.add(
                        Elephant(
                            id = key,
                            name = name,
                            sinhalaName = sinhalaName.ifBlank { name },
                            location = location,
                            locationSinhala = location,
                            type = type,
                            isLive = isLive,
                            photos = photos,
                            drawableResId = null,
                            description = desc,
                            descriptionSinhala = desc,
                            age = age,
                            gender = gender,
                            templeOwner = organization,
                            templeOwnerSinhala = organization,
                            followerCount = followerCount,
                            isFollowedByMe = false,
                            customBadge = customBadge
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching elephants from RTDB", e)
        }
        result
    }

    suspend fun fetchElephantLikes(currentUserUid: String): Map<String, Pair<Int, Boolean>> = withContext(Dispatchers.IO) {
        val likesMap = mutableMapOf<String, Pair<Int, Boolean>>()
        try {
            val request = Request.Builder()
                .url("$databaseUrl/elephant_likes.json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyMap()
                val bodyStr = response.body?.string() ?: return@withContext emptyMap()
                if (bodyStr == "null" || bodyStr.isBlank()) return@withContext emptyMap()

                val json = JSONObject(bodyStr)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val obj = json.optJSONObject(key) ?: continue
                    val count = obj.optInt("likesCount", 0)
                    val likedByObj = obj.optJSONObject("likedBy")
                    val likedByCount = likedByObj?.length() ?: 0
                    val totalLikes = maxOf(count, likedByCount)
                    val isLikedByMe = likedByObj?.optBoolean(currentUserUid, false) ?: false
                    likesMap[key] = Pair(totalLikes, isLikedByMe)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching elephant likes from RTDB", e)
        }
        likesMap
    }

    suspend fun fetchPostLikes(currentUserUid: String): Map<String, Pair<Int, Boolean>> = withContext(Dispatchers.IO) {
        val likesMap = mutableMapOf<String, Pair<Int, Boolean>>()
        try {
            val request = Request.Builder()
                .url("$databaseUrl/post_likes.json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyMap()
                val bodyStr = response.body?.string() ?: return@withContext emptyMap()
                if (bodyStr == "null" || bodyStr.isBlank()) return@withContext emptyMap()

                val json = JSONObject(bodyStr)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val obj = json.optJSONObject(key) ?: continue
                    val count = obj.optInt("likesCount", 0)
                    val likedByObj = obj.optJSONObject("likedBy")
                    val likedByCount = likedByObj?.length() ?: 0
                    val totalLikes = maxOf(count, likedByCount)
                    val isLikedByMe = likedByObj?.optBoolean(currentUserUid, false) ?: false
                    likesMap[key] = Pair(totalLikes, isLikedByMe)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching post likes from RTDB", e)
        }
        likesMap
    }

    suspend fun fetchComments(): Map<String, List<PostComment>> = withContext(Dispatchers.IO) {
        val commentsMap = mutableMapOf<String, List<PostComment>>()
        try {
            val request = Request.Builder()
                .url("$databaseUrl/post_comments.json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyMap()
                val bodyStr = response.body?.string() ?: return@withContext emptyMap()
                if (bodyStr == "null" || bodyStr.isBlank()) return@withContext emptyMap()

                val json = JSONObject(bodyStr)
                val postKeys = json.keys()
                while (postKeys.hasNext()) {
                    val postId = postKeys.next()
                    val commentsForPost = json.optJSONObject(postId) ?: continue

                    val commentList = mutableListOf<PostComment>()
                    val commentKeys = commentsForPost.keys()
                    while (commentKeys.hasNext()) {
                        val cKey = commentKeys.next()
                        val cObj = commentsForPost.optJSONObject(cKey) ?: continue
                        val author = cObj.optString("authorName", "Elephant Lover")
                        val text = cObj.optString("displayText", cObj.optString("text", ""))
                        val createdAt = cObj.optLong("createdAt", System.currentTimeMillis())
                        val status = cObj.optString("status", "visible")

                        if (status != "removed" && text.isNotBlank()) {
                            commentList.add(
                                PostComment(
                                    id = cKey,
                                    authorName = author,
                                    text = text,
                                    createdAt = createdAt
                                )
                            )
                        }
                    }
                    commentsMap[postId] = commentList.sortedByDescending { it.createdAt }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching comments from RTDB", e)
        }
        commentsMap
    }

    suspend fun fetchPosts(
        commentsMap: Map<String, List<PostComment>>,
        postLikesMap: Map<String, Pair<Int, Boolean>>
    ): List<ElephantPost> = withContext(Dispatchers.IO) {
        val result = mutableListOf<ElephantPost>()
        try {
            val request = Request.Builder()
                .url("$databaseUrl/elephant_posts.json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val bodyStr = response.body?.string() ?: return@withContext emptyList()
                if (bodyStr == "null" || bodyStr.isBlank()) return@withContext emptyList()

                val json = JSONObject(bodyStr)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val obj = json.optJSONObject(key) ?: continue

                    val authorName = obj.optString("authorName", "AliMedia Community")
                    val authorUsername = obj.optString("authorUsername", "alimedia").removePrefix("@")
                    var authorPhotoURL = obj.optString("authorPhotoURL", "")
                    if (authorPhotoURL.startsWith("/")) {
                        authorPhotoURL = "https://alimedia.dualsyntax.com$authorPhotoURL"
                    }

                    val caption = obj.optString("caption", "")
                    val elephantId = obj.optString("elephantId", "").ifBlank { null }
                    val elephantName = obj.optString("elephantName", "").ifBlank { null }
                    val elephantSinhalaName = obj.optString("elephantSinhalaName", "").ifBlank { null }
                    val isStory = obj.optBoolean("isStory", false)
                    val isStoryOnly = obj.optBoolean("isStoryOnly", false)
                    val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    val aspectRatio = obj.optString("aspectRatio", "4:3")
                    val authorUid = obj.optString("authorUid", "uid_$key")

                    var photoUrl = obj.optString("photoUrl", "")
                    if (photoUrl.isBlank()) {
                        val photosArr = obj.optJSONArray("photos")
                        if (photosArr != null && photosArr.length() > 0) {
                            photoUrl = photosArr.optString(0)
                        }
                    }
                    if (photoUrl.startsWith("/")) {
                        photoUrl = "https://alimedia.dualsyntax.com$photoUrl"
                    }

                    val comments = commentsMap[key] ?: emptyList()
                    val likeInfo = postLikesMap[key]
                    val likesCount = likeInfo?.first ?: (if (key.hashCode() % 2 == 0) 12 else 8)
                    val isLikedByMe = likeInfo?.second ?: false

                    val isAliMedia = authorUsername.equals("alimedia", ignoreCase = true) ||
                            authorName.equals("Ali Media", ignoreCase = true)

                    result.add(
                        ElephantPost(
                            id = key,
                            elephantId = elephantId,
                            elephantName = elephantName,
                            elephantSinhalaName = elephantSinhalaName,
                            photoUrl = photoUrl,
                            caption = caption,
                            authorUid = authorUid,
                            authorName = authorName,
                            authorUsername = authorUsername,
                            authorPhotoURL = authorPhotoURL,
                            authorIsAliMedia = isAliMedia,
                            likesCount = likesCount,
                            isLikedByMe = isLikedByMe,
                            commentsCount = comments.size,
                            comments = comments,
                            isStory = isStory,
                            isStoryOnly = isStoryOnly,
                            aspectRatio = aspectRatio,
                            createdAt = createdAt
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching posts from RTDB", e)
        }
        result.sortedByDescending { it.createdAt }
    }

    // -------------------------------------------------------------------------
    // WRITE operations – exact same paths & shapes as the web site
    // (postService.ts / commentService.ts / elephantService.ts)
    // -------------------------------------------------------------------------

    private suspend fun authToken(): String? =
        FirebaseAuthService.instance.getIdToken(forceRefresh = false)

    private fun authUrl(path: String, token: String?): String {
        val base = "$databaseUrl/$path"
        return if (token.isNullOrBlank()) base else "$base?auth=$token"
    }

    /**
     * Toggle like on a post.
     * Path: post_likes/{postId}  →  { likesCount, likedBy: {uid:true}, updatedAt }
     * Matches toggleLikeElephantPost() on the website.
     */
    suspend fun togglePostLike(
        postId: String,
        userUid: String,
        currentlyLiked: Boolean
    ): Pair<Boolean, Int> = withContext(Dispatchers.IO) {
        val token = authToken()
        try {
            // 1. Read current likes
            val getReq = Request.Builder()
                .url(authUrl("post_likes/$postId.json", token))
                .get()
                .build()
            val existing = client.newCall(getReq).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                val body = resp.body?.string()
                if (body.isNullOrBlank() || body == "null") null else JSONObject(body)
            }

            val likedBy = mutableSetOf<String>()
            existing?.optJSONObject("likedBy")?.let { map ->
                val keys = map.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    if (!k.startsWith("_") && map.optBoolean(k, false)) likedBy.add(k)
                }
            }
            // Legacy array form
            existing?.optJSONArray("likedBy")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val u = arr.optString(i)
                    if (u.isNotBlank()) likedBy.add(u)
                }
            }

            if (currentlyLiked) likedBy.remove(userUid) else likedBy.add(userUid)
            val newCount = likedBy.size
            val newLiked = likedBy.contains(userUid)

            val likedByMap = JSONObject()
            likedBy.forEach { likedByMap.put(it, true) }
            if (likedByMap.length() == 0) likedByMap.put("_empty", true)

            val payload = JSONObject().apply {
                put("likesCount", newCount)
                put("likedBy", likedByMap)
                put("updatedAt", System.currentTimeMillis())
            }

            val putReq = Request.Builder()
                .url(authUrl("post_likes/$postId.json", token))
                .put(okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse("application/json"),
                    payload.toString()
                ))
                .build()
            client.newCall(putReq).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.w(TAG, "togglePostLike failed: HTTP ${resp.code}")
                }
            }
            Pair(newLiked, newCount)
        } catch (e: Exception) {
            Log.e(TAG, "togglePostLike error", e)
            Pair(currentlyLiked, 0)
        }
    }

    /**
     * Add a comment under a post.
     * Path: comments/{postId}/{pushId}
     * Matches addPostComment() on the website.
     */
    suspend fun addComment(
        postId: String,
        authorUid: String,
        authorName: String,
        authorUsername: String,
        authorPhotoURL: String,
        text: String
    ): PostComment? = withContext(Dispatchers.IO) {
        val token = authToken()
        val clean = text.trim()
        if (clean.isBlank()) return@withContext null
        try {
            // Generate a push-style key (Firebase-compatible)
            val commentId = "c_${System.currentTimeMillis()}_${(1000..9999).random()}"
            val now = System.currentTimeMillis()
            val payload = JSONObject().apply {
                put("postId", postId)
                put("text", clean)
                put("displayText", clean)
                put("authorUid", authorUid)
                put("authorName", authorName.take(80))
                put("authorUsername", authorUsername.take(40))
                put("authorPhotoURL", authorPhotoURL.take(2000))
                put("authorIsAliMedia", false)
                put("createdAt", now)
                put("updatedAt", now)
                put("status", "visible")
            }

            val putReq = Request.Builder()
                .url(authUrl("comments/$postId/$commentId.json", token))
                .put(okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse("application/json"),
                    payload.toString()
                ))
                .build()
            client.newCall(putReq).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.w(TAG, "addComment failed: HTTP ${resp.code} ${resp.body?.string()}")
                    return@withContext null
                }
            }
            PostComment(id = commentId, authorName = authorName, text = clean)
        } catch (e: Exception) {
            Log.e(TAG, "addComment error", e)
            null
        }
    }

    /**
     * Create a new community post / story.
     * Path: elephant_posts/{pushId}
     * Matches addElephantPost() on the website.
     */
    suspend fun createPost(
        authorUid: String,
        authorName: String,
        authorUsername: String,
        authorPhotoURL: String,
        photoUrl: String,
        caption: String,
        elephantId: String?,
        elephantName: String?,
        elephantSinhalaName: String?,
        isStory: Boolean,
        isStoryOnly: Boolean,
        aspectRatio: String
    ): String? = withContext(Dispatchers.IO) {
        val token = authToken()
        try {
            val postId = "p_${System.currentTimeMillis()}_${(1000..9999).random()}"
            val now = System.currentTimeMillis()
            val payload = JSONObject().apply {
                put("authorUid", authorUid)
                put("authorName", authorName)
                put("authorUsername", authorUsername)
                put("authorPhotoURL", authorPhotoURL)
                put("authorIsAliMedia", false)
                put("photoUrl", photoUrl)
                put("caption", caption)
                put("likesCount", 0)
                put("isStory", isStory)
                put("isStoryOnly", isStoryOnly)
                put("aspectRatio", aspectRatio)
                put("createdAt", now)
                put("updatedAt", now)
                if (!elephantId.isNullOrBlank()) put("elephantId", elephantId)
                if (!elephantName.isNullOrBlank()) put("elephantName", elephantName)
                if (!elephantSinhalaName.isNullOrBlank()) put("elephantSinhalaName", elephantSinhalaName)
            }

            val putReq = Request.Builder()
                .url(authUrl("elephant_posts/$postId.json", token))
                .put(okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse("application/json"),
                    payload.toString()
                ))
                .build()
            client.newCall(putReq).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.w(TAG, "createPost failed: HTTP ${resp.code} ${resp.body?.string()}")
                    return@withContext null
                }
            }

            // Optionally append photo to elephant gallery (same as website)
            if (!elephantId.isNullOrBlank() && !isStoryOnly && photoUrl.startsWith("http")) {
                try {
                    val getEl = Request.Builder()
                        .url(authUrl("elephants/$elephantId.json", token))
                        .get()
                        .build()
                    client.newCall(getEl).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val body = resp.body?.string()
                            if (!body.isNullOrBlank() && body != "null") {
                                val el = JSONObject(body)
                                val photos = el.optJSONArray("photos") ?: org.json.JSONArray()
                                var already = false
                                for (i in 0 until photos.length()) {
                                    if (photos.optString(i) == photoUrl) already = true
                                }
                                if (!already) {
                                    photos.put(photoUrl)
                                    val patch = JSONObject().apply {
                                        put("photos", photos)
                                        put("updatedAt", now)
                                    }
                                    val patchReq = Request.Builder()
                                        .url(authUrl("elephants/$elephantId.json", token))
                                        .patch(okhttp3.RequestBody.create(
                                            okhttp3.MediaType.parse("application/json"),
                                            patch.toString()
                                        ))
                                        .build()
                                    client.newCall(patchReq).execute().close()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not append photo to elephant record", e)
                }
            }
            postId
        } catch (e: Exception) {
            Log.e(TAG, "createPost error", e)
            null
        }
    }

    /**
     * Toggle follow on an elephant.
     * Path: elephant_likes/{elephantId}  (or users/{uid}/followedElephants)
     * We write both the aggregate like map and the user's followed list.
     */
    suspend fun toggleFollowElephant(
        elephantId: String,
        userUid: String,
        currentlyFollowed: Boolean
    ): Pair<Boolean, Int> = withContext(Dispatchers.IO) {
        val token = authToken()
        try {
            val getReq = Request.Builder()
                .url(authUrl("elephant_likes/$elephantId.json", token))
                .get()
                .build()
            val existing = client.newCall(getReq).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                val body = resp.body?.string()
                if (body.isNullOrBlank() || body == "null") null else JSONObject(body)
            }

            val likedBy = mutableSetOf<String>()
            existing?.optJSONObject("likedBy")?.let { map ->
                val keys = map.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    if (!k.startsWith("_") && map.optBoolean(k, false)) likedBy.add(k)
                }
            }

            if (currentlyFollowed) likedBy.remove(userUid) else likedBy.add(userUid)
            val newCount = likedBy.size
            val newFollowed = likedBy.contains(userUid)

            val likedByMap = JSONObject()
            likedBy.forEach { likedByMap.put(it, true) }
            if (likedByMap.length() == 0) likedByMap.put("_empty", true)

            val payload = JSONObject().apply {
                put("likesCount", newCount)
                put("likedBy", likedByMap)
                put("updatedAt", System.currentTimeMillis())
            }

            val putReq = Request.Builder()
                .url(authUrl("elephant_likes/$elephantId.json", token))
                .put(okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse("application/json"),
                    payload.toString()
                ))
                .build()
            client.newCall(putReq).execute().close()

            // Also keep a simple list under the user profile for quick reads
            try {
                val userFollowPath = "users/$userUid/followedElephants/$elephantId.json"
                if (newFollowed) {
                    val setReq = Request.Builder()
                        .url(authUrl(userFollowPath, token))
                        .put(okhttp3.RequestBody.create(
                            okhttp3.MediaType.parse("application/json"),
                            "true"
                        ))
                        .build()
                    client.newCall(setReq).execute().close()
                } else {
                    val delReq = Request.Builder()
                        .url(authUrl(userFollowPath, token))
                        .delete()
                        .build()
                    client.newCall(delReq).execute().close()
                }
            } catch (e: Exception) {
                Log.w(TAG, "user followedElephants update failed", e)
            }

            Pair(newFollowed, newCount)
        } catch (e: Exception) {
            Log.e(TAG, "toggleFollowElephant error", e)
            Pair(currentlyFollowed, 0)
        }
    }

    companion object {
        private const val TAG = "FirebaseRtdbService"
    }
}

