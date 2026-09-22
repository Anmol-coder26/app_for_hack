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

The next development step is to reconstruct a clean Android project in this
repository. The existing APK should be treated as the reference build while
the reconstructed project is brought up screen by screen and behavior by
behavior. Do not overwrite `artifacts/Guardian-1.apk`; it is the baseline for
comparison.

See [`docs/apk-inventory.md`](docs/apk-inventory.md) for the reverse-engineered
inventory and the recommended reconstruction order.

## Baseline verification

The SHA-256 checksum of the preserved APK is recorded in the inventory so a
future replacement can be distinguished from the original upload.