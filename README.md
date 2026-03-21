# MuscleForger API

REST API backend for [MuscleForger](https://muscleforger.netlify.app) — a fitness web app that lets users browse exercises, build workout plans, and track their training.

Built with **Spring Boot 4** · **PostgreSQL** · **JWT authentication** · **Flyway migrations**

---

## Features

- **Auth** — Register, login, token refresh (JWT access + refresh tokens)
- **Exercise browsing** — Search and filter exercises by body part, target muscle, or equipment via the ExerciseDB API
- **Favorites** — Save exercises to a personal favorites list
- **Workout queue** — Build a quick workout session from any exercises
- **Recently viewed** — Automatically tracks the last exercises a user viewed
- **Workout templates** — Create multi-day weekly training plans with structured exercises per day
- **Custom exercises** — Create your own exercises and use them inside templates
- **YouTube integration** — Fetch tutorial videos for any exercise

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.3 |
| Security | Spring Security + JWT (stateless) |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Migrations | Flyway |
| Build | Maven |
| External APIs | ExerciseDB (RapidAPI), YouTube Search (RapidAPI) |

---

## Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 15+ (local or hosted)
- A [RapidAPI](https://rapidapi.com) account with subscriptions to:
  - [ExerciseDB](https://rapidapi.com/justin-WFnsXH_t6/api/exercisedb)
  - [YouTube Search and Download](https://rapidapi.com/h0p3rwe/api/youtube-search-and-download)

---

## Local Setup

### 1. Clone the repo

```bash
git clone https://github.com/your-username/muscleforger-api.git
cd muscleforger-api
```

### 2. Create the database

```sql
CREATE DATABASE muscleforger;
```

### 3. Configure environment variables

The app reads secrets from environment variables. Set the following in your environment, IDE run config, or a `.env` file (never commit this):

| Variable | Description |
|---|---|
| `DB_URL` | JDBC URL — e.g. `jdbc:postgresql://localhost:5432/muscleforger` |
| `DB_USERNAME` | PostgreSQL username — e.g. `postgres` |
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | Any long random string (256-bit+) used to sign tokens |
| `RAPIDAPI_KEY` | Your RapidAPI key |

> **Tip:** In IntelliJ, add these under **Run > Edit Configurations > Environment variables**.

### 4. Run the application

```bash
./mvnw spring-boot:run
```

Flyway will automatically create all tables on first run. The API will be available at `http://localhost:8080`.

---

## API Overview

All `/api/user/**` and `/api/user/templates/**` endpoints require a `Authorization: Bearer <token>` header.

### Auth — `/api/auth`

| Method | Path | Description |
|---|---|---|
| POST | `/register` | Create a new account |
| POST | `/login` | Login and receive access + refresh tokens |
| POST | `/refresh` | Exchange a refresh token for a new access token |
| POST | `/logout` | Logout (stateless — client discards the token) |
| GET | `/me` | Get the currently authenticated user |

### Exercises — `/api/exercises`

| Method | Path | Description |
|---|---|---|
| GET | `/` | List exercises (default limit: 100) |
| GET | `/search?q=` | Search exercises by name (includes custom exercises if authenticated) |
| GET | `/body-parts` | List all body parts |
| GET | `/body-part/{bodyPart}` | Filter by body part |
| GET | `/target/{target}` | Filter by target muscle |
| GET | `/equipment/{equipment}` | Filter by equipment |
| GET | `/{id}` | Get a single exercise by ID |

### User Data — `/api/user`

| Method | Path | Description |
|---|---|---|
| GET | `/favorites` | Get favorited exercises |
| POST | `/favorites` | Add an exercise to favorites |
| DELETE | `/favorites/{exerciseId}` | Remove from favorites |
| GET | `/workout` | Get the current workout queue |
| POST | `/workout` | Add an exercise to the workout queue |
| DELETE | `/workout/{exerciseId}` | Remove from workout queue |
| DELETE | `/workout` | Clear the entire workout queue |
| GET | `/history` | Get recently viewed exercises |
| POST | `/history` | Log an exercise view |

### Workout Templates — `/api/user/templates`

| Method | Path | Description |
|---|---|---|
| GET | `/` | List all templates |
| POST | `/` | Create a new template |
| GET | `/{templateId}` | Get template with all days and exercises |
| PATCH | `/{templateId}` | Rename a template |
| DELETE | `/{templateId}` | Delete a template |
| PATCH | `/{templateId}/days/{dayId}` | Update a day's label |
| POST | `/{templateId}/days/{dayId}/exercises` | Add an exercise to a day |
| PATCH | `/{templateId}/days/{dayId}/exercises/{exerciseId}` | Update sets / reps / rpe / notes |
| DELETE | `/{templateId}/days/{dayId}/exercises/{exerciseId}` | Remove an exercise from a day |
| POST | `/{templateId}/days/{dayId}/exercises/reorder` | Reorder exercises within a day |

### Custom Exercises — `/api/user/custom-exercises`

| Method | Path | Description |
|---|---|---|
| GET | `/` | List all custom exercises |
| POST | `/` | Create a custom exercise |
| PUT | `/{id}` | Update a custom exercise |
| DELETE | `/{id}` | Delete a custom exercise |

### Videos — `/api/videos`

| Method | Path | Description |
|---|---|---|
| GET | `/?q=` | Search YouTube for exercise tutorial videos |

---

## Deployment

The app ships with a `Dockerfile` for containerized deployment (e.g. [Render](https://render.com)).

### Required environment variables on the host

Same as local setup: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `RAPIDAPI_KEY`.

For hosted PostgreSQL (e.g. Supabase), use the **Session Pooler** connection string and append `?sslmode=require` to `DB_URL`.

### CORS

Allowed origins are configured in `SecurityConfig.java`. By default this includes `http://localhost:5173` and `https://muscleforger.netlify.app`. Update this list if you're hosting the frontend elsewhere.

---

## Project Structure

```
src/main/java/com/muscleforger/api/
├── config/          # Spring Security + app beans
├── controller/      # REST controllers
├── dto/             # Request/response records
│   ├── auth/
│   ├── template/
│   └── user/
├── entity/          # JPA entities
├── repository/      # Spring Data repositories
├── security/        # JWT filter, UserDetailsService
└── service/         # Business logic
src/main/resources/
├── application.yaml
└── db/migration/    # Flyway SQL migrations (V1–V5)
```
