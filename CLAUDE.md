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
- CameraX 1.6.2 (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-compose`) plus `lifecycle-runtime-compose` 2.9.4, for the live preview (implemented, device check pending). Design in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md#camera-live-preview).
- Planned pins when added: MediaPipe `tasks-vision` 1.1.0, OpenCV `org.opencv:opencv` 4.13.0 (stay on 4.x until 5.0's Android API is checked).
- Gradle Kotlin DSL with version catalog in `gradle/libs.versions.toml`. Add dependencies there, not inline.

## Commands

```bash
./gradlew assembleDebug            # build
./gradlew testDebugUnitTest        # JVM unit tests
./gradlew connectedDebugAndroidTest  # needs device/emulator
```

On Windows PowerShell use `.\gradlew.bat`. Camera/pose features need a real device; the emulator camera is not representative.

## Layout

Source is under `app/src/main/java/res/formalyze/` (note: path is `res/formalyze`, matching the package). What exists now: `MainActivity` (opens the camera), `camera/` (CameraX session and settings), `ui/camera/` (permission gate, camera screen), `ui/theme`. Camera tuning lives only in `camera/CameraSettings.kt`. Planned structure and data flow are in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Architecture rules

- Pipeline: camera frame → pose landmarks → angles/metrics → exercise rules + rep state machine → feedback (visual/audio/haptic) → set report.
- **Analysis code is pure Kotlin**: angle math, smoothing, rules, rep counter take plain data (landmark lists), no Android or MediaPipe types. This keeps it unit-testable on the JVM with recorded landmark fixtures.
- Pose inference never runs on the main thread. Use CameraX `ImageAnalysis` with `STRATEGY_KEEP_ONLY_LATEST` and MediaPipe `LIVE_STREAM` mode; drop frames rather than queue them.
- Prefer MediaPipe **world landmarks** (meters, hip-origin) for angle math; use normalized image landmarks for screen drawing. Always check landmark `visibility`/`presence` before trusting a joint.
- Smooth angles before applying thresholds, and use hysteresis in the rep state machine to avoid flicker and double counting.
- Exercise thresholds live in one place (see [docs/EXERCISES.md](docs/EXERCISES.md)) as data/config, not scattered magic numbers.

## Code style

Clean code first, meaningful comments second. Applies to all generated or modified code, in any language.

**Naming**
- No throwaway names like `a`, `b`, `c`, `x1`, `tmp`, `val1`. Every variable, parameter and function name must say what it holds or does (`hipAngleDegrees`, `kneeLandmark`, `repCount`), so the code reads without explanation.
- Avoid single-letter names, including loop counters (use `frameIndex`, `landmarkIndex`). Include units in names where relevant (`...Degrees`, `...Millis`).

**Comments**
1. Comments go only at the top of a file, class or function, within its first 1 to 10 lines, before the implementation.
2. No inline comments or comments between lines of code.
3. Explain purpose, overall behavior or an important design decision, not what each line does.
4. Concise, beginner-friendly, simple language.
5. Don't state the obvious or repeat names (`// Stores the username.` above `username` is wrong).
6. Don't pad. If the code is self-explanatory, use one short intro comment or none.
7. For multiple classes/functions/files, give each relevant one a short intro comment within its first 1 to 10 lines.
8. When editing existing code, keep its comment conventions unless they conflict with these rules.

```kotlin
// Counts squat reps by tracking knee angle through down and up phases.
class SquatRepCounter {
    // Implementation
}
```

## v1 scope

Manual exercise selection: **Squat, Deadlift, Bicep Curl**. Auto-detecting the exercise is a later phase. Phases are in [docs/ROADMAP.md](docs/ROADMAP.md).

## Docs

- [README.md](README.md): project introduction, features, setup, and documentation links
- [AGENTS.md](AGENTS.md): Codex workflow and handoff blueprint; both assistants use the shared project docs
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md): pipeline, modules, threading
- [docs/EXERCISES.md](docs/EXERCISES.md): per-exercise metrics, thresholds, rep phases
- [docs/ROADMAP.md](docs/ROADMAP.md): phases and open questions
- [CONTRIBUTING.md](CONTRIBUTING.md): contribution rules (including the no-commit rule)

## Keeping docs up to date (always)

The docs are living documents. **Before starting a task, read the docs relevant to it. After finishing, update every doc the change touched, in the same piece of work.** Don't wait to be asked. Don't leave docs describing things that no longer exist.

| If you change... | Update |
|---|---|
| Pipeline, packages/modules, threading, dependencies, model choice | `docs/ARCHITECTURE.md` and the Stack/Layout/Architecture sections here |
| An exercise's metrics, thresholds, cues, camera placement, rep phases | `docs/EXERCISES.md` |
| A feature is finished, started, added, dropped or reordered | `docs/ROADMAP.md` (tick checkboxes, move open questions) |
| A decision gets made (overlay rendering, persistence, Python scope) | Remove it from "Open decisions"/"Open questions" in every doc, record the decision where it belongs, update the "Rules for Claude" section here |
| Build commands, SDK versions, project name/package, folder layout | This file |
| Contribution, commit or code-style rules | `CONTRIBUTING.md`, `AGENTS.md`, and this file (they must agree) |

Rules:
- Treat "important function" broadly: any new feature, changed behavior, new dependency, or decision counts.
- If two docs disagree, fix both so they agree; code is the source of truth for what exists, the owner for what is decided.
- Mark things honestly: planned vs implemented, provisional vs validated.
- When a new `.md` file is created, link it from the Docs list above and from `CONTRIBUTING.md`.
- Doc edits are uncommitted like all other changes (see the no-commit rule).
- In your final message, list which docs you updated, or say none needed it.

## Working across separate assistant sessions

The owner prompts Claude and Codex separately. Use the current working tree and shared docs to pick up previous work; do not assume access to the other assistant's conversation. Inspect existing changes before editing and preserve unrelated work. Follow the owner's requested scope; roadmap items do not authorize starting additional features. Keep durable decisions and progress in the shared docs, with a final summary of changes, verification, and remaining blockers. Codex's detailed workflow is in [AGENTS.md](AGENTS.md).
