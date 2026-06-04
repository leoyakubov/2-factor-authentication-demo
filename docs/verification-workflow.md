# Full Verification Workflow

Use this checklist to verify the full project from a clean machine.

## 1. Check prerequisites

Make sure these are installed:

- Java 21 or newer
- Node.js
- npm

You can verify them with:

```sh
java -version
node -v
npm -v
```

You can also use the root command shortcuts:

```sh
npm run backend:verify
npm run frontend:verify
npm run verify
```

## 2. Prepare environment files

The helper scripts create `.env` files from the example files if they are missing, but it is still a good idea to inspect them first.

Backend:

- `backend/.env.example`
- `backend/.env`

Frontend:

- `frontend/.env.example`
- `frontend/.env`

If you want to create them manually:

```sh
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
```

Set `JWT_SECRET` in `backend/.env` to a long random value with at least 32 characters.

## 3. Install frontend dependencies

```sh
cd frontend
npm ci
cd ..
```

The helper scripts install dependencies automatically only when `node_modules` is missing. If dependencies were installed in a different shell or platform, delete `frontend/node_modules`, then run `npm ci` from the same environment you plan to use.

Important for Windows + WSL:

- `node_modules` contains native packages and should not be shared between Windows/Git Bash and WSL.
- If the repo is under `/mnt/c/...`, WSL can hit `EIO` when replacing Windows-native binaries.
- The most reliable WSL setup is to keep the repo inside the WSL filesystem, for example `~/projects/2-factor-authentication-demo`.

## 4. Run backend verification

- `scripts/backend-verify.sh`

On Windows, run it from Git Bash or WSL.

Expected result:

- Maven finishes with `BUILD SUCCESS`
- The Spring context test passes
- Failsafe runs the integration tests as part of the verify phase

If you prefer a direct command:

```sh
cd backend
bash ./mvnw verify
```

## 5. Run frontend verification

- `scripts/frontend-verify.sh`

On Windows, run it from Git Bash or WSL.

Expected result:

- Jest runs once and exits
- ESLint checks frontend source and test support files
- Vite creates a production `dist` folder
- The current frontend tests pass

If you prefer a direct command:

```sh
cd frontend
npm run verify
```

## 6. Start the backend

Open a new terminal window and run:

- `scripts/backend-run.sh`

On Windows, run it from Git Bash or WSL.

Expected result:

- Spring Boot starts successfully
- The API listens on `http://localhost:8081`
- Embedded Mongo starts automatically for the demo

If you prefer a direct command:

```sh
cd backend
bash ./mvnw spring-boot:run
```

## 7. Start the frontend

Open another terminal window and run:

- `scripts/frontend-run.sh`

On Windows, run it from Git Bash or WSL.

Expected result:

- React starts successfully
- The app opens on `http://localhost:3000`

If you prefer a direct command:

```sh
cd frontend
npm run dev
```

## 8. Smoke test the UI flow

1. Open the app in the browser.
2. Go to the signup screen.
3. Create a new user.
4. If MFA is enabled, stay on the signup screen and confirm the success message and QR code are shown.
5. Click the `Login` button to open the login form.
6. Sign in with the account you just created.
7. If MFA is enabled, enter the 6-digit code from your authenticator app.
8. Confirm the profile page loads and shows the avatar and logout button.
9. Click `Logout` and confirm you are returned to the login screen.

## 9. Verify error handling

Try a few failure cases on purpose:

- Leave a required field blank and submit
- Enter a wrong password
- Try a login for a user that does not exist
- Enter an invalid MFA code, if MFA is enabled

Expected result:

- The UI shows a user-friendly error message
- The backend logs include a helpful message for the failure

## 10. Optional clean restart check

If you want to verify the app starts cleanly from scratch:

1. Stop both frontend and backend terminals
2. Close any browser tab that is still open to the app
3. Start the backend again
4. Start the frontend again
5. Repeat the smoke test

This is a good final check before a demo or commit.
