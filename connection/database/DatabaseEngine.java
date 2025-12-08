package jsi.connection.database;

import jsi.connection.database.QueryType.OperationType;

/**
 * Interface representing a database engine.
 * Defines methods for executing queries and managing transactions.
 */
public abstract class DatabaseEngine {

    private StorageEngine storageEngine;
    
    /**
     * Constructor for DatabaseEngine.
     * @param storageEngine the StorageEngine to use
     */
    public DatabaseEngine(StorageEngine storageEngine) { this.storageEngine = storageEngine; }

    /**
     * Get the StorageEngine instance.
     * @return the StorageEngine
     */
    public StorageEngine getStorageEngine() { return storageEngine; }

    /**
     * Execute a database query.
     * @param query the Query to execute
     * @return the QueryResult of the execution
     */
    public QueryResult execute(Query query) {
        
        var type = query.getQueryType().getOperationType();
        var collection = query.getTargetCollection();
        var condition = query.getCondition();
        
        try {

            if (type == OperationType.READ) {

                var results = storageEngine.read(collection, condition);
                return new QueryResult(true, "Read operation successful.", results);
            } else if (type == OperationType.CREATE || type == OperationType.UPDATE) {

                var affectedFields = query.getAffectedFields();
                storageEngine.write(collection, affectedFields);
                return new QueryResult(true, "Write operation successful.", null);
            } else if (type == OperationType.DELETE) {

                storageEngine.delete(collection, condition);
                return new QueryResult(true, "Delete operation successful.", null);
            }
            
            return new QueryResult(false, "Unsupported operation type.", null);
            
        } catch (Exception e) {

            // Log l'errore per debugging
            System.err.println("Query execution failed: " + e.getMessage());
            return new QueryResult(false, "Operation failed: " + e.getMessage(), null);
        }
    }

    /**
     * Begin a transaction.
     */
    public abstract void beginTransaction();

    /**
     * Commit the current transaction.
     */
    public abstract void commit();

    /**
     * Rollback the current transaction.
     */
    public abstract void rollback();
}