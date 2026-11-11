# DecisionHub frontend

This repository contains the DecisionHub frontend, built with React and Vite.
Backend services are owned and maintained by the backend team.

## Run locally

Prerequisites: Node.js and npm.

```powershell
cd frontend
npm install
Copy-Item .env.example .env
npm run dev
```

By default, the frontend sends API requests to `http://localhost:8080/api`.
Set `VITE_API_BASE_URL` in `frontend/.env` to the backend team's API URL.
Authentication uses the API contract documented in [frontend/README.md](./frontend/README.md).

For frontend-only UI exploration, set `VITE_AUTH_MODE=demo` in `.env`. Demo
mode is clearly identified in the UI and is not real authentication.

## Validate

```powershell
cd frontend
npm run lint
npm run build
```

This repository covers the frontend foundation for Milestone 1. Coordinate
backend endpoints, JWT/session behavior, and OAuth provider credentials with
the backend team.
