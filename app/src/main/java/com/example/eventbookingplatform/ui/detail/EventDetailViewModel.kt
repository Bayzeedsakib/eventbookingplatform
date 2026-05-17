package com.example.eventbookingplatform.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventbookingplatform.data.model.Event
import com.example.eventbookingplatform.data.repository.EventRepository
import kotlinx.coroutines.launch

sealed class EventDetailUiState {
    data object Loading : EventDetailUiState()

    data class Success(val event: Event) : EventDetailUiState()

    data object NotFound : EventDetailUiState()

    data class Error(val message: String) : EventDetailUiState()
}

class EventDetailViewModel(
    private val repository: EventRepository = EventRepository(),
) : ViewModel() {

    private val _uiState = MutableLiveData<EventDetailUiState>(EventDetailUiState.Loading)
    val uiState: LiveData<EventDetailUiState> = _uiState

    fun loadEvent(eventId: String) {
        if (eventId.isBlank()) {
            _uiState.value = EventDetailUiState.NotFound
            return
        }
        viewModelScope.launch {
            _uiState.value = EventDetailUiState.Loading
            runCatching { repository.fetchPublishedEvent(eventId) }
                .onSuccess { event ->
                    _uiState.value =
                        if (event != null) {
                            EventDetailUiState.Success(event)
                        } else {
                            EventDetailUiState.NotFound
                        }
                }
                .onFailure { error ->
                    _uiState.value =
                        EventDetailUiState.Error(
                            error.message ?: "Could not load event. Please try again.",
                        )
                }
        }
    }
}
