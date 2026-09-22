# Guardian Android App

This repository preserves the current Guardian Android build and documents the
starting point for future changes.

## Current state

- Original APK: `artifacts/Guardian-1.apk`
- Application package inferred from compiled classes: `com.guardian.app`
- Build metadata: Android Gradle Plugin `8.5.2`
- The APK is built with Jetpack Compose and includes camera/barcode scanning
  support.
- The original editable Android Studio/Gradle source project was not included
  with the APK. An APK is a compiled release artifact, so it cannot be edited
  like a normal source project.

## Making changes

An editable Kotlin/Jetpack Compose reconstruction now lives at the repository
root. It keeps the `com.guardian.app` package identity and recreates the
observable app shell: authentication, onboarding, home, events, incidents,
settings, protection toggles, local incident reporting, incoming-call warnings,
and suspicious notification warnings.

The original APK remains the reference build at `artifacts/Guardian-1.apk`;
do not overwrite it. The reconstructed MVP intentionally uses local demo state
because the original backend was not included in the APK. Call protection uses
the Android phone-state permission. Message protection uses Android's
Notification Listener access, which must be enabled by the device owner in
system settings.

See [`docs/apk-inventory.md`](docs/apk-inventory.md) for the reverse-engineered
inventory and the recommended reconstruction order.

## Baseline verification

The SHA-256 checksum of the preserved APK is recorded in the inventory so a
future replacement can be distinguished from the original upload.