package com.example.eventbookingplatform.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val venue: String,
    val city: String,
    val startAt: Date,
    val imageUrl: String = "",
    val featured: Boolean = false,
    val status: String = STATUS_PUBLISHED,
    val ticketTypes: List<TicketType> = emptyList(),
) {
    val minPrice: Double?
        get() = ticketTypes.minOfOrNull { it.price }

    val locationLabel: String
        get() = listOf(venue, city).filter { it.isNotBlank() }.joinToString(" · ")

    fun formattedStartDate(): String = DATE_FORMAT.format(startAt)

    fun toMap(): Map<String, Any?> = mapOf(
        FIELD_TITLE to title,
        FIELD_DESCRIPTION to description,
        FIELD_CATEGORY to category,
        FIELD_VENUE to venue,
        FIELD_CITY to city,
        FIELD_START_AT to Timestamp(startAt),
        FIELD_IMAGE_URL to imageUrl,
        FIELD_FEATURED to featured,
        FIELD_STATUS to status,
        FIELD_TICKET_TYPES to ticketTypes.map { it.toMap() },
    )

    companion object {
        const val COLLECTION_EVENTS = "events"

        const val STATUS_DRAFT = "draft"
        const val STATUS_PUBLISHED = "published"
        const val STATUS_CANCELLED = "cancelled"

        const val CATEGORY_MUSIC = "music"
        const val CATEGORY_TECH = "tech"
        const val CATEGORY_ART = "art"

        const val FIELD_TITLE = "title"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_CATEGORY = "category"
        const val FIELD_VENUE = "venue"
        const val FIELD_CITY = "city"
        const val FIELD_START_AT = "startAt"
        const val FIELD_IMAGE_URL = "imageUrl"
        const val FIELD_FEATURED = "featured"
        const val FIELD_STATUS = "status"
        const val FIELD_TICKET_TYPES = "ticketTypes"

        private val DATE_FORMAT = SimpleDateFormat("EEE, MMM d · h:mm a", Locale.getDefault())

        fun fromDocument(document: DocumentSnapshot): Event? {
            val title = document.getString(FIELD_TITLE) ?: return null
            val description = document.getString(FIELD_DESCRIPTION) ?: ""
            val category = document.getString(FIELD_CATEGORY) ?: return null
            val venue = document.getString(FIELD_VENUE) ?: ""
            val city = document.getString(FIELD_CITY) ?: ""
            val startAt = document.getTimestamp(FIELD_START_AT)?.toDate() ?: return null
            val imageUrl = document.getString(FIELD_IMAGE_URL).orEmpty()
            val featured = document.getBoolean(FIELD_FEATURED) ?: false
            val status = document.getString(FIELD_STATUS) ?: STATUS_PUBLISHED
            val ticketTypes =
                TicketType.fromDocumentList(document.get(FIELD_TICKET_TYPES) as? List<*>)

            return Event(
                id = document.id,
                title = title,
                description = description,
                category = category,
                venue = venue,
                city = city,
                startAt = startAt,
                imageUrl = imageUrl,
                featured = featured,
                status = status,
                ticketTypes = ticketTypes,
            )
        }
    }
}
