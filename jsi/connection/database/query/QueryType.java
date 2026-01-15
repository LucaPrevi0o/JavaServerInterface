package jsi.connection.database.query;

/**
 * Interface representing the type of a database query.
 * Defines the operation type (READ, WRITE, UPDATE, DELETE).
 */
public interface QueryType {

    /**
     * Enum representing the operation types for database queries.
     */
    public enum OperationType {
        
        /**
         * Read operation (e.g., SELECT).
         */
        READ,

        /**
         * Create operation (e.g., INSERT).
         */
        CREATE,

        /**
         * Update operation (e.g., UPDATE).
         */
        UPDATE,
        
        /**
         * Delete operation (e.g., DELETE).
         */
        DELETE
    }
    
    /**
     * Get the operation type of the query.
     * @return the OperationType
     */
    OperationType getOperationType();

    /**
     * Get the keyword associated with the query type.
     * @return the query keyword
     */
    String getKeyword();
}
