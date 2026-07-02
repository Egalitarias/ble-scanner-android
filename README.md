# BLE Scanner

Android BLE scanner app (Kotlin + Jetpack Compose) with:
- Start/stop BLE scanning from UI
- Foreground-service background scanning
- iBeacon parsing (UUID, major, minor)
- Real-time RSSI minimum-threshold filtering

## Build and run

1. Open the `android/` project in Android Studio (latest stable).
2. Let Gradle sync and install SDK 37 if prompted.
3. Run the `app` configuration on a real Android device (BLE hardware required).

Or from terminal:

```bash
cd android
./gradlew build
```

## Running tests

From the `android/` directory:

```bash
# Unit tests (app/src/test)
./gradlew testDebugUnitTest

# Instrumentation test build check (app/src/androidTest compile)
./gradlew compileDebugAndroidTestKotlin

# Run connected instrumentation tests on a device/emulator
./gradlew connectedDebugAndroidTest
```

## Background scanning approach

- Scanning runs in `BleScanForegroundService`.
- The service starts as a foreground service with a persistent notification, so scanning can continue while the app is backgrounded or screen is off (within Android system limits).
- Scan results are pushed to `BleScanRepository` (in-memory `StateFlow`) and rendered by Compose UI.

### Limitations on newer Android versions

- Android background execution limits still apply; if the app process is terminated by the system, scanning stops until restarted.
- `ACCESS_FINE_LOCATION` runtime permission is required for BLE scanning.
- On Android 12+, `BLUETOOTH_SCAN`/`BLUETOOTH_CONNECT` runtime permissions are also required.
- On Android 13+, notification permission is also requested for foreground-service notification visibility.

## iBeacon parsing

- iBeacon data is parsed from manufacturer-specific data using Apple company ID `0x004C`.
- Parser checks for iBeacon prefix bytes `0x02 0x15`.
- Parsed fields:
  - Proximity UUID (16 bytes)
  - Major (2 bytes, big-endian)
  - Minor (2 bytes, big-endian)
- Non-iBeacon devices are still shown, with `iBeacon: Not detected`.

## RSSI filter

- UI includes a slider for minimum RSSI threshold (`-90` to `-40` dBm).
- Device list is filtered in real-time as scan results arrive.
