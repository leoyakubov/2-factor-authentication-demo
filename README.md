# two-factor-authentication-demo

A full-stack demo that shows username/password sign-up and login with optional two-factor authentication, QR-code enrollment, TOTP verification, and JWT-backed browser sessions using an `httpOnly` cookie.

## Table Of Contents

- [Project Snapshot](#project-snapshot)
- [What Is Implemented](#what-is-implemented)
- [Why This Project](#why-this-project)
- [Quick Start](#quick-start)
- [Docs](#docs)
- [Project Status](#project-status)

## Project Snapshot

- Backend: Spring Boot 3.5.14
- Frontend: React 19.1.1 with Vite
- Auth: JWT in `httpOnly` cookies with CSRF protection
- MFA: TOTP with QR-code enrollment and recovery codes
- Data: MongoDB for local development

## What Is Implemented

- Sign up with username, email, password, display name, and optional MFA
- QR-code enrollment for authenticator apps
- TOTP verification during login
- One-time recovery codes for MFA-enabled accounts
- JWT-backed session access through an `httpOnly` cookie
- CSRF protection for state-changing requests
- Rate limiting for sign-in, sign-up, and MFA verification
- Logout by clearing the session cookie
- Backend and frontend tests

## Why This Project

This project was built to demonstrate:

- a realistic full-stack auth flow
- a browser-friendly MFA experience
- practical Spring Boot and React architecture
- security tradeoffs and hardening decisions
- a repo that is easy to run and present in an interview

## Quick Start

1. Copy `backend/.env.example` to `backend/.env` and set `JWT_SECRET` to a long random string.
2. Copy `frontend/.env.example` to `frontend/.env` if you want to override the API URL.
3. Run backend tests: `./scripts/test-backend.sh`
4. Run frontend tests: `./scripts/test-frontend.sh`
5. Build the frontend: `./scripts/build-frontend.sh`
6. Start the backend: `./scripts/run-backend.sh`
7. Start the frontend: `./scripts/run-frontend.sh`
8. Open the app and walk through signup, MFA enrollment, login, and profile access.

## Docs

- [Technical guide](docs/technical-guide.md)
- [Security guide](docs/security-guide.md)
- [Full verification workflow](docs/verification-workflow.md)
- [Troubleshooting](docs/troubleshooting.md)

## Project Status

The app is demo-ready and the latest work focused on security hardening, documentation cleanup, and presentation polish.

