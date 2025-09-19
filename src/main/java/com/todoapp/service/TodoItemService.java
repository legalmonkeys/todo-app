package com.todoapp.service;

import com.todoapp.domain.TodoItem;
import com.todoapp.persistence.TodoItemRepository;
import com.todoapp.persistence.TodoListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for TodoItem business logic.
 * Handles creation, validation, and management of todo items.
 */
@Service
@Transactional
public class TodoItemService {

    private final TodoItemRepository itemRepository;
    private final TodoListRepository listRepository;

    public TodoItemService(TodoItemRepository itemRepository, TodoListRepository listRepository) {
        this.itemRepository = itemRepository;
        this.listRepository = listRepository;
    }

    /**
     * Creates a new todo item with validation.
     *
     * @param listId the parent list identifier
     * @param text   the item text (1-50 characters, non-blank)
     * @return the created todo item
     * @throws IllegalArgumentException if validation fails or list not found
     */
    public TodoItem createItem(UUID listId, String text) {
        validateItemText(text);

        // Verify the list exists
        listRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("List with ID " + listId + " not found"));

        // Get next position by finding max position and adding 1
        Integer maxPosition = itemRepository.findMaxPositionByListId(listId);
        int nextPosition = (maxPosition == null) ? 0 : maxPosition + 1;

