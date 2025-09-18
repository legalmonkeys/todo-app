package com.challenge.todo.unit.persistence;

import static org.assertj.core.api.Assertions.*;

import com.challenge.todo.domain.TodoItem;
import com.challenge.todo.domain.TodoList;
import com.challenge.todo.persistence.TodoItemRepository;
import com.challenge.todo.persistence.TodoListRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Unit tests for TodoItemRepository CRUD operations and constraints.
 * Tests repository layer with in-memory H2 database.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:itemtestdb",
    "spring.flyway.enabled=true"
})
@Transactional
class TodoItemRepositoryTest {

  @Autowired private TodoItemRepository repository;
  @Autowired private TodoListRepository listRepository;

  private UUID testListId;

  @BeforeEach
  void setUp() {
    // Create a test list for items
    testListId = UUID.randomUUID();
    TodoList testList = new TodoList(testListId, "Test List", Instant.now());
    listRepository.save(testList);
  }

  @Test
  void save_withValidTodoItem_shouldPersist() {
    TodoItem todoItem = new TodoItem(UUID.randomUUID(), testListId, "Test Item", false, Instant.now(), 0);

    TodoItem saved = repository.save(todoItem);

    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isEqualTo(todoItem.getId());
    assertThat(saved.getListId()).isEqualTo(testListId);
    assertThat(saved.getText()).isEqualTo("Test Item");
    assertThat(saved.isCompleted()).isFalse();
    assertThat(saved.getPosition()).isEqualTo(0);
  }

  @Test
  void findById_withExistingId_shouldReturnTodoItem() {
    TodoItem todoItem = new TodoItem(UUID.randomUUID(), testListId, "Test Item", false, Instant.now(), 0);
    repository.save(todoItem);

    Optional<TodoItem> found = repository.findById(todoItem.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getText()).isEqualTo("Test Item");
    assertThat(found.get().getListId()).isEqualTo(testListId);
  }

  @Test
  void findById_withNonExistentId_shouldReturnEmpty() {
    Optional<TodoItem> found = repository.findById(UUID.randomUUID());

    assertThat(found).isEmpty();
  }

  @Test
  void findAll_shouldReturnAllItems() {
    TodoItem item1 = new TodoItem(UUID.randomUUID(), testListId, "Item 1", false, Instant.now(), 0);
    TodoItem item2 = new TodoItem(UUID.randomUUID(), testListId, "Item 2", true, Instant.now(), 1);
    repository.save(item1);
    repository.save(item2);

    List<TodoItem> all = repository.findAll();

    assertThat(all).hasSize(2);
    assertThat(all).extracting(TodoItem::getText)
        .containsExactlyInAnyOrder("Item 1", "Item 2");
  }

  @Test
  void findAllByListIdOrderByCreatedAtDesc_shouldReturnNewestFirst() {
    Instant now = Instant.now();
    TodoItem older = new TodoItem(UUID.randomUUID(), testListId, "Older Item", false, now.minusSeconds(10), 0);
    TodoItem newer = new TodoItem(UUID.randomUUID(), testListId, "Newer Item", false, now, 1);
    
    repository.save(older);
    repository.save(newer);

    List<TodoItem> ordered = repository.findAllByListIdOrderByCreatedAtDesc(testListId);

    assertThat(ordered).hasSize(2);
    assertThat(ordered.get(0).getText()).isEqualTo("Newer Item");
    assertThat(ordered.get(1).getText()).isEqualTo("Older Item");
  }

  @Test
  void findByListIdAndCompleted_shouldFilterByStatus() {
    TodoItem incomplete = new TodoItem(UUID.randomUUID(), testListId, "Incomplete", false, Instant.now(), 0);
    TodoItem complete = new TodoItem(UUID.randomUUID(), testListId, "Complete", true, Instant.now(), 1);
    
    repository.save(incomplete);
    repository.save(complete);

    List<TodoItem> completedItems = repository.findByListIdAndCompleted(testListId, true);
    List<TodoItem> incompleteItems = repository.findByListIdAndCompleted(testListId, false);

    assertThat(completedItems).hasSize(1);
    assertThat(completedItems.get(0).getText()).isEqualTo("Complete");
    
    assertThat(incompleteItems).hasSize(1);
    assertThat(incompleteItems.get(0).getText()).isEqualTo("Incomplete");
  }

  @Test
  void countByListId_shouldReturnCorrectCount() {
    TodoItem item1 = new TodoItem(UUID.randomUUID(), testListId, "Item 1", false, Instant.now(), 0);
    TodoItem item2 = new TodoItem(UUID.randomUUID(), testListId, "Item 2", true, Instant.now(), 1);
    repository.save(item1);
    repository.save(item2);

    long count = repository.countByListId(testListId);

    assertThat(count).isEqualTo(2);
  }

  @Test
  void countCompletedByListId_shouldReturnCompletedCount() {
    TodoItem incomplete = new TodoItem(UUID.randomUUID(), testListId, "Incomplete", false, Instant.now(), 0);
    TodoItem complete1 = new TodoItem(UUID.randomUUID(), testListId, "Complete 1", true, Instant.now(), 1);
    TodoItem complete2 = new TodoItem(UUID.randomUUID(), testListId, "Complete 2", true, Instant.now(), 2);
    
    repository.save(incomplete);
    repository.save(complete1);
    repository.save(complete2);

    long completedCount = repository.countCompletedByListId(testListId);

    assertThat(completedCount).isEqualTo(2);
  }

