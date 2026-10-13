# Roadmap

Goal: portfolio / learning project. Each phase should leave the app in a runnable state.

Tick checkboxes and resolve open questions as work lands (see "Keeping docs up to date" in [CLAUDE.md](../CLAUDE.md)). Related: [ARCHITECTURE.md](ARCHITECTURE.md), [EXERCISES.md](EXERCISES.md).

## Phase 0: Foundation
- [x] Compose project scaffold, CodeQL workflow
- [x] Add CameraX to the version catalog (1.6.2)
- [ ] Add MediaPipe Tasks Vision (1.1.0) + OpenCV (4.13.0, 4.x preferred over 5.0) to the version catalog
- [x] Camera permission flow (device check pending)
- [ ] Bundle a pose model (`pose_landmarker_lite` or `full`) in `assets/`; document how to download it

## Phase 1: See the skeleton
- [x] Live camera preview in Compose (design in ARCHITECTURE.md "Camera"). Build, unit tests and lint pass; **all items below still need checking on a physical device**
  - [x] Back camera default + switch to front (front mirrored)
  - [x] Tap-to-focus, pinch-to-zoom
  - [x] Preview stabilization when supported (falls back to off if the device rejects the combination)
  - [x] Portrait + landscape
- [ ] MediaPipe Pose Landmarker in LIVE_STREAM mode on a background executor
- [ ] Show landmarks on the live feed (**rendering approach is an open decision**, see ARCHITECTURE.md)
- [ ] Measure FPS / latency on a real mid-range device

## Phase 2: Measure
- [ ] Pure-Kotlin `analysis` package: angle math, visibility gating, smoothing
- [ ] Unit tests with synthetic and recorded landmark fixtures

## Phase 3: Judge one exercise end to end
- [ ] Bicep Curl first (simplest: one driving joint)
- [ ] Rep state machine with hysteresis + unit tests
- [ ] Per-joint good/bad result drives visual feedback

## Phase 4: Remaining v1 exercises
- [ ] Squat
- [ ] Deadlift
- [ ] Exercise picker screen
- [ ] Setup guidance ("place phone side-on, show full body")

## Phase 5: Feedback
- [ ] Voice cues (TextToSpeech) and/or beeps, with throttling
- [ ] Haptic cues
- [ ] Settings: toggles for audio / haptics

## Phase 6: Post-set report
- [ ] Record per-rep results during a set
- [ ] Summary: perfect-rep %, most common mistake, snapshots of breakdown moments
- [ ] **Persistence: undecided** (session-only vs local history)

## Phase 7: Python tooling (offline, optional)
- [ ] `tools/` folder with a Python pipeline: collect landmarks, train an exercise classifier, export TFLite
- [ ] Integrate on-device classifier for exercise auto-detection
- [ ] Python scope is **not finalized**; no live backend API

## Later / ideas
- Calibration step (per-user baseline)
- Left/right asymmetry reporting
- More exercises (overhead press, lunge, push-up)
- Front-camera knee-cave detection for squats
- Accessibility: large-text cues, color-blind-safe form indicators (don't rely on green/red alone)

## Open questions
1. Overlay rendering: Compose Canvas vs OpenCV drawing (**user to decide; remind before Phase 1**)
2. Report persistence (Room vs session-only)
3. Python tooling scope and data collection plan
4. Where recorded test clips/fixtures will come from
