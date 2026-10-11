<h1 align="center">Formalyze</h1>

<p align="center">
  <strong>Your movement. Your form. A clearer picture.</strong>
</p>

<p align="center">
  <a href="https://skillicons.dev">
    <img src="https://skillicons.dev/icons?i=kotlin" alt="Kotlin" width="48" height="48" />
  </a>
</p>

<p align="center">
  A native Android workout-form coach built with Kotlin, Jetpack Compose, and Material 3.<br />
  Designed to turn your phone camera into feedback on how you move, one rep at a time.<br />
  The vision pipeline is designed around CameraX, MediaPipe Pose Landmarker, and OpenCV.
</p>

<p align="center">
  <a href="#features">Features</a> &middot;
  <a href="#getting-started">Getting started</a> &middot;
  <a href="#documentation">Documentation</a> &middot;
  <a href="#contributing">Contributing</a>
</p>

---

## Features

Formalyze is being developed around these capabilities. Implementation progress is tracked in the [roadmap](docs/ROADMAP.md).

| | Capability | Intended experience |
|---|---|---|
| 📷 | **Live pose tracking** | Follow 33 body landmarks through the phone camera to measure movement and joint angles. |
| 🏋️ | **Exercise-specific analysis** | Manually choose Squat, Deadlift, or Bicep Curl for rules tailored to the exercise. |
| 🔁 | **Rep counting** | Track movement phases and count completed reps using range-of-motion thresholds and hysteresis. |
| 🎯 | **Visual form feedback** | Highlight joints and surface short cues during a set, with camera-placement guidance when tracking is unreliable. |
| 🔊 | **Audio and haptic cues** | Provide timely voice or sound prompts and vibration, with throttling and user controls. |
| 📊 | **Post-set reports** | Review the percentage of reps meeting the form rules, common mistakes, and snapshots of breakdown moments. |
| 🔒 | **On-device processing** | Keep video and pose analysis on the phone, with no upload of video or landmarks in v1. |

Exercise checks and thresholds are provisional and will be tuned against real recordings. The [exercise guide](docs/EXERCISES.md) explains the metrics and limitations.

## Getting started

### Prerequisites

- Android Studio with support for the project's Android Gradle Plugin, as specified in [the version catalog](gradle/libs.versions.toml).
- Android SDK Platform **37**, matching the compile and target SDK configuration.
- A **JDK 25** installation available to Gradle, matching [the daemon JVM configuration](gradle/gradle-daemon-jvm.properties).
- An Android device or emulator running **API 24 or newer**. Use a physical device for camera and pose validation as those features are implemented.

### Open the project

1. Clone or download this repository.
2. Open the repository's root folder in Android Studio.
3. Let Gradle sync and install the required SDK components when prompted. Initial setup needs internet access to resolve dependencies.
4. Select the `app` run configuration and your device or emulator, then run the app.

Use the included Gradle wrapper so the build uses the version configured by the project.

### Build and test

Run these commands from the repository root.

**Windows PowerShell**

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```

**macOS / Linux**

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

For instrumented tests, connect a device or start an emulator, then run:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

On macOS or Linux, use `./gradlew connectedDebugAndroidTest`. Emulator camera input does not replace physical-device checks for pose accuracy and latency.

## Documentation

| Guide | What you will find |
|---|---|
| [Architecture](docs/ARCHITECTURE.md) | Camera-to-report pipeline, package boundaries, threading, and design decisions. |
| [Exercises](docs/EXERCISES.md) | Per-exercise metrics, camera placement, cues, and rep phases. |
| [Roadmap](docs/ROADMAP.md) | Development phases, implementation progress, and open questions. |
| [Contributing](CONTRIBUTING.md) | Code conventions, verification, documentation, and repository ownership rules. |
| [Codex workflow](AGENTS.md) | Task scope, shared context, and handoff instructions for Codex. |
| [Claude guide](CLAUDE.md) | Project context, commands, and shared development conventions. |

## Contributing

Formalyze is a portfolio and learning project. Keep changes focused, use meaningful names, and update the relevant docs alongside behavior changes. Angle math, exercise rules, and rep counting should stay in plain Kotlin so they can be tested independently of Android.

Read [CONTRIBUTING.md](CONTRIBUTING.md) before working on the project. Contributors and coding assistants leave changes unstaged and uncommitted for the owner to review and commit; work stays within the requested task.
