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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Integration test for switching between multiple lists and preserving state.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SwitchListsFlowTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void switchBetweenLists_shouldPreserveState() throws Exception {
    // Create two lists
    String groceriesId = createList("Groceries");
    String workId = createList("Work");

    // Add items to Groceries list
    String milkItemId = createItem(groceriesId, "Buy milk");
    createItem(groceriesId, "Buy bread");

    // Add items to Work list  
    createItem(workId, "Team meeting");

    // Mark milk as completed in Groceries
    markItemCompleted(milkItemId, true);

    // Switch to Work list - should see only work items
    mockMvc
        .perform(get("/api/lists/{listId}/items", workId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].text").value("Team meeting"))
        .andExpect(jsonPath("$[0].completed").value(false));

    // Switch back to Groceries - state should be preserved
    mockMvc
        .perform(get("/api/lists/{listId}/items", groceriesId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[?(@.text == 'Buy milk')].completed").value(true))
        .andExpect(jsonPath("$[?(@.text == 'Buy bread')].completed").value(false));
  }

  private String createList(String name) throws Exception {
    Map<String, String> request = Map.of("name", name);
    MvcResult result =
        mockMvc
            .perform(
                post("/api/lists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    @SuppressWarnings("unchecked")
    Map<String, Object> created = objectMapper.readValue(responseBody, Map.class);
    return (String) created.get("id");
  }

  private String createItem(String listId, String text) throws Exception {
    Map<String, String> request = Map.of("text", text);
    MvcResult result =
        mockMvc
            .perform(
                post("/api/lists/{listId}/items", listId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    @SuppressWarnings("unchecked")
    Map<String, Object> created = objectMapper.readValue(responseBody, Map.class);
    return (String) created.get("id");
  }

  private void markItemCompleted(String itemId, boolean completed) throws Exception {
    Map<String, Object> request = Map.of("completed", completed);
    mockMvc
        .perform(
            patch("/api/items/{itemId}", itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }
}
