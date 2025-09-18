package com.todoapp.persistence;

import com.todoapp.domain.TodoItem;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for TodoItem persistence operations.
 * Extends CrudRepository for basic CRUD operations and adds custom queries.
 */
@Repository
public interface TodoItemRepository extends CrudRepository<TodoItem, UUID> {

  /**
   * Find all todo items for a specific list ordered by creation date descending (newest first).
   *
   * @param listId the list identifier
   * @return list of todo items for the specified list ordered by newest first
   */
  @Query("SELECT * FROM TODO_ITEM WHERE LIST_ID = :listId ORDER BY CREATED_AT DESC")
  List<TodoItem> findAllByListIdOrderByCreatedAtDesc(@Param("listId") UUID listId);

  /**
   * Find all todo items as List (instead of Iterable).
   *
   * @return list of all todo items
   */
  @Query("SELECT * FROM TODO_ITEM")
  @Override
  @NonNull
  List<TodoItem> findAll();

  /**
   * Find all todo items by completion status for a specific list.
   *
   * @param listId    the list identifier
   * @param completed the completion status
   * @return list of todo items matching the criteria
   */
  @Query("SELECT * FROM TODO_ITEM WHERE LIST_ID = :listId AND COMPLETED = :completed ORDER BY CREATED_AT DESC")
  List<TodoItem> findByListIdAndCompleted(@Param("listId") UUID listId, @Param("completed") boolean completed);

  /**
   * Count todo items for a specific list.
   *
   * @param listId the list identifier
   * @return count of items in the list
   */
  @Query("SELECT COUNT(*) FROM TODO_ITEM WHERE LIST_ID = :listId")
  long countByListId(@Param("listId") UUID listId);

  /**
   * Count completed todo items for a specific list.
   *
   * @param listId the list identifier
   * @return count of completed items in the list
   */
  @Query("SELECT COUNT(*) FROM TODO_ITEM WHERE LIST_ID = :listId AND COMPLETED = true")
  long countCompletedByListId(@Param("listId") UUID listId);

  /**
   * Find all todo items by list ID for deletion purposes.
   * Use with deleteAll() for bulk deletion.
   *
   * @param listId the list identifier
   * @return list of items to delete
   */
  @Query("SELECT * FROM TODO_ITEM WHERE LIST_ID = :listId")
  List<TodoItem> findByListId(@Param("listId") UUID listId);

  /**
   * Find all todo items for a specific list ordered by position ascending.
   *
   * @param listId the list identifier
   * @return list of todo items ordered by position then id
   */
  @Query("SELECT * FROM TODO_ITEM WHERE LIST_ID = :listId ORDER BY POSITION ASC, ID ASC")
  List<TodoItem> findAllByListIdOrderByPositionAsc(@Param("listId") UUID listId);

  /**
   * Find the maximum position for items in a specific list.
   *
   * @param listId the list identifier
   * @return maximum position value or null if list is empty
   */
  @Query("SELECT MAX(POSITION) FROM TODO_ITEM WHERE LIST_ID = :listId")
  Integer findMaxPositionByListId(@Param("listId") UUID listId);

  /**
   * Bulk update to hide all completed items in a specific list.
   * Sets hidden=true for all completed items that are not already hidden.
   *
   * @param listId the list identifier
   * @return the number of items updated
   */
  @Modifying
  @Query("UPDATE TODO_ITEM SET HIDDEN = true WHERE LIST_ID = :listId AND COMPLETED = true AND HIDDEN = false")
  int hideCompletedItemsByListId(@Param("listId") UUID listId);
}
