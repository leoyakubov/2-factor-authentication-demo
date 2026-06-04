# Demo Script

Use this flow for a GitHub walkthrough, interview demo, or quick local smoke test.

## Before The Demo

1. Run `./scripts/backend-verify.sh`.
2. Run `./scripts/frontend-verify.sh`.
3. Start the backend with `./scripts/backend-run.sh`.
4. Start the frontend with `./scripts/frontend-run.sh`.
5. Open `http://localhost:3000`.

## Flow 1: Signup Without MFA

1. Open the signup page.
2. Create a user without MFA.
3. Confirm the success message appears.
4. Click `Login`.
5. Sign in with the new account.
6. Confirm the protected profile page loads.
7. Click `Logout`.

## Flow 2: Signup With MFA

1. Open the signup page.
2. Create a different user with MFA enabled.
3. Confirm the QR code and recovery codes are shown.
4. Scan the QR code with an authenticator app.
5. Click `Login`.
6. Sign in with username/email and password.
7. Enter the current authenticator code.
8. Confirm the protected profile page loads.
9. Click `Logout`.

## Flow 3: Recovery Code

1. Sign in with the MFA-enabled account.
2. On the verification screen, enter one unused recovery code instead of an authenticator code.
3. Confirm the profile page loads.
4. Try the same recovery code again later and confirm it no longer works.

## Flow 4: Error Handling

1. Try signing in with a wrong password.
2. Confirm the UI shows a friendly message.
3. Try an invalid MFA code.
4. Confirm the UI shows a friendly message.
5. Check backend logs for request-level messages with request IDs.
