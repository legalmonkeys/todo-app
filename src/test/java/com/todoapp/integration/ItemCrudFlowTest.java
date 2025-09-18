package com.todoapp.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Integration test for item CRUD operations in an active list.
 * Tests the complete flow of adding, editing, and deleting items.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemCrudFlowTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private String testListId;

  @BeforeEach
  void setUp() throws Exception {
    // Create a test list to work with
    Map<String, String> listRequest = Map.of("name", "Test List");
    MvcResult result =
        mockMvc
            .perform(
                post("/api/lists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listRequest)))
            .andExpect(status().isCreated())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    @SuppressWarnings("unchecked")
    Map<String, Object> createdList = objectMapper.readValue(responseBody, Map.class);
    testListId = (String) createdList.get("id");
  }

  @Test
  void addEditDeleteItem_shouldWorkEndToEnd() throws Exception {
    // Initially no items in the list
    mockMvc
        .perform(get("/api/lists/{listId}/items", testListId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));

    // Add an item
    Map<String, String> createRequest = Map.of("text", "Buy milk");
    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/lists/{listId}/items", testListId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.listId").value(testListId))
            .andExpect(jsonPath("$.text").value("Buy milk"))
            .andExpect(jsonPath("$.completed").value(false))
            .andExpect(jsonPath("$.createdAt").exists())
            .andReturn();

    // Extract item ID
    String responseBody = createResult.getResponse().getContentAsString();
    @SuppressWarnings("unchecked")
    Map<String, Object> createdItem = objectMapper.readValue(responseBody, Map.class);
    String itemId = (String) createdItem.get("id");

    // Verify item appears in list
    mockMvc
        .perform(get("/api/lists/{listId}/items", testListId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(itemId))
        .andExpect(jsonPath("$[0].text").value("Buy milk"));

    // Edit the item text
    Map<String, Object> updateRequest = Map.of("text", "Buy oat milk");
    mockMvc
        .perform(
            patch("/api/items/{itemId}", itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(itemId))
        .andExpect(jsonPath("$.text").value("Buy oat milk"))
        .andExpect(jsonPath("$.completed").value(false));

    // Mark item as completed
    Map<String, Object> completeRequest = Map.of("completed", true);
    mockMvc
        .perform(
            patch("/api/items/{itemId}", itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(itemId))
        .andExpect(jsonPath("$.text").value("Buy oat milk"))
        .andExpect(jsonPath("$.completed").value(true));

    // Verify changes persist when navigating away and back
    mockMvc
        .perform(get("/api/lists/{listId}/items", testListId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].text").value("Buy oat milk"))
        .andExpect(jsonPath("$[0].completed").value(true));

    // Delete the item
    mockMvc
        .perform(delete("/api/items/{itemId}", itemId))
        .andExpect(status().isNoContent());

    // Verify item is removed
    mockMvc
        .perform(get("/api/lists/{listId}/items", testListId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void addMultipleItems_shouldOrderByPosition() throws Exception {
    // Add first item
    Map<String, String> request1 = Map.of("text", "First Item");
    mockMvc
        .perform(
            post("/api/lists/{listId}/items", testListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
        .andExpect(status().isCreated());

    // Small delay to ensure different timestamps
    Thread.sleep(10);

    // Add second item
    Map<String, String> request2 = Map.of("text", "Second Item");
    mockMvc
        .perform(
            post("/api/lists/{listId}/items", testListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
        .andExpect(status().isCreated());

    // Verify position-based ordering (first item added has position 0, second has position 1)
    mockMvc
        .perform(get("/api/lists/{listId}/items", testListId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].text").value("First Item"))
        .andExpect(jsonPath("$[0].position").value(0))
        .andExpect(jsonPath("$[1].text").value("Second Item"))
        .andExpect(jsonPath("$[1].position").value(1));
  }

  @Test
  void addItem_withEmptyText_shouldReturn400() throws Exception {
    Map<String, String> request = Map.of("text", "");

    mockMvc
        .perform(
            post("/api/lists/{listId}/items", testListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
