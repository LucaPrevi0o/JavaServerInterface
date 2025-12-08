package jsi.connection.database.mysql;

import jsi.connection.database.Query;

public class MySqlQuery extends Query<MySqlQueryCondition> {

    /**
     * Construct a MySqlQuery from a raw SQL query string.
     * Delegates parsing to MySqlQueryManager.
     * @param queryString the raw SQL query string
     */
    public MySqlQuery(String queryString) {

        if (queryString == null || queryString.trim().isEmpty()) 
            throw new IllegalArgumentException("Query cannot be null or empty");

        var sql = queryString.trim();
        this.rawQuery = sql;

        // Determine query type
        var queryType = MySqlQueryManager.determineQueryType(sql);
        this.queryType = queryType;
        
        // Parse based on query type
        switch (queryType.getOperationType()) {
            case READ:
                MySqlQueryManager.parseSelect(sql, this);
                break;
            case CREATE:
                MySqlQueryManager.parseInsert(sql, this);
                break;
            case UPDATE:
                MySqlQueryManager.parseUpdate(sql, this);
                break;
            case DELETE:
                MySqlQueryManager.parseDelete(sql, this);
                break;
            default:
                // For CREATE, DROP, ALTER - just store raw SQL
                break;
        }
    }
}
