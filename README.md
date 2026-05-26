# spring-boot-two-factor-authentication

Monorepo layout:

- `backend`: Spring Boot API and authentication flow
- `frontend`: React client for signup, signin, QR enrollment, and OTP verification

## Run locally

- Start MongoDB on the backend port configured in `backend/src/main/resources/application.yml`
- Run the backend from `backend`
- Run the frontend from `frontend`
