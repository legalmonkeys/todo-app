package com.todoapp.web;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.todoapp.domain.TodoItem;
import com.todoapp.domain.TodoList;
import com.todoapp.service.TodoItemService;
import com.todoapp.service.TodoListService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

@ExtendWith(MockitoExtension.class)
class ViewControllerTest {

    @Mock
    private TodoListService listService;

    @Mock
    private TodoItemService itemService;

    @Mock
    private Model model;

    private ViewController controller;
    private UUID testListId;
    private TodoList testList;
    private List<TodoItem> testItems;

    @BeforeEach
    void setUp() {
        controller = new ViewController(listService, itemService);
        testListId = UUID.randomUUID();
        testList = new TodoList(testListId, "Test List", Instant.now());
        
        // Create test items
        TodoItem item1 = new TodoItem(UUID.randomUUID(), testListId, "Item 1", false, Instant.now());
        TodoItem item2 = new TodoItem(UUID.randomUUID(), testListId, "Item 2", true, Instant.now());
        
        testItems = List.of(item1, item2);
    }

    @Test
    void itemsPage_withValidListId_shouldReturnItemsViewWithCorrectAttributes() {
        // Given
        String listIdStr = testListId.toString();
        when(listService.getListById(testListId)).thenReturn(Optional.of(testList));
        when(itemService.getItemsByList(testListId)).thenReturn(testItems);

        // When
        String result = controller.itemsPage(listIdStr, null, model);

        // Then
        assertThat(result).isEqualTo("items");
        verify(model).addAttribute("list", testList);
        verify(model).addAttribute("items", testItems);
        verify(model).addAttribute("hasItems", true);
        verify(model).addAttribute("listId", listIdStr);
        verify(model).addAttribute("hideCompleted", null);
        verify(model, never()).addAttribute(eq("error"), any());
    }

    @Test
    void itemsPage_withValidListIdAndHideCompletedTrue_shouldReturnFilteredItems() {
        // Given
        String listIdStr = testListId.toString();
        List<TodoItem> incompleteItems = List.of(testItems.get(0)); // Only incomplete item
        when(listService.getListById(testListId)).thenReturn(Optional.of(testList));
        when(itemService.getItemsByListAndStatus(testListId, false)).thenReturn(incompleteItems);

        // When
        String result = controller.itemsPage(listIdStr, true, model);

        // Then
        assertThat(result).isEqualTo("items");
        verify(model).addAttribute("list", testList);
        verify(model).addAttribute("items", incompleteItems);
        verify(model).addAttribute("hasItems", true);
        verify(model).addAttribute("listId", listIdStr);
        verify(model).addAttribute("hideCompleted", true);
        verify(model, never()).addAttribute(eq("error"), any());
    }

    @Test
    void itemsPage_withValidListIdAndHideCompletedFalse_shouldReturnAllItems() {
        // Given
        String listIdStr = testListId.toString();
        when(listService.getListById(testListId)).thenReturn(Optional.of(testList));
        when(itemService.getItemsByList(testListId)).thenReturn(testItems);

        // When
        String result = controller.itemsPage(listIdStr, false, model);

        // Then
        assertThat(result).isEqualTo("items");
        verify(model).addAttribute("list", testList);
        verify(model).addAttribute("items", testItems);
        verify(model).addAttribute("hasItems", true);
        verify(model).addAttribute("listId", listIdStr);
        verify(model).addAttribute("hideCompleted", false);
        verify(model, never()).addAttribute(eq("error"), any());
    }

    @Test
    void itemsPage_withEmptyItemsList_shouldSetHasItemsFalse() {
        // Given
        String listIdStr = testListId.toString();
        List<TodoItem> emptyItems = List.of();
        when(listService.getListById(testListId)).thenReturn(Optional.of(testList));
        when(itemService.getItemsByList(testListId)).thenReturn(emptyItems);

        // When
        String result = controller.itemsPage(listIdStr, null, model);

        // Then
        assertThat(result).isEqualTo("items");
        verify(model).addAttribute("list", testList);
        verify(model).addAttribute("items", emptyItems);
        verify(model).addAttribute("hasItems", false);
        verify(model).addAttribute("listId", listIdStr);
        verify(model).addAttribute("hideCompleted", null);
        verify(model, never()).addAttribute(eq("error"), any());
    }

