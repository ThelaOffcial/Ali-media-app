package com.example.data.model

data class Notice(
    val id: String,
    val title: String,
    val titleSinhala: String,
    val message: String,
    val messageSinhala: String,
    val tag: String = "Announcement",
    val tagSinhala: String = "නිවේදනය",
    val date: String,
    val author: String = "AliMedia Admin",
    val isUrgent: Boolean = false
)

data class PlatformFeedback(
    val id: String,
    val type: FeedbackType,
    val message: String,
    val authorUid: String,
    val authorName: String,
    val authorPhotoURL: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val status: String = "open" // "open" | "reviewed"
)

enum class FeedbackType {
    SUGGESTION,
    COMPLAINT
}

data class UserProfile(
    val uid: String,
    val displayName: String,
    val username: String,
    val email: String,
    val photoURL: String = "",
    val bio: String = "",
    val bioSinhala: String = "",
    val followedElephants: List<String> = emptyList(),
    val postsCount: Int = 0,
    val followersCount: Int = 128,
    val followingCount: Int = 45,
    val isSuspended: Boolean = false
)
