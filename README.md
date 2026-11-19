# AdSkipper

A small native Android app (Kotlin) that automatically taps the **Skip ads** button in the official YouTube app (`com.google.android.youtube`).

YouTube is never modified, patched or repackaged. AdSkipper registers an Android `AccessibilityService` limited to YouTube's package. When a Skip button appears on screen, the service clicks it. It reads only the YouTube screen, has no INTERNET permission, and collects or sends nothing.

For personal use, installed by APK. Not intended for the Play Store.

## Build

Requirements: JDK 17 and the Android SDK (platform 36).

```bash
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

Create a `local.properties` file with your SDK path if Gradle cannot find it:

```
sdk.dir=/path/to/Android/Sdk
```

## Install

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or copy the APK to the phone and install it by hand.

## Set up on the phone

1. On Android 13 and later: Settings > Apps > AdSkipper > three-dot menu > **Allow restricted settings**.
2. Open AdSkipper, tap **Open Accessibility settings**, choose AdSkipper and switch it ON. The app should read "Service is ON".
3. Tap **Open battery optimization settings** and set AdSkipper to **Unrestricted** so Android does not stop the service.
4. Play a YouTube video with a skippable ad. The Skip button should be tapped within about a second.

To confirm clicks over adb:

```bash
adb logcat -s SkipAdService
```

It prints `Clicked skip button` for each skip.

## If the button is not clicked

The view id `skip_ad_button` is a best guess and can change when YouTube updates. The service falls back to matching the exact text or content description (`skip ads`, `skip ad`, `skip`). To inspect the real node while a skippable ad is on screen:

```bash
adb shell uiautomator dump /sdcard/ui.xml
adb pull /sdcard/ui.xml .
grep -i -E "skip" ui.xml
```

Read the `resource-id`, `text` and `content-desc` of the matching node, then update the view id in `SkipAdService.kt` and add any new label (lowercase) to `LABELS`. Android Studio's Layout Inspector does not work on YouTube, so use the dump instead.

## Limitations

- Only ads that show a Skip button are skipped, which appears after the countdown (usually 5 seconds). Non-skippable and banner ads stay.
- A YouTube update can rename the button id or label, which breaks the click until the constants are updated.
- Google's Play policy restricts accessibility services, so this is for personal sideloaded use only.

## Project layout

| File | Purpose |
| --- | --- |
| `app/src/main/AndroidManifest.xml` | Declares MainActivity and the accessibility service |
| `app/src/main/res/xml/accessibility_config.xml` | Limits the service to YouTube's package |
| `app/src/main/res/values/strings.xml` | App name and service description |
| `app/src/main/java/com/example/adskipper/SkipAdService.kt` | Finds and clicks the Skip button |
| `app/src/main/java/com/example/adskipper/MainActivity.kt` | Shows ON/OFF status and buttons to open Accessibility and battery settings |
