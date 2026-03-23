# New Features — Backend API Requirements

This document describes the **Settings** and **Progress** features built on the frontend with mock/localStorage data. The backend needs to provide real API endpoints and database tables so these features persist per-user.

All endpoints require JWT authentication via `Authorization: Bearer <token>` header. All request/response bodies are JSON.

---

## 1. Settings Page

### 1.1 User Profile

**What the frontend does:** Displays and allows editing of username, email, and profile picture (avatar). Avatar is currently uploaded as a file and previewed client-side only.

**Endpoints needed:**

| Method | Path | Body / Params | Response | Notes |
|--------|------|---------------|----------|-------|
| `GET` | `/api/users/me` | — | `{ id, username, email, avatarUrl, createdAt }` | Returns the authenticated user's profile |
| `PUT` | `/api/users/me` | `{ username?, email? }` | Updated user object | Partial update — only send changed fields |
| `POST` | `/api/users/me/avatar` | `multipart/form-data` with `file` field | `{ avatarUrl }` | Upload profile picture. Store in S3/cloud storage or as a blob. Return the public URL |
| `DELETE` | `/api/users/me/avatar` | — | `204` | Remove profile picture |

**Database: `users` table (extend existing)**
```
avatarUrl   VARCHAR(500)  nullable
```

### 1.2 Password Change

**What the frontend does:** Form with current password, new password, confirm new password. UI-only right now.

**Endpoint needed:**

| Method | Path | Body | Response | Notes |
|--------|------|------|----------|-------|
| `PUT` | `/api/users/me/password` | `{ currentPassword, newPassword }` | `200` or `400` with error | Validate current password server-side. Enforce password policy (min 8 chars, etc.) |

### 1.3 Delete Account

**What the frontend does:** "Danger Zone" section with a delete account button. UI-only right now.

**Endpoint needed:**

| Method | Path | Body | Response | Notes |
|--------|------|------|----------|-------|
| `DELETE` | `/api/users/me` | `{ password }` (confirmation) | `204` | Cascade delete all user data (weight logs, photos, plans, favorites, etc.) |

### 1.4 User Preferences

**What the frontend does:** Theme (dark/light) and weight unit (lbs/kg) are stored in `localStorage` (`mf_theme`, `mf_weight_unit`). These work fine locally but don't sync across devices.

**Endpoint needed (optional / low priority):**

| Method | Path | Body | Response | Notes |
|--------|------|------|----------|-------|
| `GET` | `/api/users/me/preferences` | — | `{ theme, weightUnit }` | |
| `PUT` | `/api/users/me/preferences` | `{ theme?, weightUnit? }` | Updated preferences | Partial update |

**Database: `user_preferences` table (or columns on `users`)**
```
theme        VARCHAR(10)  default 'dark'     -- 'dark' or 'light'
weight_unit  VARCHAR(5)   default 'lbs'      -- 'lbs' or 'kg'
```

> Frontend will continue using localStorage as a fast cache. On login, fetch preferences from the API and sync to localStorage. On change, write to both localStorage (immediate) and API (background).

---

## 2. Progress Page — Bodyweight Tracking

### 2.1 Weight Logs

**What the frontend does:**
- Log weight entries with date and weight (stored in lbs internally; displayed as lbs or kg based on user preference)
- Display stats: current weight, change over time range, average, highest, lowest, first/last log, total count
- Recharts area chart with time range filters (1M, 3M, 6M, 1Y, 2Y, All, custom months)
- Log history grouped by Year → Month, collapsible sections
- Delete individual entries with confirmation dialog

**Currently stored in:** `localStorage` key `mf_bodyweight` as a JSON array

**Data shape per entry:**
```json
{
  "id": 1,
  "weight": 182.5,
  "date": "2026-03-22",
  "unit": "lbs"
}
```

> The `unit` field is always `"lbs"` — all weights are stored in lbs internally. The frontend converts to kg for display only. The backend should also store in lbs.

**Endpoints needed:**

| Method | Path | Body / Params | Response | Notes |
|--------|------|---------------|----------|-------|
| `GET` | `/api/weight-logs` | Query: `?from=2025-01-01&to=2026-03-22` (optional) | `[ { id, weight, date } ]` sorted by date ASC | If no query params, return all. Used for chart + history |
| `POST` | `/api/weight-logs` | `{ weight, date }` | Created entry with `id` | Weight in lbs. Date as `YYYY-MM-DD` string. Reject duplicates on same date (or allow — your call) |
| `DELETE` | `/api/weight-logs/{id}` | — | `204` | |

**Database: `weight_logs` table**
```sql
CREATE TABLE weight_logs (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    weight      DECIMAL(5,1) NOT NULL,  -- in lbs
    date        DATE NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW(),

    UNIQUE(user_id, date)  -- optional: one entry per day per user
);

CREATE INDEX idx_weight_logs_user_date ON weight_logs(user_id, date);
```

### 2.2 Progress Photos

**What the frontend does:**
- Upload progress photo **entries** — each entry has a **date**, optional **caption**, and **one or more photos** (like an Instagram post)
- Photo gallery grouped by Year → Month, collapsible sections
- Instagram-style carousel viewer (swipe on mobile, arrows on desktop) with dot indicators
- "Show Weight" button on each photo entry that finds the closest weight log to that date
- Delete individual photos from an entry, or delete the entire entry
- Upload supports selecting multiple images at once

**Currently stored in:** `localStorage` key `mf_progress_photos` as a JSON array. Photos are stored as base64 data URLs (this will NOT scale — backend must use file storage).

