package com.example.eventbookingplatform.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventbookingplatform.data.model.Event
import com.example.eventbookingplatform.data.repository.EventRepository
import kotlinx.coroutines.launch

sealed class AdminEventsUiState {
    data object Loading : AdminEventsUiState()
    data class Content(val events: List<Event>) : AdminEventsUiState()
    data class Error(val message: String) : AdminEventsUiState()
}

class AdminEventsViewModel(
    private val repository: EventRepository = EventRepository(),
) : ViewModel() {

    private val _uiState = MutableLiveData<AdminEventsUiState>(AdminEventsUiState.Loading)
    val uiState: LiveData<AdminEventsUiState> = _uiState

    private val _deleteError = MutableLiveData<String?>(null)
    val deleteError: LiveData<String?> = _deleteError

    private val _eventListChanged = MutableLiveData<Unit>()
    val eventListChanged: LiveData<Unit> = _eventListChanged

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = AdminEventsUiState.Loading
            runCatching { repository.fetchAllEventsForAdmin() }
                .onSuccess { events -> _uiState.value = AdminEventsUiState.Content(events) }
                .onFailure { error ->
                    _uiState.value =
                        AdminEventsUiState.Error(
                            error.message ?: "Could not load events.",
                        )
                }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _deleteError.value = null
            runCatching { repository.deleteEvent(eventId) }
                .onSuccess {
                    loadEvents()
                    _eventListChanged.value = Unit
                }
                .onFailure { error ->
                    _deleteError.value = error.message ?: "Could not delete event."
                }
        }
    }

    fun clearDeleteError() {
        _deleteError.value = null
    }
}
