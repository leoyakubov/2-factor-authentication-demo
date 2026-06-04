# Architecture

This project uses a small full-stack architecture where the frontend owns the user journey and the backend owns security-sensitive behavior.

## Runtime Overview

```mermaid
flowchart LR
    User["User"] --> Browser["Browser"]
    Browser --> Frontend["React + Vite"]
    Frontend -->|GET /csrf| Backend["Spring Boot API"]
    Frontend -->|POST /users| Registration["Registration flow"]
    Frontend -->|POST /signin| Authentication["Authentication flow"]
    Frontend -->|POST /verify| MFA["MFA verification flow"]
    Frontend -->|GET /users/me| Profile["Protected profile API"]
    Registration --> Backend
    Authentication --> Backend
    MFA --> Backend
    Profile --> Backend
    Backend --> Mongo["Embedded MongoDB local demo"]
    Backend --> Cookies["httpOnly JWT cookie"]
    Backend --> CSRF["CSRF token"]
    Backend --> RateLimit["In-memory rate limiting"]
```

## Signup With MFA

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
    Backend->>Mongo: Store user, hashed password, MFA secret, hashed recovery codes
    Backend-->>Frontend: Signup response with QR image and recovery codes
    User->>Authenticator: Scan QR code
```

## Login With MFA

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant Backend
    participant Browser

    User->>Frontend: Submit username/email and password
    Frontend->>Backend: POST /signin
    Backend->>Backend: Verify credentials
    Backend-->>Frontend: MFA required
    User->>Frontend: Submit authenticator or recovery code
    Frontend->>Backend: POST /verify
    Backend->>Backend: Verify code
    Backend-->>Browser: Set-Cookie: httpOnly JWT
    Frontend->>Backend: GET /users/me
    Backend-->>Frontend: Protected profile
```

## Security Boundaries

- The browser stores the JWT in an `httpOnly` cookie, so JavaScript cannot read the token directly.
- CSRF protection is required because cookie-backed credentials are sent automatically by the browser.
- The backend validates credentials, MFA codes, recovery codes, JWT signatures, and profile access.
- Frontend routes are UX boundaries only; backend routes remain the real authorization boundary.
