package com.todoapp.web;

import com.todoapp.domain.TodoItem;
import com.todoapp.domain.TodoList;
import com.todoapp.service.TodoItemService;
import com.todoapp.service.TodoListService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

/**
 * Controller for server-rendered HTML views.
 * Provides web pages for managing todo lists and items.
 */
@Controller
public class ViewController {

    private final TodoListService listService;
    private final TodoItemService itemService;

    public ViewController(TodoListService listService, TodoItemService itemService) {
        this.listService = listService;
        this.itemService = itemService;
    }

    /**
     * Displays the main lists page.
     *
     * @param model the Spring MVC model
     * @return the lists template name
     */
    @GetMapping("/lists")
    public String listsPage(Model model) {
        try {
            List<TodoList> lists = listService.getAllLists();
            model.addAttribute("lists", lists);
            model.addAttribute("hasLists", !lists.isEmpty());
        } catch (Exception e) {
            // Handle service errors gracefully - still show the page
            model.addAttribute("lists", List.of());
            model.addAttribute("hasLists", false);
            model.addAttribute("error", "Unable to load lists. Please try again.");
        }
        return "lists";
    }

    /**
     * Displays the items page for a specific list.
     *
     * @param listId        the list identifier
     * @param hideCompleted optional parameter to hide completed items
     * @param model         the Spring MVC model
     * @return the items template name
     */
    @GetMapping("/lists/{listId}/items")
    public String itemsPage(@PathVariable String listId,
                            @RequestParam(required = false) Boolean hideCompleted,
                            Model model
    ) {
        try {
            // Get the list details
            UUID listUUID = UUID.fromString(listId);
            TodoList todoList = listService.getListById(listUUID)
                    .orElseThrow(() -> new IllegalArgumentException("List not found"));

            List<TodoItem> items = getTodoItems(listUUID, hideCompleted);

            model.addAttribute("list", todoList);
            model.addAttribute("items", items);
            model.addAttribute("hasItems", !items.isEmpty());
            model.addAttribute("listId", listId);
            model.addAttribute("hideCompleted", hideCompleted);

        } catch (IllegalArgumentException e) {
            // Handle invalid UUID or list not found
            handleException(model, listId, hideCompleted, "List not found or invalid ID.");
        } catch (Exception e) {
            // Handle other service errors gracefully
            handleException(model, listId, hideCompleted, "Unable to load items. Please try again.");
        }
        return "items";
    }

    private static void handleException(Model model, String listId, Boolean hideCompleted, String attributeValue) {
        model.addAttribute("list", null);
        model.addAttribute("items", List.of());
        model.addAttribute("hasItems", false);
        model.addAttribute("listId", listId);
        model.addAttribute("hideCompleted", hideCompleted);
        model.addAttribute("error", attributeValue);
    }

    List<TodoItem> getTodoItems(UUID listUUID, Boolean hideCompleted) {
        if (Boolean.TRUE.equals(hideCompleted)) {
            return itemService.getItemsByListAndStatus(listUUID, false);
        } else {
            return itemService.getItemsByList(listUUID);
        }
    }
}
