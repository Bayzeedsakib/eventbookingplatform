package com.example.eventbookingplatform.ui.tickets

import com.example.eventbookingplatform.data.model.Booking
import com.example.eventbookingplatform.data.model.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TicketDisplayItem(
    val booking: Booking,
    val eventStartAt: Date?,
    val venue: String,
    val city: String,
    val category: String,
) {
    val isUpcoming: Boolean
        get() {
            val startAt = eventStartAt ?: return true
            return startAt.time >= System.currentTimeMillis()
        }

    val locationLabel: String
        get() = listOf(venue, city).filter { it.isNotBlank() }.joinToString(" · ")

    fun formattedEventDate(): String =
        eventStartAt?.let { DATE_FORMAT.format(it) } ?: ""

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("EEE, MMM d · h:mm a", Locale.getDefault())

        fun fromBooking(booking: Booking, event: Event?): TicketDisplayItem =
            TicketDisplayItem(
                booking = booking,
                eventStartAt = event?.startAt,
                venue = event?.venue.orEmpty(),
                city = event?.city.orEmpty(),
                category = event?.category.orEmpty(),
            )
    }
}
