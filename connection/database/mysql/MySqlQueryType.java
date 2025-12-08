package jsi.connection.database.mysql;

import jsi.connection.database.QueryType;

public enum MySqlQueryType {
    
    SELECT(QueryType.READ, "SELECT"),
    INSERT(QueryType.CREATE, "INSERT"),
    UPDATE(QueryType.UPDATE, "UPDATE"),
    DELETE(QueryType.DELETE, "DELETE"),
    CREATE(QueryType.CREATE, "CREATE"),
    DROP(QueryType.CREATE, "DROP"),
    ALTER(QueryType.CREATE, "ALTER");

    private final String sqlKeyword;
    private final QueryType queryCategory;

    /**
     * Constructor for MySqlQueryType.
     * @param queryCategory the category of the query
     * @param sqlKeyword the SQL keyword representing the query type
     */
    MySqlQueryType(QueryType queryCategory, String sqlKeyword) {
        
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
    public QueryType getQueryCategory() { return queryCategory; }
}
