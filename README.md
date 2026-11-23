# 🎟 Event Booking Platform (Android)

A native **Android** app for discovering events and booking tickets, written in **Kotlin** with **Firebase Authentication** and **Cloud Firestore**. Attendees browse events, book tickets and show a **QR-code ticket** at the door. Admins create and manage events from inside the app.

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=black)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black)
![Firestore](https://img.shields.io/badge/Cloud_Firestore-FF6F00?style=flat-square&logo=firebase&logoColor=white)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-0E4D64?style=flat-square)

## ✨ Features

### For attendees
- 🔐 **Sign up / log in** with Firebase Authentication (email and password)
- 🏠 **Home**: featured events, upcoming events and category chips (Music, Tech, Art)
- 🔎 **Explore**: browse all published events
- 📄 **Event details**: venue, city, date, description, and ticket types with prices
- 🛒 **Booking**: pick a ticket type and quantity. Seats are reserved in a **Firestore transaction**, so two people can never buy the last ticket at the same time.
- 🎫 **My Tickets**: every booking as a ticket card with a **QR code** (ZXing)
- 👤 **Profile**: account details and logout

### For admins
- 🛠 **Event management**: create, edit and delete events
- 🎚 Set ticket types (name, price, capacity), a featured flag, and status (`draft` / `published` / `cancelled`)
- 🌱 Demo events are **seeded automatically** the first time an admin opens the app

## 🔒 Security (Firestore Rules)

All access control is enforced on the server in [`firestore.rules`](firestore.rules):

- Users can only read and write **their own** profile.
- Only **published** events are visible to non-admins.
- Only **admins** can create, delete or fully edit events.
- Signed-in users may update **only an event's `ticketTypes`** (the inventory) when booking. Every other field is locked.
- Bookings can be read only by their owner. They must have a positive integer quantity and **cannot be edited or deleted**.

## 🧱 Architecture

The app uses **MVVM** with repositories, ViewModels, LiveData and Kotlin coroutines.

```
app/src/main/java/com/example/eventbookingplatform/
├── data/
│   ├── model/        Event, TicketType, Booking, UserProfile
│   ├── repository/   AuthRepository, EventRepository, BookingRepository
│   └── seed/         Demo event data
└── ui/
    ├── auth/         Login / registration
    ├── home/         Featured + upcoming events, category chips
    ├── explore/      All events
    ├── detail/       Event details
    ├── booking/      Ticket type and quantity selection
    ├── tickets/      My tickets + QR code generator
    ├── profile/      User profile
    └── admin/        Admin event list and editor
```

Navigation is handled by **Jetpack Navigation** with a bottom bar: Home · Explore · Tickets · Profile.

## 🛠 Tech Stack

| Area | Libraries |
|---|---|
| Language | Kotlin, Coroutines |
| UI | Material Components, ViewBinding, RecyclerView, ConstraintLayout |
| Architecture | ViewModel, LiveData, Jetpack Navigation |
| Backend | Firebase Auth, Cloud Firestore |
| Images | Coil + OkHttp |
| QR codes | ZXing |
| Min / Target SDK | 24 / 36 |

## 🚀 Getting Started

**Prerequisites:** Android Studio (latest stable) and a Firebase project.

```bash
git clone https://github.com/Bayzeedsakib/eventbookingplatform.git
```

1. In the [Firebase console](https://console.firebase.google.com/), create a project and add an Android app with the package name `com.example.eventbookingplatform`.
2. Enable **Authentication → Email/Password** and create a **Cloud Firestore** database.
3. Download `google-services.json` and put it in `app/`. It is git-ignored, so it is not included in this repo.
4. Deploy the security rules and indexes:

   ```bash
   firebase deploy --only firestore
   ```

5. Open the project in Android Studio and run it on an emulator or a device.
6. To use the admin features, set `role: "admin"` on your user document in the `users` collection.

## 👤 Author

**Bayzeed Sakib** · [GitHub](https://github.com/Bayzeedsakib) · [Portfolio](https://bayzeed-sakib-portfolio.vercel.app)
