package jsi.connection.database.storage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for storage engines that handle persistence and caching.
 * Implementations are responsible for serialization, caching, and file I/O.
 */
public interface StorageEngine {
    
    /**
     * Writes raw byte data for low-level operations.
     * @param key the identifier
     * @param data the byte array to store
     * @throws IOException if an I/O error occurs
     */
    void write(String key, byte[] data) throws IOException;
    
    /**
     * Reads raw byte data for low-level operations.
     * @param key the identifier
     * @return the byte array, or null if not found
     * @throws IOException if an I/O error occurs
     */
    byte[] read(String key) throws IOException;
    
    /**
     * Deletes data by key.
     * @param key the identifier to delete
     * @throws IOException if an I/O error occurs
     */
    void delete(String key) throws IOException;
    
    /**
     * Loads all table names from storage.
     * @return set of table names
     */
    Set<String> loadTableNames();
    
    /**
     * Loads schema information for a table.
     * @param tableName the table name
     * @return set of column names, or empty set if not found
     */
    Set<String> loadTableSchema(String tableName);
    
    /**
     * Loads all rows from a table.
     * @param tableName the table name
     * @return list of rows (each row is a map of column->value)
     */
    List<Map<String, Object>> loadTable(String tableName);
    
    /**
     * Saves a table with its rows.
     * @param tableName the table name
     * @param rows the table data
     */
    void saveTable(String tableName, List<Map<String, Object>> rows);
    
    /**
     * Saves schema information for all tables.
     * @param schemas map of table name to column names
     */
    void saveSchemas(Map<String, Set<String>> schemas);
    
    /**
     * Deletes a table from storage.
     * @param tableName the table name to delete
     */
    void deleteTable(String tableName);
}