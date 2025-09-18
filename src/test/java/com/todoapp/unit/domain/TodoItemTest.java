package com.todoapp.unit.domain;

import com.todoapp.domain.TodoItem;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for TodoItem domain model validation rules.
 * Tests validation constraints and domain invariants.
 */
class TodoItemTest {

  @Test
  void createTodoItem_withValidData_shouldSucceed() {
    UUID id = UUID.randomUUID();
    UUID listId = UUID.randomUUID();
    String text = "Valid item text";
    boolean completed = false;
    Instant createdAt = Instant.now();

    TodoItem todoItem = new TodoItem(id, listId, text, completed, createdAt);

    assertThat(todoItem.getId()).isEqualTo(id);
    assertThat(todoItem.getListId()).isEqualTo(listId);
    assertThat(todoItem.getText()).isEqualTo(text);
    assertThat(todoItem.isCompleted()).isEqualTo(completed);
    assertThat(todoItem.getCreatedAt()).isEqualTo(createdAt);
  }

  @Test
  public void createTodoItem_hiddenIsFalse() {
    // setup

    // act
    TodoItem todoItem = new TodoItem(UUID.randomUUID(), UUID.randomUUID(), "Valid text", false, Instant.now());

    // verify
    assertThat(todoItem.isHidden()).isFalse();
  }

  @Test
  void createTodoItem_withNullId_shouldThrowException() {
    assertThatThrownBy(() ->
        new TodoItem(null, UUID.randomUUID(), "Valid text", false, Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ID cannot be null");
  }

  @Test
  void createTodoItem_withNullListId_shouldThrowException() {
    assertThatThrownBy(() ->
        new TodoItem(UUID.randomUUID(), null, "Valid text", false, Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("List ID cannot be null");
  }

  @Test
  void createTodoItem_withNullText_shouldThrowException() {
    assertThatThrownBy(() ->
        new TodoItem(UUID.randomUUID(), UUID.randomUUID(), null, false, Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Text cannot be null or blank");
  }

  @Test
  void createTodoItem_withEmptyText_shouldThrowException() {
    assertThatThrownBy(() ->
        new TodoItem(UUID.randomUUID(), UUID.randomUUID(), "", false, Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Text cannot be null or blank");
  }

  @Test
  void createTodoItem_withWhitespaceOnlyText_shouldThrowException() {
    assertThatThrownBy(() ->
        new TodoItem(UUID.randomUUID(), UUID.randomUUID(), "   ", false, Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Text cannot be null or blank");
  }

  @Test
  void createTodoItem_withTextTooLong_shouldThrowException() {
    String longText = "a".repeat(51); // 51 characters - exceeds 50 limit

    assertThatThrownBy(() ->
        new TodoItem(UUID.randomUUID(), UUID.randomUUID(), longText, false, Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Text cannot exceed 50 characters");
  }

  @Test
  void createTodoItem_withMaxLengthText_shouldSucceed() {
    String maxText = "a".repeat(50); // Exactly 50 characters

    TodoItem todoItem = new TodoItem(UUID.randomUUID(), UUID.randomUUID(), maxText, false, Instant.now());

    assertThat(todoItem.getText()).isEqualTo(maxText);
  }

  @Test
  void createTodoItem_withNullCreatedAt_shouldThrowException() {
    assertThatThrownBy(() ->
        new TodoItem(UUID.randomUUID(), UUID.randomUUID(), "Valid text", false, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("CreatedAt cannot be null");
  }

  @Test
  void setText_withValidText_shouldUpdateText() {
    TodoItem todoItem = new TodoItem(UUID.randomUUID(), UUID.randomUUID(), "Original text", false, Instant.now());

    todoItem.setText("Updated text");

    assertThat(todoItem.getText()).isEqualTo("Updated text");
  }

  @Test
  void setText_withInvalidText_shouldThrowException() {
    TodoItem todoItem = new TodoItem(UUID.randomUUID(), UUID.randomUUID(), "Original text", false, Instant.now());

    assertThatThrownBy(() -> todoItem.setText(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Text cannot be null or blank");
  }

  @Test
  void setCompleted_shouldUpdateCompletedStatus() {
    TodoItem todoItem = new TodoItem(UUID.randomUUID(), UUID.randomUUID(), "Valid text", false, Instant.now());

    todoItem.setCompleted(true);

    assertThat(todoItem.isCompleted()).isTrue();

    todoItem.setCompleted(false);

    assertThat(todoItem.isCompleted()).isFalse();
  }

  @Test
  void equals_withSameId_shouldReturnTrue() {
    UUID id = UUID.randomUUID();
    TodoItem item1 = new TodoItem(id, UUID.randomUUID(), "Text 1", false, Instant.now());
    TodoItem item2 = new TodoItem(id, UUID.randomUUID(), "Text 2", true, Instant.now());

    assertThat(item1).isEqualTo(item2);
  }

  @Test
  void equals_withDifferentId_shouldReturnFalse() {
    TodoItem item1 = new TodoItem(UUID.randomUUID(), UUID.randomUUID(), "Same text", false, Instant.now());
    TodoItem item2 = new TodoItem(UUID.randomUUID(), UUID.randomUUID(), "Same text", false, Instant.now());

    assertThat(item1).isNotEqualTo(item2);
  }

  @Test
  void hashCode_withSameId_shouldReturnSameHashCode() {
    UUID id = UUID.randomUUID();
    TodoItem item1 = new TodoItem(id, UUID.randomUUID(), "Text 1", false, Instant.now());
    TodoItem item2 = new TodoItem(id, UUID.randomUUID(), "Text 2", true, Instant.now());

    assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
  }

  @Test
  void createTodoItem_withValidPosition_shouldSucceed() {
    TodoItem todoItem = new TodoItem(UUID.randomUUID(), UUID.randomUUID(), "Valid text", false, Instant.now(), 0);

    assertThat(todoItem.getPosition()).isEqualTo(0);
  }

  @Test
  void createTodoItem_withNegativePosition_shouldThrowException() {
    assertThatThrownBy(() ->
        new TodoItem(UUID.randomUUID(), UUID.randomUUID(), "Valid text", false, Instant.now(), -1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Position cannot be negative");
  }

  @Test
  void setPosition_withValidPosition_shouldUpdatePosition() {
    TodoItem todoItem = new TodoItem(UUID.randomUUID(), UUID.randomUUID(), "Valid text", false, Instant.now(), 0);

    todoItem.setPosition(5);

    assertThat(todoItem.getPosition()).isEqualTo(5);
  }

  @Test
  void setPosition_withNegativePosition_shouldThrowException() {
    TodoItem todoItem = new TodoItem(UUID.randomUUID(), UUID.randomUUID(), "Valid text", false, Instant.now(), 0);

    assertThatThrownBy(() -> todoItem.setPosition(-1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Position cannot be negative");
  }
}
