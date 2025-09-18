package com.challenge.todo.unit.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.challenge.todo.domain.TodoList;
import com.challenge.todo.persistence.TodoListRepository;
import com.challenge.todo.service.TodoListService;
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
 * Unit tests for TodoListService business logic.
 * Tests service layer with mocked repository dependencies.
 */
@ExtendWith(MockitoExtension.class)
class TodoListServiceTest {

  @Mock private TodoListRepository repository;

  private TodoListService service;

  @BeforeEach
  void setUp() {
    service = new TodoListService(repository);
  }

  @Test
  void createList_withValidName_shouldCreateAndReturnList() {
    // Given
    String listName = "My New List";
    when(repository.existsByName(listName)).thenReturn(false);
    when(repository.save(any(TodoList.class))).thenAnswer(invocation -> {
      TodoList saved = invocation.getArgument(0);
      saved.markNotNew(); // Simulate persistence
      return saved;
    });

    // When
    TodoList created = service.createList(listName);

    // Then
    assertThat(created).isNotNull();
    assertThat(created.getName()).isEqualTo(listName);
    assertThat(created.getId()).isNotNull();
    assertThat(created.getCreatedAt()).isNotNull();
    
    verify(repository).existsByName(listName);
    verify(repository).save(any(TodoList.class));
  }

  @Test
  void createList_withDuplicateName_shouldThrowException() {
    // Given
    String duplicateName = "Existing List";
    when(repository.existsByName(duplicateName)).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> service.createList(duplicateName))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("already exists");
    
