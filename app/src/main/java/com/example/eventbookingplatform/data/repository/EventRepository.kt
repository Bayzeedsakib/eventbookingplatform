package com.example.eventbookingplatform.data.repository

import com.example.eventbookingplatform.data.model.Event
import com.example.eventbookingplatform.data.model.UserProfile
import com.example.eventbookingplatform.data.seed.EventSeedData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class EventRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {

    /**
     * Writes demo events when the collection is empty. Works in Firestore test mode, or when
     * signed in as admin after security rules are deployed. Returns false if skipped or denied.
     */
    suspend fun seedDemoEventsIfEmpty(): Boolean {
        if (hasAnyEvents()) return false

        return runCatching {
            commitSeedBatch()
            true
        }.recoverCatching { error ->
            if (!isPermissionDenied(error)) throw error
            seedAsAdminIfEmpty()
        }.getOrDefault(false)
    }

    private suspend fun hasAnyEvents(): Boolean {
        val existing =
            firestore.collection(Event.COLLECTION_EVENTS).limit(1).get().await()
        return !existing.isEmpty
    }

    private suspend fun seedAsAdminIfEmpty(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val userDoc =
            firestore.collection(UserProfile.COLLECTION_USERS).document(uid).get().await()
        if (userDoc.getString(UserProfile.FIELD_ROLE) != UserProfile.ROLE_ADMIN) return false
        if (hasAnyEvents()) return false
        commitSeedBatch()
        return true
    }

    private suspend fun commitSeedBatch() {
        val batch = firestore.batch()
        EventSeedData.demoEvents.forEach { event ->
            val doc = firestore.collection(Event.COLLECTION_EVENTS).document(event.id)
            batch.set(doc, event.toMap())
        }
        batch.commit().await()
    }

    private fun isPermissionDenied(error: Throwable): Boolean {
        val message = error.message.orEmpty().lowercase()
        return message.contains("permission") || message.contains("denied")
    }

    suspend fun fetchPublishedEvents(): List<Event> {
        val snapshot =
            firestore.collection(Event.COLLECTION_EVENTS)
                .whereEqualTo(Event.FIELD_STATUS, Event.STATUS_PUBLISHED)
                .get()
                .await()
        return snapshot.documents
            .mapNotNull { Event.fromDocument(it) }
            .sortedBy { it.startAt }
    }

    suspend fun fetchFeaturedPublishedEvents(limit: Int = 5): List<Event> =
        fetchPublishedEvents().filter { it.featured }.take(limit)

    suspend fun fetchPublishedEvent(eventId: String): Event? {
        val document =
            firestore.collection(Event.COLLECTION_EVENTS).document(eventId).get().await()
        if (!document.exists()) return null
        val event = Event.fromDocument(document) ?: return null
        return event.takeIf { it.status == Event.STATUS_PUBLISHED }
    }
}
