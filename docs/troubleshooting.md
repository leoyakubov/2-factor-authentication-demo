# Troubleshooting

- If `8081` is already in use, stop the old backend process and start it again
- If you hit `/login`, `/signup`, `/verify`, or `/qrcode` on the backend port by mistake, the backend now redirects you to the frontend instead of returning a noisy 404
- If `/me` returns `401`, sign out, clear the browser site data or cookies once, and sign in again
- If frontend tests complain about missing packages, run `cd frontend && npm ci` from the same shell/environment you plan to use for the app
- If the frontend build fails on warnings, run the test/build scripts from the repo root so the environment is set up consistently
- If frontend dev, test, or build scripts complain about a missing `rolldown` native binding, `node_modules` was probably installed for another platform. Delete `frontend/node_modules`, then run `cd frontend && npm ci` in the same shell/environment you plan to use.
- If WSL on `/mnt/c/...` fails with `EIO` while deleting Windows-native packages, delete `frontend/node_modules` from Windows File Explorer or PowerShell, then rerun `npm ci` from WSL. The cleaner long-term option is to keep the repo inside the WSL filesystem, for example under `~/projects`.
- If the authenticator code fails, make sure the QR code was scanned into the authenticator app for the same user account
- If WSL reports `libcrypto.so.1.1` or embedded Mongo fails to start, make sure you are using the latest embedded Mongo version from `backend/src/main/resources/application.yml` and that your WSL distro is up to date
