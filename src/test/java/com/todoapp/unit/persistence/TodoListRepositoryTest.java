package com.todoapp.unit.persistence;

import static org.assertj.core.api.Assertions.*;

import com.todoapp.domain.TodoList;
import com.todoapp.persistence.TodoListRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Unit tests for TodoListRepository CRUD operations and constraints.
 * Tests repository layer with in-memory H2 database.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.flyway.enabled=true"
})
@Transactional
class TodoListRepositoryTest {

  @Autowired private TodoListRepository repository;

  @Test
  void save_withValidTodoList_shouldPersist() {
    TodoList todoList = new TodoList(UUID.randomUUID(), "Test List", Instant.now());

    TodoList saved = repository.save(todoList);

    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isEqualTo(todoList.getId());
    assertThat(saved.getName()).isEqualTo(todoList.getName());
    assertThat(saved.getCreatedAt()).isEqualTo(todoList.getCreatedAt());
  }

  @Test
  void findById_withExistingId_shouldReturnTodoList() {
    TodoList todoList = new TodoList(UUID.randomUUID(), "Test List", Instant.now());
    repository.save(todoList);

    Optional<TodoList> found = repository.findById(todoList.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Test List");
  }

  @Test
  void findById_withNonExistentId_shouldReturnEmpty() {
    Optional<TodoList> found = repository.findById(UUID.randomUUID());

    assertThat(found).isEmpty();
  }

  @Test
  void findAll_shouldReturnAllLists() {
    TodoList list1 = new TodoList(UUID.randomUUID(), "List 1", Instant.now());
    TodoList list2 = new TodoList(UUID.randomUUID(), "List 2", Instant.now());
    repository.save(list1);
    repository.save(list2);

    List<TodoList> all = repository.findAll();

    assertThat(all).hasSize(2);
    assertThat(all).extracting(TodoList::getName)
        .containsExactlyInAnyOrder("List 1", "List 2");
  }

  @Test
  void findAllOrderByCreatedAtDesc_shouldReturnNewestFirst() {
    Instant now = Instant.now();
    TodoList older = new TodoList(UUID.randomUUID(), "Older List", now.minusSeconds(10));
    TodoList newer = new TodoList(UUID.randomUUID(), "Newer List", now);
    
    repository.save(older);
    repository.save(newer);

    List<TodoList> ordered = repository.findAllOrderByCreatedAtDesc();

    assertThat(ordered).hasSize(2);
    assertThat(ordered.get(0).getName()).isEqualTo("Newer List");
    assertThat(ordered.get(1).getName()).isEqualTo("Older List");
  }

  @Test
  void existsByName_withExistingName_shouldReturnTrue() {
    TodoList todoList = new TodoList(UUID.randomUUID(), "Unique Name", Instant.now());
    repository.save(todoList);

    boolean exists = repository.existsByName("Unique Name");

    assertThat(exists).isTrue();
  }

  @Test
  void existsByName_withNonExistentName_shouldReturnFalse() {
    boolean exists = repository.existsByName("Non-existent Name");

    assertThat(exists).isFalse();
  }

  @Test
  void existsByNameAndIdNot_withSameNameDifferentId_shouldReturnTrue() {
    TodoList existing = new TodoList(UUID.randomUUID(), "Same Name", Instant.now());
    repository.save(existing);

    boolean exists = repository.existsByNameAndIdNot("Same Name", UUID.randomUUID());

    assertThat(exists).isTrue();
  }

  @Test
  void existsByNameAndIdNot_withSameNameSameId_shouldReturnFalse() {
    TodoList existing = new TodoList(UUID.randomUUID(), "Same Name", Instant.now());
    repository.save(existing);

    boolean exists = repository.existsByNameAndIdNot("Same Name", existing.getId());

    assertThat(exists).isFalse();
  }

  @Test
  void deleteById_shouldRemoveTodoList() {
    TodoList todoList = new TodoList(UUID.randomUUID(), "To Delete", Instant.now());
    repository.save(todoList);

    repository.deleteById(todoList.getId());

    Optional<TodoList> found = repository.findById(todoList.getId());
    assertThat(found).isEmpty();
  }

  @Test
  void count_shouldReturnCorrectCount() {
    TodoList list1 = new TodoList(UUID.randomUUID(), "List 1", Instant.now());
    TodoList list2 = new TodoList(UUID.randomUUID(), "List 2", Instant.now());
    repository.save(list1);
    repository.save(list2);

    long count = repository.count();

    assertThat(count).isEqualTo(2);
  }

  @Test
  void update_shouldModifyExistingRecord() {
    TodoList original = new TodoList(UUID.randomUUID(), "Original Name", Instant.now());
    TodoList saved = repository.save(original);
    
    // Mark as not new for update operation
    saved.markNotNew();
    saved.setName("Updated Name");
    TodoList updated = repository.save(saved);

    assertThat(updated.getName()).isEqualTo("Updated Name");
    assertThat(updated.getId()).isEqualTo(original.getId());
    
    Optional<TodoList> found = repository.findById(original.getId());
    assertThat(found.get().getName()).isEqualTo("Updated Name");
  }
}
