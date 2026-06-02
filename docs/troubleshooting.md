# Troubleshooting

- If `8081` is already in use, stop the old backend process and start it again
- If you hit `/login`, `/signup`, `/verify`, or `/qrcode` on the backend port by mistake, the backend now redirects you to the frontend instead of returning a noisy 404
- If `/me` returns `401`, sign out, clear the browser site data or cookies once, and sign in again
- If frontend tests complain about missing packages, run `cd frontend && npm install`
- If the frontend build fails on warnings, run the test/build scripts from the repo root so the environment is set up consistently
- If the authenticator code fails, make sure the QR code was scanned into the authenticator app for the same user account

