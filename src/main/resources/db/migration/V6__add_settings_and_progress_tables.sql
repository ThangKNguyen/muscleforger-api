-- ── Extend users table ────────────────────────────────────────────────────────
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500);

-- ── User preferences ─────────────────────────────────────────────────────────
CREATE TABLE user_preferences (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     BIGINT      NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    theme       VARCHAR(10) NOT NULL DEFAULT 'dark',
    weight_unit VARCHAR(5)  NOT NULL DEFAULT 'lbs'
);

-- ── Weight logs ──────────────────────────────────────────────────────────────
CREATE TABLE weight_logs (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    weight      DECIMAL(5,1) NOT NULL,
    date        DATE         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, date)
);

CREATE INDEX idx_weight_logs_user_date ON weight_logs(user_id, date);

-- ── Progress entries ─────────────────────────────────────────────────────────
CREATE TABLE progress_entries (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date        DATE      NOT NULL,
    caption     TEXT      DEFAULT '',
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_progress_entries_user_date ON progress_entries(user_id, date DESC);

-- ── Progress photos ──────────────────────────────────────────────────────────
CREATE TABLE progress_photos (
    id          BIGSERIAL    PRIMARY KEY,
    entry_id    BIGINT       NOT NULL REFERENCES progress_entries(id) ON DELETE CASCADE,
    url         VARCHAR(500) NOT NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_progress_photos_entry ON progress_photos(entry_id);
