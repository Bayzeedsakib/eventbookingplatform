package com.example.eventbookingplatform.data.repository

import com.example.eventbookingplatform.data.model.Booking
import com.example.eventbookingplatform.data.model.Event
import com.example.eventbookingplatform.data.model.TicketType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BookingRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {

    suspend fun createBooking(
        eventId: String,
        ticketTypeId: String,
        quantity: Int,
    ): Result<String> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(BookingException.NotSignedIn)

        if (quantity <= 0) {
            return Result.failure(BookingException.InvalidQuantity)
        }

        return runCatching {
            firestore.runTransaction { transaction ->
                val eventRef = firestore.collection(Event.COLLECTION_EVENTS).document(eventId)
                val eventSnapshot = transaction.get(eventRef)
                val event = Event.fromDocument(eventSnapshot)
                    ?: throw BookingException.EventNotFound

                if (event.status != Event.STATUS_PUBLISHED) {
                    throw BookingException.EventNotFound
                }

                val ticketType = event.ticketTypes.find { it.id == ticketTypeId }
                    ?: throw BookingException.TicketTypeNotFound

                if (quantity > ticketType.available) {
                    throw BookingException.InsufficientCapacity(
                        available = ticketType.available,
                    )
                }

                val updatedTicketTypes =
                    event.ticketTypes.map { type ->
                        if (type.id == ticketTypeId) {
                            type.copy(available = type.available - quantity)
                        } else {
                            type
                        }
                    }

                transaction.update(
                    eventRef,
                    mapOf(Event.FIELD_TICKET_TYPES to updatedTicketTypes.map(TicketType::toMap)),
                )

                val bookingRef =
                    firestore.collection(Booking.COLLECTION_BOOKINGS).document()
                val unitPrice = ticketType.price
                val booking =
                    Booking(
                        id = bookingRef.id,
                        userId = userId,
                        eventId = event.id,
                        eventTitle = event.title,
                        ticketTypeId = ticketType.id,
                        ticketTypeName = ticketType.name,
                        quantity = quantity,
                        unitPrice = unitPrice,
                        totalPrice = unitPrice * quantity,
                    )
                transaction.set(bookingRef, booking.toMap())
                bookingRef.id
            }.await()
        }
    }
}

sealed class BookingException(message: String) : Exception(message) {
    data object NotSignedIn : BookingException("You must be signed in to book.")

    data object InvalidQuantity : BookingException("Select at least one ticket.")

    data object EventNotFound : BookingException("This event is no longer available.")

    data object TicketTypeNotFound : BookingException("This ticket type is no longer available.")

    class InsufficientCapacity(val available: Int) :
        BookingException("Only $available ticket(s) left for this type.")
}
