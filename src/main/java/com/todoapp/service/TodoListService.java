package com.todoapp.service;

import com.todoapp.domain.TodoList;
import com.todoapp.persistence.TodoListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for TodoList business logic.
 * Handles creation, validation, and management of todo lists.
 */
@Service
@Transactional
public class TodoListService {

    private final TodoListRepository repository;

    public TodoListService(TodoListRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a new todo list with validation.
     *
     * @param name the list name (must be unique, 1-50 characters, non-blank)
     * @return the created todo list
     * @throws IllegalArgumentException if validation fails
     */
    public TodoList createList(String name) {
        validateListName(name);

        if (repository.existsByName(name)) {
            throw new IllegalArgumentException("List with name '" + name + "' already exists");
        }

        TodoList newList = new TodoList(UUID.randomUUID(), name, Instant.now());
        return repository.save(newList);
    }

    /**
     * Gets all todo lists ordered by creation date descending (newest first).
     *
     * @return list of all todo lists
     */
    @Transactional(readOnly = true)
    public List<TodoList> getAllLists() {
        return repository.findAllOrderByCreatedAtDesc();
    }

    /**
     * Gets a todo list by ID.
     *
     * @param id the list identifier
     * @return optional containing the list if found
     */
    @Transactional(readOnly = true)
    public Optional<TodoList> getListById(UUID id) {
        return repository.findById(id);
    }

    /**
     * Renames an existing todo list.
     *
     * @param id      the list identifier
     * @param newName the new name (must be unique, 1-50 characters, non-blank)
     * @return the updated todo list
     * @throws IllegalArgumentException if list not found or validation fails
     */
    public TodoList renameList(UUID id, String newName) {
        // Find the list first to ensure it exists
        TodoList existingList = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("List with ID " + id + " not found"));

        // Then validate the new name
        validateListName(newName);

        if (repository.existsByNameAndIdNot(newName, id)) {
            throw new IllegalArgumentException("List with name '" + newName + "' already exists");
        }

        existingList.markNotNew(); // Mark as existing for update
        existingList.setName(newName);
        return repository.save(existingList);
    }

    /**
     * Deletes a todo list.
     *
     * @param id the list identifier
     * @throws IllegalArgumentException if list not found
     */
    public void deleteList(UUID id) {
        if (!repository.findById(id).isPresent()) {
            throw new IllegalArgumentException("List with ID " + id + " not found");
        }

        repository.deleteById(id);
    }

    /**
     * Gets the total count of todo lists.
     *
     * @return the number of lists
     */
    @Transactional(readOnly = true)
    public long getListCount() {
        return repository.count();
    }

    /**
     * Checks if a list with the given name exists.
     *
     * @param name the list name to check
     * @return true if a list with this name exists
     */
    @Transactional(readOnly = true)
    public boolean listExists(String name) {
        return repository.existsByName(name);
    }

    /**
     * Validates list name according to business rules.
     *
     * @param name the name to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateListName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("List name cannot be empty or whitespace");
        }

        if (name.length() > 50) {
            throw new IllegalArgumentException("List name is too long (maximum 50 characters)");
        }
    }
}
