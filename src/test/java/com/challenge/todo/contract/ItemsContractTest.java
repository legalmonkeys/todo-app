package com.challenge.todo.contract;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contract tests for Items endpoints based on OpenAPI specification.
 * These tests verify the API contract without depending on implementation details.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ItemsContractTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private String createTestList() throws Exception {
    String uniqueName = "Test List " + UUID.randomUUID().toString().substring(0, 8);
    Map<String, String> createRequest = Map.of("name", uniqueName);
    
    String response = mockMvc
        .perform(
            post("/api/lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();
    
    return objectMapper.readTree(response).get("id").asText();
  }

  private String createTestItem(String listId) throws Exception {
    Map<String, String> createRequest = Map.of("text", "Test Item");
    
    String response = mockMvc
        .perform(
            post("/api/lists/{listId}/items", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();
    
    return objectMapper.readTree(response).get("id").asText();
  }

  @Test
  void getItemsInList_shouldReturn200() throws Exception {
    String listId = createTestList();
    
    mockMvc
        .perform(get("/api/lists/{listId}/items", listId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void createItem_withValidText_shouldReturn201() throws Exception {
    String listId = createTestList();
    Map<String, String> request = Map.of("text", "Test Item");

    mockMvc
        .perform(
            post("/api/lists/{listId}/items", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.listId").value(listId))
        .andExpect(jsonPath("$.text").value("Test Item"))
        .andExpect(jsonPath("$.completed").value(false))
        .andExpect(jsonPath("$.createdAt").exists());
  }

  @Test
  void createItem_withInvalidText_shouldReturn400() throws Exception {
    Map<String, String> request = Map.of("text", "");

    mockMvc
        .perform(
            post("/api/lists/{listId}/items", createTestList())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateItem_withValidData_shouldReturn200() throws Exception {
    // First create a list and item
    String listId = createTestList();
    String itemId = createTestItem(listId);
    
    Map<String, Object> request = Map.of("text", "Updated Item", "completed", true);

    mockMvc
        .perform(
            patch("/api/items/{itemId}", itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(itemId))
        .andExpect(jsonPath("$.text").value("Updated Item"))
        .andExpect(jsonPath("$.completed").value(true))
        .andExpect(jsonPath("$.createdAt").exists());
  }

  @Test
  void deleteItem_shouldReturn204() throws Exception {
    // First create a list and item
    String listId = createTestList();
    String itemId = createTestItem(listId);
    
    mockMvc
        .perform(delete("/api/items/{itemId}", itemId))
        .andExpect(status().isNoContent());
  }

  @Test
  void getItems_shouldReturnOrderedByPosition() throws Exception {
    String listId = createTestList();
    createTestItem(listId);
    createTestItem(listId);
    createTestItem(listId);

    mockMvc
        .perform(get("/api/lists/{listId}/items", listId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].position").exists())
        .andExpect(jsonPath("$[1].position").exists())
        .andExpect(jsonPath("$[2].position").exists());
  }

  @Test
  void reorderItems_withValidPayload_shouldReturn204() throws Exception {
    String listId = createTestList();
    String itemId1 = createTestItem(listId);
    String itemId2 = createTestItem(listId);
    String itemId3 = createTestItem(listId);
    
    Map<String, Object> reorderRequest = Map.of(
        "itemIds", java.util.List.of(itemId3, itemId1, itemId2)
    );

    mockMvc
        .perform(
            put("/api/lists/{listId}/items/reorder", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reorderRequest)))
        .andExpect(status().isNoContent());
  }

  @Test
  void reorderItems_withInvalidItemIds_shouldReturn400() throws Exception {
    String listId = createTestList();
    createTestItem(listId);
    
    // Include non-existent item ID
    Map<String, Object> reorderRequest = Map.of(
        "itemIds", java.util.List.of("00000000-0000-0000-0000-000000000999", "00000000-0000-0000-0000-000000000888")
    );

    mockMvc
        .perform(
            put("/api/lists/{listId}/items/reorder", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reorderRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void reorderItems_withNonExistentList_shouldReturn404() throws Exception {
    Map<String, Object> reorderRequest = Map.of("itemIds", java.util.List.of("00000000-0000-0000-0000-000000000001", "00000000-0000-0000-0000-000000000002"));

    mockMvc
        .perform(
            put("/api/lists/{listId}/items/reorder", "00000000-0000-0000-0000-000000000999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reorderRequest)))
        .andExpect(status().isNotFound());
  }
}