        TodoItem newItem = new TodoItem(UUID.randomUUID(), listId, text, false, Instant.now(), nextPosition);
        return itemRepository.save(newItem);
    }

    /**
     * Gets all todo items for a list ordered by importance first (descending), then by position ascending.
     *
     * @param listId the list identifier
     * @return list of todo items for the specified list ordered by importance first, then position
     */
    @Transactional(readOnly = true)
    public List<TodoItem> getItemsByList(UUID listId) {
        return itemRepository.findAllByListIdOrdered(listId);
    }

    /**
     * Gets todo items for a list filtered by completion status.
     *
     * @param listId    the list identifier
     * @param completed the completion status to filter by
     * @return list of todo items matching the criteria
     */
    @Transactional(readOnly = true)
    public List<TodoItem> getItemsByListAndStatus(UUID listId, boolean completed) {
        return itemRepository.findByListIdAndCompleted(listId, completed);
    }

    /**
     * Gets a todo item by ID.
     *
     * @param id the item identifier
     * @return optional containing the item if found
     */
    @Transactional(readOnly = true)
    public Optional<TodoItem> getItemById(UUID id) {
        return itemRepository.findById(id);
    }

    /**
     * Updates the text of an existing todo item.
     *
     * @param id      the item identifier
     * @param newText the new text (1-50 characters, non-blank)
     * @return the updated todo item
     * @throws IllegalArgumentException if item not found or validation fails
     */
    public TodoItem updateItemText(UUID id, String newText) {
        // Find the item first
        TodoItem existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item with ID " + id + " not found"));

        // Then validate the new text
        validateItemText(newText);

        existingItem.markNotNew(); // Mark as existing for update
        existingItem.setText(newText);
        return itemRepository.save(existingItem);
    }

    /**
     * Updates both text and completion status of an existing todo item.
     *
     * @param id        the item identifier
     * @param newText   the new text (1-50 characters, non-blank)
     * @param completed the new completion status
     * @return the updated todo item
     * @throws IllegalArgumentException if item not found or validation fails
     */
    public TodoItem updateItem(UUID id, String newText, boolean completed) {
        TodoItem existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item with ID " + id + " not found"));

        validateItemText(newText);

        existingItem.markNotNew(); // Mark as existing for update
        existingItem.setText(newText);
        existingItem.setCompleted(completed);
        return itemRepository.save(existingItem);
    }

    /**
     * Updates only the completion status of a todo item.
     *
     * @param id        the item identifier
     * @param completed the new completion status
     * @return the updated todo item
     * @throws IllegalArgumentException if item not found
     */
    public TodoItem updateItemCompletion(UUID id, boolean completed) {
        TodoItem existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item with ID " + id + " not found"));

        existingItem.markNotNew(); // Mark as existing for update
        existingItem.setCompleted(completed);
        return itemRepository.save(existingItem);
    }

    /**
     * Toggles the completion status of a todo item.
     *
     * @param id the item identifier
     * @return the updated todo item
     * @throws IllegalArgumentException if item not found
     */
    public TodoItem toggleItemCompletion(UUID id) {
        TodoItem existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item with ID " + id + " not found"));

        existingItem.markNotNew(); // Mark as existing for update
        existingItem.setCompleted(!existingItem.isCompleted());
        return itemRepository.save(existingItem);
    }

    public TodoItem toggleItemImportance(UUID id) {
        TodoItem existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item with ID " + id + " not found"));
        existingItem.markNotNew();
        existingItem.setImportant(!existingItem.isImportant());
        itemRepository.save(existingItem);
        return null;
    }

    /**
     * Deletes a todo item.
     *
     * @param id the item identifier
     * @throws IllegalArgumentException if item not found
     */
    public void deleteItem(UUID id) {
        if (!itemRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Item with ID " + id + " not found");
        }

        itemRepository.deleteById(id);
    }

    /**
     * Deletes all todo items in a specific list.
     * Used when a list is deleted or cleared.
     *
     * @param listId the list identifier
     * @return the number of items deleted
     */
    public int deleteAllItemsInList(UUID listId) {
        List<TodoItem> itemsToDelete = itemRepository.findByListId(listId);
        itemRepository.deleteAll(itemsToDelete);
        return itemsToDelete.size();
    }

    /**
     * Gets the count of todo items in a specific list.
     *
     * @param listId the list identifier
     * @return the number of items in the list
     */
    @Transactional(readOnly = true)
    public long getItemCountByList(UUID listId) {
        return itemRepository.countByListId(listId);
    }

    /**
     * Gets the count of completed todo items in a specific list.
     *
     * @param listId the list identifier
     * @return the number of completed items in the list
     */
    @Transactional(readOnly = true)
    public long getCompletedItemCountByList(UUID listId) {
        return itemRepository.countCompletedByListId(listId);
    }

    /**
     * Reorders items within a list atomically.
     *
     * @param listId         the list identifier
     * @param orderedItemIds the complete ordered list of item IDs (must match exactly with existing items)
     * @throws IllegalArgumentException if validation fails or item set doesn't match
     */
    public void reorderItems(UUID listId, List<UUID> orderedItemIds) {
        // Verify the list exists
        listRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("List with ID " + listId + " not found"));

        // Get all current items in the list
        List<TodoItem> currentItems = itemRepository.findAllByListIdOrdered(listId);

        // Validate that provided IDs exactly match existing items
        if (currentItems.size() != orderedItemIds.size()) {
            throw new IllegalArgumentException("Provided item count (" + orderedItemIds.size() +
                    ") does not match existing item count (" + currentItems.size() + ")");
        }

        // Check that all provided IDs exist and belong to this list
        for (UUID itemId : orderedItemIds) {
            boolean found = currentItems.stream().anyMatch(item -> item.getId().equals(itemId));
            if (!found) {
                throw new IllegalArgumentException("Item with ID " + itemId + " not found in list " + listId);
            }
        }

        // Check that we don't have duplicate IDs
        if (orderedItemIds.stream().distinct().count() != orderedItemIds.size()) {
            throw new IllegalArgumentException("Duplicate item IDs provided in reorder request");
        }

        // Update positions: use high values first to avoid unique constraint violations
        // Then update to final positions
        for (int i = 0; i < orderedItemIds.size(); i++) {
            UUID itemId = orderedItemIds.get(i);
            TodoItem item = currentItems.stream()
                    .filter(it -> it.getId().equals(itemId))
                    .findFirst()
                    .orElseThrow(); // Should not happen due to validation above

            item.markNotNew();
            // Set to high value first to avoid constraint conflicts (1000 + index)
            item.setPosition(1000 + i);
            itemRepository.save(item);
        }

        // Now update to final contiguous positions
        for (int i = 0; i < orderedItemIds.size(); i++) {
            UUID itemId = orderedItemIds.get(i);
            TodoItem item = currentItems.stream()
                    .filter(it -> it.getId().equals(itemId))
                    .findFirst()
                    .orElseThrow();

            item.setPosition(i);
            itemRepository.save(item);
        }
    }

    /**
     * Validates item text according to business rules.
     *
     * @param text the text to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateItemText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Item text cannot be empty or whitespace");
        }

        if (text.length() > 50) {
            throw new IllegalArgumentException("Item text is too long (maximum 50 characters)");
        }
    }
}
