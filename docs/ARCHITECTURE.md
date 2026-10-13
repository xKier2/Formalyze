# Architecture

Status: **in progress**. The live camera preview is implemented (see [Camera](#camera-live-preview)). Everything after the camera is still planned. This document describes the intended design; update it as the code lands (see "Keeping docs up to date" in [CLAUDE.md](../CLAUDE.md)). Related: [EXERCISES.md](EXERCISES.md), [ROADMAP.md](ROADMAP.md).

## Pipeline

```
CameraX ImageAnalysis
      │  frames (latest only, dropped if busy)
      ▼
Pose Landmarker (MediaPipe, LIVE_STREAM)
      │  33 landmarks: normalized image + world coords + visibility
      ▼
Preprocessing  (visibility gating, smoothing, OpenCV matrix ops where useful)
      ▼
Metrics        (joint angles, torso lean, distances)
      ▼
Exercise rules + Rep state machine   (per-exercise config)
      ▼
FormResult { phase, repCount, issues[], per-joint good/bad }
      ├──► Feedback: visual (skeleton color), audio/voice, haptics
      └──► Set recorder → post-set report (perfect-rep %, top mistake, snapshots)
```

## Planned modules (packages under `res.formalyze`)

| Package | Responsibility | Android-free? |
|---|---|---|
| `camera` | CameraX setup, frame delivery, front/back selection, zoom/focus | no |
| `pose` | MediaPipe wrapper; converts results into the app's own `Landmark` types | no |
| `analysis` | Angle math, smoothing, metrics | **yes** |
| `exercise` | Per-exercise rules, thresholds, rep state machines | **yes** |
| `feedback` | Audio/TTS cues, haptics, cue throttling | no |
| `report` | Set recording and summary | mostly |
| `ui` | Compose screens: exercise picker, live session, report | no |

`analysis` and `exercise` must not import `android.*` or `com.google.mediapipe.*`. The `pose` package maps MediaPipe types to plain Kotlin types at the boundary. This allows JVM unit tests using recorded landmark sequences.

## Camera (live preview)

Status: **implemented; builds, unit tests and lint pass. Not yet verified on a physical device.** Preview only: no `ImageAnalysis`, MediaPipe, OpenCV or overlay yet. `MainActivity` opens straight into the permission gate and then the camera screen.

| File | Role |
|---|---|
| `camera/CameraSettings.kt` | All camera tuning in one place: resolution strategy, target frame rate, stabilization, preview scaling |
| `camera/CameraLens.kt` | Back/front lens choice |
| `camera/CameraSession.kt` | Owns the CameraX `Preview` use case, binds it to the lifecycle, exposes the viewfinder `SurfaceRequest`, handles zoom, focus, lens switching and rotation |
| `ui/camera/CameraPermissionGate.kt` | Runtime camera permission: explanation, request, denied state with "Open settings" |
| `ui/camera/CameraScreen.kt` | Full-screen viewfinder, tap-to-focus, pinch-to-zoom, switch-lens button |

Decisions:
- **CameraX 1.6.2** (latest stable), with `camera-compose`'s `CameraXViewfinder` so the preview is a native composable (no `AndroidView`/`PreviewView`).
- **Resolution**: highest available, preferring 16:9 with automatic fallback. CameraX caps preview size at the display's resolution anyway.
- **Frame rate**: target 30 fps (a hint; the device picks the closest supported range).
- **Preview stabilization**: enabled only when the device reports support. It can add slight latency and crop; turn it off in `CameraSettings` if that hurts.
- **Lenses**: back camera by default (better quality), switch button only when both cameras exist; falls back to whichever camera exists. Front camera preview is mirrored by CameraX's default `Preview` behavior (explicit `setMirrorMode` is still an experimental API, so it isn't used).
- **Dependencies**: `lifecycle-runtime-compose` 2.9.4 is declared explicitly for `LocalLifecycleOwner` and `collectAsStateWithLifecycle`; it matches the version Compose already resolves, so nothing is upgraded.
- **Gestures**: tap-to-focus (focus + exposure at the tapped point), pinch-to-zoom clamped to the camera's zoom range.
- **Orientation**: portrait and landscape. The activity recreates on rotation; the chosen lens survives via saved state. The preview's target rotation also follows 180° flips, which don't recreate the activity.
- **Scaling**: **Fit** (whole camera frame visible, may letterbox) rather than Crop, so what the user sees is what the camera captures. This matters for "is my full body in frame?" and for lining up landmarks later. Changeable in `CameraSettings`.
- **Permission**: `CAMERA` requested at runtime; manifest declares `android.hardware.camera.any` as required. After a refusal the screen offers "Try again" and "Open settings"; returning from Settings re-checks the permission.
- **Focus feedback**: a white ring shows briefly where the user tapped. Taps in the letterbox area (outside the camera frame) are ignored. This is a UI hint, not the skeleton overlay, so it doesn't touch the open overlay decision.

## Threading

- CameraX analyzer runs on a dedicated executor, never the main thread.
- Use `STRATEGY_KEEP_ONLY_LATEST`; if inference is slower than the camera, frames are dropped, not queued.
- MediaPipe `LIVE_STREAM` mode delivers results asynchronously via a listener; push results into a `StateFlow` that the UI collects.
- Close/release the landmarker and camera with the lifecycle.

## Accuracy notes

- Use **world landmarks** (meters, hip-centered) for angles; they are less distorted by camera perspective than normalized image coordinates.
- Gate every joint on `visibility`/`presence`. If a required joint is occluded or low-confidence, show "adjust camera" instead of judging form.
- Smooth angles (EMA or similar) and use hysteresis on thresholds. Raw per-frame landmarks jitter.
- Single-camera 3D is an estimate. Depth (z) is the least reliable axis, so rules should rely mainly on angles visible in the camera plane. This is why docs/EXERCISES.md specifies a camera placement per exercise.
- Model size tradeoff: MediaPipe ships lite / full / heavy pose models. Start with `lite` or `full`; measure FPS and latency on a mid-range phone before going heavier.

## Privacy

All processing is on-device in v1. No video or landmarks leave the phone. Camera permission is requested at runtime with a clear explanation.

## Open decisions

- **Overlay rendering**: how the skeleton is drawn on the live feed (Compose Canvas vs OpenCV). Deliberately undecided; do not assume either until the user decides.
- **Persistence** of set reports (Room vs session-only). Undecided.
- **Python tooling**: offline only (classifier training, TFLite export). Details not finalized; see [ROADMAP.md](ROADMAP.md).
