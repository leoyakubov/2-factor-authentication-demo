# two-factor-authentication-demo

A small full-stack demo showing username/password sign-up and login with optional two-factor authentication, QR-code enrollment, TOTP verification, and JWT-backed session access.

Default local URLs:

- Backend: `http://localhost:8081`
- Frontend: `http://localhost:3000`

## Project Intent

This repo is meant to demonstrate a simple auth flow with:

- registration with or without MFA
- QR-code based authenticator enrollment
- login + OTP verification when MFA is enabled
- JWT-protected profile access

It is intentionally demo-focused rather than production hardened, but it is structured so it is easy to understand, run, and extend.

## Quick Start

1. Copy `backend/.env.example` to `backend/.env` and set `JWT_SECRET` to a long random secret string used to sign JWTs.
2. Copy `frontend/.env.example` to `frontend/.env` if you want to override the API URL.
3. Run the backend tests: `./scripts/test-backend.sh`
4. Run the frontend tests: `./scripts/test-frontend.sh`
5. Build the frontend once before a demo: `./scripts/build-frontend.sh`
6. Start the backend: `./scripts/run-backend.sh`
7. Start the frontend: `./scripts/run-frontend.sh`
8. Open the app in the browser and walk through signup, MFA enrollment, login, and profile access.

## Key Terms

- Two-factor authentication (2FA): a login flow that asks for two kinds of proof, usually a password plus a one-time code from an authenticator app
- QR-code enrollment: the step where the app shows a QR code that links your account to an authenticator app
- TOTP: time-based one-time password, meaning a 6-digit code that changes every few seconds
- JWT-backed session access: after login, the backend returns a signed token and the frontend sends it on future requests in the `Authorization` header

## What Is Implemented

- Sign up with username, email, password, display name, and optional MFA
- Generate a QR code when MFA is enabled during signup
- Store the MFA secret on the backend for the user account
- Log in with username or email plus password
- Require a second step when MFA is enabled
- Verify the 6-digit authenticator code on the backend
- Return a signed JWT after successful login or MFA verification
- Use the JWT to load the protected profile page
- Support logout by clearing the stored access token

## MFA App

When MFA is enabled, the app shows a QR code during signup. That QR code is scanned by an authenticator app on your phone.

What it is:

- An authenticator app generates short, time-based 6-digit codes
- Those codes change every few seconds and are used as the second login factor

Where to get one:

- Google Authenticator
- Microsoft Authenticator
- Authy
- 1Password or any other TOTP-compatible authenticator app

How to use it:

1. Sign up with MFA enabled.
2. Open your authenticator app and add a new account.
3. Scan the QR code with your phone camera.
4. The app will start showing 6-digit codes.
5. Enter the current code on the login verification screen when prompted.

If MFA is disabled for the account, the login flow skips the QR code and verification step.

## Tech Stack

Backend:

- Java 21
- Spring Boot 3.5.14
- Spring Security
- Spring Web
- Spring Data MongoDB
- Embedded MongoDB via Flapdoodle
- JWT via `jjwt`
- TOTP / QR generation via `dev.samstevens.totp`

Frontend:

- React 19.1.1
- React Router v5
- Ant Design
- Fetch API

Tooling:

- Maven Wrapper
- npm
- PowerShell scripts for Windows
- shell scripts for Unix/macOS

## Repository Layout

- `backend`: Spring Boot API
- `frontend`: React client
- `scripts`: helper scripts for running and testing

## Local Setup

### Prerequisites

- Java 21 or compatible
- Node.js and npm

### Environment files

The helper scripts will create `.env` files from the examples if they are missing.

- `backend/.env.example` contains backend runtime values, including `JWT_SECRET`
- `frontend/.env.example` contains `REACT_APP_API_BASE_URL`

If you want to set them manually:

```powershell
copy backend\.env.example backend\.env
copy frontend\.env.example frontend\.env
```

### Install frontend dependencies

```powershell
cd frontend
npm install
cd ..
```

The frontend helper scripts will also run `npm install` automatically if `node_modules` is missing.

### Run the apps

Windows:

- `scripts/run-backend.ps1`
- `scripts/run-frontend.ps1`
- `scripts/build-frontend.ps1`

Unix/macOS:

- `scripts/run-backend.sh`
- `scripts/run-frontend.sh`
- `scripts/build-frontend.sh`

If you prefer direct commands:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

```powershell
cd frontend
npm start
```

## Full Verification Workflow

Use this checklist to verify the full project from a clean machine.

### 1. Check prerequisites

Make sure these are installed:

- Java 21 or compatible
- Node.js
- npm

You can verify them with:

```powershell
java -version
node -v
npm -v
```

