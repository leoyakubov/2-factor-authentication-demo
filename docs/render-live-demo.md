# Render Live Demo Setup

This guide sets up the public demo on Render while keeping embedded MongoDB in the backend.

## What you will create

- A Render web service for the backend
- A Render static site for the frontend
- GitHub Actions verification on every push and pull request

## Before you start

1. Push the repository to GitHub.
2. Make sure the backend verify workflow exists at [`.github/workflows/verify.yml`](../.github/workflows/verify.yml).
3. Make sure `infra/render.yaml` is present in the repo.

## Step 1: Create the Render services

1. Open Render.
2. Create a new Blueprint / deploy from GitHub repo.
3. Select this repository.
4. Render will read [`infra/render.yaml`](../infra/render.yaml) and create both services.

## Step 2: Configure the backend service

Set these environment variables on the backend service:

- `JWT_SECRET`
- `MFA_SECRET_ENCRYPTION_KEY`
- `FRONTEND_ORIGIN`
- `BACKEND_ORIGIN`

Use the Render URLs for the frontend and backend services.

Notes:

- The backend reads `PORT` automatically on Render.
- The backend still uses embedded MongoDB, so demo data can reset after restarts. That is fine for this demo.

## Step 3: Configure the frontend service

Set this frontend environment variable:

- `VITE_API_BASE_URL`

Suggested value:

- `VITE_API_BASE_URL` = backend Render URL

Important:

- The frontend CSP is built from `VITE_API_BASE_URL`, so set it before redeploying the static site.

## Step 4: Verify the deployment

1. Wait for the backend service to finish building.
2. Wait for the frontend static site to finish building.
3. Open the frontend Render URL.
4. Create a user.
5. Log in.
6. Try MFA if enabled.
7. Open the profile page.
8. Log out.

## Step 5: Keep CI in place

GitHub Actions should keep running on every push and pull request so the repo stays healthy.

Recommended checks:

- backend verify
- frontend verify

## Step 6: Enable GitHub Actions deploy and smoke checks

Add these repository secrets or variables:

- `RENDER_BACKEND_DEPLOY_HOOK_URL`
- `RENDER_FRONTEND_DEPLOY_HOOK_URL`
- `RENDER_BACKEND_URL`
- `RENDER_FRONTEND_URL`

What the workflows do:

- `verify.yml` runs tests and build checks
- `deploy-backend.yml` triggers the backend Render deploy hook
- `deploy-frontend.yml` triggers the frontend Render deploy hook
- `smoke-backend.yml` checks the backend `/csrf` endpoint
- `smoke-frontend.yml` checks the frontend page loads
- `ping-render.yml` pings the backend `/csrf` endpoint every 5 minutes

## Troubleshooting

- If the frontend cannot reach the backend, confirm `VITE_API_BASE_URL` points to the backend Render URL.
- If the backend returns CORS errors, confirm `FRONTEND_ORIGIN` matches the frontend Render URL exactly.
- If Render redeploys reset the data, that is expected with embedded MongoDB.
