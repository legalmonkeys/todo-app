package com.todoapp.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain model for a TodoList entity.
 * Represents a collection of todo items with validation constraints.
 */
@Table("TODO_LIST")
public class TodoList implements Persistable<UUID> {

    @Id
    private UUID id;
    private String name;

    @Column("CREATED_AT")
    private Instant createdAt;

    @Transient
    private boolean isNew = true;

    /**
     * Default constructor for Spring Data JDBC.
     */
    public TodoList() {
        // Default constructor required by Spring Data JDBC
    }

    /**
     * Creates a new TodoList with validation.
     *
     * @param id        the unique identifier (cannot be null)
     * @param name      the list name (1-50 characters, non-blank)
     * @param createdAt the creation timestamp (cannot be null)
     * @throws IllegalArgumentException if any validation fails
     */
    @PersistenceCreator
    public TodoList(UUID id, String name, Instant createdAt) {
        validateId(id);
        validateName(name);
        validateCreatedAt(createdAt);

        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    /**
     * Sets the name with validation.
     *
     * @param name the new name (1-50 characters, non-blank)
     * @throws IllegalArgumentException if validation fails
     */
    public void setName(String name) {
        validateName(name);
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void markNotNew() {
        this.isNew = false;
    }

    private void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Name cannot exceed 50 characters");
        }
    }

    private void validateCreatedAt(Instant createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt cannot be null");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TodoList todoList = (TodoList) obj;
        return Objects.equals(id, todoList.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TodoList{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", createdAt=" + createdAt
                + '}';
    }
}
