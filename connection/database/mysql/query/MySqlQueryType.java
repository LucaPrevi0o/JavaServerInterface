package jsi.connection.database.mysql.query;

import jsi.connection.database.query.QueryType;

public enum MySqlQueryType implements QueryType {

    /**
     * SELECT query type.
     * Used for reading data from the database.
     */
    SELECT(OperationType.READ),

    /**
     * INSERT query type.
     * Used for adding new data to the database.
     */
    INSERT(OperationType.CREATE),

    /**
     * CREATE query type.
     * Used for creating new database structures or objects.
     */
    CREATE(OperationType.CREATE),

    /**
     * UPDATE query type.
     * Used for modifying existing data in the database.
     */
    UPDATE(OperationType.UPDATE),

    /**
     * ALTER query type.
     * Used for modifying database structures or objects.
     */
    ALTER(OperationType.UPDATE),

    /**
     * DELETE query type.
     * Used for removing data from the database.
     */
    DELETE(OperationType.DELETE),

    /**
     * DROP query type.
     * Used for deleting database structures or objects.
     */
    DROP(OperationType.DELETE);

    private final OperationType operationType;

    /**
     * Constructor for MySqlQueryType.
     * @param operationType the operation type of the query
     */
    MySqlQueryType(OperationType operationType) { this.operationType = operationType; }

    /**
     * Get the operation type of the query.
     * @return the OperationType
     */
    @Override
    public OperationType getOperationType() { return operationType; }

    /**
     * Get the keyword associated with the query type.
     * @return the query keyword
     */
    @Override
    public String getKeyword() { return this.name(); }
}
