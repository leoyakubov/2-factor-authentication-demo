# 2-Factor Authentication Demo

[![Verify](https://github.com/leoyakubov/two-factor-authentication-demo/actions/workflows/verify.yml/badge.svg)](https://github.com/leoyakubov/two-factor-authentication-demo/actions/workflows/verify.yml)
[![Deploy Backend](https://github.com/leoyakubov/two-factor-authentication-demo/actions/workflows/deploy-backend.yml/badge.svg)](https://github.com/leoyakubov/two-factor-authentication-demo/actions/workflows/deploy-backend.yml)
[![Deploy Frontend](https://github.com/leoyakubov/two-factor-authentication-demo/actions/workflows/deploy-frontend.yml/badge.svg)](https://github.com/leoyakubov/two-factor-authentication-demo/actions/workflows/deploy-frontend.yml)
[![Render Backend Health](https://github.com/leoyakubov/two-factor-authentication-demo/actions/workflows/ping-render.yml/badge.svg)](https://github.com/leoyakubov/two-factor-authentication-demo/actions/workflows/ping-render.yml)
[![Smoke Backend](https://github.com/leoyakubov/two-factor-authentication-demo/actions/workflows/smoke-backend.yml/badge.svg)](https://github.com/leoyakubov/two-factor-authentication-demo/actions/workflows/smoke-backend.yml)
[![Smoke Frontend](https://github.com/leoyakubov/two-factor-authentication-demo/actions/workflows/smoke-frontend.yml/badge.svg)](https://github.com/leoyakubov/two-factor-authentication-demo/actions/workflows/smoke-frontend.yml)

A full-stack authentication demo for learning modern web authentication patterns. It demonstrates a browser-friendly signup and login flow with optional TOTP-based two-factor authentication, QR-code enrollment, recovery codes, CSRF protection, and JWT sessions stored in an `httpOnly` cookie.

## Live Demo

Open the deployed app here: [two-factor-authentication-demo-frontend.onrender.com](https://two-factor-authentication-demo-frontend.onrender.com)

Render free services can sleep after about 15 minutes of inactivity, so the first open after a pause may take a few extra seconds while the services wake up.

## Preview

![Demo animation](docs/images/demo.gif)

| Signup | MFA enrollment |
| --- | --- |
| ![Signup screen](docs/images/signup-form.png) | ![MFA enrollment screen](docs/images/signup-mfa.png) |
| Login | Profile |
| ![Login screen](docs/images/login-form.png) | ![Profile screen](docs/images/profile-page.png) |

## Key Terms

- **Two-factor authentication (2FA)**: a login flow that requires two proofs, usually a password plus a one-time code.
- **MFA**: multi-factor authentication. In this project it means password login plus an optional TOTP verification step.
- **TOTP**: time-based one-time password, usually a 6-digit code that changes every few seconds.
- **QR-code enrollment**: the step where the app shows a QR code that links a user account to an authenticator app.
- **Recovery code**: a one-time backup code that can be used if the user loses access to the authenticator app.
- **JWT-backed session**: a signed token issued by the backend after login and stored in an `httpOnly` browser cookie.
- **CSRF token**: a browser security token required for state-changing requests when authentication uses cookies.

## What Is Implemented

- Signup with username, email, display name, password, and optional MFA.
- QR-code enrollment for TOTP-compatible authenticator apps.
- One-time recovery codes for MFA-enabled accounts.
- Login with username or email and password.
- MFA verification with either an authenticator code or an unused recovery code.
- BCrypt password hashing.
- Encrypted-at-rest MFA secrets for newly created MFA users.
- JWT sessions stored in `httpOnly` cookies so JavaScript cannot read the token directly.
- CSRF bootstrap and protected state-changing requests.
- Protected profile endpoint and profile screen.
- Logout by clearing the auth cookie without redirecting from the backend.
- User-friendly frontend validation and backend error responses.
- Backend request logging with request IDs.
- Backend unit, slice, and integration tests.
- Frontend component, flow, and API utility tests.

## MFA App

When MFA is enabled during signup, the app shows a QR code. Scan it with a TOTP-compatible authenticator app on your phone. The authenticator app then generates short 6-digit codes that change over time and are used as the second login step.

Common apps that work with this flow:

- Google Authenticator
- Microsoft Authenticator
- Authy
- 1Password
- Any other TOTP-compatible authenticator app

Basic flow:

1. Sign up with MFA enabled.
2. Scan the QR code with your authenticator app.
3. Save the recovery codes shown after signup.
4. Log in with username or email and password.
5. Enter the current authenticator code when prompted.

## Tech Stack

- **Backend**: Java 21+, Spring Boot 4.0.6, Spring Security, Spring Web, Spring Data MongoDB, Spring Validation, Spring Actuator, Maven.
- **API documentation**: OpenAPI 3 and Swagger UI via `springdoc-openapi` 3.0.3.
- **Security**: BCrypt, JWT via `jjwt` 0.13.0, `httpOnly` cookies, CSRF tokens, security headers, in-memory auth rate limiting.
- **MFA**: TOTP and QR generation via `dev.samstevens.totp` 1.7.1, encrypted MFA secret storage, hashed recovery codes.
- **Local persistence**: embedded MongoDB via Flapdoodle 4.33.0 for local app runs, `mongo-java-server` 1.47.0 for tests.
- **Backend quality**: JUnit 5, Mockito, embedded Mongo integration tests, Maven Surefire, Maven Failsafe.
- **Frontend**: React 19.2.7, Vite 8.0.16, React Router 7.16.0, Ant Design 6.4.3, Fetch API.
- **Frontend quality**: Jest 30.4.2, React Testing Library 16.3.2, ESLint 9.39.4.

## Run Locally

This is the setup path after cloning the project.

Prerequisites:

- Java 21 JDK or newer
- Node.js 20.19+ or 22.12+ with the npm version bundled with that Node install
- Git Bash / WSL (at Windows)

Check that the tools are available:

```sh
java -version
node -v
npm -v
```

Start from the project root:

1. Copy `backend/.env.example` to `backend/.env`: `cp backend/.env.example backend/.env`
2. Optional: Set `JWT_SECRET` in `backend/.env` to a long random value with at least 32 characters.
3. Copy `frontend/.env.example` to `frontend/.env` if you want to change the backend URL: `cp frontend/.env.example frontend/.env`
4. Run backend verification: `./scripts/backend-verify.sh`
5. Run frontend verification: `./scripts/frontend-verify.sh`
6. Start the backend in one terminal: `./scripts/backend-run.sh`
7. Start the frontend in another terminal: `./scripts/frontend-run.sh`
8. Open `http://localhost:3000`.
9. Optional: open Swagger UI at `http://localhost:8081/swagger-ui.html`.

Local runs use the `local` profile by default. Render uses the `demo` profile.

Expected verification results:

- Backend verification ends with Maven `BUILD SUCCESS`.
- Backend unit tests run through Surefire.
- Backend integration tests run through Failsafe during `verify`.
- Frontend verification runs ESLint, Jest, and a Vite production build.

## Coverage

- Backend coverage is generated by JaCoCo when you run `./scripts/backend-verify.sh` or `mvn verify`. The HTML report is written under `backend/target/site/jacoco/index.html`.
- Frontend coverage is available through `npm run test:coverage`.
- Current frontend coverage snapshot: 79.78% statements, 78.57% branches, 80.55% functions, 79.78% lines.

Root npm shortcuts:

- `npm run backend:verify`
- `npm run frontend:verify`
- `npm run verify`
- `npm run backend:run`
- `npm run frontend:run`
- `npm run docker:local`
- `npm run docker:demo`

Docker is not required for the local demo workflow. The backend uses embedded MongoDB locally.

Important for Windows and WSL:

- Run scripts from Git Bash or WSL on Windows.
- `frontend/node_modules` contains native packages and should not be shared between Windows/Git Bash and WSL.
- If dependencies were installed in a different shell or platform, delete `frontend/node_modules`, then run `npm ci` from the same environment you plan to use.
- For WSL, the most reliable setup is keeping the repo inside the WSL filesystem, for example `~/projects/2-factor-authentication-demo`.

## Troubleshooting

- If `8081` is already in use, stop the old backend process and start it again.
- If you hit `/login`, `/signup`, `/verify`, or `/qrcode` on the backend port by mistake, the backend redirects you to the frontend.
- If `/users/me` returns `401`, sign out, clear browser site data or cookies once, and sign in again.
- If frontend tests complain about missing packages, run `cd frontend && npm ci` from the same shell/environment you plan to use for the app.
- If frontend dev, test, or build scripts complain about a missing `rolldown` native binding, `node_modules` was probably installed for another platform.
- If WSL on `/mnt/c/...` fails with `EIO` while deleting Windows-native packages, delete `frontend/node_modules` from Windows File Explorer or PowerShell, then rerun `npm ci` from WSL.
- If the authenticator code fails, make sure the QR code was scanned into the authenticator app for the same user account.
- If embedded MongoDB fails to start in WSL, make sure your WSL distro is up to date and that the repo is not fighting Windows-native dependencies under `/mnt/c`.
- If the Render demo still tries `localhost:27017`, remove any old `MONGODB_HOST` or `MONGODB_PORT` env vars from the backend service so the embedded defaults can take over.

## Demo Setup

Use the local setup first. For the Render live demo, follow [docs/render-live-demo.md](docs/render-live-demo.md).

### Local setup

1. Make sure Java 21+ and Node.js 20.19+ or 22.12+ are installed.
2. Copy `backend/.env.example` to `backend/.env`: `cp backend/.env.example backend/.env`
3. Set `JWT_SECRET` and `MFA_SECRET_ENCRYPTION_KEY` in `backend/.env`.
4. Copy `frontend/.env.example` to `frontend/.env` if you want to change the backend URL.
5. Run backend verification: `./scripts/backend-verify.sh`
6. Run frontend verification: `./scripts/frontend-verify.sh`
7. Start the backend: `./scripts/backend-run.sh`
8. Start the frontend: `./scripts/frontend-run.sh`
9. Open `http://localhost:3000`

## Demo Walkthrough

Before the walkthrough:

1. Run `./scripts/backend-verify.sh`.
2. Run `./scripts/frontend-verify.sh`.
3. Start the backend with `./scripts/backend-run.sh`.
4. Start the frontend with `./scripts/frontend-run.sh`.
5. Open `http://localhost:3000`.

Scenario 1: signup without MFA:

1. Open the signup page.
2. Create a user without MFA.
3. Confirm the success message appears.

Scenario 2: login without MFA:

1. Click `Login` from the signup success screen.
2. Sign in with the account created in scenario 1.
3. Confirm the protected profile page loads.
4. Click `Logout`.

Scenario 3: signup with MFA:

1. Open the signup page.
2. Create a different user with MFA enabled.
3. Confirm the QR code and recovery codes are shown.
4. Scan the QR code with an authenticator app.
5. Save one recovery code for scenario 5.

Scenario 4: login with MFA app code:

1. Click `Login` from the MFA signup success screen.
2. Sign in with the MFA-enabled account.
3. Open your authenticator app and read the current 6-digit code.
4. Enter that code on the verification screen.
5. Confirm the protected profile page loads.
6. Click `Logout`.

Scenario 5: login with a one-time recovery code:

1. Sign in with the MFA-enabled account.
2. On the verification screen, enter one unused recovery code instead of an authenticator code.
3. Confirm the protected profile page loads.
4. Click `Logout`.
5. Try the same recovery code again later and confirm it no longer works.

Error handling:

1. Try signing in with a wrong password.
2. Confirm the UI shows a friendly message.
3. Try an invalid MFA code.
4. Confirm the UI shows a friendly message.
5. Check backend logs for request-level messages with request IDs.

## Architecture

### Runtime Architecture

```mermaid
flowchart LR
    User["User"] --> Browser["Browser"]
    Browser --> Frontend["React + Vite"]
    Frontend -->|API calls with CSRF token| Backend["Spring Boot API"]
    Backend --> MongoLocal["Embedded MongoDB local demo"]
    Backend --> MongoDemo["Embedded MongoDB on Render demo"]
    Backend --> Cookies["httpOnly JWT cookie"]
    Backend --> MFA["TOTP, QR enrollment, recovery codes"]
    Backend --> Security["Spring Security, CSRF, rate limiting"]
```

Responsibility split:

- Frontend: signup, login, MFA enrollment display, verification form, profile view, and user-friendly error messages.
- Backend: input validation, password hashing, MFA secret handling, recovery code handling, credential checks, JWT issuing, cookie handling, CSRF checks, rate limiting, and profile authorization.
- Browser: stores the auth cookie and sends it automatically on same-site API requests.
- Database: stores users, hashed passwords, MFA state, encrypted MFA secrets, and hashed recovery codes. Locally and on Render demo this is embedded MongoDB inside the backend container.

Security boundaries:

- Frontend routes are UX boundaries only.
- Backend authorization remains the real security boundary.
- JavaScript cannot read the JWT because it is stored in an `httpOnly` cookie.
- CSRF protection is required because cookie-backed credentials are sent automatically by the browser.
- Secrets and credentials should never be exposed through frontend state, logs, screenshots, or docs.

### Flows

The diagrams below match the main demo scenarios: signup without MFA, login without MFA, signup with MFA, login with an authenticator app code, login with a one-time recovery code, and logout.

Cookie-backed JWT flow:

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant B as Backend
    participant Br as Browser

    U->>F: Open app
    F->>B: GET /csrf
    B-->>F: CSRF token + XSRF-TOKEN cookie
    U->>F: Submit username/email and password
    F->>B: POST /signin + X-XSRF-TOKEN
    B->>B: Verify credentials
    B-->>Br: Set-Cookie: AUTH_TOKEN=httpOnly JWT
    F->>B: GET /users/me
    Note over Br,B: Browser sends AUTH_TOKEN automatically
    B->>B: Validate JWT signature and expiry
    B-->>F: User profile
```

Signup without MFA:

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant Backend
    participant Mongo

    User->>Frontend: Submit signup form without MFA
    Frontend->>Backend: POST /users
    Backend->>Backend: Validate input and hash password
    Backend->>Mongo: Store user record
    Backend-->>Frontend: 201 Created without QR or recovery codes
```

Login without MFA:

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant Backend
    participant Browser

    User->>Frontend: Submit username/email and password
    Frontend->>Backend: POST /signin
    Backend->>Backend: Verify credentials and rate limit
    Backend-->>Browser: Set-Cookie: AUTH_TOKEN=httpOnly JWT
    Frontend->>Backend: GET /users/me
    Backend-->>Frontend: Protected profile
```

Signup with MFA:

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant Backend
    participant Mongo
    participant Authenticator

    User->>Frontend: Submit signup form with MFA enabled
    Frontend->>Backend: POST /users
    Backend->>Backend: Validate input and hash password
    Backend->>Backend: Generate TOTP secret and recovery codes
    Backend->>Backend: Encrypt MFA secret and hash recovery codes
    Backend->>Mongo: Store user record
    Backend-->>Frontend: QR code image and one-time recovery codes
    User->>Authenticator: Scan QR code
```

Login with MFA:

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant Backend
    participant Browser

    User->>Frontend: Submit username/email and password
    Frontend->>Backend: POST /signin
    Backend->>Backend: Verify credentials and rate limit
    Backend-->>Frontend: MFA required
    User->>Frontend: Submit authenticator or recovery code
    Frontend->>Backend: POST /verify
    Backend->>Backend: Verify TOTP or consume recovery code
    Backend-->>Browser: Set-Cookie: AUTH_TOKEN=httpOnly JWT
    Frontend->>Backend: GET /users/me
    Backend-->>Frontend: Protected profile
```

Recovery code variant:

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant Backend
    participant Browser

    User->>Frontend: Enter username/email and password
    Frontend->>Backend: POST /signin
    Backend-->>Frontend: MFA required
    User->>Frontend: Enter a one-time recovery code
    Frontend->>Backend: POST /verify
    Backend->>Backend: Hash lookup and one-time code consumption
    Backend-->>Browser: Set-Cookie: AUTH_TOKEN=httpOnly JWT
    Frontend->>Backend: GET /users/me
    Backend-->>Frontend: Protected profile
```

Logout flow:

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant Backend
    participant Browser

    User->>Frontend: Click logout
    Frontend->>Backend: POST /logout + CSRF token
    Backend-->>Browser: 204 No Content + expired AUTH_TOKEN cookie
    Frontend->>Frontend: Clear local auth state and show login screen
```

### API Overview

| Method | Path | Purpose | Auth |
| --- | --- | --- | --- |
| `GET` | `/csrf` | Creates and returns the CSRF token used by frontend POST requests. | Public |
| `POST` | `/users` | Creates a user, returns `201 Created`, and returns MFA enrollment data when MFA is enabled. | Public + CSRF |
| `POST` | `/signin` | Verifies username/email and password. Returns MFA-required response or sets the auth cookie. | Public + CSRF |
| `POST` | `/verify` | Verifies an authenticator code or recovery code and sets the auth cookie. | Public + CSRF |
| `POST` | `/logout` | Clears the auth cookie and returns `204 No Content`. | Public + CSRF |
| `GET` | `/users/me` | Returns the current authenticated profile. | Cookie JWT |

The backend also accepts browser navigation to `/`, `/login`, `/signup`, `/verify`, and `/qrcode` and redirects those routes to the frontend app.

OpenAPI documentation is available locally after starting the backend:

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

### Test Structure

- Backend unit tests use `*Test`.
- Backend integration tests use `*IT` and run in Maven Failsafe during `verify`.
- Frontend tests run through the custom Jest runner used by `npm run test:ci`.
- Full frontend verification runs lint, tests, and production build through `npm run verify`.

### Repository Structure

```text
.
+-- backend
|   +-- src/main/java/com/github/leoyakubov/twofactorauth
|   |   +-- config
|   |   +-- controller
|   |   +-- exception
|   |   +-- model
|   |   +-- payload
|   |   +-- repository
|   |   +-- service
|   +-- src/main/resources
|   +-- src/test/java
+-- frontend
|   +-- src
|   |   +-- profile
|   |   +-- qrcode
|   |   +-- shared
|   |   +-- signin
|   |   +-- signup
|   |   +-- verifycode
|   +-- test
+-- docs
+-- scripts
```

## Limitations and security notes

This project implements a realistic local authentication flow, but it is still a demo. The notes below separate what is already handled from what would need hardening before a real deployment.

### Current security posture:

- Passwords are hashed with BCrypt before storage.
- JWTs are stored in `httpOnly` cookies instead of `localStorage`.
- State-changing browser requests use CSRF tokens with a frontend-readable `XSRF-TOKEN` cookie and `X-XSRF-TOKEN` request header.
- MFA secrets generated for new MFA users are encrypted before storage.
- Recovery codes are stored as hashes and are consumed after use.
- Repeated signup, signin, and MFA verification attempts are rate limited in memory.
- Backend routes remain the authorization boundary; frontend routing is only a user experience layer.
- Security headers are configured through Spring properties, including Content Security Policy.

### Limitations

- Embedded MongoDB is convenient locally and for the Render demo. Production deployments should still use a managed or separately operated MongoDB instance.
- Rate limiting is in-memory and resets on backend restart.
- Email verification, password reset, and full lost-MFA recovery flows are not implemented.
- Local development runs over HTTP; production use should enforce HTTPS and secure cookie settings.
- Refresh token rotation is not implemented; the demo uses a single cookie-backed JWT access token.
- MFA secrets are encrypted before storage, but the local default can derive the encryption key from `JWT_SECRET`; production systems should use managed key storage and rotation.
- Recovery codes are shown once during signup; there is no UI for regenerating them.

### Security concerns and possible solutions:

- Cookie-backed JWTs reduce token exposure to JavaScript, but injected scripts could still make authenticated requests from the page.
  Possible solution: keep React output escaping, avoid unsafe HTML, keep dependencies updated, and keep the Content Security Policy strict.
- CSRF protection is required because cookies are sent automatically by the browser.
  Possible solution: keep the `/csrf` bootstrap flow, require the `X-XSRF-TOKEN` header for state-changing requests, and review any future public POST endpoints carefully.
- Local HTTP is acceptable for development, but credentials and cookies must be protected in deployed environments.
  Possible solution: enforce HTTPS outside local development, set `JWT_COOKIE_SECURE=true`, and use production-specific CORS origins.
- MFA secrets are encrypted before storage, but the encryption key still needs production-grade management.
  Possible solution: store keys in a secret manager or vault, restrict access, rotate keys carefully, and expose QR enrollment data only during setup.
- The JWT secret must remain private and rotated over time.
  Possible solution: load it from managed secrets, rotate it periodically, and never commit real secret values.
- In-memory rate limiting is useful locally but does not protect a multi-instance deployment.
  Possible solution: move rate-limit counters to Redis or another shared store and add edge-layer protection.
- Recovery codes are one-time use, but there is no regeneration or recovery workflow yet.
  Possible solution: add authenticated recovery-code regeneration, password reset, email verification, and a documented lost-MFA flow.
- Embedded MongoDB makes local development easy, but it is not a production database setup.
  Possible solution: use a managed or separately operated MongoDB deployment with backups, monitoring, access controls, and network restrictions.

### Hardening roadmap:

1. Add deployment profiles for HTTPS, secure cookies, production CORS origins, and external MongoDB.
2. Move secrets and encryption keys to a managed secret store.
3. Add password reset, email verification, and recovery-code regeneration flows.
4. Replace in-memory rate limiting with shared persistent counters.
5. Add refresh token rotation or a clearer short-lived session strategy.
6. Add production observability guidance for auth failures, lockouts, and suspicious activity.
