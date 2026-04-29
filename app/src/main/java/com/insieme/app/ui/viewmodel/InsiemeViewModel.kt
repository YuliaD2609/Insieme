package com.insieme.app.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.insieme.app.data.model.*
import com.insieme.app.data.repository.FirestoreRepositoryImpl
import com.insieme.app.data.repository.InsiemeRepository
import com.insieme.app.util.LinkScraper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOrder { DEFAULT, COST, DURATION }

class InsiemeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val prefs = application.getSharedPreferences("insieme_prefs", Context.MODE_PRIVATE)

    private val _spaceId = MutableStateFlow(prefs.getString("space_id", "") ?: "")
    val spaceId: StateFlow<String> = _spaceId

    private val _currentUserId = MutableStateFlow(prefs.getString("user_id", "Tu") ?: "Tu")
    val currentUserId: StateFlow<String> = _currentUserId

    private val _profileImage = MutableStateFlow(prefs.getString("profile_image", null))
    val profileImage: StateFlow<String?> = _profileImage

    private val _sortOrder = MutableStateFlow(SortOrder.DEFAULT)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var repository: InsiemeRepository? = null

    private fun getValidationRepo(id: String) = FirestoreRepositoryImpl(Firebase.firestore, id)

    val activities: StateFlow<List<Activity>> = combine(_spaceId, _sortOrder) { id, order ->
        id to order
    }.flatMapLatest { (id, order) ->
        if (id.isBlank()) MutableStateFlow(emptyList())
        else {
            repository = FirestoreRepositoryImpl(Firebase.firestore, id)
            repository!!.getActivities().map { list ->
                when (order) {
                    SortOrder.COST -> list.sortedBy { it.budget }
                    SortOrder.DURATION -> list.sortedBy { 
                        when(it.time) { "Breve" -> 0; "Media" -> 1; "Lunga" -> 2; else -> 3 }
                    }
                    SortOrder.DEFAULT -> list
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allParticipantNames: StateFlow<Set<String>> = combine(activities, _currentUserId) { list, me ->
        val names = list.flatMap { it.participants }.toMutableSet()
        list.forEach { names.add(it.creatorId) }
        names.add(me)
        names.remove("")
        names
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), setOf("Tu"))

    val groupSize: StateFlow<Int> = allParticipantNames.map { if (it.size < 2) 2 else it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val mediaItems: StateFlow<List<MediaItem>> = _spaceId.flatMapLatest { id ->
        if (id.isBlank()) MutableStateFlow(emptyList())
        else repository!!.getMediaItems()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val games: StateFlow<List<GameItem>> = _spaceId.flatMapLatest { id ->
        if (id.isBlank()) MutableStateFlow(emptyList())
        else repository!!.getGames()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sharedWishlist: StateFlow<List<WishlistItem>> = _spaceId.flatMapLatest { id ->
        if (id.isBlank()) MutableStateFlow(emptyList())
        else repository!!.getWishlist(true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSpaceId(id: String) {
        _spaceId.value = id
        _errorMessage.value = null
        prefs.edit().putString("space_id", id).apply()
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun clearError() { _errorMessage.value = null }

    fun createSpace() {
        viewModelScope.launch {
            val newId = "${(1000..9999).random()}-${(1000..9999).random()}"
            Firebase.firestore.collection("spaces").document(newId).set(mapOf("createdAt" to System.currentTimeMillis()))
            setSpaceId(newId)
        }
    }

    fun joinSpace(id: String) {
        val trimmedId = id.trim()
        val format = Regex("""^\d{4}-\d{4}$""")
        if (!format.matches(trimmedId)) {
            _errorMessage.value = "Formato codice errato (es: 1234-5678)"
            return
        }
        viewModelScope.launch {
            val exists = getValidationRepo(trimmedId).checkSpaceExists(trimmedId)
            if (exists) setSpaceId(trimmedId) else _errorMessage.value = "Il gruppo non esiste!"
        }
    }

    fun setCurrentUserId(name: String) {
        _currentUserId.value = name
        prefs.edit().putString("user_id", name).apply()
    }

    fun setProfileImage(url: String?) {
        _profileImage.value = url
        prefs.edit().putString("profile_image", url).apply()
    }

    // Activities
    fun addActivity(activity: Activity) {
        viewModelScope.launch {
            repository?.addActivity(activity.copy(creatorId = _currentUserId.value))
        }
    }

    fun updateActivity(activity: Activity) {
        viewModelScope.launch {
            repository?.updateActivity(activity)
        }
    }

    fun toggleParticipation(activity: Activity) {
        viewModelScope.launch {
            if (activity.status == ActivityStatus.DONE) {
                repository?.updateActivity(activity.copy(status = ActivityStatus.TODO))
                return@launch
            }
            val currentList = activity.participants.toMutableList()
            if (currentList.contains(_currentUserId.value)) currentList.remove(_currentUserId.value)
            else currentList.add(_currentUserId.value)
            repository?.updateActivity(activity.copy(participants = currentList))
        }
    }

    fun markActivityAsDone(activity: Activity) {
        viewModelScope.launch {
            repository?.updateActivity(activity.copy(status = ActivityStatus.DONE))
        }
    }

    fun deleteActivity(id: String) {
        viewModelScope.launch { repository?.deleteActivity(id) }
    }

    // Media
    fun toggleMediaStatus(item: MediaItem) {
        viewModelScope.launch {
            val nextStatus = if (item.status == ActivityStatus.TODO) ActivityStatus.DONE else ActivityStatus.TODO
            repository?.updateMediaItem(item.copy(status = nextStatus))
        }
    }

    fun addMediaItem(item: MediaItem) {
        viewModelScope.launch { repository?.addMediaItem(item.copy(creatorId = _currentUserId.value)) }
    }

    fun updateMediaItem(item: MediaItem) {
        viewModelScope.launch { repository?.updateMediaItem(item) }
    }

    fun deleteMediaItem(id: String) {
        viewModelScope.launch { repository?.deleteMediaItem(id) }
    }

    // Games
    fun toggleGameStatus(game: GameItem) {
        viewModelScope.launch {
            val nextStatus = if (game.status == ActivityStatus.TODO) ActivityStatus.DONE else ActivityStatus.TODO
            repository?.updateGame(game.copy(status = nextStatus))
        }
    }

    fun addGame(game: GameItem) {
        viewModelScope.launch { repository?.addGame(game.copy(creatorId = _currentUserId.value)) }
    }

    fun updateGame(game: GameItem) {
        viewModelScope.launch { repository?.updateGame(game) }
    }

    fun deleteGame(id: String) {
        viewModelScope.launch { repository?.deleteGame(id) }
    }

    // Wishlist
    fun addWishlistItem(titleInput: String, link: String) {
        viewModelScope.launch {
            val metadata = if (link.isNotBlank()) LinkScraper.getMetadata(link) else LinkScraper.Metadata(null, null)
            val finalTitle = titleInput.ifBlank { metadata.title ?: "Oggetto senza nome" }
            val item = WishlistItem(
                title = finalTitle,
                link = link,
                imageUrl = metadata.imageUrl,
                isShared = true,
                ownerId = _currentUserId.value
            )
            repository?.addWishlistItem(item)
        }
    }

    fun updateWishlistItem(id: String, titleInput: String, link: String) {
        viewModelScope.launch {
            val metadata = if (link.isNotBlank()) LinkScraper.getMetadata(link) else LinkScraper.Metadata(null, null)
            val item = repository?.getWishlistItem(id, true) ?: return@launch
            val finalTitle = titleInput.ifBlank { metadata.title ?: item.title }
            repository?.updateWishlistItem(item.copy(
                title = finalTitle,
                link = link,
                imageUrl = metadata.imageUrl ?: item.imageUrl
            ), true)
        }
    }

    fun deleteWishlistItem(id: String) {
        viewModelScope.launch {
            repository?.deleteWishlistItem(id, true)
        }
    }
}
