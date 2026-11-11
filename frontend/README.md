# DecisionHub frontend

DecisionHub's frontend is a React 19 application built with Vite. It includes
authentication screens, route protection, role-aware navigation, a dashboard
and profile skeleton, and an Axios client for the Spring Boot API.

## Run locally

```sh
npm install
cp .env.example .env
npm run dev
```

On Windows PowerShell, copy the example with:

```powershell
Copy-Item .env.example .env
```

By default, the app uses the backend at `http://localhost:8080/api`. Set
`VITE_API_BASE_URL` in `.env` to the backend's actual API base URL if it differs.
Restart Vite after changing environment values.

For frontend-only exploration, set `VITE_AUTH_MODE=demo`. The UI labels this
mode and clearly reports that login/registration are simulated and no password
reset email is sent. Demo mode is not authentication and must not be used for
production. Leave this setting empty or unset for real API requests. API errors
are shown in the forms; they are not treated as successful authentication.

## Backend contract expected by the current client

The backend team should confirm these paths and response shapes before
integration. The current defaults are:

| Operation | Request | Expected response |
|---|---|---|
| `POST /auth/login` | `{ "email": "...", "password": "..." }` | `{ "token": "...", "user": { "id": "...", "name": "...", "email": "...", "role": "USER" } }` |
| `POST /auth/register` | `{ "name": "...", "email": "...", "password": "..." }` | Same token/user shape as login |
| `GET /auth/me` | Authenticated request | Current user and roles |
| `POST /auth/forgot-password` | `{ "email": "..." }` | Generic `{ "message": "..." }`; requires SMTP configuration |
| `POST /auth/reset-password` | `{ "token": "...", "newPassword": "..." }` | `{ "message": "..." }` |
| `POST /auth/logout` | Authenticated request | Clears the HttpOnly auth cookie |

The client also accepts `accessToken` instead of `token`, and `roles` instead
of a single `role`. Role names `USER`, `COMMUNITY_MODERATOR`, and
`ADMINISTRATOR` are normalized for navigation. Backend error responses may
provide a `message`, `error`, or `errors` field.

JWTs are kept in memory for the `Authorization: Bearer` header and also issued
as an `HttpOnly`, `SameSite=Lax` cookie so the app can restore login after a
refresh without exposing the cookie to JavaScript. They are never written to
local or session storage. Cookie-authenticated write requests also use a
separate CSRF token cookie/header. Frontend route guards are only a UI
convenience; the backend must enforce authentication and permissions on every
protected endpoint.

Google sign-in is optional. Configure the backend's `oauth` profile and set
`VITE_GOOGLE_OAUTH_URL` to
`http://localhost:8080/api/oauth2/authorization/google` to show the sign-in
button. The OAuth flow returns an HttpOnly cookie and the callback verifies it
through `GET /auth/me`.

## Project layout

- `src/components/`: reusable form fields, buttons, navigation, and route guard
- `src/context/`: in-memory authentication state
- `src/layouts/`: shared signed-in application shell
- `src/pages/`: auth, dashboard, profile, and role placeholder screens
- `src/services/`: Axios client and authentication API calls
- `src/utils/`: form validation and role normalization

## Verify

```sh
npm run lint
npm run build
```

The dashboard, profile, and role-specific navigation are foundation screens.
Decision-management, voting, discussions, notifications, and analytics belong
to later milestones.
