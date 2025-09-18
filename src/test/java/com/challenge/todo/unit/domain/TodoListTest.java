package com.challenge.todo.unit.domain;

import static org.assertj.core.api.Assertions.*;

import com.challenge.todo.domain.TodoList;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for TodoList domain model validation rules.
 * Tests validation constraints and domain invariants.
 */
class TodoListTest {

  @Test
  void createTodoList_withValidData_shouldSucceed() {
    UUID id = UUID.randomUUID();
    String name = "Valid List Name";
    Instant createdAt = Instant.now();

    TodoList todoList = new TodoList(id, name, createdAt);

    assertThat(todoList.getId()).isEqualTo(id);
    assertThat(todoList.getName()).isEqualTo(name);
    assertThat(todoList.getCreatedAt()).isEqualTo(createdAt);
  }

  @Test
  void createTodoList_withNullId_shouldThrowException() {
    assertThatThrownBy(() -> new TodoList(null, "Valid Name", Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ID cannot be null");
  }

  @Test
  void createTodoList_withNullName_shouldThrowException() {
    assertThatThrownBy(() -> new TodoList(UUID.randomUUID(), null, Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Name cannot be null or blank");
  }

  @Test
  void createTodoList_withEmptyName_shouldThrowException() {
    assertThatThrownBy(() -> new TodoList(UUID.randomUUID(), "", Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Name cannot be null or blank");
  }

  @Test
  void createTodoList_withWhitespaceOnlyName_shouldThrowException() {
    assertThatThrownBy(() -> new TodoList(UUID.randomUUID(), "   ", Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Name cannot be null or blank");
  }

  @Test
  void createTodoList_withNameTooLong_shouldThrowException() {
    String longName = "a".repeat(51); // 51 characters - exceeds 50 limit

    assertThatThrownBy(() -> new TodoList(UUID.randomUUID(), longName, Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Name cannot exceed 50 characters");
  }

  @Test
  void createTodoList_withMaxLengthName_shouldSucceed() {
    String maxName = "a".repeat(50); // Exactly 50 characters

    TodoList todoList = new TodoList(UUID.randomUUID(), maxName, Instant.now());

    assertThat(todoList.getName()).isEqualTo(maxName);
  }

  @Test
  void createTodoList_withNullCreatedAt_shouldThrowException() {
    assertThatThrownBy(() -> new TodoList(UUID.randomUUID(), "Valid Name", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("CreatedAt cannot be null");
  }

  @Test
  void setName_withValidName_shouldUpdateName() {
    TodoList todoList = new TodoList(UUID.randomUUID(), "Original Name", Instant.now());

    todoList.setName("Updated Name");

    assertThat(todoList.getName()).isEqualTo("Updated Name");
  }

  @Test
  void setName_withInvalidName_shouldThrowException() {
    TodoList todoList = new TodoList(UUID.randomUUID(), "Original Name", Instant.now());

    assertThatThrownBy(() -> todoList.setName(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Name cannot be null or blank");
  }

  @Test
  void equals_withSameId_shouldReturnTrue() {
    UUID id = UUID.randomUUID();
    TodoList list1 = new TodoList(id, "Name 1", Instant.now());
    TodoList list2 = new TodoList(id, "Name 2", Instant.now());

    assertThat(list1).isEqualTo(list2);
  }

  @Test
  void equals_withDifferentId_shouldReturnFalse() {
    TodoList list1 = new TodoList(UUID.randomUUID(), "Same Name", Instant.now());
    TodoList list2 = new TodoList(UUID.randomUUID(), "Same Name", Instant.now());

    assertThat(list1).isNotEqualTo(list2);
  }

  @Test
  void hashCode_withSameId_shouldReturnSameHashCode() {
    UUID id = UUID.randomUUID();
    TodoList list1 = new TodoList(id, "Name 1", Instant.now());
    TodoList list2 = new TodoList(id, "Name 2", Instant.now());

    assertThat(list1.hashCode()).isEqualTo(list2.hashCode());
  }
}
