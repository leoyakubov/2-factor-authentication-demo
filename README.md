# two-factor-authentication-demo

A small full-stack demo showing username/password sign-up and login with optional two-factor authentication, QR-code enrollment, TOTP verification, and JWT-backed session access.

## Project Intent

This repo is meant to demonstrate a simple auth flow with:

- registration with or without MFA
- QR-code based authenticator enrollment
- login + OTP verification when MFA is enabled
- JWT-protected profile access

It is intentionally demo-focused rather than production hardened, but it is structured so it is easy to understand, run, and extend.

## Tech Stack

Backend:

- Java 21
- Spring Boot 3.2
- Spring Security
- Spring Web
- Spring Data MongoDB
- Embedded MongoDB via Flapdoodle
- JWT via `jjwt`
- TOTP / QR generation via `dev.samstevens.totp`

Frontend:

- React 18
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

Unix/macOS:

- `scripts/run-backend.sh`
- `scripts/run-frontend.sh`

If you prefer direct commands:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

```powershell
cd frontend
npm start
```

## Run Tests

Windows:

- `scripts/test-backend.ps1`
- `scripts/test-frontend.ps1`

Unix/macOS:

- `scripts/test-backend.sh`
- `scripts/test-frontend.sh`

If you prefer direct commands:

```powershell
cd backend
.\mvnw.cmd test
```

```powershell
cd frontend
CI=true npm test -- --watchAll=false
```

## Demo Flow

1. Open the frontend in the browser.
2. Sign up a new user.
3. If MFA is enabled, scan the QR code in an authenticator app.
4. Log in with username and password.
5. Enter the 6-digit authenticator code if prompted.
6. Confirm the protected profile page loads.
7. Log out and verify the session is cleared.

## Notes

- The backend and frontend are configured to use local defaults, so the project should run out of the box after dependencies are installed.
- If you change the backend port, update `REACT_APP_API_BASE_URL` in `frontend/.env`.
- The backend secret should be changed for any real deployment.
