package jsi.connection.database;

import java.util.List;

import jsi.connection.database.query.QueryCondition;

/**
 * Interface for storage engines that handle persistence and caching.
 * Implementations are responsible for serialization, caching, and file I/O.
 */
public interface StorageEngine {
    
    /**
     * Write data to the storage.
     * @param collection the target collection
     * @param data the data to write
     */
    void write(String collection, List<Field> data) throws Exception;

    /**
     * Read data from the storage.
     * @param collection the target collection
     * @param condition the query condition
     * @return the list of fields read
     */
    List<Field> read(String collection, QueryCondition condition) throws Exception;

    /**
     * Delete data from the storage.
     * @param collection the target collection
     * @param condition the query condition
     */
    void delete(String collection, QueryCondition condition) throws Exception;
}