-- Widen exercise_id columns to VARCHAR(50) to accommodate custom_ prefixed IDs
ALTER TABLE favorites          ALTER COLUMN exercise_id TYPE VARCHAR(50);
ALTER TABLE workout_exercises  ALTER COLUMN exercise_id TYPE VARCHAR(50);
ALTER TABLE recently_viewed    ALTER COLUMN exercise_id TYPE VARCHAR(50);
