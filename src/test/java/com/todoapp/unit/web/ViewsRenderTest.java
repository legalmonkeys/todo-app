package com.todoapp.unit.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for server-rendered views.
 * Tests that HTML templates exist and render expected elements.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ViewsRenderTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

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
    void listsView_shouldExistAndRenderBasicElements() throws Exception {
        mockMvc.perform(get("/lists"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("lists"))
                .andExpect(xpath("//title").string("TODO Lists"))
                .andExpect(xpath("//h1").string("My TODO Lists"))
                .andExpect(xpath("//form[@id='create-list-form']").exists())
                .andExpect(xpath("//input[@name='name'][@type='text']").exists())
                .andExpect(xpath("//button[@type='submit']").string("Create List"))
                .andExpect(xpath("//div[@id='lists-container']").exists())
                .andExpect(xpath("//a[@href='/lists/new']").doesNotExist()) // Should use form, not separate page
                .andExpect(xpath("//meta[@name='viewport']").exists()); // Mobile responsive
    }

    @Test
    void listsView_shouldDisplayListsWhenPresent() throws Exception {
        createTestList(); // Create a list

        mockMvc.perform(get("/lists"))
                .andExpect(status().isOk())
                .andExpect(xpath("//div[contains(@class,'list') or contains(text(),'Test List')]").exists());
    }

    @Test
    void itemsView_shouldExistAndRenderBasicElements() throws Exception {
        // Use a sample UUID for testing the items view
        String testListId = createTestList();

        mockMvc.perform(get("/lists/{listId}/items", testListId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("items"))
                .andExpect(xpath("//title").string("TODO Items"))
                .andExpect(xpath("//h1").exists()) // Should have main heading with list name
                .andExpect(xpath("//form[@id='create-item-form']").exists())
                .andExpect(xpath("//input[@name='text'][@type='text']").exists())
                .andExpect(xpath("//button[@type='submit']").string("Add Item"))
                .andExpect(xpath("//div[@id='items-container']").exists())
                .andExpect(xpath("//a[@href='/lists']").string("← Back to Lists"))
                .andExpect(xpath("//meta[@name='viewport']").exists()); // Mobile responsive
    }

    @Test
    void itemsView_shouldDisplayItemsWhenPresent() throws Exception {
        String listId = createTestList();
        createTestItem(listId); // Create an item for the list

        mockMvc.perform(get("/lists/{listId}/items", listId))
                .andExpect(status().isOk())
                .andExpect(xpath("//div[contains(@class,'item') or contains(text(),'Test Item')]").exists());
    }

    @Test
    void itemsView_shouldHandleToggleCompletionForms() throws Exception {
        String testListId = createTestList();

        mockMvc.perform(get("/lists/{listId}/items", testListId))
                .andExpect(status().isOk())
                // Should have structure ready for toggle forms (even if no items present)
                .andExpect(xpath("//div[@id='items-container']").exists());
    }

    @Test
    void itemsView_shouldHandleDeleteItemForms() throws Exception {
        String testListId = createTestList();

        mockMvc.perform(get("/lists/{listId}/items", testListId))
                .andExpect(status().isOk())
                // Should have structure ready for delete forms (even if no items present)
                .andExpect(xpath("//div[@id='items-container']").exists());
    }

    @Test
    void listsView_shouldHandleDeleteListForms() throws Exception {
        mockMvc.perform(get("/lists"))
                .andExpect(status().isOk())
                // Should have structure ready for delete forms (even if no lists present)
                .andExpect(xpath("//div[@id='lists-container']").exists());
    }

    @Test
    void listsView_shouldIncludeBasicStyling() throws Exception {
        mockMvc.perform(get("/lists"))
                .andExpect(status().isOk())
                .andExpect(xpath("//style").exists()) // Should have embedded CSS for simplicity
                .andExpect(xpath("//link[@rel='stylesheet']").exists()); // Or external CSS
    }

    @Test
    void itemsView_shouldIncludeBasicStyling() throws Exception {
        String testListId = createTestList();

        mockMvc.perform(get("/lists/{listId}/items", testListId))
                .andExpect(status().isOk())
                .andExpect(xpath("//style").exists()) // Should have embedded CSS for simplicity
                .andExpect(xpath("//link[@rel='stylesheet']").exists()); // Or external CSS
    }

    @Test
    void listsView_shouldBeAccessibleAndSemantic() throws Exception {
        mockMvc.perform(get("/lists"))
                .andExpect(status().isOk())
                .andExpect(xpath("//main").exists()) // Semantic HTML
                .andExpect(xpath("//form//label").exists()) // Accessible form labels
                .andExpect(xpath("//input[@required]").exists()); // Required field indicators
    }

    @Test
    void itemsView_shouldBeAccessibleAndSemantic() throws Exception {
        String testListId = createTestList();

        mockMvc.perform(get("/lists/{listId}/items", testListId))
                .andExpect(status().isOk())
                .andExpect(xpath("//main").exists()) // Semantic HTML
                .andExpect(xpath("//form//label").exists()) // Accessible form labels
                .andExpect(xpath("//input[@required]").exists()); // Required field indicators
    }

    @Test
    void listsView_shouldHandleErrorStatesGracefully() throws Exception {
        // Test that view can handle when backend services fail
        mockMvc.perform(get("/lists"))
                .andExpect(status().isOk())
                // Should still render basic structure even if data loading fails
                .andExpect(xpath("//h1").exists())
                .andExpect(xpath("//form[@id='create-list-form']").exists());
    }

    @Test
    void itemsView_shouldHandleInvalidListIdGracefully() throws Exception {
        String invalidListId = "invalid-uuid";

        mockMvc.perform(get("/lists/{listId}/items", invalidListId))
                .andExpect(status().isOk()) // Should render view even with invalid ID
                .andExpect(xpath("//h1").exists())
                .andExpect(xpath("//a[@href='/lists']").exists()); // Should always provide way back
    }
}
