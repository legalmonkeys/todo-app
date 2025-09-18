-- Initial schema for Multi-List TODO App
-- Based on data model: TodoList and TodoItem entities

CREATE TABLE todo_list (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE todo_item (
    id UUID PRIMARY KEY,
    list_id UUID NOT NULL,
    text VARCHAR(50) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    FOREIGN KEY (list_id) REFERENCES todo_list(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_todo_item_list_id ON todo_item(list_id);
CREATE INDEX idx_todo_list_created_at ON todo_list(created_at DESC);
CREATE INDEX idx_todo_item_created_at ON todo_item(created_at DESC);
