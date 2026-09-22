# APK inventory

This document records only what can be established from the compiled APK. It
is not a substitute for the missing source project.

## File facts

| Field | Value |
| --- | --- |
| Original filename | `Guardian-1_(1)_(3)_1790091742537.apk` |
| APK size | 32,926,496 bytes |
| SHA-256 | `ad77be82b9c63a0cd22b8e1c51469ae719e9d5eab9711d3e11a9e6d427b0a0fa` |
| Android build metadata | Android Gradle Plugin `8.5.2` |
| DEX files | `classes.dex`, `classes2.dex`, `classes3.dex`, `classes4.dex` |
| Native ABIs | `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64` |

## Inferred application structure

Compiled class names identify the package `com.guardian.app` and these
application areas:

- `MainActivity` with a bottom-bar/root navigation surface
- `AuthScreen`
- `OnboardingScreen`
- `HomeScreen`
- `EventsScreen`
- `IncidentsScreen`
- `SettingsScreen`
- QR scanning through `QrScanActivity`
- Incoming-call monitoring and warnings through
  `CallMonitorService` and `CallWarningActivity`
- Link interception through `LinkInterceptActivity`
- UPI interception through `UpiInterceptActivity`
- Notification monitoring through `MessageListenerService`
- Local state and network coordination through `Store`, `Prefs`, `Api`, and
  `EventStream`

The build also contains ML Kit barcode assets and CameraX-related components,
which supports the QR-scanning inference.

## Reconstruction plan

1. Create a new Android Gradle project using Kotlin and Jetpack Compose.
2. Recreate the app shell, authentication, onboarding, home, event,
   incident, and settings flows.
3. Add QR scanning with CameraX and ML Kit.
4. Recreate call, link, UPI, and notification protection as separate,
   permission-gated services/activities.
5. Add the backend contract and local persistence only after the observable
   behavior of the baseline is documented.
6. Build each milestone and compare it with the preserved APK on a test
   device.

## Important limitation

The APK does not preserve the original Gradle files, Kotlin source files,
resource source files, backend source, signing configuration, or development
history. Any implementation added to this repository should therefore be
considered a clean reconstruction, not a recovered copy of the original
project.