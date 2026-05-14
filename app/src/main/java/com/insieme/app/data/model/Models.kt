package com.insieme.app.data.model

import com.google.firebase.firestore.PropertyName

data class Activity(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val budget: Double = 0.0,
    val time: String = "Breve",
    @get:PropertyName("atHome") @set:PropertyName("atHome")
    var isAtHome: Boolean = true,
    val locationDetail: String = "",
    val status: ActivityStatus = ActivityStatus.TODO,
    val creatorId: String = "",
    val participants: List<String> = emptyList()
)

enum class ActivityStatus {
    TODO, DONE
}

data class MediaItem(
    val id: String = "",
    val title: String = "",
    val type: MediaType = MediaType.FILM,
    val status: ActivityStatus = ActivityStatus.TODO,
    val creatorId: String = "",
    @get:PropertyName("converted") @set:PropertyName("converted")
    var isConverted: Boolean = false,
    val participants: List<String> = emptyList()
)

enum class MediaType {
    FILM, SERIE_TV
}

data class GameItem(
    val id: String = "",
    val title: String = "",
    val status: ActivityStatus = ActivityStatus.TODO,
    val creatorId: String = "",
    @get:PropertyName("owned") @set:PropertyName("owned")
    var isOwned: Boolean = false,
    val participants: List<String> = emptyList()
)

data class WishlistItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val link: String? = null,
    val ownerId: String = "",
    @get:PropertyName("shared") @set:PropertyName("shared")
    var isShared: Boolean = true
)
