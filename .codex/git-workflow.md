# Git Workflow

This repository uses task-sized Git work units. For every Git-related task, first read this file and the relevant template files listed in `AGENTS.md`.

## Default Work Unit

1. Inspect the current state with `git status --short --branch`.
2. Confirm the target branch and remote with `git branch --show-current` and `git remote -v`.
3. If the task needs tracking, create or update a GitHub issue using the issue templates.
4. Create a short-lived branch from `main` unless the current branch is already the correct task branch.
5. Make the requested changes only.
6. Run the smallest useful verification for the change.
7. Review the diff before committing.
8. Commit using `.gitmessage.txt` and Conventional Commits.
9. Push the branch.
10. Open or update a PR using `.github/PULL_REQUEST_TEMPLATE.md`.
11. Review using `.github/REVIEW_TEMPLATE.md`.
12. Merge only when the PR is ready, checks are acceptable, and no blocking review item remains.

## Branch Names

Use lowercase, hyphenated names:

- `feat/<short-topic>`
- `fix/<short-topic>`
- `chore/<short-topic>`
- `docs/<short-topic>`
- `test/<short-topic>`
- `refactor/<short-topic>`

## Commits

Use Conventional Commits:

- `feat: add calendar month view`
- `fix: handle expired access token`
- `chore: configure github templates`
- `docs: document local setup`
- `test: cover schedule validation`
- `refactor: split auth service`

Each commit should represent one coherent work unit. Do not mix unrelated cleanup with feature or bug work.

## Pull Requests

PRs should describe:

- Why the change exists
- What changed
- How it was verified
- Any risk, follow-up, or deployment note

Open draft PRs for unfinished work. Mark ready only when the implementation and verification are complete.

## Reviews

Review for correctness first:

- Bugs or regressions
- Missing validation or security issues
- Broken platform assumptions
- Missing tests for risky behavior
- Confusing API or data contract changes

Style-only comments should not block unless they affect readability or maintenance.

## Merge Rules

Before merge:

- Working tree is clean
- PR branch is pushed
- Required checks are passing or the reason for skipped checks is documented
- Blocking review comments are resolved
- PR summary and verification notes are accurate

Prefer squash merge for task branches unless the repository policy says otherwise.
