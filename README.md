# DecisionHub

DecisionHub is a React + Spring Boot foundation for collaborative decision
boards and community polling.

## Milestone 1 setup

Prerequisites: Java 21, Docker Desktop, Node.js, and npm.

1. Start PostgreSQL from the repository root:

   ```powershell
   docker compose up -d postgres
   ```

2. Set a strong base64 JWT signing key in PowerShell (do not commit it):

   ```powershell
   cd backend
   Copy-Item .env.example .env
   $bytes = New-Object byte[] 32
   $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
   $rng.GetBytes($bytes)
   $secret = [Convert]::ToBase64String($bytes)
   (Get-Content .env) -replace '^JWT_SECRET=.*$', "JWT_SECRET=$secret" | Set-Content .env
   ```

3. Start the backend:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   The API is served at `http://localhost:8080/api`. Flyway creates the initial
   PostgreSQL schema on startup.

4. In a second terminal, start the frontend:

   ```powershell
   cd frontend
   Copy-Item .env.example .env
   npm install
   npm run dev
   ```

   Visit `http://localhost:5173`. Demo mode is opt-in only (`VITE_AUTH_MODE=demo`);
   otherwise authentication requests go to the backend and API errors are shown.

## Authentication and OAuth2

The backend provides registration, login, current-user, logout, and
password-reset endpoints. Passwords are BCrypt-hashed. JWTs are returned for
the frontend's in-memory bearer header and set as HttpOnly cookies for safe
session restoration. Only `USER` can be self-assigned at registration; higher
roles must be granted by an administrator. Frontend role guards do not replace
backend authorization.

Google OAuth2 is optional. Configure the OAuth client in Google Cloud with
redirect URI `http://localhost:8080/api/login/oauth2/code/google`, then set
`GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, and `SPRING_PROFILES_ACTIVE=oauth`.
Set `VITE_GOOGLE_OAUTH_URL=http://localhost:8080/api/oauth2/authorization/google`
in `frontend/.env` to show its button. Use secure cookies and HTTPS in production.

Password reset delivery requires SMTP. Set `MAIL_HOST`, `MAIL_PORT`,
`MAIL_USERNAME`, `MAIL_PASSWORD`, and `MAIL_FROM`; without mail configuration,
the backend returns an explicit service-unavailable error instead of claiming
an email was sent.

## Verification

```powershell
cd backend
.\mvnw.cmd test
cd ..\frontend
npm run lint
npm run build
```

Milestone 1 establishes the app skeleton, auth flow, role model, and initial
database schema. Full decision, voting, discussion, notification, analytics,
and report workflows are later milestone work.
