package com.insieme.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.insieme.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreRepositoryImpl(
    private val db: FirebaseFirestore,
    private val spaceId: String
) : InsiemeRepository {

    override suspend fun checkSpaceExists(id: String): Boolean {
        return try {
            val doc = db.collection("spaces").document(id).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    override fun getActivities(): Flow<List<Activity>> =
        db.collection("spaces").document(spaceId).collection("activities")
            .snapshots().map { snapshot -> snapshot.toObjects(Activity::class.java) }

    override suspend fun addActivity(activity: Activity) {
        val doc = db.collection("spaces").document(spaceId).collection("activities").document()
        db.collection("spaces").document(spaceId).collection("activities")
            .document(doc.id).set(activity.copy(id = doc.id)).await()
    }

    override suspend fun updateActivity(activity: Activity) {
        db.collection("spaces").document(spaceId).collection("activities")
            .document(activity.id).set(activity).await()
    }

    override suspend fun deleteActivity(id: String) {
        db.collection("spaces").document(spaceId).collection("activities")
            .document(id).delete().await()
    }

    override fun getMediaItems(): Flow<List<MediaItem>> =
        db.collection("spaces").document(spaceId).collection("media")
            .snapshots().map { snapshot -> snapshot.toObjects(MediaItem::class.java) }

    override suspend fun addMediaItem(item: MediaItem) {
        val doc = db.collection("spaces").document(spaceId).collection("media").document()
        db.collection("spaces").document(spaceId).collection("media")
            .document(doc.id).set(item.copy(id = doc.id)).await()
    }

    override suspend fun updateMediaItem(item: MediaItem) {
        db.collection("spaces").document(spaceId).collection("media")
            .document(item.id).set(item).await()
    }

    override suspend fun deleteMediaItem(id: String) {
        db.collection("spaces").document(spaceId).collection("media")
            .document(id).delete().await()
    }

    override suspend fun convertMediaToActivity(item: MediaItem) {
        val activity = Activity(title = item.title, description = "Aggiunto da Media")
        addActivity(activity)
        deleteMediaItem(item.id)
    }

    override fun getGames(): Flow<List<GameItem>> =
        db.collection("spaces").document(spaceId).collection("games")
            .snapshots().map { snapshot -> snapshot.toObjects(GameItem::class.java) }

    override suspend fun addGame(game: GameItem) {
        val doc = db.collection("spaces").document(spaceId).collection("games").document()
        db.collection("spaces").document(spaceId).collection("games")
            .document(doc.id).set(game.copy(id = doc.id)).await()
    }

    override suspend fun updateGame(game: GameItem) {
        db.collection("spaces").document(spaceId).collection("games")
            .document(game.id).set(game).await()
    }

    override suspend fun deleteGame(id: String) {
        db.collection("spaces").document(spaceId).collection("games")
            .document(id).delete().await()
    }

    override fun getWishlist(shared: Boolean): Flow<List<WishlistItem>> {
        val collection = if (shared) "shared_wishlist" else "wishlist"
        return db.collection("spaces").document(spaceId).collection(collection)
            .snapshots().map { snapshot -> snapshot.toObjects(WishlistItem::class.java) }
    }

    override suspend fun getWishlistItem(id: String, shared: Boolean): WishlistItem? {
        val collection = if (shared) "shared_wishlist" else "wishlist"
        return try {
            db.collection("spaces").document(spaceId).collection(collection)
                .document(id).get().await().toObject(WishlistItem::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun addWishlistItem(item: WishlistItem) {
        val collection = if (item.isShared) "shared_wishlist" else "wishlist"
        val doc = db.collection("spaces").document(spaceId).collection(collection).document()
        db.collection("spaces").document(spaceId).collection(collection)
            .document(doc.id).set(item.copy(id = doc.id)).await()
    }

    override suspend fun updateWishlistItem(item: WishlistItem, shared: Boolean) {
        val collection = if (shared) "shared_wishlist" else "wishlist"
        db.collection("spaces").document(spaceId).collection(collection)
            .document(item.id).set(item).await()
    }

    override suspend fun deleteWishlistItem(id: String, shared: Boolean) {
        val collection = if (shared) "shared_wishlist" else "wishlist"
        db.collection("spaces").document(spaceId).collection(collection)
            .document(id).delete().await()
    }
}
