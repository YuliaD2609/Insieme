package com.insieme.app.data.repository

import com.insieme.app.data.model.Activity
import com.insieme.app.data.model.GameItem
import com.insieme.app.data.model.MediaItem
import com.insieme.app.data.model.WishlistItem
import kotlinx.coroutines.flow.Flow

interface InsiemeRepository {
    // Space Validation
    suspend fun checkSpaceExists(id: String): Boolean

    // Activities
    fun getActivities(): Flow<List<Activity>>
    suspend fun addActivity(activity: Activity)
    suspend fun updateActivity(activity: Activity)
    suspend fun deleteActivity(id: String)

    // Media
    fun getMediaItems(): Flow<List<MediaItem>>
    suspend fun addMediaItem(item: MediaItem)
    suspend fun updateMediaItem(item: MediaItem)
    suspend fun deleteMediaItem(id: String)
    suspend fun convertMediaToActivity(item: MediaItem)

    // Games
    fun getGames(): Flow<List<GameItem>>
    suspend fun addGame(game: GameItem)
    suspend fun updateGame(game: GameItem)
    suspend fun deleteGame(id: String)

    // Wishlist
    fun getWishlist(): Flow<List<WishlistItem>>
    suspend fun getWishlistItem(id: String): WishlistItem?
    suspend fun addWishlistItem(item: WishlistItem)
    suspend fun updateWishlistItem(item: WishlistItem)
    suspend fun deleteWishlistItem(id: String)
}
