-- Add position column to todo_item table
ALTER TABLE todo_item
    ADD COLUMN position INTEGER NOT NULL DEFAULT 0;

-- Create index for efficient ordering queries
CREATE INDEX idx_todo_item_list_position ON todo_item (list_id, position);

-- Backfill positions for existing items per list using creation order
-- H2 compatible update with subquery
UPDATE todo_item
SET position = (SELECT COUNT(*) - 1
                FROM todo_item t2
                WHERE t2.list_id = todo_item.list_id
                  AND (t2.created_at < todo_item.created_at
                    OR (t2.created_at = todo_item.created_at AND t2.id <= todo_item.id)));

-- Add unique constraint to ensure no duplicate positions within a list
ALTER TABLE todo_item
    ADD CONSTRAINT uk_todo_item_list_position UNIQUE (list_id, position);