    verify(repository).existsByName(duplicateName);
    verify(repository, never()).save(any());
  }

  @Test
  void createList_withEmptyName_shouldThrowException() {
    // When & Then
    assertThatThrownBy(() -> service.createList(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be empty");
    
    verify(repository, never()).existsByName(any());
    verify(repository, never()).save(any());
  }

  @Test
  void createList_withWhitespaceName_shouldThrowException() {
    // When & Then
    assertThatThrownBy(() -> service.createList("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be empty");
    
    verify(repository, never()).existsByName(any());
    verify(repository, never()).save(any());
  }

  @Test
  void createList_withTooLongName_shouldThrowException() {
    // Given
    String tooLongName = "A".repeat(51); // 51 characters

    // When & Then
    assertThatThrownBy(() -> service.createList(tooLongName))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("too long");
    
    verify(repository, never()).existsByName(any());
    verify(repository, never()).save(any());
  }

  @Test
  void getAllLists_shouldReturnListsOrderedByCreatedAtDesc() {
    // Given
    Instant now = Instant.now();
    TodoList older = new TodoList(UUID.randomUUID(), "Older List", now.minusSeconds(10));
    TodoList newer = new TodoList(UUID.randomUUID(), "Newer List", now);
    when(repository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(newer, older));

    // When
    List<TodoList> result = service.getAllLists();

    // Then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getName()).isEqualTo("Newer List");
    assertThat(result.get(1).getName()).isEqualTo("Older List");
    
    verify(repository).findAllOrderByCreatedAtDesc();
  }

  @Test
  void getAllLists_withNoLists_shouldReturnEmptyList() {
    // Given
    when(repository.findAllOrderByCreatedAtDesc()).thenReturn(List.of());

    // When
    List<TodoList> result = service.getAllLists();

    // Then
    assertThat(result).isEmpty();
    verify(repository).findAllOrderByCreatedAtDesc();
  }

  @Test
  void getListById_withExistingId_shouldReturnList() {
    // Given
    UUID listId = UUID.randomUUID();
    TodoList existingList = new TodoList(listId, "Test List", Instant.now());
    when(repository.findById(listId)).thenReturn(Optional.of(existingList));

    // When
    Optional<TodoList> result = service.getListById(listId);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Test List");
    
    verify(repository).findById(listId);
  }

  @Test
  void getListById_withNonExistentId_shouldReturnEmpty() {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

    // When
    Optional<TodoList> result = service.getListById(nonExistentId);

    // Then
    assertThat(result).isEmpty();
    verify(repository).findById(nonExistentId);
  }

  @Test
  void renameList_withValidData_shouldUpdateAndReturnList() {
    // Given
    UUID listId = UUID.randomUUID();
    String newName = "Renamed List";
    TodoList existingList = new TodoList(listId, "Original Name", Instant.now());
    
    when(repository.findById(listId)).thenReturn(Optional.of(existingList));
    when(repository.existsByNameAndIdNot(newName, listId)).thenReturn(false);
    when(repository.save(any(TodoList.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    TodoList renamed = service.renameList(listId, newName);

    // Then
    assertThat(renamed).isNotNull();
    assertThat(renamed.getName()).isEqualTo(newName);
    assertThat(renamed.getId()).isEqualTo(listId);
    
    verify(repository).findById(listId);
    verify(repository).existsByNameAndIdNot(newName, listId);
    verify(repository).save(existingList);
  }

  @Test
  void renameList_withNonExistentId_shouldThrowException() {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> service.renameList(nonExistentId, "New Name"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not found");
    
    verify(repository).findById(nonExistentId);
    verify(repository, never()).existsByNameAndIdNot(any(), any());
    verify(repository, never()).save(any());
  }

  @Test
  void renameList_withDuplicateName_shouldThrowException() {
    // Given
    UUID listId = UUID.randomUUID();
    String duplicateName = "Existing List";
    TodoList existingList = new TodoList(listId, "Original Name", Instant.now());
    
    when(repository.findById(listId)).thenReturn(Optional.of(existingList));
    when(repository.existsByNameAndIdNot(duplicateName, listId)).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> service.renameList(listId, duplicateName))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("already exists");
    
    verify(repository).findById(listId);
    verify(repository).existsByNameAndIdNot(duplicateName, listId);
    verify(repository, never()).save(any());
  }

  @Test
  void renameList_withInvalidName_shouldThrowException() {
    // Given
    UUID listId = UUID.randomUUID();
    TodoList existingList = new TodoList(listId, "Original Name", Instant.now());
    when(repository.findById(listId)).thenReturn(Optional.of(existingList));

    // When & Then - empty name
    assertThatThrownBy(() -> service.renameList(listId, ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be empty");
    
    // When & Then - too long name
    String tooLongName = "A".repeat(51);
    assertThatThrownBy(() -> service.renameList(listId, tooLongName))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("too long");
    
    verify(repository, times(2)).findById(listId);
    verify(repository, never()).existsByNameAndIdNot(any(), any());
    verify(repository, never()).save(any());
  }

  @Test
  void deleteList_withExistingId_shouldDeleteList() {
    // Given
    UUID listId = UUID.randomUUID();
    TodoList existingList = new TodoList(listId, "To Delete", Instant.now());
    when(repository.findById(listId)).thenReturn(Optional.of(existingList));

    // When
    service.deleteList(listId);

    // Then
    verify(repository).findById(listId);
    verify(repository).deleteById(listId);
  }

  @Test
  void deleteList_withNonExistentId_shouldThrowException() {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> service.deleteList(nonExistentId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not found");
    
    verify(repository).findById(nonExistentId);
    verify(repository, never()).deleteById(any());
  }

  @Test
  void getListCount_shouldReturnRepositoryCount() {
    // Given
    when(repository.count()).thenReturn(5L);

    // When
    long count = service.getListCount();

    // Then
    assertThat(count).isEqualTo(5L);
    verify(repository).count();
  }

  @Test
  void listExists_withExistingName_shouldReturnTrue() {
    // Given
    String existingName = "Existing List";
    when(repository.existsByName(existingName)).thenReturn(true);

    // When
    boolean exists = service.listExists(existingName);

    // Then
    assertThat(exists).isTrue();
    verify(repository).existsByName(existingName);
  }

  @Test
  void listExists_withNonExistentName_shouldReturnFalse() {
    // Given
    String nonExistentName = "Non-existent List";
    when(repository.existsByName(nonExistentName)).thenReturn(false);

    // When
    boolean exists = service.listExists(nonExistentName);

    // Then
    assertThat(exists).isFalse();
    verify(repository).existsByName(nonExistentName);
  }
}
