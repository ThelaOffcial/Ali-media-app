package com.example.data.model

data class PostComment(
    val id: String,
    val authorName: String,
    val authorPhotoUrl: String? = null,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class ElephantPost(
    val id: String,
    val elephantId: String? = null,
    val elephantName: String? = null,
    val elephantSinhalaName: String? = null,
    val photoUrl: String = "",
    val drawableResId: Int? = null,
    val caption: String = "",
    val authorUid: String = "guest_101",
    val authorName: String = "Sri Lanka Tusker Fan",
    val authorUsername: String = "tusker_lover",
    val authorPhotoURL: String = "",
    val authorIsAliMedia: Boolean = false,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val commentsCount: Int = 0,
    val comments: List<PostComment> = emptyList(),
    val isStory: Boolean = false,
    val isStoryOnly: Boolean = false,
    val aspectRatio: String = "4:3",
    val createdAt: Long = System.currentTimeMillis()
)
