package com.todoapp.unit.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.todoapp.domain.TodoItem;
import com.todoapp.domain.TodoList;
import com.todoapp.persistence.TodoItemRepository;
import com.todoapp.persistence.TodoListRepository;
import com.todoapp.service.TodoItemService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for TodoItemService business logic.
 * Tests service layer with mocked repository dependencies.
 */
@ExtendWith(MockitoExtension.class)
class TodoItemServiceTest {

  @Mock private TodoItemRepository itemRepository;
  @Mock private TodoListRepository listRepository;

  private TodoItemService service;

  private UUID testListId;
  private TodoList testList;

  @BeforeEach
  void setUp() {
    service = new TodoItemService(itemRepository, listRepository);
    
    testListId = UUID.randomUUID();
    testList = new TodoList(testListId, "Test List", Instant.now());
  }

  @Test
  void createItem_withValidData_shouldCreateAndReturnItem() {
    // Given
    String itemText = "New task";
    when(listRepository.findById(testListId)).thenReturn(Optional.of(testList));
    when(itemRepository.findMaxPositionByListId(testListId)).thenReturn(2); // Existing items at 0, 1, 2
    when(itemRepository.save(any(TodoItem.class))).thenAnswer(invocation -> {
      TodoItem saved = invocation.getArgument(0);
      saved.markNotNew(); // Simulate persistence
      return saved;
    });

    // When
    TodoItem created = service.createItem(testListId, itemText);

    // Then
    assertThat(created).isNotNull();
    assertThat(created.getListId()).isEqualTo(testListId);
    assertThat(created.getText()).isEqualTo(itemText);
    assertThat(created.isCompleted()).isFalse();
    assertThat(created.getPosition()).isEqualTo(3); // Should be max + 1
    assertThat(created.getId()).isNotNull();
    assertThat(created.getCreatedAt()).isNotNull();
    
    verify(listRepository).findById(testListId);
    verify(itemRepository).findMaxPositionByListId(testListId);
    verify(itemRepository).save(any(TodoItem.class));
  }

  @Test
  void createItem_inEmptyList_shouldSetPositionZero() {
    // Given
    String itemText = "First task";
    when(listRepository.findById(testListId)).thenReturn(Optional.of(testList));
    when(itemRepository.findMaxPositionByListId(testListId)).thenReturn(null); // Empty list
    when(itemRepository.save(any(TodoItem.class))).thenAnswer(invocation -> {
      TodoItem saved = invocation.getArgument(0);
      saved.markNotNew(); // Simulate persistence
      return saved;
    });

    // When
    TodoItem created = service.createItem(testListId, itemText);

    // Then
    assertThat(created).isNotNull();
    assertThat(created.getPosition()).isEqualTo(0); // Should be 0 for first item
    
    verify(listRepository).findById(testListId);
    verify(itemRepository).findMaxPositionByListId(testListId);
    verify(itemRepository).save(any(TodoItem.class));
  }

