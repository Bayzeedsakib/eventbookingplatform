package com.example.eventbookingplatform.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class TicketType(
    val id: String,
    val name: String,
    val price: Double,
    val available: Int,
) {
    fun toMap(): Map<String, Any> = mapOf(
        FIELD_ID to id,
        FIELD_NAME to name,
        FIELD_PRICE to price,
        FIELD_AVAILABLE to available,
    )

    companion object {
        const val FIELD_ID = "id"
        const val FIELD_NAME = "name"
        const val FIELD_PRICE = "price"
        const val FIELD_AVAILABLE = "available"

        fun fromMap(map: Map<*, *>): TicketType? {
            val id = map[FIELD_ID] as? String ?: return null
            val name = map[FIELD_NAME] as? String ?: return null
            val price = (map[FIELD_PRICE] as? Number)?.toDouble() ?: return null
            val available = (map[FIELD_AVAILABLE] as? Number)?.toInt() ?: return null
            return TicketType(id = id, name = name, price = price, available = available)
        }

        fun fromDocumentList(raw: List<*>?): List<TicketType> {
            if (raw == null) return emptyList()
            return raw.mapNotNull { item ->
                (item as? Map<*, *>)?.let { fromMap(it) }
            }
        }
    }
}
