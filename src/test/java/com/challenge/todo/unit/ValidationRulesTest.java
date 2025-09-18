package com.challenge.todo.unit;

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

/**
 * Unit tests for validation rules: blocking empty/whitespace names and items, max length 50.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ValidationRulesTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void createList_withEmptyName_shouldReturn400() throws Exception {
    Map<String, String> request = Map.of("name", "");

    mockMvc
        .perform(
            post("/api/lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createList_withWhitespaceOnlyName_shouldReturn400() throws Exception {
    Map<String, String> request = Map.of("name", "   ");

    mockMvc
        .perform(
            post("/api/lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createList_withNameTooLong_shouldReturn400() throws Exception {
    String longName = "a".repeat(51); // 51 characters - exceeds 50 limit
    Map<String, String> request = Map.of("name", longName);

    mockMvc
        .perform(
            post("/api/lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createList_withMaxLengthName_shouldSucceed() throws Exception {
    String maxName = "a".repeat(50); // Exactly 50 characters
    Map<String, String> request = Map.of("name", maxName);

    mockMvc
        .perform(
            post("/api/lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value(maxName));
  }

  @Test
  void createList_withDuplicateName_shouldReturn400() throws Exception {
    // Create first list
    String duplicateName = "Duplicate " + System.currentTimeMillis();
    Map<String, String> request1 = Map.of("name", duplicateName);
    mockMvc
        .perform(
            post("/api/lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
        .andExpect(status().isCreated());

    // Try to create second list with same name
    Map<String, String> request2 = Map.of("name", duplicateName);
    mockMvc
        .perform(
            post("/api/lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
        .andExpect(status().isConflict());
  }

  @Test
  void createItem_withEmptyText_shouldReturn400() throws Exception {
    // First create a list
    String listId = createTestList();

    Map<String, String> request = Map.of("text", "");
    mockMvc
        .perform(
            post("/api/lists/{listId}/items", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createItem_withWhitespaceOnlyText_shouldReturn400() throws Exception {
    String listId = createTestList();

    Map<String, String> request = Map.of("text", "   ");
    mockMvc
        .perform(
            post("/api/lists/{listId}/items", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createItem_withTextTooLong_shouldReturn400() throws Exception {
    String listId = createTestList();

    String longText = "a".repeat(51); // 51 characters - exceeds 50 limit
    Map<String, String> request = Map.of("text", longText);
    mockMvc
        .perform(
            post("/api/lists/{listId}/items", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createItem_withMaxLengthText_shouldSucceed() throws Exception {
    String listId = createTestList();

    String maxText = "a".repeat(50); // Exactly 50 characters
    Map<String, String> request = Map.of("text", maxText);
    mockMvc
        .perform(
            post("/api/lists/{listId}/items", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.text").value(maxText));
  }

  private String createTestList() throws Exception {
    Map<String, String> listRequest = Map.of("name", "Test List");
    String response =
        mockMvc
            .perform(
                post("/api/lists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    @SuppressWarnings("unchecked")
    Map<String, Object> created = objectMapper.readValue(response, Map.class);
    return (String) created.get("id");
  }
}
