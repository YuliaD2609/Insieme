package com.insieme.app.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.insieme.app.data.model.*
import com.insieme.app.data.repository.FirestoreRepositoryImpl
import com.insieme.app.data.repository.InsiemeRepository
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class SortOrder { DEFAULT, COST, DURATION }

class InsiemeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Firebase.firestore.apply {
        // Attiva la persistenza offline esplicita
        firestoreSettings = firestoreSettings {
            setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
        }
    }
    private val prefs = application.getSharedPreferences("insieme_prefs", Context.MODE_PRIVATE)
    
    private val _spaceId = MutableStateFlow(prefs.getString("space_id", "") ?: "")
    val spaceId: StateFlow<String> = _spaceId

    private val _currentUserId = MutableStateFlow(prefs.getString("user_id", "Tu") ?: "Tu")
    val currentUserId: StateFlow<String> = _currentUserId

    private val _profileImage = MutableStateFlow<String?>(prefs.getString("profile_image", null))
    val profileImage: StateFlow<String?> = _profileImage

    private val _userImages = MutableStateFlow<Map<String, String>>(emptyMap())
    val userImages: StateFlow<Map<String, String>> = _userImages

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var repository: InsiemeRepository? = null

    private val _sortOrder = MutableStateFlow(SortOrder.DEFAULT)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    val activities = combine(_spaceId, _sortOrder) { id, order -> id to order }
        .flatMapLatest { (id, order) ->
            if (id.isBlank()) flowOf(emptyList())
            else {
                repository = FirestoreRepositoryImpl(db, id)
                repository!!.getActivities().map { list ->
                    when (order) {
                        SortOrder.COST -> list.sortedBy { it.budget }
                        SortOrder.DURATION -> list.sortedBy { 
                            when(it.time) {
                                "Breve" -> 1
                                "Media" -> 2
                                "Lunga" -> 3
                                else -> 4
                            }
                        }
                        else -> list
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mediaItems = _spaceId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList())
        else (repository ?: FirestoreRepositoryImpl(db, id)).getMediaItems()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val games = _spaceId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList())
        else (repository ?: FirestoreRepositoryImpl(db, id)).getGames()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sharedWishlist = _spaceId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList())
        else (repository ?: FirestoreRepositoryImpl(db, id)).getWishlist()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allParticipantNames = activities.map { list ->
        list.flatMap { it.participants }.toSet() + _currentUserId.value
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), setOf(_currentUserId.value))

    val groupSize = allParticipantNames.map { it.size }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    init {
        // Observe users to get profile images
        _spaceId.onEach { id ->
            if (id.isNotBlank()) {
                db.collection("spaces").document(id).collection("users")
                    .addSnapshotListener { snapshot, _ ->
                        val map = snapshot?.documents?.associate { 
                            it.id to (it.getString("photoUrl") ?: "")
                        } ?: emptyMap()
                        _userImages.value = map
                    }
                updateUserProfile()
                clearCurrentSpace() // ESECUZIONE UNA TANTUM: Svuota il DB all'avvio
            }
        }.launchIn(viewModelScope)
    }

    fun clearCurrentSpace() {
        val id = _spaceId.value
        if (id.isNotBlank()) {
            viewModelScope.launch {
                try {
                    // Svuota collezioni principali
                    val collections = listOf("activities", "media", "games", "wishlist")
                    for (col in collections) {
                        val docs = db.collection("spaces").document(id).collection(col).get().await()
                        for (doc in docs) {
                            doc.reference.delete()
                        }
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Errore durante lo svuotamento"
                }
            }
        }
    }

    fun setSpaceId(id: String) {
        _spaceId.value = id
        prefs.edit().putString("space_id", id).apply()
        updateUserProfile()
    }

    fun setCurrentUserId(name: String) {
        val trimmedName = name.trim().ifBlank { "Tu" }
        _currentUserId.value = trimmedName
        prefs.edit().putString("user_id", trimmedName).apply()
        updateUserProfile()
    }

    fun setProfileImage(uri: String) {
        _profileImage.value = uri
        prefs.edit().putString("profile_image", uri).apply()
        updateUserProfile()
    }

    private fun updateUserProfile() {
        val id = _spaceId.value
        val name = _currentUserId.value
        val photo = _profileImage.value
        if (id.isNotBlank() && name.isNotBlank() && name != "Tu") {
            viewModelScope.launch {
                db.collection("spaces").document(id).collection("users").document(name)
                    .set(mapOf("name" to name, "photoUrl" to photo))
            }
        }
    }

    fun createSpace() {
        viewModelScope.launch {
            try {
                val newId = "${(1000..9999).random()}-${(1000..9999).random()}"
                db.collection("spaces").document(newId).set(mapOf("createdAt" to System.currentTimeMillis())).await()
                setSpaceId(newId)
            } catch (e: Exception) {
                _errorMessage.value = "Errore creazione: ${e.message}"
            }
        }
    }

    fun joinSpace(id: String) {
        val trimmedId = id.trim()
        if (!Regex("""^\d{4}-\d{4}$""").matches(trimmedId)) {
            _errorMessage.value = "Codice non valido"
            return
        }
        viewModelScope.launch {
            try {
                val exists = db.collection("spaces").document(trimmedId).get().await().exists()
                if (exists) {
                    setSpaceId(trimmedId)
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Gruppo non trovato"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Errore connessione: ${e.message}"
            }
        }
    }

    fun logout() {
        _spaceId.value = ""
        prefs.edit().remove("space_id").apply()
    }

    fun clearError() { _errorMessage.value = null }
    fun setSortOrder(order: SortOrder) { _sortOrder.value = order }

    // Activities
    fun addActivity(activity: Activity) { viewModelScope.launch { repository?.addActivity(activity.copy(creatorId = _currentUserId.value)) } }
    fun updateActivity(activity: Activity) { viewModelScope.launch { repository?.updateActivity(activity) } }
    fun deleteActivity(id: String) { viewModelScope.launch { repository?.deleteActivity(id) } }
    fun toggleParticipation(activity: Activity) {
        val newList = if (activity.participants.contains(_currentUserId.value)) activity.participants - _currentUserId.value else activity.participants + _currentUserId.value
        viewModelScope.launch { repository?.updateActivity(activity.copy(participants = newList)) }
    }
    fun markActivityAsDone(activity: Activity) { viewModelScope.launch { repository?.updateActivity(activity.copy(status = ActivityStatus.DONE)) } }

    // Media
    fun addMediaItem(item: MediaItem) { viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).addMediaItem(item.copy(creatorId = _currentUserId.value)) } }
    fun updateMediaItem(item: MediaItem) { viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).updateMediaItem(item) } }
    fun deleteMediaItem(id: String) { viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).deleteMediaItem(id) } }
    fun toggleMediaStatus(item: MediaItem) {
        val newStatus = if (item.status == ActivityStatus.TODO) ActivityStatus.DONE else ActivityStatus.TODO
        viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).updateMediaItem(item.copy(status = newStatus)) }
    }
    fun toggleMediaParticipation(item: MediaItem) {
        val newList = if (item.participants.contains(_currentUserId.value)) item.participants - _currentUserId.value else item.participants + _currentUserId.value
        viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).updateMediaItem(item.copy(participants = newList)) }
    }

    // Games
    fun addGame(item: GameItem) { viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).addGame(item.copy(creatorId = _currentUserId.value)) } }
    fun updateGame(item: GameItem) { viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).updateGame(item) } }
    fun deleteGame(id: String) { viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).deleteGame(id) } }
    fun toggleGameStatus(item: GameItem) {
        val newStatus = if (item.status == ActivityStatus.TODO) ActivityStatus.DONE else ActivityStatus.TODO
        viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).updateGame(item.copy(status = newStatus)) }
    }
    fun toggleGameParticipation(item: GameItem) {
        val newList = if (item.participants.contains(_currentUserId.value)) item.participants - _currentUserId.value else item.participants + _currentUserId.value
        viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).updateGame(item.copy(participants = newList)) }
    }

    // Wishlist
    fun addWishlistItem(title: String, link: String?) { viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).addWishlistItem(WishlistItem(title = title, link = link, ownerId = _currentUserId.value)) } }
    fun updateWishlistItem(id: String, title: String, link: String?) { viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).updateWishlistItem(WishlistItem(id = id, title = title, link = link, ownerId = _currentUserId.value)) } }
    fun deleteWishlistItem(id: String) { viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).deleteWishlistItem(id) } }
}
