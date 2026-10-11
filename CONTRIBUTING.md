# Contributing to Formalyze

## Do not commit

**Do not run `git commit` (or `git push`, `git merge`, `git rebase`, `git reset`, `git stash`) on this repository.**

This applies to everyone and everything working in the repo, including AI coding agents such as Claude Code. Make your changes in the working tree and leave them uncommitted. The project owner reviews the changes and commits them personally.

- Read-only git commands (`git status`, `git diff`, `git log`) are fine.
- Do not stage files either (`git add`); the owner decides what goes into each commit.
- If a task seems to require a commit, stop and ask first.

## Working on the code

- Read [CLAUDE.md](CLAUDE.md) for build commands and conventions, and [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for how the pipeline fits together.
- Exercise rules and thresholds live in [docs/EXERCISES.md](docs/EXERCISES.md). Change the doc and the code together.
- Planned work and phases are in [docs/ROADMAP.md](docs/ROADMAP.md).

### Style

- Kotlin, official Kotlin code style, 4-space indent.
- Keep angle math, rep counting and form rules free of Android imports so they can be unit-tested on the JVM.
- Add or update unit tests for any change to rules, thresholds or rep counting.

### Before handing work over

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

(On Windows PowerShell use `.\gradlew.bat` instead of `./gradlew`.)

Summarize what changed and what you ran so the owner can review and commit.