### 2. Prepare environment files

The helper scripts create `.env` files from the example files if they are missing, but it is still a good idea to inspect them first.

Backend:

- `backend/.env.example`
- `backend/.env`

Frontend:

- `frontend/.env.example`
- `frontend/.env`

If you want to create them manually:

```powershell
copy backend\.env.example backend\.env
copy frontend\.env.example frontend\.env
```

Set `JWT_SECRET` in `backend/.env` to a long random value if you are not using the default demo value.

### 3. Install frontend dependencies

```powershell
cd frontend
npm install
cd ..
```

The helper scripts also install dependencies automatically if `node_modules` is missing.

### 4. Run backend tests

Windows:

- `scripts/test-backend.ps1`

Unix/macOS:

- `scripts/test-backend.sh`

Expected result:

- Maven finishes with `BUILD SUCCESS`
- The Spring context test passes

If you prefer a direct command:

```powershell
cd backend
.\mvnw.cmd test
```

### 5. Run frontend tests

Windows:

- `scripts/test-frontend.ps1`

Unix/macOS:

- `scripts/test-frontend.sh`

Expected result:

- Jest runs once and exits
- The current frontend tests pass

If you prefer a direct command:

```powershell
cd frontend
npm test -- --watchAll=false
```

### 6. Build the frontend

This is optional, but it is a good final validation step.

Windows:

- `scripts/build-frontend.ps1`

Unix/macOS:

- `scripts/build-frontend.sh`

Expected result:

- React creates a production `build` folder
- The build completes without lint or compilation errors

If you prefer a direct command:

```powershell
cd frontend
npm run build
```

### 7. Start the backend

Open a new terminal window and run:

Windows:

- `scripts/run-backend.ps1`

Unix/macOS:

- `scripts/run-backend.sh`

Expected result:

- Spring Boot starts successfully
- The API listens on `http://localhost:8081`
- Embedded Mongo starts automatically for the demo

If you prefer a direct command:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### 8. Start the frontend

Open another terminal window and run:

Windows:

- `scripts/run-frontend.ps1`

Unix/macOS:

- `scripts/run-frontend.sh`

Expected result:

- React starts successfully
- The app opens on `http://localhost:3000`

If you prefer a direct command:

```powershell
cd frontend
npm start
```

### 9. Smoke test the UI flow

1. Open the app in the browser.
2. Go to the signup screen.
3. Create a new user.
4. If MFA is enabled, stay on the signup screen and confirm the success message and QR code are shown.
5. Click the `Login` button to open the login form.
6. Sign in with the account you just created.
7. If MFA is enabled, enter the 6-digit code from your authenticator app.
8. Confirm the profile page loads and shows the avatar and logout button.
9. Click `Logout` and confirm you are returned to the login screen.

### 10. Verify error handling

Try a few failure cases on purpose:

- Leave a required field blank and submit
- Enter a wrong password
- Try a login for a user that does not exist
- Enter an invalid MFA code, if MFA is enabled

Expected result:

- The UI shows a user-friendly error message
- The backend logs include a helpful message for the failure

### 11. Optional clean restart check

If you want to verify the app starts cleanly from scratch:

1. Stop both frontend and backend terminals
2. Close any browser tab that is still open to the app
3. Start the backend again
4. Start the frontend again
5. Repeat the smoke test

This is a good final check before a demo or commit.

## Notes

- The backend and frontend are configured to use local defaults, so the project should run out of the box after dependencies are installed.
- If you change the backend port, update `REACT_APP_API_BASE_URL` in `frontend/.env`.
- The backend secret should be changed for any real deployment.

## API Overview

- `POST /users` creates a new user and returns the MFA QR code when MFA is enabled
- `POST /signin` checks username/email plus password and returns a JWT or MFA-required response
- `POST /verify` checks the 6-digit authenticator code and returns a JWT
- `GET /me` returns the current authenticated user profile

## Troubleshooting

- If `8081` is already in use, stop the old backend process and start it again
- If `/me` returns `401`, clear the browser token once and sign in again
- If frontend tests complain about missing packages, run `cd frontend && npm install`
- If the frontend build fails on warnings, run the test/build scripts from the repo root so the environment is set up consistently
- If the authenticator code fails, make sure the QR code was scanned into the authenticator app for the same user account

## Limitations

- This is a demo project, not a production security implementation
- There is no rate limiting or brute-force protection
- There are no recovery codes or backup authentication methods
- There is no password reset flow
- There is no account lockout policy
- The demo uses embedded MongoDB for local development
- The JWT secret is loaded from local environment configuration and should be rotated and managed securely in a real deployment
- The app does not enforce HTTPS locally
