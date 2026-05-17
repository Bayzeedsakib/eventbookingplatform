package com.example.eventbookingplatform.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class UserProfile(
    val name: String,
    val email: String,
    val role: String = ROLE_ATTENDEE,
) {
    val greetingName: String
        get() = name.trim().substringBefore(' ').ifEmpty { name }

    fun toMap(): Map<String, Any> = mapOf(
        FIELD_NAME to name,
        FIELD_EMAIL to email,
        FIELD_ROLE to role,
    )

    companion object {
        const val COLLECTION_USERS = "users"
        const val ROLE_ATTENDEE = "attendee"

        const val FIELD_NAME = "name"
        const val FIELD_EMAIL = "email"
        const val FIELD_ROLE = "role"

        fun fromDocument(document: DocumentSnapshot): UserProfile? {
            val name = document.getString(FIELD_NAME) ?: return null
            val email = document.getString(FIELD_EMAIL) ?: return null
            val role = document.getString(FIELD_ROLE) ?: ROLE_ATTENDEE
            return UserProfile(name = name, email = email, role = role)
        }
    }
}
