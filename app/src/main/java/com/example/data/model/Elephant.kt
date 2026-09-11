package com.example.data.model

data class Elephant(
    val id: String,
    val name: String,
    val sinhalaName: String,
    val location: String,
    val locationSinhala: String,
    val type: ElephantType = ElephantType.TUSKER,
    val isLive: Boolean = true,
    val photos: List<String> = emptyList(),
    val drawableResId: Int? = null,
    val description: String,
    val descriptionSinhala: String,
    val age: Int,
    val gender: String = "Male",
    val templeOwner: String,
    val templeOwnerSinhala: String,
    val followerCount: Int = 0,
    val isFollowedByMe: Boolean = false,
    val customBadge: String = ""
)

enum class ElephantType {
    TUSKER,   // ඇතා (Tusker)
    ELEPHANT  // අලියා (Elephant without tusks)
}
