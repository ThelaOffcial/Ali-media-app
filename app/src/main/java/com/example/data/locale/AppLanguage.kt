package com.example.data.locale

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    SINHALA("si", "Sinhala", "සිංහල")
}

object AppStrings {
    // Navigation
    fun feed(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "මුල් පිටුව" else "Feed"
    fun elephants(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "ඇතුන්" else "Elephants"
    fun create(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "සටහනක්" else "Create"
    fun notices(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "දැන්වීම්" else "Notices"
    fun profile(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "ගිණුම" else "Profile"

    // App Branding
    fun appTitle(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "අලිMedia" else "AliMedia"
    fun appSubtitle(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "ශ්‍රී ලාංකේය හීලෑ අලි සහ ඇතුන්ගේ එකමුතුව" else "Sri Lankan Domesticated Elephants & Tuskers"

    // Feed & Stories
    fun stories(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "කතා" else "Stories"
    fun addStory(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "ඔබේ කතාව" else "Your Story"
    fun like(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "මනාප" else "Like"
    fun comment(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "අදහස්" else "Comment"
    fun share(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "බෙදාගන්න" else "Share"
    fun commentsTitle(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "අදහස් දැක්වීම්" else "Comments"
    fun writeComment(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "අදහසක් එක් කරන්න..." else "Write a comment..."
    fun postButton(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "පළ කරන්න" else "Post"
    fun noPostsYet(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "තවමත් සටහන් නොමැත" else "No posts yet"
    fun beFirstToPost(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "පළමු ඡායාරූපය පළකරන්න!" else "Be the first to share an elephant photo!"

    // Time Ago
    fun justNow(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "දැන්" else "Just now"
    fun hoursAgo(lang: AppLanguage, hours: Long) = if (lang == AppLanguage.SINHALA) "පැය ${hours}කට පෙර" else "${hours}h ago"
    fun minutesAgo(lang: AppLanguage, mins: Long) = if (lang == AppLanguage.SINHALA) "විනාඩි ${mins}කට පෙර" else "${mins}m ago"
    fun daysAgo(lang: AppLanguage, days: Long) = if (lang == AppLanguage.SINHALA) "දින ${days}කට පෙර" else "${days}d ago"

    // Elephants Screen
    fun searchElephants(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "ඇතුන්, අලින් හෝ විහාරස්ථාන සොයන්න..." else "Search elephants, tuskers or temples..."
    fun allFilter(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "සියල්ල" else "All"
    fun tuskersFilter(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "ඇතුන් (Tuskers)" else "Tuskers"
    fun elephantsFilter(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "අලින් (Aliya)" else "Elephants"
    fun follow(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "අනුගමනය" else "Follow"
    fun following(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "අනුගමනය කර ඇත" else "Following"
    fun followers(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "අනුගාමිකයින්" else "Followers"
    fun yearsOld(lang: AppLanguage, age: Int) = if (lang == AppLanguage.SINHALA) "වයස අවුරුදු $age" else "$age years old"
    fun templeOrOwner(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "භාරකාරත්වය / විහාරස්ථානය" else "Temple / Custodian"
    fun elephantBio(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "තොරතුරු" else "Biography"
    fun taggedPosts(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "ටැග් කළ සටහන්" else "Tagged Posts"

    // Composer (3-step)
    fun step1Title(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "පියවර 1: ඡායාරූපය තෝරන්න" else "Step 1: Choose Photo"
    fun step2Title(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "පියවර 2: ඇතා ටැග් කරන්න" else "Step 2: Tag Elephant"
    fun step3Title(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "පියවර 3: විස්තර හා පළ කිරීම" else "Step 3: Details & Publish"
    fun tapToChoosePhoto(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "ඡායාරූපයක් තෝරා ගැනීමට තට්ටු කරන්න" else "Tap to choose a photo"
    fun orPasteUrl(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "හෝ ඡායාරූප සබැඳිය (URL) යොදන්න" else "Or paste an image URL"
    fun imageUrlPlaceholder(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "https://example.com/elephant.jpg" else "https://example.com/elephant.jpg"
    fun continueBtn(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "ඉදිරියට" else "Continue"
    fun backBtn(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "ආපසු" else "Back"
    fun skipForNow(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "දැනට මඟහරින්න" else "Skip for now"
    fun tagElephantPrompt(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "මෙම ඡායාරූපයේ සිටින ඇතා / අලියා තෝරන්න (අවශ්‍ය නම් පමණි)" else "Select the tusker/elephant in this photo (optional)"
    fun captionPlaceholder(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "මෙම ඇතා පිළිබඳ විස්තරයක් ලියන්න (වචන 5,000 දක්වා)..." else "Write about this tusker/elephant (up to 5,000 words)..."
    fun wordCount(lang: AppLanguage, words: Int) = if (lang == AppLanguage.SINHALA) "වචන: $words / 5,000" else "Words: $words / 5,000"
    fun feedAndStoryOption(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "Feed + Story (කතාව)" else "Feed + Story"
    fun feedAndStoryDesc(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "මුල් පිටුවෙහි සහ පැය 24 කතාවේ දිස්වේ" else "Appears on the feed and in 24h stories"
    fun storyOnlyOption(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "Story Only (කතාව පමණි)" else "Story Only"
    fun storyOnlyDesc(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "පැය 24කින් ස්වයංක්‍රීයව මැකී යයි" else "Disappears automatically in 24 hours"
    fun publishPost(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "දැන් පළකරන්න" else "Publish Now"

    // Notices
    fun noticesTitle(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "පෙරහැර සහ ඇතුන් පිළිබඳ නිවේදන" else "Perahera & Tusker Notices"
    fun urgentBadge(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "විශේෂ" else "Urgent"

    // Profile & Settings
    fun myPosts(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "මගේ සටහන්" else "My Posts"
    fun followedTuskers(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "අනුගමනය කරන ඇතුන්" else "Followed Elephants"
    fun settings(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "සැකසුම්" else "Settings"
    fun darkMode(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "අඳුරු තේමාව (Dark Mode)" else "Dark Theme"
    fun language(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "භාෂාව (Language)" else "Language"
    fun feedbackBoard(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "යෝජනා සහ ප්‍රතිපෝෂණ" else "Feedback & Suggestions"
    fun aboutAliMedia(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "AliMedia පිළිබඳව" else "About AliMedia"
    fun aboutDesc(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "ශ්‍රී ලාංකේය සංස්කෘතික උරුමයේ මුදුන්මල්කඩ බඳු හීලෑ අලි ඇතුන් රැකගැනීමට සහ ගෞරව දැක්වීමට නිර්මාණය කරන ලද ඩිජිටල් වේදිකාව." else "A dedicated digital platform honoring and documenting Sri Lankan domesticated elephants and tuskers."

    // Feedback
    fun feedbackTitle(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "යෝජනා සහ අදහස් පුවරුව" else "Community Feedback Board"
    fun shareFeedbackBtn(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "අදහසක් යොමු කරන්න" else "Share Feedback"
    fun suggestion(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "යෝජනාව" else "Suggestion"
    fun complaint(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "පැමිණිල්ල" else "Complaint"
    fun submitFeedback(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "යොමු කරන්න" else "Submit Feedback"
    fun feedbackMessagePlaceholder(lang: AppLanguage) = if (lang == AppLanguage.SINHALA) "ඔබේ අදහස හෝ යෝජනාව මෙහි සටහන් කරන්න..." else "Share your thoughts or suggestions..."
}
