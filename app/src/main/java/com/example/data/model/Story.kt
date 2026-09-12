package com.example.data.model

data class Story(
    val id: String,
    val authorUid: String,
    val authorName: String,
    val authorUsername: String,
    val authorPhotoUrl: String = "",
    val elephantId: String? = null,
    val elephantName: String? = null,
    val elephantSinhalaName: String? = null,
    val mediaUrl: String = "",
    val drawableResId: Int? = null,
    val caption: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
)
