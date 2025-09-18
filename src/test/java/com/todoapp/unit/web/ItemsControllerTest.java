package com.todoapp.unit.web;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.todoapp.domain.TodoItem;
import com.todoapp.service.TodoItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for ItemsController REST API endpoints.
 * Tests controller layer with mocked service dependencies using MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:itemswebtest"
})
class ItemsControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private TodoItemService itemService;

  @Test
  void getItemsByList_shouldReturnItemsInJsonFormat() throws Exception {
    // Given
    UUID listId = UUID.randomUUID();
    Instant now = Instant.now();
    TodoItem item1 = new TodoItem(UUID.randomUUID(), listId, "Buy groceries", false, now.minusSeconds(10));
    TodoItem item2 = new TodoItem(UUID.randomUUID(), listId, "Walk the dog", true, now);
    when(itemService.getItemsByList(listId)).thenReturn(List.of(item2, item1)); // Newest first

    // When & Then
    mockMvc.perform(get("/api/lists/{listId}/items", listId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].text").value("Walk the dog"))
        .andExpect(jsonPath("$[0].completed").value(true))
        .andExpect(jsonPath("$[1].text").value("Buy groceries"))
        .andExpect(jsonPath("$[1].completed").value(false))
        .andExpect(jsonPath("$[0].listId").value(listId.toString()));

    verify(itemService).getItemsByList(listId);
  }

  @Test
  void getItemsByList_withNoItems_shouldReturnEmptyArray() throws Exception {
    // Given
    UUID listId = UUID.randomUUID();
    when(itemService.getItemsByList(listId)).thenReturn(List.of());

    // When & Then
    mockMvc.perform(get("/api/lists/{listId}/items", listId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));

    verify(itemService).getItemsByList(listId);
  }

  @Test
  void getItemsByListAndStatus_shouldFilterByCompletedStatusTrue() throws Exception {
    // Given
    UUID listId = UUID.randomUUID();
    TodoItem completedItem = new TodoItem(UUID.randomUUID(), listId, "Completed task", true, Instant.now());
    when(itemService.getItemsByListAndStatus(listId, true)).thenReturn(List.of(completedItem));

    // When & Then
    mockMvc.perform(get("/api/lists/{listId}/items", listId)
            .param("completed", "true")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].text").value("Completed task"))
        .andExpect(jsonPath("$[0].completed").value(true));

    verify(itemService).getItemsByListAndStatus(listId, true);
  }

@Test
  void getItemsByListAndStatus_shouldFilterByCompletedStatusFalse() throws Exception {
    // Given
    UUID listId = UUID.randomUUID();
    TodoItem notCompletedItem = new TodoItem(UUID.randomUUID(), listId, "Not Completed task", false, Instant.now());
    when(itemService.getItemsByListAndStatus(listId, true)).thenReturn(List.of(notCompletedItem));

    // When & Then
    mockMvc.perform(get("/api/lists/{listId}/items", listId)
            .param("completed", "true")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].text").value("Not Completed task"))
        .andExpect(jsonPath("$[0].completed").value(false));

    verify(itemService).getItemsByListAndStatus(listId, true);
  }

  @Test
  void getItemById_withExistingId_shouldReturnItem() throws Exception {
    // Given
    UUID itemId = UUID.randomUUID();
    UUID listId = UUID.randomUUID();
    TodoItem existingItem = new TodoItem(itemId, listId, "Test Item", false, Instant.now());
    when(itemService.getItemById(itemId)).thenReturn(Optional.of(existingItem));

    // When & Then
    mockMvc.perform(get("/api/items/{id}", itemId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(itemId.toString()))
        .andExpect(jsonPath("$.text").value("Test Item"))
        .andExpect(jsonPath("$.completed").value(false))
        .andExpect(jsonPath("$.listId").value(listId.toString()));

    verify(itemService).getItemById(itemId);
  }

  @Test
  void getItemById_withNonExistentId_shouldReturnNotFound() throws Exception {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    when(itemService.getItemById(nonExistentId)).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/api/items/{id}", nonExistentId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    verify(itemService).getItemById(nonExistentId);
  }

  @Test
  void createItem_withValidData_shouldCreateAndReturnItem() throws Exception {
    // Given
    UUID listId = UUID.randomUUID();
    String itemText = "New task";
    UUID newId = UUID.randomUUID();
    TodoItem createdItem = new TodoItem(newId, listId, itemText, false, Instant.now());
    
    when(itemService.createItem(listId, itemText)).thenReturn(createdItem);

    Map<String, String> requestBody = Map.of("text", itemText);

    // When & Then
    mockMvc.perform(post("/api/lists/{listId}/items", listId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(newId.toString()))
        .andExpect(jsonPath("$.text").value(itemText))
        .andExpect(jsonPath("$.completed").value(false))
        .andExpect(jsonPath("$.listId").value(listId.toString()));

    verify(itemService).createItem(listId, itemText);
  }

  @Test
  void createItem_withEmptyText_shouldReturnBadRequest() throws Exception {
    // Given
    UUID listId = UUID.randomUUID();
    Map<String, String> requestBody = Map.of("text", "");
    when(itemService.createItem(listId, "")).thenThrow(new IllegalArgumentException("Item text cannot be empty"));

    // When & Then
    mockMvc.perform(post("/api/lists/{listId}/items", listId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").exists())
        .andExpect(jsonPath("$.message").value("Item text cannot be empty"));

    verify(itemService).createItem(listId, "");
  }

  @Test
  void createItem_withNonExistentList_shouldReturnNotFound() throws Exception {
    // Given
    UUID nonExistentListId = UUID.randomUUID();
    String itemText = "New task";
    Map<String, String> requestBody = Map.of("text", itemText);
    when(itemService.createItem(nonExistentListId, itemText))
        .thenThrow(new IllegalArgumentException("List with ID " + nonExistentListId + " not found"));

    // When & Then
    mockMvc.perform(post("/api/lists/{listId}/items", nonExistentListId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").exists());

    verify(itemService).createItem(nonExistentListId, itemText);
  }

  @Test
  void updateItemText_withValidData_shouldUpdateAndReturnItem() throws Exception {
    // Given
    UUID itemId = UUID.randomUUID();
    UUID listId = UUID.randomUUID();
    String newText = "Updated task";
    TodoItem updatedItem = new TodoItem(itemId, listId, newText, false, Instant.now());
    
    when(itemService.updateItemText(itemId, newText)).thenReturn(updatedItem);

    Map<String, String> requestBody = Map.of("text", newText);

    // When & Then
    mockMvc.perform(patch("/api/items/{id}", itemId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(itemId.toString()))
        .andExpect(jsonPath("$.text").value(newText))
        .andExpect(jsonPath("$.listId").value(listId.toString()));

    verify(itemService).updateItemText(itemId, newText);
  }

  @Test
  void updateItemText_withNonExistentId_shouldReturnNotFound() throws Exception {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    String newText = "Updated text";
    when(itemService.updateItemText(nonExistentId, newText))
        .thenThrow(new IllegalArgumentException("Item with ID " + nonExistentId + " not found"));

    Map<String, String> requestBody = Map.of("text", newText);

    // When & Then
    mockMvc.perform(patch("/api/items/{id}", nonExistentId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").exists());

    verify(itemService).updateItemText(nonExistentId, newText);
  }

  @Test
  void toggleItemCompletion_shouldToggleAndReturnItem() throws Exception {
    // Given
    UUID itemId = UUID.randomUUID();
    UUID listId = UUID.randomUUID();
    TodoItem toggledItem = new TodoItem(itemId, listId, "Task", true, Instant.now()); // Toggled to true
    
    when(itemService.toggleItemCompletion(itemId)).thenReturn(toggledItem);

    // When & Then
    mockMvc.perform(patch("/api/items/{id}/toggle", itemId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(itemId.toString()))
        .andExpect(jsonPath("$.completed").value(true));

    verify(itemService).toggleItemCompletion(itemId);
  }

  @Test
  void toggleItemCompletion_withNonExistentId_shouldReturnNotFound() throws Exception {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    when(itemService.toggleItemCompletion(nonExistentId))
        .thenThrow(new IllegalArgumentException("Item with ID " + nonExistentId + " not found"));

    // When & Then
    mockMvc.perform(patch("/api/items/{id}/toggle", nonExistentId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").exists());

    verify(itemService).toggleItemCompletion(nonExistentId);
  }

  @Test
  void deleteItem_withExistingId_shouldReturnNoContent() throws Exception {
    // Given
    UUID itemId = UUID.randomUUID();
    doNothing().when(itemService).deleteItem(itemId);

    // When & Then
    mockMvc.perform(delete("/api/items/{id}", itemId))
        .andExpect(status().isNoContent());

    verify(itemService).deleteItem(itemId);
  }

  @Test
  void deleteItem_withNonExistentId_shouldReturnNotFound() throws Exception {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    doThrow(new IllegalArgumentException("Item with ID " + nonExistentId + " not found"))
        .when(itemService).deleteItem(nonExistentId);

    // When & Then
    mockMvc.perform(delete("/api/items/{id}", nonExistentId))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").exists());

    verify(itemService).deleteItem(nonExistentId);
  }

  @Test
  void getItemCountByList_shouldReturnCount() throws Exception {
    // Given
    UUID listId = UUID.randomUUID();
    when(itemService.getItemCountByList(listId)).thenReturn(5L);

    // When & Then
    mockMvc.perform(get("/api/lists/{listId}/items/count", listId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.count").value(5))
        .andExpect(jsonPath("$.listId").value(listId.toString()));

    verify(itemService).getItemCountByList(listId);
  }

  @Test
  void getCompletedItemCountByList_shouldReturnCompletedCount() throws Exception {
    // Given
    UUID listId = UUID.randomUUID();
    when(itemService.getCompletedItemCountByList(listId)).thenReturn(3L);

    // When & Then
    mockMvc.perform(get("/api/lists/{listId}/items/completed/count", listId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.completedCount").value(3))
        .andExpect(jsonPath("$.listId").value(listId.toString()));

    verify(itemService).getCompletedItemCountByList(listId);
  }

  @Test
  void deleteAllItemsInList_shouldReturnDeletedCount() throws Exception {
    // Given
    UUID listId = UUID.randomUUID();
    when(itemService.deleteAllItemsInList(listId)).thenReturn(3);

    // When & Then
    mockMvc.perform(delete("/api/lists/{listId}/items", listId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.deletedCount").value(3))
        .andExpect(jsonPath("$.listId").value(listId.toString()));

    verify(itemService).deleteAllItemsInList(listId);
  }
}
