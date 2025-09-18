-- Add hidden column to todo_item table
-- Allows items to be hidden from view while preserving data
ALTER TABLE todo_item ADD COLUMN hidden BOOLEAN NOT NULL DEFAULT FALSE;
