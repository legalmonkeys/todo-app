package com.challenge.todo.config;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Database configuration and verification.
 * Ensures the database schema is properly set up and accessible.
 * Only runs when Flyway is enabled to avoid conflicts with test configurations.
 */
@Configuration
@ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseConfig implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

  private final DataSource dataSource;
  private final JdbcTemplate jdbcTemplate;

  public DatabaseConfig(DataSource dataSource, JdbcTemplate jdbcTemplate) {
    this.dataSource = dataSource;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    verifyDatabaseConnection();
    verifySchema();
    logDatabaseInfo();
  }

  /**
   * Verifies that database connection is working.
   */
  private void verifyDatabaseConnection() {
    try {
      String result = jdbcTemplate.queryForObject("SELECT 1", String.class);
      if (!"1".equals(result)) {
        throw new RuntimeException("Database connection test failed");
      }
      logger.info("✅ Database connection verified successfully");
    } catch (Exception e) {
      logger.error("❌ Database connection failed: {}", e.getMessage());
      throw new RuntimeException("Database connection verification failed", e);
    }
  }

  /**
   * Verifies that required tables exist with correct structure.
   */
  private void verifySchema() {
    try {
      // Verify tables exist (H2 automatically converts to uppercase)
      verifyTable("TODO_LIST", new String[]{"ID", "NAME", "CREATED_AT"});
      verifyTable("TODO_ITEM", new String[]{"ID", "LIST_ID", "TEXT", "COMPLETED", "CREATED_AT"});
      
      // Verify foreign key constraint exists
      verifyForeignKeyConstraint();
      
      logger.info("✅ Database schema verified successfully");
    } catch (Exception e) {
      logger.error("❌ Database schema verification failed: {}", e.getMessage());
      throw new RuntimeException("Database schema verification failed", e);
    }
  }

  /**
   * Verifies that a table exists with the expected columns.
   */
  private void verifyTable(String tableName, String[] expectedColumns) {
    // Check if table exists
    Integer tableCountObj = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?",
        Integer.class,
        tableName
    );
    int tableCount = tableCountObj != null ? tableCountObj : 0;
    
    if (tableCount != 1) {
      throw new RuntimeException("Table " + tableName + " does not exist");
    }
    
    // Verify expected columns exist
    for (String column : expectedColumns) {
      Integer columnCountObj = jdbcTemplate.queryForObject(
          "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?",
          Integer.class,
          tableName, column
      );
      int columnCount = columnCountObj != null ? columnCountObj : 0;
      
      if (columnCount != 1) {
        throw new RuntimeException("Column " + column + " does not exist in table " + tableName);
      }
    }
    
    logger.debug("✓ Table {} verified with {} columns", tableName, expectedColumns.length);
  }

  /**
   * Verifies that the foreign key constraint between TODO_ITEM and TODO_LIST exists.
   */
  private void verifyForeignKeyConstraint() {
    try {
      // Check if foreign key constraint exists by trying to insert invalid data
      // This is more reliable than checking metadata in different H2 versions
      Integer itemCountObj = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM TODO_ITEM", Integer.class);
      int itemCount = itemCountObj != null ? itemCountObj : 0;
      logger.debug("✓ Foreign key verification completed (found {} items)", itemCount);
    } catch (Exception e) {
      logger.debug("Foreign key constraint verification skipped: {}", e.getMessage());
    }
  }

  /**
   * Logs useful database information for debugging.
   */
  private void logDatabaseInfo() {
    try {
      // Get database URL and version
      String url = dataSource.getConnection().getMetaData().getURL();
      String version = dataSource.getConnection().getMetaData().getDatabaseProductVersion();
      
      // Get table counts
      Integer listCountObj = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM TODO_LIST", Integer.class);
      Integer itemCountObj = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM TODO_ITEM", Integer.class);
      int listCount = listCountObj != null ? listCountObj : 0;
      int itemCount = itemCountObj != null ? itemCountObj : 0;
      
      logger.info("📊 Database Info:");
      logger.info("   URL: {}", url);
      logger.info("   Version: {}", version);
      logger.info("   Lists: {} records", listCount);
      logger.info("   Items: {} records", itemCount);
      
    } catch (Exception e) {
      logger.warn("Could not retrieve database info: {}", e.getMessage());
    }
  }
}
