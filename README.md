## Currency Calculator

I could only find apps on my android phone that had ads on them when I used their simple multiplication for counting the cash register at night at work. I got fed up with this so I used Android studio to make my own. This is a simple Android app that allows users to input quantities of various currency denominations. The app calculates and displays the total value in real time.

## Features

- Home page with tiles for quick access to each tool.
- Navigation drawer (hamburger menu) available on every page.
- **Currency Counter** — supports the following currency denominations:
    - 1¢, 5¢, 10¢, 25¢, $1, $5, $10, $20, $50, $100.
- **Currency Converter** — two-sided picker (any listed currency to any other) with static offline rates for v1.

## Requirements

- **Android Version**: API Level 21 (Android 5.0) or higher
- **Build Tool**: Gradle
- **Kotlin Version**: 1.9.20
- **Compose Version**: 1.5.4

## Installation

1. Download the APK file: Currency_Counter
2. Transfer the APK to your Android device.
3. Open the APK file and install the app.

## How to Use

1. Open the app.
2. Pick a tool from the home page (or the hamburger menu).
3. **Currency Counter**: Enter the quantity of each denomination in the corresponding input field and the total updates in real time.
4. **Currency Converter**: Enter an amount, pick a "from" and "to" currency, and the converted result is shown instantly.

## Development

- **Language**: Kotlin
- **Framework**: Android Jetpack Compose (single-activity, Navigation-Compose)
- **IDE**: Android Studio
- **Build**: Gradle 8.2 (wrapper included), Android Gradle Plugin 8.2.2, JDK 17 (path configured in `gradle.properties` via `org.gradle.java.home`)

To build from the command line:

```
./gradlew assembleDebug
```

The debug APK is output to `app/build/outputs/apk/debug/`.

## Future Improvements

- Currency Converter: live exchange rates via a free API (e.g., Frankfurter), with the current static rates as an offline fallback.
- Persist counter sessions and add a count history page.
- Settings screen.
- Visual redesign of the Currency Counter screen to match the new theme.
- Improve UI design and add animations.

## Author
Cole Dorazio