**Data shape per entry:**
```json
{
  "id": 900001,
  "date": "2026-03-15",
  "caption": "Looking lean after cut",
  "photos": ["data:image/jpeg;base64,...", "data:image/jpeg;base64,..."]
}
```

**Endpoints needed:**

| Method | Path | Body / Params | Response | Notes |
|--------|------|---------------|----------|-------|
| `GET` | `/api/progress-photos` | Query: `?year=2026` (optional) | `[ { id, date, caption, photos: [{ id, url }] } ]` sorted by date DESC | Each entry includes an array of photo objects with URLs |
| `POST` | `/api/progress-photos` | `multipart/form-data`: `date`, `caption`, `files[]` (multiple images) | Created entry with `id` and photo URLs | Store images in S3/cloud. Return the full entry |
| `POST` | `/api/progress-photos/{entryId}/photos` | `multipart/form-data`: `files[]` | Updated entry | Add more photos to an existing entry |
| `PUT` | `/api/progress-photos/{entryId}` | `{ caption }` | Updated entry | Edit caption only |
| `DELETE` | `/api/progress-photos/{entryId}` | — | `204` | Delete entire entry + all its photos from storage |
| `DELETE` | `/api/progress-photos/{entryId}/photos/{photoId}` | — | `204` or updated entry | Remove a single photo. If last photo in entry, delete the entry too |

**Database:**

```sql
CREATE TABLE progress_entries (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date        DATE NOT NULL,
    caption     TEXT DEFAULT '',
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE progress_photos (
    id          BIGSERIAL PRIMARY KEY,
    entry_id    BIGINT NOT NULL REFERENCES progress_entries(id) ON DELETE CASCADE,
    url         VARCHAR(500) NOT NULL,  -- S3/cloud storage URL
    sort_order  INT DEFAULT 0,          -- preserve upload order
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_progress_entries_user_date ON progress_entries(user_id, date DESC);
```

---

## 3. Demo / Mock Data Mode

**What the frontend does:**
The Progress page has a "Preview with Sample Data" button that lets users (especially new ones with no data) see what the page looks like when populated. When demo mode is active:

- The page shows hardcoded sample weight logs (~18 months of realistic entries) and sample photo entries
- The user's real data is **not modified or overwritten** — demo data is a completely separate, read-only overlay
- A visible banner indicates demo mode is active, with a "Remove Sample Data" button to return to real data
- All interactive features (chart range filtering, collapsing year/month groups, photo carousel) work with demo data
- Logging new weights or uploading photos is disabled while demo mode is active

**This is a frontend-only feature.** No backend changes needed. The demo data is hardcoded in the frontend JavaScript bundle. The backend does not need to know about demo mode.

**Why it exists:** New users land on an empty Progress page and have no idea what it could look like. Rather than forcing them to manually enter data first, the demo mode gives them an instant, interactive preview of the full experience — charts, stats, photo galleries, and all.

---

## 4. Existing Endpoints the Frontend Already Calls

For reference, these endpoints already exist or are planned in the backend. The new features above are **in addition to** these:

### Auth
- `POST /api/auth/login` — `{ username, password }` → `{ token, user }`
- `POST /api/auth/register` — `{ username, email, password }` → `{ token, user }`

### Exercises (proxied from ExerciseDB via backend)
- `GET /api/exercises?limit=N`
- `GET /api/exercises/bodyPartList`
- `GET /api/exercises/bodyPart/{bodyPart}?limit=N`
- `GET /api/exercises/target/{target}`
- `GET /api/exercises/equipment/{equipment}`
- `GET /api/exercises/exercise/{id}`

### YouTube (proxied via backend)
- `GET /api/youtube/search?query=...`

### Favorites
- `GET /api/favorites`
- `POST /api/favorites` — `{ exerciseId }`
- `DELETE /api/favorites/{exerciseId}`

### Workout Plans / Templates
- `GET /api/templates`
- `POST /api/templates`
- `PUT /api/templates/{id}`
- `DELETE /api/templates/{id}`

### Custom Exercises
- `GET /api/custom-exercises`
- `POST /api/custom-exercises`

---

## 5. Summary of New Database Tables

```
users (extend)
├── avatar_url VARCHAR(500)
│
├── user_preferences (or columns on users)
│   ├── theme VARCHAR(10)
│   └── weight_unit VARCHAR(5)
│
├── weight_logs
│   ├── id BIGSERIAL PK
│   ├── user_id FK → users
│   ├── weight DECIMAL(5,1)  -- lbs
│   ├── date DATE
│   └── created_at TIMESTAMP
│
├── progress_entries
│   ├── id BIGSERIAL PK
│   ├── user_id FK → users
│   ├── date DATE
│   ├── caption TEXT
│   └── created_at TIMESTAMP
│
└── progress_photos
    ├── id BIGSERIAL PK
    ├── entry_id FK → progress_entries
    ├── url VARCHAR(500)
    ├── sort_order INT
    └── created_at TIMESTAMP
```

All foreign keys should cascade on user delete.

---

## 6. Migration Notes

The frontend currently uses `localStorage` for all data. When the backend is ready:

1. On login, fetch data from the API and hydrate stores
2. On mutation (add/edit/delete), call the API first, then update the local store on success
3. `localStorage` can remain as a cache layer for offline/fast reads, but the API is the source of truth
4. TanStack Query is planned for caching and request deduplication (see CLAUDE.md TODO)
5. Photo storage must move from base64 in localStorage to proper file uploads (S3, Cloudinary, etc.) — base64 does not scale
