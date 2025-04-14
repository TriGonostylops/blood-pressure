# Blood Pressure App

A simple Android application built in Java using Firebase Authentication. This app allows users to register, log in, and (in future features) record their blood pressure measurements. It is developed as part of a school project with a focus on material design, user authentication, and modular navigation using Jetpack Navigation components.

---

## Features

- ✅ Firebase Authentication implemented
    - Users can register and log in
- ✅ Proper input field types
    - Password fields are obscured
    - Email fields show appropriate keyboard
- ✅ Uses both `ConstraintLayout` and another layout type (`LinearLayout`)
- ✅ Responsive UI
    - Layout adapts well to different screen sizes and orientations (e.g. tablet, landscape)
- ✅ One animation used (Material FadeThrough on fragment transitions)
- ✅ Intents and activity navigation properly implemented
    - All Activities are accessible

---

## Tech Stack

- **Language**: Java
- **UI Framework**: XML layouts with `ConstraintLayout` and `LinearLayout`
- **Architecture**: Single-activity architecture with multiple Fragments
- **Firebase**: Authentication
- **Navigation**: Jetpack Navigation component
- **UI Components**: Material Components for Android
- **Animations**: Material Shared Transitions

---

## Navigation Structure

- `HomeFragment` – displays welcome message with user's email
- `AddMeasurementFragment` – placeholder for recording new measurements
- `MeasurementFragment` – placeholder for viewing past records
- `SettingsFragment` – placeholder for user preferences

---

## Next Milestone Goals

- [ ] Responsive UI remains elegant on large displays and orientation changes
- [ ] At least **two different animations** used
- [ ] Intents used across multiple activities for all navigation flows
- [ ] Use **at least one lifecycle hook** in a meaningful way
    - (Note: `onCreate()` does not count)
- [ ] Use **at least two Android resources that require permissions**
    - (e.g., accessing storage, location, or camera in a way that makes sense for the app)
- [ ] Use **two distinct Android system services**, such as:
    - Notifications
    - AlarmManager
    - JobScheduler
- [ ] Implement full **CRUD functionality** for measurements
- [ ] Implement **three complex Firestore queries** that require indexes
    - (e.g., filters with `where`, `orderBy`, pagination, or limits)
- [ ] Subjective evaluation: loses points if the project is rushed, low effort, or overly similar to reference videos

---


