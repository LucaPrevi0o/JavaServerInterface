package server.connection.database.mysql;

import common.Request;
import server.connection.database.DatabaseEngine;
import server.connection.database.Query;
import server.connection.database.QueryResult;
import java.util.List;
import java.util.Map;

public class MySqlQuery extends Query {

    private QueryType queryType;
    private String rawSql;
    private String tableName;
    private List<String> columns;
    private Map<String, Object> values;
    private String whereClause;
    
    // Getters and setters
    public QueryType getQueryType() {
        return queryType;
    }
    
    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }
    
    public String getRawSql() {
        return rawSql;
    }
    
    public void setRawSql(String rawSql) {
        this.rawSql = rawSql;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public List<String> getColumns() {
        return columns;
    }
    
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
    
    public Map<String, Object> getValues() {
        return values;
    }
    
    public void setValues(Map<String, Object> values) {
        this.values = values;
    }
    
    public String getWhereClause() {
        return whereClause;
    }
    
    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }
    
    @Override
    public QueryResult execute(DatabaseEngine databaseEngine) {
        return databaseEngine.execute(this);
    }

    @Override
    public Request parse(String input) {
        throw new UnsupportedOperationException("Use MySqlDatabaseClientHandler.parseRequest instead");
    }
    
    @Override
    public String toString() {
        return "MySqlQuery{" +
                "type=" + queryType +
                ", table='" + tableName + '\'' +
                ", columns=" + columns +
                ", where='" + whereClause + '\'' +
                '}';
    }
}
