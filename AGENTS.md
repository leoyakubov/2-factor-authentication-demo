# Repo Guidelines

## Scope
- This repository contains a Spring Boot backend and a React frontend.
- Make focused changes and avoid unrelated refactors unless they are part of the requested work.

## Verification Rules
- After fixing a bug, run the related tests for the affected area before marking the work done.
- Always run `backend` and `frontend` verify checks before committing changes.
- If a verify step fails, fix the failure and rerun the relevant verify command before committing.

## Working Rules
- Prefer small, reviewable changes.
- Use `apply_patch` for file edits.
- Keep changes consistent with the existing backend and frontend structure.
- Do not revert unrelated user changes.

## Recommended Checks
- Backend: run `backend` verify through the repo scripts or Maven wrapper.
- Frontend: run `frontend` verify through the repo scripts or npm scripts.
- If a change touches both apps, verify both sides before committing.

