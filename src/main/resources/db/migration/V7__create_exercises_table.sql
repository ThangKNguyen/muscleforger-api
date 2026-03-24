CREATE TABLE exercises (
    id                VARCHAR(20)  PRIMARY KEY,
    name              VARCHAR(200) NOT NULL,
    body_part         VARCHAR(100) NOT NULL,
    target            VARCHAR(100) NOT NULL,
    equipment         VARCHAR(100) NOT NULL,
    secondary_muscles TEXT,        -- stored as JSON array string
    instructions      TEXT,        -- stored as JSON array string
    description       TEXT,
    difficulty        VARCHAR(20),
    category          VARCHAR(50),
    gif_url           VARCHAR(500)
);

CREATE INDEX idx_exercises_body_part  ON exercises(body_part);
CREATE INDEX idx_exercises_target     ON exercises(target);
CREATE INDEX idx_exercises_equipment  ON exercises(equipment);
CREATE INDEX idx_exercises_name       ON exercises(name);
