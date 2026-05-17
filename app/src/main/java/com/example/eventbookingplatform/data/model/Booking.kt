package com.example.eventbookingplatform.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

data class Booking(
    val id: String,
    val userId: String,
    val eventId: String,
    val eventTitle: String,
    val ticketTypeId: String,
    val ticketTypeName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val status: String = STATUS_CONFIRMED,
    val createdAt: Date = Date(),
) {
    fun toMap(): Map<String, Any> = mapOf(
        FIELD_USER_ID to userId,
        FIELD_EVENT_ID to eventId,
        FIELD_EVENT_TITLE to eventTitle,
        FIELD_TICKET_TYPE_ID to ticketTypeId,
        FIELD_TICKET_TYPE_NAME to ticketTypeName,
        FIELD_QUANTITY to quantity,
        FIELD_UNIT_PRICE to unitPrice,
        FIELD_TOTAL_PRICE to totalPrice,
        FIELD_STATUS to status,
        FIELD_CREATED_AT to Timestamp(createdAt),
    )

    companion object {
        const val COLLECTION_BOOKINGS = "bookings"

        const val STATUS_CONFIRMED = "confirmed"
        const val STATUS_CANCELLED = "cancelled"

        const val FIELD_USER_ID = "userId"
        const val FIELD_EVENT_ID = "eventId"
        const val FIELD_EVENT_TITLE = "eventTitle"
        const val FIELD_TICKET_TYPE_ID = "ticketTypeId"
        const val FIELD_TICKET_TYPE_NAME = "ticketTypeName"
        const val FIELD_QUANTITY = "quantity"
        const val FIELD_UNIT_PRICE = "unitPrice"
        const val FIELD_TOTAL_PRICE = "totalPrice"
        const val FIELD_STATUS = "status"
        const val FIELD_CREATED_AT = "createdAt"

        fun fromDocument(document: DocumentSnapshot): Booking? {
            val userId = document.getString(FIELD_USER_ID) ?: return null
            val eventId = document.getString(FIELD_EVENT_ID) ?: return null
            val eventTitle = document.getString(FIELD_EVENT_TITLE) ?: return null
            val ticketTypeId = document.getString(FIELD_TICKET_TYPE_ID) ?: return null
            val ticketTypeName = document.getString(FIELD_TICKET_TYPE_NAME) ?: return null
            val quantity = document.getLong(FIELD_QUANTITY)?.toInt() ?: return null
            val unitPrice = document.getDouble(FIELD_UNIT_PRICE) ?: return null
            val totalPrice = document.getDouble(FIELD_TOTAL_PRICE) ?: return null
            val status = document.getString(FIELD_STATUS) ?: STATUS_CONFIRMED
            val createdAt = document.getTimestamp(FIELD_CREATED_AT)?.toDate() ?: Date()

            return Booking(
                id = document.id,
                userId = userId,
                eventId = eventId,
                eventTitle = eventTitle,
                ticketTypeId = ticketTypeId,
                ticketTypeName = ticketTypeName,
                quantity = quantity,
                unitPrice = unitPrice,
                totalPrice = totalPrice,
                status = status,
                createdAt = createdAt,
            )
        }
    }
}
