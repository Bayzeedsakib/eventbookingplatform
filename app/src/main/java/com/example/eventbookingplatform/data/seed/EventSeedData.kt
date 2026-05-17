package com.example.eventbookingplatform.data.seed

import com.example.eventbookingplatform.data.model.Event
import com.example.eventbookingplatform.data.model.TicketType
import java.util.Calendar

object EventSeedData {

  const val ID_NEON_NIGHTS = "event_neon_nights"
  const val ID_TECH_FORWARD = "event_tech_forward"
  const val ID_URBAN_ART = "event_urban_art"

  val demoEvents: List<Event> = listOf(
      neonNightsFestival(),
      techForwardSummit(),
      urbanArtWalk(),
  )

  private fun neonNightsFestival(): Event {
    val startAt = daysFromNow(days = 14, hour = 20, minute = 0)
    return Event(
        id = ID_NEON_NIGHTS,
        title = "Neon Nights Festival",
        description =
            "An open-air electronic music night with local DJs, light installations, and food trucks.",
        category = Event.CATEGORY_MUSIC,
        venue = "Riverside Amphitheater",
        city = "Dhaka",
        startAt = startAt,
        featured = true,
        status = Event.STATUS_PUBLISHED,
        ticketTypes =
            listOf(
                TicketType(id = "general", name = "General", price = 25.0, available = 200),
                TicketType(id = "vip", name = "VIP", price = 65.0, available = 40),
            ),
    )
  }

  private fun techForwardSummit(): Event {
    val startAt = daysFromNow(days = 21, hour = 10, minute = 0)
    return Event(
        id = ID_TECH_FORWARD,
        title = "Tech Forward Summit",
        description =
            "Talks and workshops on mobile development, cloud, and product design for students and juniors.",
        category = Event.CATEGORY_TECH,
        venue = "Innovation Hub Hall A",
        city = "Dhaka",
        startAt = startAt,
        featured = true,
        status = Event.STATUS_PUBLISHED,
        ticketTypes =
            listOf(
                TicketType(id = "general", name = "General", price = 15.0, available = 150),
                TicketType(id = "student", name = "Student", price = 8.0, available = 80),
            ),
    )
  }

  private fun urbanArtWalk(): Event {
    val startAt = daysFromNow(days = 7, hour = 16, minute = 30)
    return Event(
        id = ID_URBAN_ART,
        title = "Urban Art Walk",
        description =
            "Guided walk through street murals and pop-up galleries. Includes sketchbook and map.",
        category = Event.CATEGORY_ART,
        venue = "Old Town Arts District",
        city = "Chittagong",
        startAt = startAt,
        featured = false,
        status = Event.STATUS_PUBLISHED,
        ticketTypes =
            listOf(
                TicketType(id = "general", name = "General", price = 12.0, available = 60),
            ),
    )
  }

  private fun daysFromNow(days: Int, hour: Int, minute: Int) =
      Calendar.getInstance()
          .apply {
            add(Calendar.DAY_OF_YEAR, days)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
          }
          .time
}
