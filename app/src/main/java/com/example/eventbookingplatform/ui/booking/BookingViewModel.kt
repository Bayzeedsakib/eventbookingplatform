package com.example.eventbookingplatform.ui.booking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventbookingplatform.data.model.Event
import com.example.eventbookingplatform.data.model.TicketType
import com.example.eventbookingplatform.data.repository.BookingException
import com.example.eventbookingplatform.data.repository.BookingRepository
import com.example.eventbookingplatform.data.repository.EventRepository
import kotlinx.coroutines.launch

sealed class BookingUiState {
    data object Loading : BookingUiState()

    data object NotFound : BookingUiState()

    data class Error(val message: String) : BookingUiState()

    data class SelectTickets(
        val event: Event,
        val selectedTicketTypeId: String?,
        val quantity: Int,
    ) : BookingUiState() {
        val selectedTicketType: TicketType?
            get() = event.ticketTypes.find { it.id == selectedTicketTypeId }

        val maxQuantity: Int
            get() = selectedTicketType?.available ?: 0

        val totalPrice: Double
            get() = (selectedTicketType?.price ?: 0.0) * quantity

        val canContinue: Boolean
            get() = selectedTicketType != null && quantity in 1..maxQuantity
    }

    data class Payment(
        val event: Event,
        val ticketType: TicketType,
        val quantity: Int,
        val totalPrice: Double,
    ) : BookingUiState()

    data object Processing : BookingUiState()

    data class Success(val eventTitle: String) : BookingUiState()
}

class BookingViewModel(
    private val eventRepository: EventRepository = EventRepository(),
    private val bookingRepository: BookingRepository = BookingRepository(),
) : ViewModel() {

    private val _uiState = MutableLiveData<BookingUiState>(BookingUiState.Loading)
    val uiState: LiveData<BookingUiState> = _uiState

    private val _userMessage = MutableLiveData<String?>(null)
    val userMessage: LiveData<String?> = _userMessage

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun loadEvent(eventId: String) {
        if (eventId.isBlank()) {
            _uiState.value = BookingUiState.NotFound
            return
        }
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading
            runCatching { eventRepository.fetchPublishedEvent(eventId) }
                .onSuccess { event ->
                    if (event == null || event.ticketTypes.isEmpty()) {
                        _uiState.value = BookingUiState.NotFound
                        return@onSuccess
                    }
                    val defaultType =
                        event.ticketTypes.firstOrNull { it.available > 0 }
                            ?: event.ticketTypes.first()
                    _uiState.value =
                        BookingUiState.SelectTickets(
                            event = event,
                            selectedTicketTypeId = defaultType.id,
                            quantity = if (defaultType.available > 0) 1 else 0,
                        )
                }
                .onFailure { error ->
                    _uiState.value =
                        BookingUiState.Error(
                            error.message ?: "Could not load event. Please try again.",
                        )
                }
        }
    }

    fun selectTicketType(ticketTypeId: String) {
        val state = _uiState.value as? BookingUiState.SelectTickets ?: return
        val ticketType = state.event.ticketTypes.find { it.id == ticketTypeId } ?: return
        val quantity =
            when {
                state.quantity == 0 && ticketType.available > 0 -> 1
                state.quantity > ticketType.available -> ticketType.available
                else -> state.quantity
            }
        _uiState.value =
            state.copy(
                selectedTicketTypeId = ticketTypeId,
                quantity = quantity,
            )
    }

    fun setQuantity(quantity: Int) {
        val state = _uiState.value as? BookingUiState.SelectTickets ?: return
        val max = state.maxQuantity
        val clamped =
            when {
                max == 0 -> 0
                quantity < 1 -> 1
                quantity > max -> max
                else -> quantity
            }
        _uiState.value = state.copy(quantity = clamped)
    }

    fun incrementQuantity() {
        val state = _uiState.value as? BookingUiState.SelectTickets ?: return
        setQuantity(state.quantity + 1)
    }

    fun decrementQuantity() {
        val state = _uiState.value as? BookingUiState.SelectTickets ?: return
        setQuantity(state.quantity - 1)
    }

    fun continueToPayment() {
        val state = _uiState.value as? BookingUiState.SelectTickets ?: return
        val ticketType = state.selectedTicketType ?: return
        if (!state.canContinue) return
        _uiState.value =
            BookingUiState.Payment(
                event = state.event,
                ticketType = ticketType,
                quantity = state.quantity,
                totalPrice = state.totalPrice,
            )
    }

    fun backToSelectTickets() {
        val state = _uiState.value as? BookingUiState.Payment ?: return
        _uiState.value =
            BookingUiState.SelectTickets(
                event = state.event,
                selectedTicketTypeId = state.ticketType.id,
                quantity = state.quantity,
            )
    }

    fun confirmPayment() {
        val state = _uiState.value as? BookingUiState.Payment ?: return
        viewModelScope.launch {
            _uiState.value = BookingUiState.Processing
            bookingRepository
                .createBooking(
                    eventId = state.event.id,
                    ticketTypeId = state.ticketType.id,
                    quantity = state.quantity,
                )
                .onSuccess {
                    _uiState.value = BookingUiState.Success(eventTitle = state.event.title)
                }
                .onFailure { error ->
                    when (error) {
                        is BookingException.InsufficientCapacity -> {
                            _userMessage.value = error.message
                            reloadAfterCapacityError(state.event.id)
                        }
                        is BookingException -> {
                            _uiState.value = BookingUiState.Error(error.message ?: "Booking failed.")
                        }
                        else -> {
                            _uiState.value =
                                BookingUiState.Error(
                                    error.message ?: "Payment could not be completed. Please try again.",
                                )
                        }
                    }
                }
        }
    }

    private fun reloadAfterCapacityError(eventId: String) {
        viewModelScope.launch {
            val event = runCatching { eventRepository.fetchPublishedEvent(eventId) }.getOrNull()
            if (event == null) {
                _uiState.value = BookingUiState.NotFound
                return@launch
            }
            val soldOut = event.ticketTypes.all { it.available <= 0 }
            _uiState.value =
                if (soldOut) {
                    BookingUiState.Error("This event is sold out.")
                } else {
                    val firstAvailable =
                        event.ticketTypes.firstOrNull { it.available > 0 } ?: event.ticketTypes.first()
                    BookingUiState.SelectTickets(
                        event = event,
                        selectedTicketTypeId = firstAvailable.id,
                        quantity = if (firstAvailable.available > 0) 1 else 0,
                    )
                }
        }
    }
}
