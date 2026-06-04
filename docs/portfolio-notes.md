# Portfolio Notes

Use this file as a quick source for GitHub, CV, and interview wording.

## GitHub Repository Description

Full-stack Spring Boot and React demo with optional TOTP MFA, QR enrollment, recovery codes, JWT `httpOnly` cookie sessions, CSRF protection, and rate limiting.

## CV Bullet

Built a full-stack authentication demo with Spring Boot, React, MongoDB, optional TOTP-based MFA, QR enrollment, recovery codes, JWT cookie sessions, CSRF protection, and automated backend/frontend tests.

## Interview Talking Points

- Designed a stateless browser auth flow using signed JWTs stored in `httpOnly` cookies.
- Added CSRF protection because cookie-backed auth is automatically sent by the browser.
- Implemented optional MFA with TOTP QR enrollment and one-time recovery codes.
- Split backend responsibilities across focused authentication, registration, MFA, user lookup, token, and recovery-code services.
- Covered backend behavior with unit, controller slice, and lightweight integration tests without Docker.
- Covered frontend auth screens and API utilities with Jest and React Testing Library.
