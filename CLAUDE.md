# Formalyze

Real-time Android workout-form coach. The phone camera tracks the user's body, the app measures joint angles, judges form per exercise, counts reps, and gives instant feedback (colors, audio, haptics) plus a post-set report. Project goal: portfolio / learning, so keep things lean and practical.

Name: **Formalyze** (package `res.formalyze`). "FitForm" was an earlier working name; don't use it.

## Rules for Claude

- **NEVER run `git commit`, `git add`, `git push` or other history-changing git commands.** Leave all changes in the working tree; the owner commits. Read-only git (`status`, `diff`, `log`) is fine. See [CONTRIBUTING.md](CONTRIBUTING.md).
- **Overlay rendering is an open decision** (Compose Canvas vs OpenCV drawing). Don't pick one or write code/docs that assume either. Remind the user before touching overlay/rendering work.
- **Python is not finalized.** It is offline tooling only (training/exporting an exercise classifier). Don't build a backend API.
- Persistence for post-set reports is undecided. Don't add Room or other storage unless asked.

## Stack (confirmed)

- Kotlin, native Android, Jetpack Compose UI (Material 3). `minSdk 24`, `compileSdk/targetSdk 37`.
- Google MediaPipe Pose Landmarker (Tasks Vision): 33 landmarks, runs on-device.
- OpenCV (Android) for matrix operations and image processing.
- CameraX for camera frames (planned).
- Gradle Kotlin DSL with version catalog in `gradle/libs.versions.toml`. Add dependencies there, not inline.

## Commands

```bash
./gradlew assembleDebug            # build
./gradlew testDebugUnitTest        # JVM unit tests
./gradlew connectedDebugAndroidTest  # needs device/emulator
```

On Windows PowerShell use `.\gradlew.bat`. Camera/pose features need a real device; the emulator camera is not representative.

## Layout

Source is under `app/src/main/java/res/formalyze/` (note: path is `res/formalyze`, matching the package). Currently only the Compose template exists (`MainActivity`, `ui/theme`). Planned structure and data flow are in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Architecture rules

- Pipeline: camera frame → pose landmarks → angles/metrics → exercise rules + rep state machine → feedback (visual/audio/haptic) → set report.
- **Analysis code is pure Kotlin**: angle math, smoothing, rules, rep counter take plain data (landmark lists), no Android or MediaPipe types. This keeps it unit-testable on the JVM with recorded landmark fixtures.
- Pose inference never runs on the main thread. Use CameraX `ImageAnalysis` with `STRATEGY_KEEP_ONLY_LATEST` and MediaPipe `LIVE_STREAM` mode; drop frames rather than queue them.
- Prefer MediaPipe **world landmarks** (meters, hip-origin) for angle math; use normalized image landmarks for screen drawing. Always check landmark `visibility`/`presence` before trusting a joint.
- Smooth angles before applying thresholds, and use hysteresis in the rep state machine to avoid flicker and double counting.
- Exercise thresholds live in one place (see [docs/EXERCISES.md](docs/EXERCISES.md)) as data/config, not scattered magic numbers.

## v1 scope

Manual exercise selection: **Squat, Deadlift, Bicep Curl**. Auto-detecting the exercise is a later phase. Phases are in [docs/ROADMAP.md](docs/ROADMAP.md).

## Docs

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md): pipeline, modules, threading
- [docs/EXERCISES.md](docs/EXERCISES.md): per-exercise metrics, thresholds, rep phases
- [docs/ROADMAP.md](docs/ROADMAP.md): phases and open questions
- [CONTRIBUTING.md](CONTRIBUTING.md): contribution rules (including the no-commit rule)

Keep docs and code in sync: if you change a threshold or the pipeline, update the doc in the same change.
