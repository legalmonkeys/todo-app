package com.challenge.todo.web;

import com.challenge.todo.domain.TodoItem;
import com.challenge.todo.domain.TodoList;
import com.challenge.todo.service.TodoItemService;
import com.challenge.todo.service.TodoListService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
   * @param listId the list identifier
   * @param model the Spring MVC model
   * @return the items template name
   */
  @GetMapping("/lists/{listId}/items")
  public String itemsPage(@PathVariable String listId, Model model) {
    try {
      UUID uuid = UUID.fromString(listId);
      
      // Get the list details
      TodoList todoList = listService.getListById(uuid)
          .orElseThrow(() -> new IllegalArgumentException("List not found"));
      
      // Get items for this list
      List<TodoItem> items = itemService.getItemsByList(uuid);
      
      model.addAttribute("list", todoList);
      model.addAttribute("items", items);
      model.addAttribute("hasItems", !items.isEmpty());
      model.addAttribute("listId", listId);
      
    } catch (IllegalArgumentException e) {
      // Handle invalid UUID or list not found
      model.addAttribute("list", null);
      model.addAttribute("items", List.of());
      model.addAttribute("hasItems", false);
      model.addAttribute("listId", listId);
      model.addAttribute("error", "List not found or invalid ID.");
      
    } catch (Exception e) {
      // Handle other service errors gracefully
      model.addAttribute("list", null);
      model.addAttribute("items", List.of());
      model.addAttribute("hasItems", false);
      model.addAttribute("listId", listId);
      model.addAttribute("error", "Unable to load items. Please try again.");
    }
    
    return "items";
  }
}
