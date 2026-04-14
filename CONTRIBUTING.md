# Contributing Guide

Thank you for your interest in contributing to Halo.

This document explains the recommended workflow for submitting high-quality contributions, including code, tests, and documentation updates.

## Before You Start

- For new features or major behavior changes, please open an issue first so we can align on scope and design.
- For clear bug fixes, you can submit a pull request directly.
- If your report is not about the core project itself (for example, deployment questions), please use Discussions instead of Issues.

## Development Environment

This repository mainly contains:

- Backend and platform modules built with Gradle.
- Frontend code in `ui`, managed with `pnpm` workspaces.

### Prerequisites

- Git
- JDK (version compatible with the project build)
- Node.js and `pnpm` (see `ui/package.json` for the current package manager)
- Docker / Docker Compose (required for e2e scenarios)

## Contribution Workflow

### 1. Fork and Clone

Fork this repository, then clone your fork:

```bash
git clone https://github.com/{YOUR_USERNAME}/{REPOSITORY}.git
cd {REPOSITORY}
```

### 2. Add Upstream Remote

```bash
git remote add upstream https://github.com/halo-dev/halo.git
git fetch upstream
```

### 3. Create a Branch

Use a focused branch name that reflects your change:

```bash
git checkout -b feat/short-description
```

### 4. Implement and Validate

Run relevant checks before opening a PR.

Backend and general checks:

```bash
./gradlew clean check
```

Frontend checks (in `ui`):

```bash
cd ui
pnpm install
pnpm build:packages
pnpm lint
pnpm typecheck
pnpm test:unit
```

### 5. Commit and Push

```bash
git push origin <your-branch>
```

### 6. Open a Pull Request

Open a PR from your branch to `main` and fill out the PR template carefully:

- Describe what changed and why.
- Link related issues (for example, `Fixes #123`).
- Add release note content or `NONE` when no user-facing change is introduced.
- Add proper `/kind` labels as requested in the template.

## AI-Assisted Contribution Policy

AI-assisted development is not prohibited, including code generation and refactoring support.

However, you are fully responsible for any code in your PR.

If you used AI tools, please follow these rules:

- Review all AI-generated content before submission.
- Verify correctness, security, performance, and maintainability.
- Ensure generated code follows project conventions and architecture.
- Remove low-quality or redundant generated code.
- Mention AI assistance in your PR description when AI materially contributed to the final changes.

In short: AI assistance is allowed, but unreviewed AI output is not acceptable.

## Testing Expectations

- Add or update tests whenever you change behavior.
- If you add or modify APIs, please include corresponding e2e test cases.
- See `e2e/README.md` for e2e workflow and local execution details.

## Coding Standards

- Follow the project coding style guide: <https://docs.halo.run/developer-guide/core/code-style>
- Keep changes focused and avoid unrelated refactors in the same PR.
- Run formatters and linters before pushing.

## Keep Your Fork Updated

Before starting new work, sync your branch with upstream:

```bash
git fetch upstream
git checkout main
git merge upstream/main
git push origin main
```

## Need Help?

- Open an issue for confirmed bugs and feature proposals.
- Use Discussions for general questions and usage/deployment topics.

Thanks again for helping improve Halo.
