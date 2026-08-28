# I:ON Android Client

The Android application is the user-facing client for I:ON. It presents parenting workbooks, voice-report results, chatbot conversations, onboarding, and profile-related flows while delegating persistence and AI processing to backend APIs.

## Features

- Account registration and sign-in, including the parenting-style questionnaire used during onboarding.
- Home and profile views for user information, progress, rewards, and recent content.
- Workbook chapter and lesson navigation, written and multiple-choice activities, conversation simulation, and feedback.
- Selection and upload of conversation media, plus visualization of voice-report summaries and details.
- Parenting chatbot conversations and chat-history browsing.

## Client Architecture

The code is separated into three main layers:

- `presentation/` contains Jetpack Compose screens, navigation graphs, UI state, and Hilt ViewModels.
- `domain/` defines application models and repository interfaces used by the presentation layer.
- `data/` contains Retrofit services, remote and local data sources, repository implementations, DTOs, and mappers.

```text
Compose UI / ViewModel
          |
          v
   Domain repository
          |
          v
Repository implementation
     |             |
     v             v
Retrofit API   Local DataStore
```

Hilt provides application-wide dependencies. Navigation Compose connects the authentication, home, workbook, voice-report, and chatbot graphs.

## Networking and Session Handling

Retrofit and OkHttp provide the REST client. Kotlin Serialization maps request and response DTOs, and an OkHttp interceptor attaches the stored `JSESSIONID` cookie when a session is available. The session identifier is persisted with Android DataStore.

The client uses JSON for normal API calls and multipart requests for profile images and voice-report media. Debug builds enable HTTP body logging; release builds disable it.

## Main Packages

- `presentation/auth` — registration, sign-in, parenting-style questionnaire, and onboarding
- `presentation/home` — home content and navigation
- `presentation/workbook` — chapters, lessons, exercises, simulation, and feedback
- `presentation/voicescreen` — media selection, upload, report list, and report details
- `presentation/chatbot` — chat sessions and history
- `core/` — navigation, design-system components, networking, and shared utilities
- `data/` and `domain/` — API integration, persistence contracts, DTO mapping, and domain models

## Technology

- Kotlin and Java 17 bytecode target
- Jetpack Compose and Material 3
- Navigation Compose and lifecycle ViewModel
- Hilt and KSP
- Retrofit, OkHttp, and Kotlin Serialization
- Android DataStore
- Coil, Lottie, Firebase Crashlytics, and Kakao Login

The project targets Android API 35 and supports API 28 and later.

## Local Configuration

`android/local.properties` is intentionally excluded from Git. Android Studio normally creates it with the local SDK path; the build also expects local values used for Kakao integration and external application links. Keep all local values out of commits.

The backend base URL is supplied through `BuildConfig` in the app module. A working local build must point it at an available backend instance; the original demonstration server is not assumed to be online.

## Build

Requirements:

- Android Studio with Android SDK 35
- JDK 17
- Required values in `android/local.properties`

From the repository root:

```bash
cd android
./gradlew assembleDebug
```

The original team project was primarily validated through manual, device-based user flows. The retired hosted backend and AI services limit current end-to-end execution unless equivalent local services are configured.

## Related Documentation

- [Project overview](../README.md)
- [Backend](../backend/README.md)
- [AI services](../ai/README.md)
