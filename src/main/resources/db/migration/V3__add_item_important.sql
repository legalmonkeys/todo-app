-- Add important column to todo_item table
ALTER TABLE todo_item
    ADD COLUMN important BOOLEAN NOT NULL DEFAULT FALSE;