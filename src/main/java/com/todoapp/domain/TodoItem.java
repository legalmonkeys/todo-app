package com.todoapp.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Domain model for a TodoItem entity.
 * Represents a single todo item within a list with validation constraints.
 */
@Table("TODO_ITEM")
public class TodoItem implements Persistable<UUID> {

  @Id
  private UUID id;
  
  @Column("LIST_ID")
  private UUID listId;
  private String text;
  private boolean completed;
  private int position;
  
  @Column("CREATED_AT")
  private Instant createdAt;
  
  @Transient
  private boolean isNew = true;

  /**
   * Default constructor for Spring Data JDBC.
   */
  public TodoItem() {
    // Default constructor required by Spring Data JDBC
  }

  /**
   * Creates a new TodoItem with validation.
   *
   * @param id the unique identifier (cannot be null)
   * @param listId the parent list identifier (cannot be null)
   * @param text the item text (1-50 characters, non-blank)
   * @param completed the completion status
   * @param createdAt the creation timestamp (cannot be null)
   * @throws IllegalArgumentException if any validation fails
   */
  public TodoItem(UUID id, UUID listId, String text, boolean completed, Instant createdAt) {
    this(id, listId, text, completed, createdAt, 0);
  }

  /**
   * Creates a new TodoItem with validation including position.
   *
   * @param id the unique identifier (cannot be null)
   * @param listId the parent list identifier (cannot be null)
   * @param text the item text (1-50 characters, non-blank)
   * @param completed the completion status
   * @param createdAt the creation timestamp (cannot be null)
   * @param position the position within the list (cannot be negative)
   * @throws IllegalArgumentException if any validation fails
   */
  @PersistenceCreator
  public TodoItem(UUID id, UUID listId, String text, boolean completed, Instant createdAt, int position) {
    validateId(id);
    validateListId(listId);
    validateText(text);
    validateCreatedAt(createdAt);
    validatePosition(position);
    
    this.id = id;
    this.listId = listId;
    this.text = text;
    this.completed = completed;
    this.createdAt = createdAt;
    this.position = position;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getListId() {
    return listId;
  }

  public void setListId(UUID listId) {
    this.listId = listId;
  }

  public String getText() {
    return text;
  }

  /**
   * Sets the text with validation.
   *
   * @param text the new text (1-50 characters, non-blank)
   * @throws IllegalArgumentException if validation fails
   */
  public void setText(String text) {
    validateText(text);
    this.text = text;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public int getPosition() {
    return position;
  }

  /**
   * Sets the position with validation.
   *
   * @param position the new position (cannot be negative)
   * @throws IllegalArgumentException if validation fails
   */
  public void setPosition(int position) {
    validatePosition(position);
    this.position = position;
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

  private void validateListId(UUID listId) {
    if (listId == null) {
      throw new IllegalArgumentException("List ID cannot be null");
    }
  }

  private void validateText(String text) {
    if (text == null || text.trim().isEmpty()) {
      throw new IllegalArgumentException("Text cannot be null or blank");
    }
    if (text.length() > 50) {
      throw new IllegalArgumentException("Text cannot exceed 50 characters");
    }
  }

  private void validateCreatedAt(Instant createdAt) {
    if (createdAt == null) {
      throw new IllegalArgumentException("CreatedAt cannot be null");
    }
  }

  private void validatePosition(int position) {
    if (position < 0) {
      throw new IllegalArgumentException("Position cannot be negative");
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
    TodoItem todoItem = (TodoItem) obj;
    return Objects.equals(id, todoItem.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "TodoItem{"
        + "id=" + id
        + ", listId=" + listId
        + ", text='" + text + '\''
        + ", completed=" + completed
        + ", position=" + position
        + ", createdAt=" + createdAt
        + '}';
  }
}
