# Security Guide

## Limitations

- This is a demo project, not a production security implementation
- There is no password reset flow
- There is no self-service email recovery flow
- The demo uses embedded MongoDB for local development
- The JWT secret is loaded from local environment configuration and should be rotated and managed securely in a real deployment
- The app does not enforce HTTPS locally
- Rate limiting is in-memory and resets when the backend restarts

## Security Concerns / Risks

This repo is a demo, so it intentionally keeps some things simple that would need extra hardening in a real product.

- Cookie-backed JWTs are safer than storing tokens in `localStorage`, but the app should still be reviewed for XSS risks because injected script can still act on the page. Possible solution: keep strict output escaping, avoid unsafe HTML, and keep the Content Security Policy tight.
- The MFA secret is stored on the backend for the demo flow, which is fine for learning but should be handled carefully in a production design. Possible solution: encrypt the secret at rest, restrict access to it, and only expose the QR code during enrollment.
- The JWT secret lives in local environment configuration and must be rotated and protected in real deployments. Possible solution: load it from a secret manager or vault, rotate it periodically, and avoid committing it anywhere in the repo.
- The backend redirects accidental SPA route hits like `/login` to the frontend for convenience, but that redirect should not be treated as an authorization boundary. Possible solution: keep authorization checks in Spring Security and controller annotations, and treat redirects as a UX fallback only.
- HTTPS is not enforced locally, so cookie and credential handling should be revisited before any deployment outside a trusted dev environment. Possible solution: terminate TLS in front of the app, set secure cookies, and only enable HTTP for local development.

## Possible Improvements

The next useful hardening steps are:

1. Tighten secrets and storage
   - document secret manager / vault usage
   - move away from demo-only storage assumptions
2. Strengthen transport and runtime security
   - document HTTPS for non-local use
   - keep secure cookie defaults
   - treat SPA redirects as UX only, not security
   - add CSP guidance
3. Add a more complete account recovery story
   - password reset flow
   - optional recovery-email workflow
   - stronger admin recovery path for lost MFA access