  @Test
  void createItem_withNonExistentList_shouldThrowException() {
    // Given
    UUID nonExistentListId = UUID.randomUUID();
    when(listRepository.findById(nonExistentListId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> service.createItem(nonExistentListId, "Some text"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not found");
    
    verify(listRepository).findById(nonExistentListId);
    verify(itemRepository, never()).save(any());
  }

  @Test
  void createItem_withEmptyText_shouldThrowException() {
    // When & Then
    assertThatThrownBy(() -> service.createItem(testListId, ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be empty");
    
    verify(listRepository, never()).findById(any());
    verify(itemRepository, never()).save(any());
  }

  @Test
  void createItem_withWhitespaceText_shouldThrowException() {
    // When & Then
    assertThatThrownBy(() -> service.createItem(testListId, "   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be empty");
    
    verify(listRepository, never()).findById(any());
    verify(itemRepository, never()).save(any());
  }

  @Test
  void createItem_withTooLongText_shouldThrowException() {
    // Given
    String tooLongText = "A".repeat(51); // 51 characters

    // When & Then
    assertThatThrownBy(() -> service.createItem(testListId, tooLongText))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("too long");
    
    verify(listRepository, never()).findById(any());
    verify(itemRepository, never()).save(any());
  }

  @Test
  void getItemsByList_shouldReturnItemsOrderedByPosition() {
    // Given
    TodoItem first = new TodoItem(UUID.randomUUID(), testListId, "First task", false, Instant.now(), 0);
    TodoItem second = new TodoItem(UUID.randomUUID(), testListId, "Second task", false, Instant.now(), 1);
    when(itemRepository.findAllByListIdOrderByPositionAsc(testListId))
        .thenReturn(List.of(first, second));

    // When
    List<TodoItem> result = service.getItemsByList(testListId);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getText()).isEqualTo("First task");
    assertThat(result.get(1).getText()).isEqualTo("Second task");
    
    verify(itemRepository).findAllByListIdOrderByPositionAsc(testListId);
  }

  @Test
  void getItemsByList_withNoItems_shouldReturnEmptyList() {
    // Given
    when(itemRepository.findAllByListIdOrderByPositionAsc(testListId)).thenReturn(List.of());

    // When
    List<TodoItem> result = service.getItemsByList(testListId);

    // Then
    assertThat(result).isEmpty();
    verify(itemRepository).findAllByListIdOrderByPositionAsc(testListId);
  }

  @Test
  void getItemsByListAndStatus_shouldFilterByCompletedStatus() {
    // Given
    TodoItem incomplete = new TodoItem(UUID.randomUUID(), testListId, "Incomplete", false, Instant.now());
    TodoItem complete = new TodoItem(UUID.randomUUID(), testListId, "Complete", true, Instant.now());
    
    when(itemRepository.findByListIdAndCompleted(testListId, true)).thenReturn(List.of(complete));
    when(itemRepository.findByListIdAndCompleted(testListId, false)).thenReturn(List.of(incomplete));

    // When
    List<TodoItem> completedItems = service.getItemsByListAndStatus(testListId, true);
    List<TodoItem> incompleteItems = service.getItemsByListAndStatus(testListId, false);

    // Then
    assertThat(completedItems).hasSize(1);
    assertThat(completedItems.get(0).getText()).isEqualTo("Complete");
    
    assertThat(incompleteItems).hasSize(1);
    assertThat(incompleteItems.get(0).getText()).isEqualTo("Incomplete");
    
    verify(itemRepository).findByListIdAndCompleted(testListId, true);
    verify(itemRepository).findByListIdAndCompleted(testListId, false);
  }

  @Test
  void getItemById_withExistingId_shouldReturnItem() {
    // Given
    UUID itemId = UUID.randomUUID();
    TodoItem existingItem = new TodoItem(itemId, testListId, "Test Item", false, Instant.now());
    when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

    // When
    Optional<TodoItem> result = service.getItemById(itemId);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getText()).isEqualTo("Test Item");
    
    verify(itemRepository).findById(itemId);
  }

  @Test
  void getItemById_withNonExistentId_shouldReturnEmpty() {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    when(itemRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // When
    Optional<TodoItem> result = service.getItemById(nonExistentId);

    // Then
    assertThat(result).isEmpty();
    verify(itemRepository).findById(nonExistentId);
  }

  @Test
  void updateItemText_withValidData_shouldUpdateAndReturnItem() {
    // Given
    UUID itemId = UUID.randomUUID();
    String newText = "Updated text";
    TodoItem existingItem = new TodoItem(itemId, testListId, "Original text", false, Instant.now());
    
    when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
    when(itemRepository.save(any(TodoItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    TodoItem updated = service.updateItemText(itemId, newText);

    // Then
    assertThat(updated).isNotNull();
    assertThat(updated.getText()).isEqualTo(newText);
    assertThat(updated.getId()).isEqualTo(itemId);
    
    verify(itemRepository).findById(itemId);
    verify(itemRepository).save(existingItem);
  }

  @Test
  void updateItemText_withNonExistentId_shouldThrowException() {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    when(itemRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> service.updateItemText(nonExistentId, "New text"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not found");
    
    verify(itemRepository).findById(nonExistentId);
    verify(itemRepository, never()).save(any());
  }

  @Test
  void updateItemText_withInvalidText_shouldThrowException() {
    // Given
    UUID itemId = UUID.randomUUID();
    TodoItem existingItem = new TodoItem(itemId, testListId, "Original text", false, Instant.now());
    when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

    // When & Then - empty text
    assertThatThrownBy(() -> service.updateItemText(itemId, ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be empty");
    
    // When & Then - too long text
    String tooLongText = "A".repeat(51);
    assertThatThrownBy(() -> service.updateItemText(itemId, tooLongText))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("too long");
    
    verify(itemRepository, times(2)).findById(itemId);
    verify(itemRepository, never()).save(any());
  }

  @Test
  void toggleItemCompletion_withExistingItem_shouldToggleAndReturnItem() {
    // Given
    UUID itemId = UUID.randomUUID();
    TodoItem existingItem = new TodoItem(itemId, testListId, "Test item", false, Instant.now());
    
    when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
    when(itemRepository.save(any(TodoItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    TodoItem toggled = service.toggleItemCompletion(itemId);

    // Then
    assertThat(toggled).isNotNull();
    assertThat(toggled.isCompleted()).isTrue(); // Should be toggled from false to true
    assertThat(toggled.getId()).isEqualTo(itemId);
    
    verify(itemRepository).findById(itemId);
    verify(itemRepository).save(existingItem);
  }

  @Test
  void toggleItemCompletion_withNonExistentId_shouldThrowException() {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    when(itemRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> service.toggleItemCompletion(nonExistentId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not found");
    
    verify(itemRepository).findById(nonExistentId);
    verify(itemRepository, never()).save(any());
  }

  @Test
  void deleteItem_withExistingId_shouldDeleteItem() {
    // Given
    UUID itemId = UUID.randomUUID();
    TodoItem existingItem = new TodoItem(itemId, testListId, "To delete", false, Instant.now());
    when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

    // When
    service.deleteItem(itemId);

    // Then
    verify(itemRepository).findById(itemId);
    verify(itemRepository).deleteById(itemId);
  }

  @Test
  void deleteItem_withNonExistentId_shouldThrowException() {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    when(itemRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> service.deleteItem(nonExistentId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not found");
    
    verify(itemRepository).findById(nonExistentId);
    verify(itemRepository, never()).deleteById(any());
  }

  @Test
  void deleteAllItemsInList_shouldFindAndDeleteAllItems() {
    // Given
    TodoItem item1 = new TodoItem(UUID.randomUUID(), testListId, "Item 1", false, Instant.now());
    TodoItem item2 = new TodoItem(UUID.randomUUID(), testListId, "Item 2", true, Instant.now());
    when(itemRepository.findByListId(testListId)).thenReturn(List.of(item1, item2));

    // When
    int deletedCount = service.deleteAllItemsInList(testListId);

    // Then
    assertThat(deletedCount).isEqualTo(2);
    verify(itemRepository).findByListId(testListId);
    verify(itemRepository).deleteAll(List.of(item1, item2));
  }

  @Test
  void deleteAllItemsInList_withNoItems_shouldReturnZero() {
    // Given
    when(itemRepository.findByListId(testListId)).thenReturn(List.of());

    // When
    int deletedCount = service.deleteAllItemsInList(testListId);

    // Then
    assertThat(deletedCount).isEqualTo(0);
    verify(itemRepository).findByListId(testListId);
    verify(itemRepository).deleteAll(List.of());
  }

  @Test
  void getItemCountByList_shouldReturnRepositoryCount() {
    // Given
    when(itemRepository.countByListId(testListId)).thenReturn(3L);

    // When
    long count = service.getItemCountByList(testListId);

    // Then
    assertThat(count).isEqualTo(3L);
    verify(itemRepository).countByListId(testListId);
  }

  @Test
  void getCompletedItemCountByList_shouldReturnCompletedCount() {
    // Given
    when(itemRepository.countCompletedByListId(testListId)).thenReturn(2L);

    // When
    long count = service.getCompletedItemCountByList(testListId);

    // Then
    assertThat(count).isEqualTo(2L);
    verify(itemRepository).countCompletedByListId(testListId);
  }

  @Test
  void hideCompletedItemsInList_withValidList_shouldHideCompletedItems() {
    // Given
    TodoList existingList = new TodoList(testListId, "Test List", Instant.now());
    when(listRepository.findById(testListId)).thenReturn(Optional.of(existingList));
    when(itemRepository.hideCompletedItemsByListId(testListId)).thenReturn(3);

    // When
    int hiddenCount = service.hideCompletedItemsInList(testListId);

    // Then
    assertThat(hiddenCount).isEqualTo(3);
    verify(listRepository).findById(testListId);
    verify(itemRepository).hideCompletedItemsByListId(testListId);
  }

  @Test
  void hideCompletedItemsInList_withNonExistentList_shouldThrowException() {
    // Given
    UUID nonExistentListId = UUID.randomUUID();
    when(listRepository.findById(nonExistentListId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> service.hideCompletedItemsInList(nonExistentListId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("List with ID " + nonExistentListId + " not found");
    
    verify(listRepository).findById(nonExistentListId);
    verify(itemRepository, never()).hideCompletedItemsByListId(any());
  }

  @Test
  void hideCompletedItemsInList_withNoCompletedItems_shouldReturnZero() {
    // Given
    TodoList existingList = new TodoList(testListId, "Test List", Instant.now());
    when(listRepository.findById(testListId)).thenReturn(Optional.of(existingList));
    when(itemRepository.hideCompletedItemsByListId(testListId)).thenReturn(0);

    // When
    int hiddenCount = service.hideCompletedItemsInList(testListId);

    // Then
    assertThat(hiddenCount).isEqualTo(0);
    verify(listRepository).findById(testListId);
    verify(itemRepository).hideCompletedItemsByListId(testListId);
  }
}
