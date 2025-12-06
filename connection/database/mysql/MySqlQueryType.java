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

    MySqlQueryType(QueryType queryCategory, String sqlKeyword) {
        
        this.queryCategory = queryCategory;
        this.sqlKeyword = sqlKeyword;
    }

    public String getSqlKeyword() { return sqlKeyword; }
    public QueryType getQueryCategory() { return queryCategory; }
}
