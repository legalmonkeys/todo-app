package com.todoapp.contract;

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
 * Contract tests for Lists endpoints based on OpenAPI specification.
 * These tests verify the API contract without depending on implementation details.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ListsContractTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void getAllLists_shouldReturn200() throws Exception {
    mockMvc
        .perform(get("/api/lists").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void createList_withValidName_shouldReturn201() throws Exception {
    String uniqueName = "Test List " + UUID.randomUUID().toString().substring(0, 8);
    Map<String, String> request = Map.of("name", uniqueName);

    mockMvc
        .perform(
            post("/api/lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value(uniqueName))
        .andExpect(jsonPath("$.createdAt").exists());
  }

  @Test
  void createList_withInvalidName_shouldReturn400() throws Exception {
    Map<String, String> request = Map.of("name", "");

    mockMvc
        .perform(
            post("/api/lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void renameList_withValidData_shouldReturn200() throws Exception {
    // First create a list
    String uniqueName = "Original List " + UUID.randomUUID().toString().substring(0, 8);
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
    
    // Extract the ID from the response
    String listId = objectMapper.readTree(response).get("id").asText();
    
    // Now rename the list
    String updatedName = "Updated List " + UUID.randomUUID().toString().substring(0, 8);
    Map<String, String> request = Map.of("name", updatedName);

    mockMvc
        .perform(
            patch("/api/lists/{listId}", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(listId))
        .andExpect(jsonPath("$.name").value(updatedName))
        .andExpect(jsonPath("$.createdAt").exists());
  }

  @Test
  void deleteList_shouldReturn204() throws Exception {
    // First create a list
    String uniqueName = "List to Delete " + UUID.randomUUID().toString().substring(0, 8);
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
    
    // Extract the ID from the response
    String listId = objectMapper.readTree(response).get("id").asText();
    
    // Now delete the list
    mockMvc
        .perform(delete("/api/lists/{listId}", listId))
        .andExpect(status().isNoContent());
  }
}
