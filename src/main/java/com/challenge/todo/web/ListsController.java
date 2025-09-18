package com.challenge.todo.web;

import com.challenge.todo.domain.TodoList;
import com.challenge.todo.service.TodoListService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for TodoList operations.
 * Provides CRUD endpoints for managing todo lists.
 */
@RestController
@RequestMapping("/api/lists")
@CrossOrigin(origins = "*") // Allow CORS for frontend integration
public class ListsController {

  private final TodoListService listService;

  public ListsController(TodoListService listService) {
    this.listService = listService;
  }

  /**
   * Gets all todo lists ordered by creation date descending.
   *
   * @return list of all todo lists
   */
  @GetMapping
  public ResponseEntity<List<TodoList>> getAllLists() {
    List<TodoList> lists = listService.getAllLists();
    return ResponseEntity.ok(lists);
  }

  /**
   * Gets a specific todo list by ID.
   *
   * @param id the list identifier
   * @return the todo list if found, 404 if not found
   */
  @GetMapping("/{id}")
  public ResponseEntity<TodoList> getListById(@PathVariable UUID id) {
    return listService.getListById(id)
        .map(list -> ResponseEntity.ok(list))
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Creates a new todo list.
   *
   * @param request the request body containing list name
   * @return the created todo list with 201 status
   */
  @PostMapping
  public ResponseEntity<?> createList(@RequestBody Map<String, String> request) {
    try {
      String name = request.get("name");
      if (name == null) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Bad Request", "message", "Missing required field: name"));
      }

      TodoList createdList = listService.createList(name);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdList);
      
    } catch (IllegalArgumentException e) {
      String message = e.getMessage();
      HttpStatus status = message.contains("already exists") 
          ? HttpStatus.CONFLICT 
          : HttpStatus.BAD_REQUEST;
      
      return ResponseEntity.status(status)
          .body(Map.of("error", status.getReasonPhrase(), "message", message));
    }
  }

  /**
   * Updates an existing todo list name.
   *
   * @param id the list identifier
   * @param request the request body containing new name
   * @return the updated todo list
   */
  @PatchMapping("/{id}")
  public ResponseEntity<?> updateList(@PathVariable UUID id, @RequestBody Map<String, String> request) {
    try {
      String name = request.get("name");
      if (name == null) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Bad Request", "message", "Missing required field: name"));
      }

      TodoList updatedList = listService.renameList(id, name);
      return ResponseEntity.ok(updatedList);
      
    } catch (IllegalArgumentException e) {
      String message = e.getMessage();
      HttpStatus status = message.contains("not found") 
          ? HttpStatus.NOT_FOUND 
          : HttpStatus.BAD_REQUEST;
      
      return ResponseEntity.status(status)
          .body(Map.of("error", status.getReasonPhrase(), "message", message));
    }
  }

  /**
   * Deletes a todo list.
   *
   * @param id the list identifier
   * @return 204 No Content if successful, 404 if not found
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteList(@PathVariable UUID id) {
    try {
      listService.deleteList(id);
      return ResponseEntity.noContent().build();
      
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Not Found", "message", e.getMessage()));
    }
  }

  /**
   * Gets the total count of todo lists.
   *
   * @return the count of lists
   */
  @GetMapping("/count")
  public ResponseEntity<Map<String, Long>> getListCount() {
    long count = listService.getListCount();
    return ResponseEntity.ok(Map.of("count", count));
  }

  /**
   * Checks if a list with the given name exists.
   *
   * @param name the list name to check
   * @return existence status with the name
   */
  @GetMapping("/exists")
  public ResponseEntity<?> checkListExists(@RequestParam(required = false) String name) {
    if (name == null || name.trim().isEmpty()) {
      return ResponseEntity.badRequest()
          .body(Map.of("error", "Bad Request", "message", "Missing required parameter: name"));
    }

    boolean exists = listService.listExists(name);
    return ResponseEntity.ok(Map.of("exists", exists, "name", name));
  }
}
