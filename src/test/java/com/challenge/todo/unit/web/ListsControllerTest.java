package com.challenge.todo.unit.web;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.challenge.todo.domain.TodoList;
import com.challenge.todo.service.TodoListService;
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
 * Unit tests for ListsController REST API endpoints.
 * Tests controller layer with mocked service dependencies using MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:webtest"
})
class ListsControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private TodoListService listService;

  @Test
  void getAllLists_shouldReturnListsInJsonFormat() throws Exception {
    // Given
    Instant now = Instant.now();
    TodoList list1 = new TodoList(UUID.randomUUID(), "Work Tasks", now.minusSeconds(10));
    TodoList list2 = new TodoList(UUID.randomUUID(), "Personal Tasks", now);
    when(listService.getAllLists()).thenReturn(List.of(list2, list1)); // Newest first

    // When & Then
    mockMvc.perform(get("/api/lists")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Personal Tasks"))
        .andExpect(jsonPath("$[1].name").value("Work Tasks"))
        .andExpect(jsonPath("$[0].id").isString())
        .andExpect(jsonPath("$[0].createdAt").isString());

    verify(listService).getAllLists();
  }

  @Test
  void getAllLists_withNoLists_shouldReturnEmptyArray() throws Exception {
    // Given
    when(listService.getAllLists()).thenReturn(List.of());

    // When & Then
    mockMvc.perform(get("/api/lists")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));

    verify(listService).getAllLists();
  }

  @Test
  void getListById_withExistingId_shouldReturnList() throws Exception {
    // Given
    UUID listId = UUID.randomUUID();
    TodoList existingList = new TodoList(listId, "Test List", Instant.now());
    when(listService.getListById(listId)).thenReturn(Optional.of(existingList));

    // When & Then
    mockMvc.perform(get("/api/lists/{id}", listId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(listId.toString()))
        .andExpect(jsonPath("$.name").value("Test List"))
        .andExpect(jsonPath("$.createdAt").isString());

    verify(listService).getListById(listId);
  }

  @Test
  void getListById_withNonExistentId_shouldReturnNotFound() throws Exception {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    when(listService.getListById(nonExistentId)).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/api/lists/{id}", nonExistentId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    verify(listService).getListById(nonExistentId);
  }

  @Test
  void createList_withValidData_shouldCreateAndReturnList() throws Exception {
    // Given
    String listName = "New List";
    UUID newId = UUID.randomUUID();
    TodoList createdList = new TodoList(newId, listName, Instant.now());
    
    when(listService.createList(listName)).thenReturn(createdList);

    Map<String, String> requestBody = Map.of("name", listName);

    // When & Then
    mockMvc.perform(post("/api/lists")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(newId.toString()))
        .andExpect(jsonPath("$.name").value(listName))
        .andExpect(jsonPath("$.createdAt").isString());

    verify(listService).createList(listName);
  }

  @Test
  void createList_withEmptyName_shouldReturnBadRequest() throws Exception {
    // Given
    Map<String, String> requestBody = Map.of("name", "");
    when(listService.createList("")).thenThrow(new IllegalArgumentException("List name cannot be empty"));

    // When & Then
    mockMvc.perform(post("/api/lists")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").exists())
        .andExpect(jsonPath("$.message").value("List name cannot be empty"));

    verify(listService).createList("");
  }

  @Test
  void createList_withDuplicateName_shouldReturnConflict() throws Exception {
    // Given
    String duplicateName = "Existing List";
    Map<String, String> requestBody = Map.of("name", duplicateName);
    when(listService.createList(duplicateName))
        .thenThrow(new IllegalArgumentException("List with name 'Existing List' already exists"));

    // When & Then
    mockMvc.perform(post("/api/lists")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").exists())
        .andExpect(jsonPath("$.message").value("List with name 'Existing List' already exists"));

    verify(listService).createList(duplicateName);
  }

  @Test
  void createList_withMissingName_shouldReturnBadRequest() throws Exception {
    // Given - empty request body
    Map<String, Object> requestBody = Map.of();

    // When & Then
    mockMvc.perform(post("/api/lists")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").exists());
  }

  @Test
  void updateList_withValidData_shouldUpdateAndReturnList() throws Exception {
    // Given
    UUID listId = UUID.randomUUID();
    String newName = "Updated List";
    TodoList updatedList = new TodoList(listId, newName, Instant.now());
    
    when(listService.renameList(listId, newName)).thenReturn(updatedList);

    Map<String, String> requestBody = Map.of("name", newName);

    // When & Then
    mockMvc.perform(patch("/api/lists/{id}", listId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(listId.toString()))
        .andExpect(jsonPath("$.name").value(newName))
        .andExpect(jsonPath("$.createdAt").isString());

    verify(listService).renameList(listId, newName);
  }

  @Test
  void updateList_withNonExistentId_shouldReturnNotFound() throws Exception {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    String newName = "Updated Name";
    when(listService.renameList(nonExistentId, newName))
        .thenThrow(new IllegalArgumentException("List with ID " + nonExistentId + " not found"));

    Map<String, String> requestBody = Map.of("name", newName);

    // When & Then
    mockMvc.perform(patch("/api/lists/{id}", nonExistentId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").exists());

    verify(listService).renameList(nonExistentId, newName);
  }

  @Test
  void updateList_withInvalidName_shouldReturnBadRequest() throws Exception {
    // Given
    UUID listId = UUID.randomUUID();
    String invalidName = "A".repeat(51); // Too long
    when(listService.renameList(listId, invalidName))
        .thenThrow(new IllegalArgumentException("List name is too long"));

    Map<String, String> requestBody = Map.of("name", invalidName);

    // When & Then
    mockMvc.perform(patch("/api/lists/{id}", listId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").exists());

    verify(listService).renameList(listId, invalidName);
  }

  @Test
  void deleteList_withExistingId_shouldReturnNoContent() throws Exception {
    // Given
    UUID listId = UUID.randomUUID();
    doNothing().when(listService).deleteList(listId);

    // When & Then
    mockMvc.perform(delete("/api/lists/{id}", listId))
        .andExpect(status().isNoContent());

    verify(listService).deleteList(listId);
  }

  @Test
  void deleteList_withNonExistentId_shouldReturnNotFound() throws Exception {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    doThrow(new IllegalArgumentException("List with ID " + nonExistentId + " not found"))
        .when(listService).deleteList(nonExistentId);

    // When & Then
    mockMvc.perform(delete("/api/lists/{id}", nonExistentId))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").exists());

    verify(listService).deleteList(nonExistentId);
  }

  @Test
  void getListCount_shouldReturnCount() throws Exception {
    // Given
    when(listService.getListCount()).thenReturn(5L);

    // When & Then
    mockMvc.perform(get("/api/lists/count")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.count").value(5));

    verify(listService).getListCount();
  }

  @Test
  void checkListExists_withExistingName_shouldReturnTrue() throws Exception {
    // Given
    String existingName = "Existing List";
    when(listService.listExists(existingName)).thenReturn(true);

    // When & Then
    mockMvc.perform(get("/api/lists/exists")
            .param("name", existingName)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.exists").value(true))
        .andExpect(jsonPath("$.name").value(existingName));

    verify(listService).listExists(existingName);
  }

  @Test
  void checkListExists_withNonExistentName_shouldReturnFalse() throws Exception {
    // Given
    String nonExistentName = "Non-existent List";
    when(listService.listExists(nonExistentName)).thenReturn(false);

    // When & Then
    mockMvc.perform(get("/api/lists/exists")
            .param("name", nonExistentName)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.exists").value(false))
        .andExpect(jsonPath("$.name").value(nonExistentName));

    verify(listService).listExists(nonExistentName);
  }

  @Test
  void checkListExists_withMissingNameParam_shouldReturnBadRequest() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/lists/exists")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").exists());
  }
}
