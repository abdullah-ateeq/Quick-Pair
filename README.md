# Quick Pair 🎴⚡

**Quick Pair** is a fast-paced, dynamic memory-matching puzzle game for Android. Built from the ground up using **Jetpack Compose**, **Material 3**, and modern Android architectural practices, Quick Pair tests your recall speed, focus, and reflexes with continuous board scaling and timed combo streaks!

---

## 🌟 Key Features

### 🎮 Dynamic & Addictive Gameplay
- **3 Difficulty Levels**:
  - **Easy**: 6 pairs (12 tiles), 20s starting time, +1.5s match bonus, -0.5s wrong penalty.
  - **Medium**: 8 pairs (16 tiles), 16s starting time, +1.2s match bonus, -0.7s wrong penalty.
  - **Hard**: 12 pairs (24 tiles), 13s starting time, +0.9s match bonus, -1.0s wrong penalty.
- **Infinite Board Progression**: Clear a board to unlock board bonuses (+250 score, +2.0s time extension) and advance to larger grids.
- **Streak & Combo Multipliers**: Build consecutive match streaks to trigger milestone titles (*GOOD!*, *GREAT!*, *ON FIRE!*, *PAIR MASTER!*) and bonus points.
- **Speed Scoring**: Earn bonus points based on how fast you find matching tile pairs.

### 🔊 Built-in Audio & Haptics Engine
- **Synthesized Audio System**: Custom real-time PCM audio synthesis using Android `AudioTrack`—delivering crisp sound effects for selections, matches, timer warnings, and board clears without heavy asset overhead.
- **Tactile Haptic Feedback**: Contextual haptic vibrations for tile selections, successful matches, missteps, and game over.

### 🎨 Clean UI & Accessibility
- **Material 3 Design**: Modern, responsive Jetpack Compose interface with fluid layout animations.
- **Dark & Light Themes**: Full support for System Default, Light, and Dark modes.
- **Reduced Motion Support**: Accessibility setting to minimize animations and visual shaking effects.

### 📊 Comprehensive Performance Tracking
- Persistent game statistics using `SharedPreferences` and Kotlin `StateFlow`.
- Track **Games Played**, **Best Score**, **Max Pairs Matched**, **Accuracy %**, **Highest Streak**, and **Boards Cleared**.

---

## 🛠️ Architecture & Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/) (2.2.10)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/)
- **Architecture**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF)
- **State Management**: Kotlin Coroutines & `StateFlow` / `SharedFlow`
- **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/guide/navigation/navigation-compose)
- **Audio Engine**: Low-latency `AudioTrack` PCM Tone Generator
- **Storage**: `SharedPreferences` with Reactive `StateFlow` wrappers
- **Target SDK**: Android 16 / API 37 (Min SDK: API 26 - Android 8.0)

---

## 📂 Project Structure

```text
com.example.quickpair/
├── data/
│   ├── GamePreferences.kt       # Persistent settings & statistics manager
│   ├── GameStats.kt             # Data class for score & performance stats
│   └── SoundManager.kt          # Procedural PCM audio synthesizer & haptic engine
├── model/
│   ├── Difficulty.kt            # Easy, Medium, Hard configuration parameters
│   ├── GameState.kt             # Game lifecycle states (Countdown, Playing, Paused, etc.)
│   ├── PairSymbol.kt            # Visual icon symbols for matching tiles
│   ├── PairTile.kt              # Grid tile entity & selection state
│   ├── QuickPairEngine.kt       # Core game logic, board generator, scoring & streak math
│   └── QuickPairResult.kt       # Game over summary data structure
├── navigation/
│   └── Screen.kt                # Navigation destinations
├── ui/
│   ├── components/              # Reusable Compose UI components (Tiles, Banners, Dialogs)
│   ├── screens/                 # Screen Composables (Splash, Home, Game, Result, Settings, About)
│   ├── theme/                   # Material 3 Color palettes, Typography, and Theme setup
│   └── viewmodel/               # QuickPairViewModel managing game state & UI flows
└── MainActivity.kt              # Single activity entry point with Navigation Compose
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug / ME 2024.2.1 or newer
- **JDK**: Version 11 or higher
- **Android Device / Emulator**: Running Android 8.0 (API 26) or above

### Building & Running
1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/Quick-Pair.git
   cd Quick-Pair
   ```
2. **Open in Android Studio**:
   - File -> Open -> Select project directory.
3. **Build & Run**:
   - Select your connected device or emulator.
   - Click **Run** (`Shift + F10` / `Control + R`).

---

## 🧪 Testing

Unit tests for game logic, board generation, scoring calculations, and difficulty rules are located in `app/src/test`.

To run tests via command line:
```bash
./gradlew test
```

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
