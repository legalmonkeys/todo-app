package com.todoapp.web;

import com.todoapp.domain.TodoItem;
import com.todoapp.service.TodoItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for TodoItem operations.
 * Provides CRUD endpoints for managing todo items within lists.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow CORS for frontend integration
public class ItemsController {

    private final TodoItemService itemService;

    public ItemsController(TodoItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * Gets all todo items for a specific list, optionally filtered by completion status.
     *
     * @param listId    the list identifier
     * @param completed optional completion status filter
     * @return list of todo items for the specified list
     */
    @GetMapping("/lists/{listId}/items")
    public ResponseEntity<List<TodoItem>> getItemsByList(
            @PathVariable UUID listId,
            @RequestParam(required = false) Boolean completed) {

        List<TodoItem> items = completed != null
                ? itemService.getItemsByListAndStatus(listId, completed)
                : itemService.getItemsByList(listId);

        return ResponseEntity.ok(items);
    }

    /**
     * Gets a specific todo item by ID.
     *
     * @param id the item identifier
     * @return the todo item if found, 404 if not found
     */
    @GetMapping("/items/{id}")
    public ResponseEntity<TodoItem> getItemById(@PathVariable UUID id) {
        return itemService.getItemById(id)
                .map(item -> ResponseEntity.ok(item))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new todo item in a specific list.
     *
     * @param listId  the list identifier
     * @param request the request body containing item text
     * @return the created todo item with 201 status
     */
    @PostMapping("/lists/{listId}/items")
    public ResponseEntity<?> createItem(@PathVariable UUID listId, @RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            if (text == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Bad Request", "message", "Missing required field: text"));
            }

            TodoItem createdItem = itemService.createItem(listId, text);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);

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
     * Updates an existing todo item (text and/or completion status).
     *
     * @param id      the item identifier
     * @param request the request body containing new text and/or completed status
     * @return the updated todo item
     */
    @PatchMapping("/items/{id}")
    public ResponseEntity<?> updateItem(@PathVariable UUID id, @RequestBody Map<String, Object> request) {
        try {
            String text = (String) request.get("text");
            Boolean completed = (Boolean) request.get("completed");

            if (text == null && completed == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Bad Request", "message", "At least one field (text or completed) must be provided"));
            }

            TodoItem updatedItem;
            if (text != null && completed != null) {
                updatedItem = itemService.updateItem(id, text, completed);
            } else if (text != null) {
                updatedItem = itemService.updateItemText(id, text);
            } else if (completed != null) {
                // completed is guaranteed not null here
                updatedItem = itemService.updateItemCompletion(id, completed.booleanValue());
            } else {
                // This should never happen due to the earlier check, but handle it defensively
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Bad Request", "message", "At least one field (text or completed) must be provided"));
            }

            return ResponseEntity.ok(updatedItem);

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
     * Toggles the completion status of a todo item.
     *
     * @param id the item identifier
     * @return the updated todo item
     */
    @PatchMapping("/items/{id}/toggle")
    public ResponseEntity<?> toggleItemCompletion(@PathVariable UUID id) {
        try {
            TodoItem toggledItem = itemService.toggleItemCompletion(id);
            return ResponseEntity.ok(toggledItem);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found", "message", e.getMessage()));
        }
    }

    @PatchMapping("/items/{id}/toggle-important")
    public ResponseEntity<?> toggleItemImportance(@PathVariable UUID id) {
        try {
            TodoItem toggledItem = itemService.toggleItemImportance(id);
            return ResponseEntity.ok(toggledItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found", "message", e.getMessage()));
        }
    }

    /**
     * Deletes a todo item.
     *
     * @param id the item identifier
     * @return 204 No Content if successful, 404 if not found
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable UUID id) {
        try {
            itemService.deleteItem(id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found", "message", e.getMessage()));
        }
    }

    /**
     * Gets the count of todo items in a specific list.
     *
     * @param listId the list identifier
     * @return the count of items
     */
    @GetMapping("/lists/{listId}/items/count")
    public ResponseEntity<Map<String, Object>> getItemCountByList(@PathVariable UUID listId) {
        long count = itemService.getItemCountByList(listId);
        return ResponseEntity.ok(Map.of("count", count, "listId", listId));
    }

    /**
     * Gets the count of completed todo items in a specific list.
     *
     * @param listId the list identifier
     * @return the count of completed items
     */
    @GetMapping("/lists/{listId}/items/completed/count")
    public ResponseEntity<Map<String, Object>> getCompletedItemCountByList(@PathVariable UUID listId) {
        long completedCount = itemService.getCompletedItemCountByList(listId);
        return ResponseEntity.ok(Map.of("completedCount", completedCount, "listId", listId));
    }

    /**
     * Reorders items within a list.
     *
     * @param listId  the list identifier
     * @param request the request body containing ordered item IDs
     * @return 204 No Content if successful
     */
    @PutMapping("/lists/{listId}/items/reorder")
    public ResponseEntity<?> reorderItems(@PathVariable UUID listId, @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Object> itemIdsObjects = (List<Object>) request.get("itemIds");

            if (itemIdsObjects == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Bad Request", "message", "Missing required field: itemIds"));
            }

            // Convert to UUIDs from String representations
            List<UUID> itemIds = itemIdsObjects.stream()
                    .map(obj -> {
                        if (obj instanceof String) {
                            return UUID.fromString((String) obj);
                        } else {
                            throw new IllegalArgumentException("Invalid item ID format: " + obj + ". Expected UUID string.");
                        }
                    })
                    .toList();

            itemService.reorderItems(listId, itemIds);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            HttpStatus status = message.contains("not found")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(Map.of("error", status.getReasonPhrase(), "message", message));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad Request", "message", "Invalid request format: " + e.getMessage()));
        }
    }

    /**
     * Deletes all todo items in a specific list.
     *
     * @param listId the list identifier
     * @return the number of deleted items
     */
    @DeleteMapping("/lists/{listId}/items")
    public ResponseEntity<Map<String, Object>> deleteAllItemsInList(@PathVariable UUID listId) {
        int deletedCount = itemService.deleteAllItemsInList(listId);
        return ResponseEntity.ok(Map.of("deletedCount", deletedCount, "listId", listId));
    }
}
