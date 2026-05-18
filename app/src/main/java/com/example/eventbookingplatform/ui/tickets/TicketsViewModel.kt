package com.example.eventbookingplatform.ui.tickets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventbookingplatform.data.model.Booking
import com.example.eventbookingplatform.data.repository.BookingException
import com.example.eventbookingplatform.data.repository.BookingRepository
import com.example.eventbookingplatform.data.repository.EventRepository
import kotlinx.coroutines.launch

enum class TicketsTab {
    UPCOMING,
    PAST,
}

sealed class TicketsUiState {
    data object Loading : TicketsUiState()

    data class Content(
        val upcoming: List<TicketDisplayItem>,
        val past: List<TicketDisplayItem>,
    ) : TicketsUiState()

    data class Error(val message: String) : TicketsUiState()
}

class TicketsViewModel(
    private val bookingRepository: BookingRepository = BookingRepository(),
    private val eventRepository: EventRepository = EventRepository(),
) : ViewModel() {

    private val _uiState = MutableLiveData<TicketsUiState>(TicketsUiState.Loading)
    val uiState: LiveData<TicketsUiState> = _uiState

    private val _selectedTab = MutableLiveData(TicketsTab.UPCOMING)
    val selectedTab: LiveData<TicketsTab> = _selectedTab

    fun setSelectedTab(tab: TicketsTab) {
        _selectedTab.value = tab
    }

    fun loadTickets() {
        viewModelScope.launch {
            _uiState.value = TicketsUiState.Loading
            val bookingsResult = bookingRepository.fetchUserBookings()
            bookingsResult
                .onSuccess { bookings ->
                    val confirmed = bookings.filter { it.status == Booking.STATUS_CONFIRMED }
                    val items = buildDisplayItems(confirmed)
                    val upcoming =
                        items.filter { it.isUpcoming }
                            .sortedBy { it.eventStartAt?.time ?: Long.MAX_VALUE }
                    val past =
                        items.filter { !it.isUpcoming }
                            .sortedByDescending { it.eventStartAt?.time ?: 0L }
                    _uiState.value = TicketsUiState.Content(upcoming = upcoming, past = past)
                }
                .onFailure { error ->
                    _uiState.value =
                        TicketsUiState.Error(
                            when (error) {
                                is BookingException.NotSignedIn ->
                                    "Sign in to view your tickets."
                                else -> error.message ?: "Could not load tickets."
                            },
                        )
                }
        }
    }

    private suspend fun buildDisplayItems(bookings: List<Booking>): List<TicketDisplayItem> {
        val eventCache = mutableMapOf<String, com.example.eventbookingplatform.data.model.Event?>()
        return bookings.map { booking ->
            val event =
                eventCache.getOrPut(booking.eventId) {
                    eventRepository.fetchPublishedEvent(booking.eventId)
                }
            TicketDisplayItem.fromBooking(booking, event)
        }
    }
}
