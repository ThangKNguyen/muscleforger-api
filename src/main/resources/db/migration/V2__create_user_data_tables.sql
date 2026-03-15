CREATE TABLE favorites (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    exercise_id VARCHAR(20) NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, exercise_id)
);

CREATE TABLE workout_exercises (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    exercise_id VARCHAR(20) NOT NULL,
    added_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, exercise_id)
);

CREATE TABLE recently_viewed (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    exercise_id VARCHAR(20) NOT NULL,
    viewed_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, exercise_id)
);

CREATE INDEX idx_favorites_user        ON favorites(user_id);
CREATE INDEX idx_workout_user          ON workout_exercises(user_id);
CREATE INDEX idx_recently_viewed_user  ON recently_viewed(user_id);
