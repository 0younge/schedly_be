# Git Workflow

Use one issue, branch, commit set, PR, review, and merge per task. Read only the template needed next.

## Flow

1. Check state: `git status --short --branch`, branch, and remote.
2. Create/update an issue when tracking helps.
3. Branch from `main` unless already on the task branch.
4. Change only the requested scope.
5. Run the smallest useful verification; note skipped checks.
6. Review the diff before commit.
7. Commit with `.gitmessage.txt` and Conventional Commits.
8. Push; open/update PR with `.github/PULL_REQUEST_TEMPLATE.md`.
9. Review with `.github/REVIEW_TEMPLATE.md`.
10. Merge only when clean, checks are acceptable, blockers are resolved, and the PR text is accurate. Prefer squash.

## Templates

- Issue: matching `.github/ISSUE_TEMPLATE/*.md`
- Branch: `feat|fix|chore|docs|test|refactor/<topic>`
- Commit: `type: summary`, for example `chore: optimize git workflow docs`

## Guardrails

- Do not mix unrelated work.
- Document skipped verification in the PR.
