package com.challenge.todo.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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
 * End-to-end integration test for item reordering flow.
 * Tests the complete workflow: create items → reorder → verify persistence.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ItemReorderFlowTest {

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

  private String createTestItem(String listId, String text) throws Exception {
    Map<String, String> createRequest = Map.of("text", text);
    
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
  void completeReorderFlow_shouldPersistNewOrder() throws Exception {
    // Create a list and three items
    String listId = createTestList();
    String itemId1 = createTestItem(listId, "First Item");
    String itemId2 = createTestItem(listId, "Second Item");
    String itemId3 = createTestItem(listId, "Third Item");

    // Get initial order
    String initialResponse = mockMvc
        .perform(get("/api/lists/{listId}/items", listId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    JsonNode initialItems = objectMapper.readTree(initialResponse);
    assertThat(initialItems).hasSize(3);

    // Reorder: [third, first, second]
    List<String> newOrder = List.of(itemId3, itemId1, itemId2);
    Map<String, Object> reorderRequest = Map.of("itemIds", newOrder);

    mockMvc
        .perform(
            put("/api/lists/{listId}/items/reorder", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reorderRequest)))
        .andExpect(status().isNoContent());

    // Verify new order persists
    String reorderedResponse = mockMvc
        .perform(get("/api/lists/{listId}/items", listId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    JsonNode reorderedItems = objectMapper.readTree(reorderedResponse);
    assertThat(reorderedItems).hasSize(3);
    
    // Check positions and order
    assertThat(reorderedItems.get(0).get("id").asText()).isEqualTo(itemId3);
    assertThat(reorderedItems.get(0).get("position").asInt()).isEqualTo(0);
    assertThat(reorderedItems.get(0).get("text").asText()).isEqualTo("Third Item");
    
    assertThat(reorderedItems.get(1).get("id").asText()).isEqualTo(itemId1);
    assertThat(reorderedItems.get(1).get("position").asInt()).isEqualTo(1);
    assertThat(reorderedItems.get(1).get("text").asText()).isEqualTo("First Item");
    
    assertThat(reorderedItems.get(2).get("id").asText()).isEqualTo(itemId2);
    assertThat(reorderedItems.get(2).get("position").asInt()).isEqualTo(2);
    assertThat(reorderedItems.get(2).get("text").asText()).isEqualTo("Second Item");
  }

  @Test
  void reorderWithMissingIds_shouldReturnBadRequest() throws Exception {
    String listId = createTestList();
    createTestItem(listId, "Item 1");
    createTestItem(listId, "Item 2");

    // Try to reorder with missing item ID
    Map<String, Object> reorderRequest = Map.of("itemIds", List.of("00000000-0000-0000-0000-000000000999"));

    mockMvc
        .perform(
            put("/api/lists/{listId}/items/reorder", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reorderRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void reorderWithExtraIds_shouldReturnBadRequest() throws Exception {
    String listId = createTestList();
    String itemId1 = createTestItem(listId, "Item 1");

    // Try to reorder with extra item ID
    Map<String, Object> reorderRequest = Map.of("itemIds", List.of(itemId1, "00000000-0000-0000-0000-000000000999"));

    mockMvc
        .perform(
            put("/api/lists/{listId}/items/reorder", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reorderRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void newItemsAppendToEnd_shouldHaveCorrectPosition() throws Exception {
    String listId = createTestList();
    String itemId1 = createTestItem(listId, "First Item");
    String itemId2 = createTestItem(listId, "Second Item");

    // Reorder to put second item first
    Map<String, Object> reorderRequest = Map.of("itemIds", List.of(itemId2, itemId1));
    mockMvc
        .perform(
            put("/api/lists/{listId}/items/reorder", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reorderRequest)))
        .andExpect(status().isNoContent());

    // Add a new item
    String itemId3 = createTestItem(listId, "Third Item");

    // Verify new item is at the end
    String response = mockMvc
        .perform(get("/api/lists/{listId}/items", listId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    JsonNode items = objectMapper.readTree(response);
    assertThat(items).hasSize(3);
    assertThat(items.get(2).get("id").asText()).isEqualTo(itemId3);
    assertThat(items.get(2).get("position").asInt()).isEqualTo(2);
    assertThat(items.get(2).get("text").asText()).isEqualTo("Third Item");
  }
}
