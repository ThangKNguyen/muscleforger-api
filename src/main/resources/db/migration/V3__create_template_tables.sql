CREATE TABLE custom_exercises (
    id           BIGSERIAL    PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name         VARCHAR(200) NOT NULL,
    body_part    VARCHAR(100) NOT NULL,
    target       VARCHAR(100) NOT NULL,
    equipment    VARCHAR(100) NOT NULL,
    instructions TEXT,
    description  TEXT,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_custom_exercises_user ON custom_exercises(user_id);

CREATE TABLE workout_templates (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_workout_templates_user ON workout_templates(user_id);

CREATE TABLE template_days (
    id          BIGSERIAL PRIMARY KEY,
    template_id BIGINT    NOT NULL REFERENCES workout_templates(id) ON DELETE CASCADE,
    day_number  SMALLINT  NOT NULL CHECK (day_number BETWEEN 1 AND 7),
    label       VARCHAR(50),
    UNIQUE (template_id, day_number)
);
CREATE INDEX idx_template_days_template ON template_days(template_id);

CREATE TABLE template_exercises (
    id          BIGSERIAL    PRIMARY KEY,
    day_id      BIGINT       NOT NULL REFERENCES template_days(id) ON DELETE CASCADE,
    exercise_id VARCHAR(50)  NOT NULL,
    position    SMALLINT     NOT NULL DEFAULT 0,
    sets        SMALLINT     NOT NULL,
    reps        SMALLINT     NOT NULL,
    rpe         NUMERIC(3,1) CHECK (rpe BETWEEN 1.0 AND 10.0),
    UNIQUE (day_id, position)
);
CREATE INDEX idx_template_exercises_day ON template_exercises(day_id);
