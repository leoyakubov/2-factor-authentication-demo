# frontend

Vite-powered React client for the two-factor authentication demo.

Default local URL:

- `http://localhost:3000`

## Local Setup

1. Copy [`.env.example`](./.env.example) to `.env` if you want to override the backend URL.
2. Set `VITE_API_BASE_URL` to the backend URL you want to use.
3. Run `npm run dev` or use the helper script from the repo root.

The default backend URL is `http://localhost:8081`.

## Helper Scripts

- Use `scripts/run-frontend.ps1` or `scripts/run-frontend.sh` to start the app
- Use `scripts/test-frontend.ps1` or `scripts/test-frontend.sh` to run tests
- Use `scripts/build-frontend.ps1` or `scripts/build-frontend.sh` to create a production build

## Available npm Scripts

- `npm run dev`
- `npm start`
- `npm test`
- `npm run test:ci`
- `npm run build`
- `npm run build:ci`
- `npm run preview`

## Notes

- The frontend talks to the backend through `VITE_API_BASE_URL`
- If you change the backend port, update the frontend `.env` file too
- Vite writes production output to `dist/`, not `build/`
