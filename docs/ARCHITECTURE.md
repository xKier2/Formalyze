# Architecture

Status: **planned**. The repo currently contains only the Compose project template. This document describes the intended design; update it as the code lands (see "Keeping docs up to date" in [CLAUDE.md](../CLAUDE.md)). Related: [EXERCISES.md](EXERCISES.md), [ROADMAP.md](ROADMAP.md).

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
| `camera` | CameraX setup, frame delivery, front/back selection | no |
| `pose` | MediaPipe wrapper; converts results into the app's own `Landmark` types | no |
| `analysis` | Angle math, smoothing, metrics | **yes** |
| `exercise` | Per-exercise rules, thresholds, rep state machines | **yes** |
| `feedback` | Audio/TTS cues, haptics, cue throttling | no |
| `report` | Set recording and summary | mostly |
| `ui` | Compose screens: exercise picker, live session, report | no |

`analysis` and `exercise` must not import `android.*` or `com.google.mediapipe.*`. The `pose` package maps MediaPipe types to plain Kotlin types at the boundary. This allows JVM unit tests using recorded landmark sequences.

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
