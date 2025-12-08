package jsi.connection.database.mysql;

import jsi.connection.database.OperationType;
import jsi.connection.database.QueryType;

public enum MySqlQueryType implements QueryType {
    
    SELECT(OperationType.READ, "SELECT"),
    SHOW(OperationType.READ, "SHOW"),
    INSERT(OperationType.CREATE, "INSERT"),
    UPDATE(OperationType.UPDATE, "UPDATE"),
    DELETE(OperationType.DELETE, "DELETE"),
    CREATE(OperationType.CREATE, "CREATE"),
    DROP(OperationType.CREATE, "DROP"),
    ALTER(OperationType.CREATE, "ALTER");

    private final String sqlKeyword;
    private final OperationType queryCategory;

    /**
     * Constructor for MySqlQueryType.
     * @param queryCategory the category of the query
     * @param sqlKeyword the SQL keyword representing the query type
     */
    MySqlQueryType(OperationType queryCategory, String sqlKeyword) {
        
        this.queryCategory = queryCategory;
        this.sqlKeyword = sqlKeyword;
    }

    /**
     * Get the SQL keyword for the query type.
     * @return the SQL keyword
     */
    public String getSqlKeyword() { return sqlKeyword; }

    /**
     * Get the category of the query.
     * @return the QueryType
     */
    public OperationType getOperationType() { return queryCategory; }
}
