package com.example.eventbookingplatform.ui.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventbookingplatform.BuildConfig
import com.example.eventbookingplatform.data.model.Event
import com.example.eventbookingplatform.data.repository.EventRepository
import kotlinx.coroutines.launch

sealed class EventsUiState {
    data object Loading : EventsUiState()

    data class Success(
        val events: List<Event>,
        val featuredEvents: List<Event>,
    ) : EventsUiState()

    data class Error(val message: String) : EventsUiState()
}

class EventsViewModel(
    private val repository: EventRepository = EventRepository(),
) : ViewModel() {

    private val _eventsState = MutableLiveData<EventsUiState>(EventsUiState.Loading)
    val eventsState: LiveData<EventsUiState> = _eventsState

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _eventsState.value = EventsUiState.Loading
            runCatching {
                if (BuildConfig.DEBUG) {
                    runCatching { repository.seedDemoEventsIfEmpty() }
                }
                val published = repository.fetchPublishedEvents()
                EventsUiState.Success(
                    events = published,
                    featuredEvents = published.filter { it.featured },
                )
            }
                .onSuccess { _eventsState.value = it }
                .onFailure { error ->
                    _eventsState.value =
                        EventsUiState.Error(
                            error.message ?: "Could not load events. Please try again.",
                        )
                }
        }
    }
}
