package com.example.eventbookingplatform.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventbookingplatform.data.model.Event
import com.example.eventbookingplatform.data.model.TicketType
import com.example.eventbookingplatform.data.repository.EventRepository
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AdminTicketTypeForm(
    val name: String,
    val priceText: String,
    val availableText: String,
) {
    fun toTicketTypeOrNull(): TicketType? {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return null
        val price = priceText.trim().toDoubleOrNull() ?: return null
        val available = availableText.trim().toIntOrNull() ?: return null
        if (price < 0 || available < 0) return null
        val id = trimmedName.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "_").trim('_')
            .ifEmpty { UUID.randomUUID().toString().take(8) }
        return TicketType(id = id, name = trimmedName, price = price, available = available)
    }
}

data class AdminEventForm(
    val title: String = "",
    val description: String = "",
    val category: String = Event.CATEGORY_MUSIC,
    val venue: String = "",
    val city: String = "",
    val startAt: Date? = null,
    val imageUrl: String = "",
    val featured: Boolean = false,
    val status: String = Event.STATUS_DRAFT,
    val generalTicket: AdminTicketTypeForm =
        AdminTicketTypeForm(name = "General", priceText = "", availableText = ""),
    val secondTicket: AdminTicketTypeForm =
        AdminTicketTypeForm(name = "", priceText = "", availableText = ""),
) {
    fun validate(): String? {
        if (title.isBlank()) return "Title is required."
        if (venue.isBlank() || city.isBlank()) return "Venue and city are required."
        if (startAt == null) return "Start date and time are required."
        val tickets = buildTicketTypes()
        if (tickets.isEmpty()) return "Add at least one ticket type with price and quantity."
        return null
    }

    fun buildTicketTypes(): List<TicketType> {
        val types = mutableListOf<TicketType>()
        generalTicket.toTicketTypeOrNull()?.let { types.add(it) }
        secondTicket.toTicketTypeOrNull()?.let { second ->
            if (types.none { it.id == second.id }) types.add(second)
        }
        return types
    }

    fun toEvent(eventId: String): Event =
        Event(
            id = eventId,
            title = title.trim(),
            description = description.trim(),
            category = category,
            venue = venue.trim(),
            city = city.trim(),
            startAt = startAt!!,
            imageUrl = imageUrl.trim(),
            featured = featured,
            status = status,
            ticketTypes = buildTicketTypes(),
        )

    companion object {
        fun fromEvent(event: Event): AdminEventForm {
            val general = event.ticketTypes.firstOrNull()
            val second = event.ticketTypes.getOrNull(1)
            return AdminEventForm(
                title = event.title,
                description = event.description,
                category = event.category,
                venue = event.venue,
                city = event.city,
                startAt = event.startAt,
                imageUrl = event.imageUrl,
                featured = event.featured,
                status = event.status,
                generalTicket =
                    AdminTicketTypeForm(
                        name = general?.name ?: "General",
                        priceText = general?.price?.toString().orEmpty(),
                        availableText = general?.available?.toString().orEmpty(),
                    ),
                secondTicket =
                    AdminTicketTypeForm(
                        name = second?.name.orEmpty(),
                        priceText = second?.price?.toString().orEmpty(),
                        availableText = second?.available?.toString().orEmpty(),
                    ),
            )
        }
    }
}

sealed class AdminEventEditUiState {
    data object Loading : AdminEventEditUiState()
    data class Editing(val form: AdminEventForm, val isNew: Boolean) : AdminEventEditUiState()
    data class Error(val message: String) : AdminEventEditUiState()
    data object Saved : AdminEventEditUiState()
}

class AdminEventEditViewModel(
    private val repository: EventRepository = EventRepository(),
) : ViewModel() {

    private val _uiState = MutableLiveData<AdminEventEditUiState>(AdminEventEditUiState.Loading)
    val uiState: LiveData<AdminEventEditUiState> = _uiState

    private val _saveError = MutableLiveData<String?>(null)
    val saveError: LiveData<String?> = _saveError

    private var eventId: String? = null

    fun load(eventIdArg: String?) {
        eventId = eventIdArg?.takeIf { it.isNotBlank() }
        val existingId = eventId
        if (existingId == null) {
            _uiState.value = AdminEventEditUiState.Editing(AdminEventForm(), isNew = true)
            return
        }
        viewModelScope.launch {
            _uiState.value = AdminEventEditUiState.Loading
            runCatching { repository.fetchEventForAdmin(existingId) }
                .onSuccess { event ->
                    if (event == null) {
                        _uiState.value = AdminEventEditUiState.Error("Event not found.")
                    } else {
                        _uiState.value =
                            AdminEventEditUiState.Editing(
                                AdminEventForm.fromEvent(event),
                                isNew = false,
                            )
                    }
                }
                .onFailure { error ->
                    _uiState.value =
                        AdminEventEditUiState.Error(
                            error.message ?: "Could not load event.",
                        )
                }
        }
    }

    fun save(form: AdminEventForm) {
        val validationError = form.validate()
        if (validationError != null) {
            _saveError.value = validationError
            return
        }
        viewModelScope.launch {
            _saveError.value = null
            runCatching {
                val existingId = eventId
                if (existingId == null) {
                    val newId = repository.createEvent(form.toEvent(""))
                    eventId = newId
                } else {
                    repository.updateEvent(form.toEvent(existingId))
                }
            }
                .onSuccess { _uiState.value = AdminEventEditUiState.Saved }
                .onFailure { error ->
                    _saveError.value = error.message ?: "Could not save event."
                }
        }
    }

    fun clearSaveError() {
        _saveError.value = null
    }
}
