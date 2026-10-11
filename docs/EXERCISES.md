# Exercises

v1 supports three exercises, **selected manually by the user**. Auto-detection is a later phase (see [ROADMAP.md](ROADMAP.md)).

> **All thresholds below are provisional starting points.** They are common coaching ranges, not validated values. Tune them against real recordings, and keep them in one config location in code. Formalyze is a training aid, not medical advice.

## Conventions

- Angles are measured at the middle joint, e.g. knee angle = angle(hip, knee, ankle), in degrees, using world landmarks.
- Use the body side facing the camera (or average both sides when both are visible).
- A rep counts only when the full range is reached (bottom/top thresholds below) **and** the movement returns to the start position.
- Each rule yields: `ok`, or an `issue` with a code, a short on-screen message, and a spoken cue. Cues are throttled so the user isn't nagged every frame.
- Required joints must pass the visibility gate or the app prompts to reposition the camera.

## Rep phases (shared)

```
TOP/START ──► ECCENTRIC ──► BOTTOM ──► CONCENTRIC ──► TOP/START (rep +1)
```

Transitions use hysteresis: enter/leave thresholds differ by a few degrees, and movement direction must persist for a few frames.

## Squat

- **Camera**: side view (45° to side also works), full body in frame, phone at roughly hip height.
- **Required joints**: shoulder, hip, knee, ankle.
- **Rep driver**: knee angle.

| Metric | Start/top | Bottom (valid depth) |
|---|---|---|
| Knee angle | > ~160° | < ~100° |

| Form check | Flag when | Cue |
|---|---|---|
| Depth | Bottom reached but knee angle never < ~100° | "Go deeper" |
| Torso lean | Torso (hip→shoulder) tilted forward past ~45° from vertical | "Chest up" |
| Heels / knees | Knee travels far past toes (use cautiously; depends on build) | "Sit back" |
| Knee cave (needs front view) | Knee x collapses inward of ankle | "Push knees out" |

Knee cave needs a front-facing camera angle; treat as optional / later.

## Deadlift

- **Camera**: side view, full body in frame, phone low (about hip height or lower).
- **Required joints**: shoulder, hip, knee, ankle (and ear for neck, optional).
- **Rep driver**: hip angle = angle(shoulder, hip, knee).

| Metric | Bottom (setup) | Top (lockout) |
|---|---|---|
| Hip angle | ~70° to 110° (varies by style) | > ~165° |

| Form check | Flag when | Cue |
|---|---|---|
| Back rounding | Spine curvature proxy: shoulder–hip line vs hip–knee line change during the lift beyond limit | "Keep your back straight" |
| Hips shooting up | Hip rises much faster than shoulders in the concentric phase | "Drive with your legs" |
| Incomplete lockout | Top reached but hip angle < ~165° | "Stand tall" |
| Neck | Head (ear) angle breaks neutral line with torso | "Neutral neck" |

Back-rounding detection from a few 2D/3D points is approximate. Expect to tune it, and consider it a warning, not a guarantee.

## Bicep Curl

- **Camera**: side or front view, upper body in frame.
- **Required joints**: shoulder, elbow, wrist (hip for torso swing).
- **Rep driver**: elbow angle = angle(shoulder, elbow, wrist).

| Metric | Bottom (extended) | Top (contracted) |
|---|---|---|
| Elbow angle | > ~150° | < ~50° |

| Form check | Flag when | Cue |
|---|---|---|
| Elbow drift | Elbow moves forward of torso line by more than ~a fraction of upper-arm length | "Keep elbows tucked" |
| Torso swing | Hip→shoulder line angle changes beyond ~15° during the rep | "Don't swing" |
| Partial range | Top or bottom thresholds not reached | "Full range" |
| Too fast | Eccentric phase shorter than ~1 s | "Slow down on the way down" |

## Open items

- Decide how to handle left/right asymmetry (average vs worst side).
- Collect real test clips per exercise to calibrate thresholds.
- Decide on per-user calibration (e.g., standing-pose baseline) vs fixed thresholds.
