package JavaServerInterface.connection.database.mysql;

import java.util.List;
import java.util.Map;

import JavaServerInterface.connection.database.Query;

public class MySqlQuery extends Query {

    private QueryType queryType;
    private String rawSql;
    private String tableName;
    private List<String> columns;
    private Map<String, Object> values;
    private String whereClause;
    
    /**
     * Get the type of the SQL query.
     * @return the QueryType enum value
     */
    public QueryType getQueryType() { return queryType; }
    
    /**
     * Set the type of the SQL query.
     * @param queryType the QueryType enum value
     */
    public void setQueryType(QueryType queryType) { this.queryType = queryType; }
    
    /**
     * Get the raw SQL string of the query.
     * @return the raw SQL string
     */
    public String getRawSql() { return rawSql; }
    
    /**
     * Set the raw SQL string of the query.
     * @param rawSql the raw SQL string
     */
    public void setRawSql(String rawSql) { this.rawSql = rawSql; }
    
    /**
     * Get the table name involved in the query.
     * @return the table name
     */
    public String getTableName() {
        return tableName;
    }
    
    /**
     * Set the table name involved in the query.
     * @param tableName the table name
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    /**
     * Get the list of columns involved in the query.
     * @return the list of columns
     */
    public List<String> getColumns() {
        return columns;
    }
    
    /**
     * Set the list of columns involved in the query.
     * @param columns the list of columns
     */
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
    
    /**
     * Get the map of column-value pairs involved in the query.
     * @return the map of column-value pairs
     */
    public Map<String, Object> getValues() {
        return values;
    }
    
    /**
     * Set the map of column-value pairs involved in the query.
     * @param values the map of column-value pairs
     */
    public void setValues(Map<String, Object> values) {
        this.values = values;
    }
    
    /**
     * Get the WHERE clause of the query.
     * @return the WHERE clause
     */
    public String getWhereClause() {
        return whereClause;
    }
    
    /**
     * Set the WHERE clause of the query.
     * @param whereClause the WHERE clause
     */
    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }
}
