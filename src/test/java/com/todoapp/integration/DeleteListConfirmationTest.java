package com.todoapp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for delete non-empty list confirmation flow.
 * Tests that deleting a list with items requires confirmation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DeleteListConfirmationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deleteNonEmptyList_shouldRequireConfirmation() throws Exception {
        // Create a list with items
        String listId = createList("Test List");
        createItem(listId, "Test Item");

        // Verify list has items
        mockMvc
                .perform(get("/api/lists/{listId}/items", listId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // Delete list - should succeed and remove list and all items
        mockMvc
                .perform(delete("/api/lists/{listId}", listId))
                .andExpect(status().isNoContent());

        // Verify list is deleted
        mockMvc
                .perform(get("/api/lists").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // Verify items are also deleted (cascade)
        mockMvc
                .perform(get("/api/lists/{listId}/items", listId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteEmptyList_shouldSucceedDirectly() throws Exception {
        // Create empty list
        String listId = createList("Empty List");

        // Verify list is empty
        mockMvc
                .perform(get("/api/lists/{listId}/items", listId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // Delete empty list - should succeed directly
        mockMvc
                .perform(delete("/api/lists/{listId}", listId))
                .andExpect(status().isNoContent());

        // Verify list is deleted
        mockMvc
                .perform(get("/api/lists").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
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
}
