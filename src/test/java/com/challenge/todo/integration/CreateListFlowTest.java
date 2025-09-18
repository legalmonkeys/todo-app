package com.challenge.todo.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Integration test for creating and viewing todo lists.
 * Tests the complete flow from HTTP request to database and back.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CreateListFlowTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void createListAndView_shouldWorkEndToEnd() throws Exception {
    // Initially no lists should exist
    mockMvc
        .perform(get("/api/lists").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));

    // Create a new list
    Map<String, String> createRequest = Map.of("name", "Groceries");
    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/lists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("Groceries"))
            .andExpect(jsonPath("$.createdAt").exists())
            .andReturn();

    // Extract the created list ID from response
    String responseBody = createResult.getResponse().getContentAsString();
    @SuppressWarnings("unchecked")
    Map<String, Object> createdList = objectMapper.readValue(responseBody, Map.class);
    String listId = (String) createdList.get("id");

    // Verify the list appears in the lists endpoint
    mockMvc
        .perform(get("/api/lists").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(listId))
        .andExpect(jsonPath("$[0].name").value("Groceries"));

    // Verify the list has no items initially
    mockMvc
        .perform(get("/api/lists/{listId}/items", listId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void createList_withEmptyName_shouldShowEmptyStateMessage() throws Exception {
    Map<String, String> request = Map.of("name", "");

    mockMvc
        .perform(
            post("/api/lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createMultipleLists_shouldOrderByNewestFirst() throws Exception {
    // Create first list
    Map<String, String> request1 = Map.of("name", "First List");
    mockMvc
        .perform(
            post("/api/lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
        .andExpect(status().isCreated());

    // Small delay to ensure different timestamps
    Thread.sleep(10);

    // Create second list
    Map<String, String> request2 = Map.of("name", "Second List");
    mockMvc
        .perform(
            post("/api/lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
        .andExpect(status().isCreated());

    // Verify newest first ordering
    mockMvc
        .perform(get("/api/lists").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Second List"))
        .andExpect(jsonPath("$[1].name").value("First List"));
  }
}
