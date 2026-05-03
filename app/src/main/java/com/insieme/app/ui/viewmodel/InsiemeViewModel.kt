package com.insieme.app.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.insieme.app.data.model.*
import com.insieme.app.data.repository.FirestoreRepositoryImpl
import com.insieme.app.data.repository.InsiemeRepository
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

enum class SortOrder { DEFAULT, COST, DURATION }

class InsiemeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Firebase.firestore.apply {
        firestoreSettings = firestoreSettings {
            setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
        }
    }
    private val prefs = application.getSharedPreferences("insieme_prefs", Context.MODE_PRIVATE)
    
    private val _userId = MutableStateFlow(prefs.getString("internal_user_id", null) ?: run {
        val newId = UUID.randomUUID().toString()
        prefs.edit().putString("internal_user_id", newId).apply()
        newId
    })
    val userId: StateFlow<String> = _userId

    private val _spaceId = MutableStateFlow(prefs.getString("space_id", "") ?: "")
    val spaceId: StateFlow<String> = _spaceId

    private val _currentUserId = MutableStateFlow(prefs.getString("user_id", "Tu") ?: "Tu")
    val currentUserId: StateFlow<String> = _currentUserId

    private val _profileImage = MutableStateFlow<String?>(prefs.getString("profile_image", null))
    val profileImage: StateFlow<String?> = _profileImage

    private val _userNames = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _userImages = MutableStateFlow<Map<String, String>>(emptyMap())
    val userImages: StateFlow<Map<String, String>> = _userImages

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _currentSpaceCreatorId = MutableStateFlow<String?>(null)
    val currentSpaceCreatorId: StateFlow<String?> = _currentSpaceCreatorId

    private var repository: InsiemeRepository? = null

    private val _sortOrder = MutableStateFlow(SortOrder.DEFAULT)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    private val _joinedGroups = MutableStateFlow<Set<String>>(
        prefs.getStringSet("joined_groups", emptySet()) ?: emptySet()
    )
    val joinedGroups: StateFlow<Set<String>> = _joinedGroups

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

    val allParticipantNames = _userNames.map { it.values.toSet() + _currentUserId.value.ifBlank { "Tu" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), setOf(_currentUserId.value.ifBlank { "Tu" }))

    val participantIds = _userNames.map { it.keys + _userId.value }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), setOf(_userId.value))
    
    val idToName = _userNames.map { it + (_userId.value to _currentUserId.value.ifBlank { "Tu" }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), mapOf(_userId.value to _currentUserId.value.ifBlank { "Tu" }))

    val groupSize = participantIds.map { it.size }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    init {
        _spaceId.onEach { id ->
            if (id.isNotBlank()) {
                db.collection("spaces").document(id).collection("users")
                    .addSnapshotListener { snapshot, _ ->
                        val namesMap = snapshot?.documents?.associate { 
                            it.id to (it.getString("name") ?: "Utente")
                        } ?: emptyMap()
                        val imagesMap = snapshot?.documents?.associate { 
                            it.id to (it.getString("photoUrl") ?: "")
                        } ?: emptyMap()
                        _userNames.value = namesMap
                        _userImages.value = imagesMap
                    }
                updateUserProfile()
            }
        }.launchIn(viewModelScope)
    }

    fun setSpaceId(id: String) {
        _spaceId.value = id
        prefs.edit().putString("space_id", id).apply()
        
        if (id.isNotBlank()) {
            val updatedGroups = _joinedGroups.value + id
            _joinedGroups.value = updatedGroups
            prefs.edit().putStringSet("joined_groups", updatedGroups).apply()
            
            // Fetch metadata and verify existence
            viewModelScope.launch {
                val doc = db.collection("spaces").document(id).get().await()
                if (doc.exists()) {
                    _currentSpaceCreatorId.value = doc.getString("creatorId")
                } else {
                    // Se non esiste più, lo rimuoviamo dai preferiti
                    _errorMessage.value = "Questo gruppo non esiste più."
                    val updatedGroups = _joinedGroups.value - id
                    _joinedGroups.value = updatedGroups
                    prefs.edit().putStringSet("joined_groups", updatedGroups).apply()
                    if (_spaceId.value == id) logout()
                }
            }
        } else {
            _currentSpaceCreatorId.value = null
        }
        
        updateUserProfile()
    }

    fun leaveSpace(id: String) {
        viewModelScope.launch {
            try {
                // Rimuove l'utente dalla collezione Firestore
                db.collection("spaces").document(id).collection("users").document(_userId.value).delete().await()
                
                // Aggiorna la lista locale
                val updatedGroups = _joinedGroups.value - id
                _joinedGroups.value = updatedGroups
                prefs.edit().putStringSet("joined_groups", updatedGroups).apply()
                
                // Se stiamo uscendo dallo spazio corrente, facciamo il logout
                if (_spaceId.value == id) {
                    logout()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Errore durante l'uscita: ${e.message}"
            }
        }
    }

    fun deleteSpacePermanently(id: String) {
        viewModelScope.launch {
            try {
                db.collection("spaces").document(id).delete().await()
                val updatedGroups = _joinedGroups.value - id
                _joinedGroups.value = updatedGroups
                prefs.edit().putStringSet("joined_groups", updatedGroups).apply()
                if (_spaceId.value == id) logout()
            } catch (e: Exception) {
                _errorMessage.value = "Errore eliminazione: ${e.message}"
            }
        }
    }

    fun setCurrentUserId(name: String) {
        _currentUserId.value = name
        prefs.edit().putString("user_id", name).apply()
        updateUserProfile()
    }

    fun finalizeName() {
        if (_currentUserId.value.isBlank()) {
            setCurrentUserId("Tu")
        }
    }

    fun setProfileImage(uri: String) {
        viewModelScope.launch {
            val base64 = processImageToBase64(uri)
            if (base64 != null) {
                _profileImage.value = base64
                prefs.edit().putString("profile_image", base64).apply()
                updateUserProfile()
            }
        }
    }

    private suspend fun processImageToBase64(uriString: String): String? = withContext(Dispatchers.IO) {
        try {
            val context = getApplication<Application>().applicationContext
            val inputStream = context.contentResolver.openInputStream(Uri.parse(uriString))
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 120, 120, true)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            // Restituiamo solo la stringa base64 pura per coerenza
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) { null }
    }

    private fun updateUserProfile() {
        val spaceId = _spaceId.value
        val userId = _userId.value
        val name = _currentUserId.value.ifBlank { "Tu" }
        val photo = _profileImage.value
        if (spaceId.isNotBlank() && userId.isNotBlank()) {
            viewModelScope.launch {
                db.collection("spaces").document(spaceId).collection("users").document(userId)
                    .set(mapOf("name" to name, "photoUrl" to photo, "lastUpdate" to System.currentTimeMillis()))
            }
        }
    }

    // Helper per le immagini in UI
    fun decodeImage(base64: String?): ByteArray? {
        if (base64.isNullOrBlank()) return null
        return try {
            // Se contiene il prefisso lo togliamo
            val pureBase64 = if (base64.contains(",")) base64.substringAfter(",") else base64
            Base64.decode(pureBase64, Base64.DEFAULT)
        } catch (e: Exception) { null }
    }

    fun createSpace() {
        viewModelScope.launch {
            try {
                val newId = "${(1000..9999).random()}-${(1000..9999).random()}"
                db.collection("spaces").document(newId).set(mapOf(
                    "createdAt" to System.currentTimeMillis(),
                    "creatorId" to _userId.value
                )).await()
                setSpaceId(newId)
            } catch (e: Exception) {
                _errorMessage.value = "Errore creazione: ${e.message}"
            }
        }
    }

    fun joinSpace(id: String) {
        val cleanedId = id.trim().replace(" ", "")
        // Accetta 1234-5678 oppure 12345678
        val regex = Regex("""^(\d{4}-\d{4}|\d{8})$""")
        
        if (!regex.matches(cleanedId)) {
            _errorMessage.value = "Codice non valido (usa 8 cifre)"
            return
        }

        // Formatta sempre come 1234-5678 per Firestore
        val finalId = if (cleanedId.length == 8) {
            cleanedId.take(4) + "-" + cleanedId.drop(4)
        } else {
            cleanedId
        }

        viewModelScope.launch {
            try {
                val exists = db.collection("spaces").document(finalId).get().await().exists()
                if (exists) {
                    setSpaceId(finalId)
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
    fun addActivity(activity: Activity) { viewModelScope.launch { repository?.addActivity(activity.copy(creatorId = _userId.value)) } }
    fun updateActivity(activity: Activity) { viewModelScope.launch { repository?.updateActivity(activity) } }
    fun deleteActivity(id: String) { viewModelScope.launch { repository?.deleteActivity(id) } }
    fun toggleParticipation(activity: Activity) {
        val newList = if (activity.participants.contains(_userId.value)) activity.participants - _userId.value else activity.participants + _userId.value
        viewModelScope.launch { repository?.updateActivity(activity.copy(participants = newList)) }
    }
    fun markActivityAsDone(activity: Activity) { viewModelScope.launch { repository?.updateActivity(activity.copy(status = ActivityStatus.DONE)) } }

    // Media
    fun addMediaItem(item: MediaItem) { viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).addMediaItem(item.copy(creatorId = _userId.value)) } }
    fun updateMediaItem(item: MediaItem) { viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).updateMediaItem(item) } }
    fun deleteMediaItem(id: String) { viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).deleteMediaItem(id) } }
    fun toggleMediaStatus(item: MediaItem) {
        val newStatus = if (item.status == ActivityStatus.TODO) ActivityStatus.DONE else ActivityStatus.TODO
        viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).updateMediaItem(item.copy(status = newStatus)) }
    }
    fun toggleMediaParticipation(item: MediaItem) {
        val newList = if (item.participants.contains(_userId.value)) item.participants - _userId.value else item.participants + _userId.value
        viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).updateMediaItem(item.copy(participants = newList)) }
    }

    // Games
    fun addGame(item: GameItem) { viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).addGame(item.copy(creatorId = _userId.value)) } }
    fun updateGame(item: GameItem) { viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).updateGame(item) } }
    fun deleteGame(id: String) { viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).deleteGame(id) } }
    fun toggleGameStatus(item: GameItem) {
        val newStatus = if (item.status == ActivityStatus.TODO) ActivityStatus.DONE else ActivityStatus.TODO
        viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).updateGame(item.copy(status = newStatus)) }
    }
    fun toggleGameParticipation(item: GameItem) {
        val newList = if (item.participants.contains(_userId.value)) item.participants - _userId.value else item.participants + _userId.value
        viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).updateGame(item.copy(participants = newList)) }
    }

    // Wishlist
    fun addWishlistItem(title: String, link: String?) {
        viewModelScope.launch {
            var finalTitle = title
            var finalImageUrl: String? = null
            
            if (!link.isNullOrBlank()) {
                val scraped = com.insieme.app.util.LinkScraper.scrape(link)
                if (finalTitle.isBlank()) finalTitle = scraped.title ?: "Desiderio"
                finalImageUrl = scraped.imageUrl
            }
            
            if (finalTitle.isBlank()) finalTitle = "Desiderio"
            
            (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).addWishlistItem(
                WishlistItem(title = finalTitle, link = link, imageUrl = finalImageUrl, ownerId = _userId.value)
            )
        }
    }

    fun updateWishlistItem(id: String, title: String, link: String?) {
        viewModelScope.launch {
            var finalTitle = title
            var finalImageUrl: String? = null
            
            if (!link.isNullOrBlank()) {
                val scraped = com.insieme.app.util.LinkScraper.scrape(link)
                if (finalTitle.isBlank()) finalTitle = scraped.title ?: "Desiderio"
                finalImageUrl = scraped.imageUrl
            }
            
            (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).updateWishlistItem(
                WishlistItem(id = id, title = finalTitle, link = link, imageUrl = finalImageUrl, ownerId = _userId.value)
            )
        }
    }
    fun deleteWishlistItem(id: String) { viewModelScope.launch { (repository ?: FirestoreRepositoryImpl(db, _spaceId.value)).deleteWishlistItem(id) } }
}