    @Test
    void itemsPage_withInvalidUuid_shouldHandleExceptionGracefully() {
        // Given
        String invalidListId = "invalid-uuid";

        // When
        String result = controller.itemsPage(invalidListId, null, model);

        // Then
        assertThat(result).isEqualTo("items");
        verify(model).addAttribute("list", null);
        verify(model).addAttribute("items", List.of());
        verify(model).addAttribute("hasItems", false);
        verify(model).addAttribute("listId", invalidListId);
        verify(model).addAttribute("hideCompleted", null);
        verify(model).addAttribute("error", "List not found or invalid ID.");
        verify(listService, never()).getListById(any());
    }

    @Test
    void itemsPage_withNonExistentList_shouldHandleExceptionGracefully() {
        // Given
        String listIdStr = testListId.toString();
        when(listService.getListById(testListId)).thenReturn(Optional.empty());

        // When
        String result = controller.itemsPage(listIdStr, null, model);

        // Then
        assertThat(result).isEqualTo("items");
        verify(model).addAttribute("list", null);
        verify(model).addAttribute("items", List.of());
        verify(model).addAttribute("hasItems", false);
        verify(model).addAttribute("listId", listIdStr);
        verify(model).addAttribute("hideCompleted", null);
        verify(model).addAttribute("error", "List not found or invalid ID.");
        verify(itemService, never()).getItemsByList(any());
    }

    @Test
    void itemsPage_withServiceException_shouldHandleExceptionGracefully() {
        // Given
        String listIdStr = testListId.toString();
        when(listService.getListById(testListId)).thenThrow(new RuntimeException("Database error"));

        // When
        String result = controller.itemsPage(listIdStr, true, model);

        // Then
        assertThat(result).isEqualTo("items");
        verify(model).addAttribute("list", null);
        verify(model).addAttribute("items", List.of());
        verify(model).addAttribute("hasItems", false);
        verify(model).addAttribute("listId", listIdStr);
        verify(model).addAttribute("hideCompleted", true);
        verify(model).addAttribute("error", "Unable to load items. Please try again.");
        verify(itemService, never()).getItemsByList(any());
    }

    @Test
    void getTodoItems_withHideCompletedTrue_shouldReturnIncompleteItems() {
        // Given
        List<TodoItem> incompleteItems = List.of(testItems.get(0));
        when(itemService.getItemsByListAndStatus(testListId, false)).thenReturn(incompleteItems);

        // When
        List<TodoItem> result = controller.getTodoItems(testListId, true);

        // Then
        assertThat(result).isEqualTo(incompleteItems);
        verify(itemService).getItemsByListAndStatus(testListId, false);
        verify(itemService, never()).getItemsByList(any());
    }

    @Test
    void getTodoItems_withHideCompletedFalse_shouldReturnAllItems() {
        // Given
        when(itemService.getItemsByList(testListId)).thenReturn(testItems);

        // When
        List<TodoItem> result = controller.getTodoItems(testListId, false);

        // Then
        assertThat(result).isEqualTo(testItems);
        verify(itemService).getItemsByList(testListId);
        verify(itemService, never()).getItemsByListAndStatus(any(), anyBoolean());
    }

    @Test
    void getTodoItems_withHideCompletedNull_shouldReturnAllItems() {
        // Given
        when(itemService.getItemsByList(testListId)).thenReturn(testItems);

        // When
        List<TodoItem> result = controller.getTodoItems(testListId, null);

        // Then
        assertThat(result).isEqualTo(testItems);
        verify(itemService).getItemsByList(testListId);
        verify(itemService, never()).getItemsByListAndStatus(any(), anyBoolean());
    }
}