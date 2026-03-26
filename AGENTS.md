# AGENTS.md

## Project overview
- Existing project: Spring Boot task API
- Main stack: Java 17, Gradle, Spring Boot, PostgreSQL, Docker Compose
- Preserve the current project structure unless there is a strong reason to change it

## Working rules
- Read the existing codebase before editing
- Keep diffs scoped to the requested milestone
- Reuse existing naming/package patterns
- Do not add broad catch blocks or hide exceptions
- Do not remove existing working behavior unless necessary
- If behavior changes, add or update tests

## Verification
Before finishing, always run:
- ./gradlew test
- ./gradlew build

If Docker-related files changed, also run:
- docker compose config

## Done criteria
A task is done only when:
- code is implemented
- tests pass
- build passes
- README is updated when run steps or API behavior changed

## Final response format
Always report:
1. what changed
2. changed files
3. commands run
4. result of verification
5. any remaining risks or follow-ups