  @Test
  void deleteById_shouldRemoveTodoItem() {
    TodoItem todoItem = new TodoItem(UUID.randomUUID(), testListId, "To Delete", false, Instant.now(), 0);
    repository.save(todoItem);

    repository.deleteById(todoItem.getId());

    Optional<TodoItem> found = repository.findById(todoItem.getId());
    assertThat(found).isEmpty();
  }

  @Test
  void count_shouldReturnCorrectCount() {
    TodoItem item1 = new TodoItem(UUID.randomUUID(), testListId, "Item 1", false, Instant.now(), 0);
    TodoItem item2 = new TodoItem(UUID.randomUUID(), testListId, "Item 2", true, Instant.now(), 1);
    repository.save(item1);
    repository.save(item2);

    long count = repository.count();

    assertThat(count).isEqualTo(2);
  }

  @Test
  void update_shouldModifyExistingRecord() {
    TodoItem original = new TodoItem(UUID.randomUUID(), testListId, "Original Text", false, Instant.now(), 0);
    TodoItem saved = repository.save(original);
    
    // Mark as not new for update operation
    saved.markNotNew();
    saved.setText("Updated Text");
    saved.setCompleted(true);
    TodoItem updated = repository.save(saved);

    assertThat(updated.getText()).isEqualTo("Updated Text");
    assertThat(updated.isCompleted()).isTrue();
    assertThat(updated.getId()).isEqualTo(original.getId());
    
    Optional<TodoItem> found = repository.findById(original.getId());
    assertThat(found.get().getText()).isEqualTo("Updated Text");
    assertThat(found.get().isCompleted()).isTrue();
  }

  @Test
  void findByListId_andDeleteAll_shouldRemoveAllItemsInList() {
    TodoItem item1 = new TodoItem(UUID.randomUUID(), testListId, "Item 1", false, Instant.now(), 0);
    TodoItem item2 = new TodoItem(UUID.randomUUID(), testListId, "Item 2", true, Instant.now(), 1);
    repository.save(item1);
    repository.save(item2);

    List<TodoItem> itemsToDelete = repository.findByListId(testListId);
    repository.deleteAll(itemsToDelete);

    assertThat(itemsToDelete).hasSize(2);
    assertThat(repository.countByListId(testListId)).isEqualTo(0);
  }

  @Test
  void findAllByListIdOrderByPositionAsc_shouldReturnOrderedByPosition() {
    TodoItem item1 = new TodoItem(UUID.randomUUID(), testListId, "Item 1", false, Instant.now(), 2);
    TodoItem item2 = new TodoItem(UUID.randomUUID(), testListId, "Item 2", false, Instant.now(), 0);
    TodoItem item3 = new TodoItem(UUID.randomUUID(), testListId, "Item 3", false, Instant.now(), 1);
    
    repository.save(item1);
    repository.save(item2);
    repository.save(item3);

    List<TodoItem> ordered = repository.findAllByListIdOrderByPositionAsc(testListId);

    assertThat(ordered).hasSize(3);
    assertThat(ordered.get(0).getPosition()).isEqualTo(0);
    assertThat(ordered.get(0).getText()).isEqualTo("Item 2");
    assertThat(ordered.get(1).getPosition()).isEqualTo(1);
    assertThat(ordered.get(1).getText()).isEqualTo("Item 3");
    assertThat(ordered.get(2).getPosition()).isEqualTo(2);
    assertThat(ordered.get(2).getText()).isEqualTo("Item 1");
  }

  @Test
  void findMaxPositionByListId_shouldReturnHighestPosition() {
    TodoItem item1 = new TodoItem(UUID.randomUUID(), testListId, "Item 1", false, Instant.now(), 0);
    TodoItem item2 = new TodoItem(UUID.randomUUID(), testListId, "Item 2", false, Instant.now(), 5);
    TodoItem item3 = new TodoItem(UUID.randomUUID(), testListId, "Item 3", false, Instant.now(), 2);
    
    repository.save(item1);
    repository.save(item2);
    repository.save(item3);

    Integer maxPosition = repository.findMaxPositionByListId(testListId);

    assertThat(maxPosition).isEqualTo(5);
  }

  @Test
  void findMaxPositionByListId_withEmptyList_shouldReturnNull() {
    Integer maxPosition = repository.findMaxPositionByListId(testListId);

    assertThat(maxPosition).isNull();
  }

  @Test
  void save_withDuplicatePosition_shouldThrowException() {
    TodoItem item1 = new TodoItem(UUID.randomUUID(), testListId, "Item 1", false, Instant.now(), 0);
    TodoItem item2 = new TodoItem(UUID.randomUUID(), testListId, "Item 2", false, Instant.now(), 0);
    
    repository.save(item1);

    assertThatThrownBy(() -> repository.save(item2))
        .hasCauseInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
  }
}
