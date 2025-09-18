package com.todoapp.persistence;

import com.todoapp.domain.TodoList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for TodoList persistence operations.
 * Extends CrudRepository for basic CRUD operations and adds custom queries.
 */
@Repository
public interface TodoListRepository extends CrudRepository<TodoList, UUID> {

  /**
   * Find all todo lists ordered by creation date descending (newest first).
   *
   * @return list of todo lists ordered by newest first
   */
  @Query("SELECT * FROM TODO_LIST ORDER BY CREATED_AT DESC")
  List<TodoList> findAllOrderByCreatedAtDesc();

  /**
   * Find all todo lists (as List instead of Iterable).
   *
   * @return list of all todo lists
   */
  @Query("SELECT * FROM TODO_LIST")
  @Override
  @NonNull
  List<TodoList> findAll();

  /**
   * Check if a todo list with the given name exists.
   *
   * @param name the name to check
   * @return true if a list with this name exists
   */
  boolean existsByName(String name);

  /**
   * Check if a todo list with the given name exists, excluding the specified ID.
   * Used for validating unique names during updates.
   *
   * @param name the name to check
   * @param id the ID to exclude from the check
   * @return true if another list with this name exists
   */
  @Query("SELECT COUNT(*) > 0 FROM TODO_LIST WHERE NAME = :name AND ID != :id")
  boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") UUID id);
}
