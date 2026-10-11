# Contributing to Formalyze

## Do not commit

**Do not run `git commit` (or `git push`, `git merge`, `git rebase`, `git reset`, `git stash`) on this repository.**

This applies to everyone and everything working in the repo, including AI coding agents such as Claude Code and Codex. Make your changes in the working tree and leave them uncommitted. The project owner reviews the changes and commits them personally.

- Read-only git commands (`git status`, `git diff`, `git log`) are fine.
- Do not stage files either (`git add`); the owner decides what goes into each commit.
- If a task seems to require a commit, stop and ask first.

## Working on the code

- Read [README.md](README.md) for the project introduction, features, and setup instructions.
- Codex starts with [AGENTS.md](AGENTS.md), its workflow and handoff blueprint. Claude starts with [CLAUDE.md](CLAUDE.md); both use the same shared project docs.
- The owner prompts each assistant separately. Inspect the working tree before editing, preserve existing unrelated changes, and stay within the requested task. Do not create branches, worktrees, or pull requests unless requested.
- Read [CLAUDE.md](CLAUDE.md) for build commands and conventions, and [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for how the pipeline fits together.
- Exercise rules and thresholds live in [docs/EXERCISES.md](docs/EXERCISES.md). Change the doc and the code together.
- Planned work and phases are in [docs/ROADMAP.md](docs/ROADMAP.md).

### Style

- Kotlin, official Kotlin code style, 4-space indent.
- **Meaningful names only.** No `a`, `b`, `c`, `tmp` or other throwaway names; a name should tell the reader what the value is or what the function does (`kneeAngleDegrees`, `repCount`). Avoid single-letter names, including loop counters.
- **Comments only at the top.** Put a short, beginner-friendly comment in the first 1 to 10 lines of a file, class or function explaining its purpose or an important design decision. No inline comments between lines of code, and no comments that restate the obvious. If the code is self-explanatory, skip the comment. Full rules and an example are in [CLAUDE.md](CLAUDE.md#code-style).
- Keep angle math, rep counting and form rules free of Android imports so they can be unit-tested on the JVM.
- Add or update unit tests for any change to rules, thresholds or rep counting.

### Keep the docs current

The `.md` files are living documents. When you add or change an important function (a feature, behavior, threshold, dependency, or decision), update the affected docs in the same piece of work: `docs/ARCHITECTURE.md` for structure, `docs/EXERCISES.md` for exercise rules, `docs/ROADMAP.md` for progress and open questions, and `CLAUDE.md` for commands, stack and rules. The full mapping is in [CLAUDE.md](CLAUDE.md#keeping-docs-up-to-date-always). Don't leave docs describing things that no longer exist.

Keep `AGENTS.md`, `CLAUDE.md`, and this file consistent when contribution or assistant workflow rules change. Link new Markdown documents from both `CLAUDE.md` and this file.

### Before handing work over

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

(On Windows PowerShell use `.\gradlew.bat` instead of `./gradlew`.)

For documentation-only changes, review consistency and local links and run `git diff --check`; the Gradle checks above are unnecessary. Report checks that could not run and why. Camera/pose accuracy and latency need verification on a physical device.

Summarize what changed, what you ran, and which docs you updated (or that none needed it) so the owner can review and commit.
