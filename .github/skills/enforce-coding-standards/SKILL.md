---
name: enforce-coding-standards
description: 'Enforce hmpps-community-support-api coding standards for Kotlin, tests, and Flyway SQL. Use when implementing features, fixing bugs, or reviewing PR changes to ensure style, migration safety, and verification checks are met.'
argument-hint: 'Describe the change or files to validate (for example: new endpoint with migration, service-only refactor, test-only update).'
user-invocable: true
---

# Enforce Coding Standards

## What This Skill Produces

A standards-compliant code change in this repository, with:
- Kotlin formatting aligned to project settings.
- SQL migrations and seeds that are safe to run repeatedly.
- Tests for behavioral changes.
- Local verification evidence from Gradle checks.

## When To Use

Use this skill when:
- Adding or editing Kotlin source in `src/main/kotlin`.
- Adding or editing tests in `src/test/kotlin`.
- Adding Flyway migrations or seed scripts in `src/main/resources/db`.
- Reviewing a pull request for standards compliance.

## Repository Standards

1. Kotlin formatting and style
- Follow `.editorconfig` (`indent_size = 2` for Kotlin).
- Use trailing commas where appropriate (enabled in project settings).
- Keep imports explicit and remove unused imports.

2. Verification commands
- Lint: `./gradlew ktlintCheck`
- Auto-format when needed: `./gradlew ktlintFormat`
- Tests: `./gradlew clean test`
- Full validation before handoff: `./gradlew clean build`

3. Migration and seed rules
- Place schema migrations in `src/main/resources/db/migration` using the `VNN__description.sql` pattern.
- Place seed data in `src/main/resources/db/seed`.
- Prefer idempotent SQL patterns when practical (`IF NOT EXISTS`, conflict-safe inserts, safeguards for reruns).
- When idempotency is not practical, document why and include reviewer-facing rationale.
- Prefer text columns with constraints/checks for enum-like values instead of introducing PostgreSQL enum types.
- Increment migration numbers based on the file in that directory.
- Add meaningful `COMMENT ON COLUMN` metadata for new tables/columns.

4. Kotlin architecture expectations
- Keep controller methods thin; move business logic to service classes.
- Keep repository interfaces focused on persistence concerns.
- Use domain/entity naming that matches existing package patterns under `uk.gov.justice.digital.hmpps.communitysupportapi`.
- Keep transactional boundaries explicit with `@Transactional` only where write consistency is required.

5. Testing expectations
- Add or update tests for behavior changes.
- Prefer integration tests for endpoint and persistence behavior.
- Use existing test patterns and factories in `src/test/kotlin/.../testdata/factory`.
- Prefer where possible to use concrete implementations of repositories in integration tests, rather than mocks.

6. PR hygiene expectations
- Keep pull requests focused and avoid unrelated edits.
- Provide concise verification evidence (lint, tests, build outcomes).
- Call out exceptions or risk areas explicitly in the PR description.
- Do not commit or push changes; leave version control actions to the user unless explicitly requested.
- Confirm any new files appear in git status and are tracked or staged as expected before handoff.

## Procedure

1. Classify the change
- Determine whether the change is: Kotlin-only, SQL-only, or mixed Kotlin+SQL.
- Determine if behavior changes externally (API response, DB state, side effects).

2. Apply coding standards during implementation
- Match existing package and naming conventions.
- Keep diffs minimal and avoid unrelated refactors.
- For SQL changes, ensure migration ordering and idempotency safety.

3. Enforce testing coverage
- For external behavior changes, add or update integration tests.
- For pure mapping or utility logic, add focused unit tests.

4. Run verification in this order
- `./gradlew ktlintCheck`
- If lint fails, run `./gradlew ktlintFormat` and re-run lint.
- `./gradlew clean test`
- `./gradlew clean build` for final confidence before handoff.

5. Enforce blocking quality gate
- Do not mark work complete if lint, tests, or build is failing.
- Fix failures or clearly state what is blocked and why.

6. Completion checks
- Lint passes.
- Tests pass.
- Build passes.
- Migration/seed scripts are named correctly and include clear safety rationale.
- Change includes tests for each user-visible behavior change.
- PR evidence includes command outcomes and any exceptions.
- New files are visible in git status and have been added to git as expected.

## Decision Points

- If only whitespace/style issues exist: run formatter and avoid logic edits.
- If SQL introduces enum-like values: use text + check constraints unless there is an explicit, approved exception.
- If API contract changes: require integration test updates.
- If migration is not rerunnable safely: document why one-time execution is intentionally required and highlight rollback/mitigation considerations.

## Output Format

When reporting completion:
- List files changed.
- List verification commands run and outcomes.
- List any standards exceptions with rationale.
