# Codex workflow for Formalyze

This is Codex's repository entry point. The owner prompts Codex and Claude separately; continuity comes from the working tree and shared documentation. Do not contact or launch Claude, schedule coordination, or assume access to its chat history.

## Scope and ownership

- Do only the requested task and the supporting edits needed to complete it. A roadmap item is context, not permission to implement the next phase.
- **Never stage, commit, push, merge, rebase, reset, or stash.** Do not run `git add` or other Git commands that change history, the index, or discard work. Leave changes in the working tree for the owner to review and commit. Read-only `git status`, `git diff`, and `git log` are allowed.
- Preserve existing changes from the owner or Claude. Do not revert, overwrite, or reformat unrelated work. Uncommitted work can be the intended starting point; it does not require a clean checkout.
- Do not create branches, worktrees, or pull requests unless the owner requests them.
- A request for explanation, review, or planning does not authorize implementation. If asked to wait, make no changes.

## Read before working

1. Read [CONTRIBUTING.md](CONTRIBUTING.md) and [CLAUDE.md](CLAUDE.md). Apply their shared project, architecture, code-style, and documentation rules to Codex too.
2. Read [docs/ROADMAP.md](docs/ROADMAP.md) for implemented versus planned work. Read the task-relevant sections of [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) and [docs/EXERCISES.md](docs/EXERCISES.md).
3. Inspect `git status --short` and relevant diffs, including staged changes without modifying the index. Read relevant untracked files as well; Git diffs do not include their contents.
4. Inspect the actual source, build configuration, and tests involved. Check for any more-specific `AGENTS.md` instructions in the directories being edited. Use `rg` for targeted searches and avoid generated build directories.
5. State the intended change and any material uncertainty briefly, then proceed within the owner's request.

Recheck the working tree and relevant docs when resuming or switching tasks. Do not rely on remembered chat context as the only record of a decision.

## Shared sources of truth

| File | Purpose |
|---|---|
| [AGENTS.md](AGENTS.md) | Codex task intake, scope, verification, and handoff workflow |
| [CLAUDE.md](CLAUDE.md) | Claude entry point plus shared commands, stack, and detailed conventions |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Contribution rules applying to both assistants and other contributors |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Pipeline, package boundaries, threading, and architecture decisions |
| [docs/EXERCISES.md](docs/EXERCISES.md) | Exercise metrics, provisional thresholds, cues, and rep phases |
| [docs/ROADMAP.md](docs/ROADMAP.md) | Feature progress, phase ordering, and unresolved questions |

Keep shared facts in their existing documents instead of creating a parallel Codex architecture or roadmap. Code and build files establish what exists; the owner establishes what is decided. Plans and another assistant's notes are not proof of implementation or successful testing.

If docs disagree, inspect the code and available owner instructions. Correct factual drift relevant to the task in all affected docs. Ask the owner when conflicting product decisions cannot be resolved from that evidence; continue independent work while waiting. Never silently choose an unresolved design.

## Project guardrails

- Use **Formalyze**, package `res.formalyze`, and source root `app/src/main/java/res/formalyze/`. Do not use the earlier name FitForm.
- Follow the Kotlin/Compose conventions and comment placement rules in `CLAUDE.md` and `CONTRIBUTING.md`. Use meaningful names and include units where relevant.
- Keep angle math, smoothing, exercise rules, and rep counting in plain Kotlin without Android or MediaPipe imports. Map external pose types at the boundary.
- Preserve the planned camera-to-pose-to-metrics-to-rules-to-feedback-to-report pipeline. Keep inference off the main thread, gate unreliable landmarks, smooth metrics, and use rep hysteresis as documented.
- Add dependencies through `gradle/libs.versions.toml`. Do not upgrade unrelated dependencies or SDK settings as part of another task.
- v1 uses manual selection of Squat, Deadlift, and Bicep Curl with on-device processing. Exercise thresholds remain provisional until validated against recordings.
- Overlay rendering is undecided: remind the owner and obtain the decision before implementing work that requires choosing Compose Canvas or OpenCV drawing.
- Report persistence is undecided: do not add Room or another persistence system unless requested.
- Python is optional offline tooling with unfinished scope; do not introduce a backend API.

These guardrails summarize shared rules. Read the linked docs for details and update this summary if the owner changes a decision.

## Implement and verify

- Make the smallest coherent change that satisfies the request. Reuse existing patterns and avoid speculative abstractions, broad cleanup, or unfinished future features.
- For analysis, threshold, rule, or rep-counter behavior, add or update meaningful JVM tests. Include relevant boundary conditions such as missing landmarks, jitter near thresholds, and partial or repeated reps.
- For code or build changes, run the repository's handoff checks from the root in Windows PowerShell:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

- Run `.\gradlew.bat connectedDebugAndroidTest` when the change needs device integration checks and a device or emulator is available. Real camera/pose accuracy and latency require a physical device; a successful build does not validate them.
- For documentation-only changes, review consistency, local links, and `git diff --check`; Gradle checks are unnecessary. If a check fails or cannot run, report the exact limitation and distinguish it from a code failure. Never claim an unrun check passed.
- Inspect the final diff and working-tree status against the initial state. Fix issues introduced by the task without disturbing earlier work.

## Document and hand off

Update affected shared docs in the same task using the mapping in [CLAUDE.md](CLAUDE.md#keeping-docs-up-to-date-always). Keep `AGENTS.md`, `CLAUDE.md`, and `CONTRIBUTING.md` consistent when workflow or contribution rules change. Link new Markdown documents from `CLAUDE.md` and `CONTRIBUTING.md`.

Mark roadmap items complete only when implemented and verified to the extent stated; document remaining device checks or blockers near the affected item. Record durable decisions and limitations in the relevant shared doc, rather than relying on a final chat message. Do not add a separate session log for every small task.

Finish with a concise handoff the owner can use when prompting either assistant:

- What changed and why, including the main files.
- Checks run and their results, plus any checks not run and why.
- Docs updated, or that none needed updating.
- Remaining blockers or owner decisions, if any.

Leave all work uncommitted. Do not start the next task automatically.
