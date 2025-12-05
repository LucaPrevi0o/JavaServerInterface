package server.connection.database.mysql;

import server.connection.database.DatabaseClientHandler;
import server.connection.database.DatabaseEngine;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class MySqlDatabaseClientHandler extends DatabaseClientHandler {
    
    /**
     * Constructor for MySqlDatabaseClientHandler.
     * @param clientSocket the client socket
     * @param databaseEngine the database engine
     */
    public MySqlDatabaseClientHandler(Socket clientSocket, DatabaseEngine databaseEngine) { super(clientSocket, databaseEngine); }

    /**
     * Parse the incoming request string into a MySqlQuery object.
     * @param input the input string representing the request
     * @return the parsed MySqlQuery object
     */
    @Override
    protected MySqlQuery parseRequest(String input) {

        var sql = input.trim().toUpperCase();
        var query = new MySqlQuery();
        
        var matched = false;
        for (var type : QueryType.values()) if (sql.startsWith(type.getSqlKeyword())) {

            query.setRawSql(input);
            query.setQueryType(type);
            matched = true;
            
            switch (type) {
                case SELECT -> parseSelectQuery(input, query);
                case INSERT -> parseInsertQuery(input, query);
                case UPDATE -> parseUpdateQuery(input, query);
                case DELETE -> parseDeleteQuery(input, query);
                case CREATE -> parseCreateQuery(input, query);
                case DROP -> parseDropQuery(input, query);
                default -> {} // Other types can be implemented similarly
            }
            break;
        }
        
        if (!matched) throw new IllegalArgumentException("Unsupported SQL query: " + input);
        return query;
    }
    
    /**
     * Parse a SELECT query.
     * @param sql the SQL query string
     * @param query the MySqlQuery object to populate
     */
    private void parseSelectQuery(String sql, MySqlQuery query) {
        
        // Simple regex-based parsing
        var fromPattern = Pattern.compile("FROM\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        var fromMatcher = fromPattern.matcher(sql);
        if (fromMatcher.find()) query.setTableName(fromMatcher.group(1));
        
        // Parse columns (between SELECT and FROM)
        var colPattern = Pattern.compile("SELECT\\s+(.+?)\\s+FROM", Pattern.CASE_INSENSITIVE);
        var colMatcher = colPattern.matcher(sql);
        if (colMatcher.find()) {

            var columnsStr = colMatcher.group(1).trim();
            var columns = new ArrayList<String>();
            for (var col : columnsStr.split(",")) columns.add(col.trim());
            query.setColumns(columns);
        }
        
        // Parse WHERE clause
        var wherePattern = Pattern.compile("WHERE\\s+(.+?)(?:ORDER BY|LIMIT|$)", Pattern.CASE_INSENSITIVE);
        var whereMatcher = wherePattern.matcher(sql);
        if (whereMatcher.find()) query.setWhereClause(whereMatcher.group(1).trim());
    }
    
    /**     
     * * Parse an INSERT query.
     * @param sql the SQL query string
     * @param query the MySqlQuery object to populate
     */
    private void parseInsertQuery(String sql, MySqlQuery query) {
        
        // INSERT INTO table (col1, col2) VALUES (val1, val2)
        var tablePattern = Pattern.compile("INSERT\\s+INTO\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        var tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTableName(tableMatcher.group(1));
        
        // Parse columns
        var colPattern = Pattern.compile("\\((.*?)\\)\\s*VALUES", Pattern.CASE_INSENSITIVE);
        var colMatcher = colPattern.matcher(sql);
        if (colMatcher.find()) {

            var columnsStr = colMatcher.group(1);
            var columns = new ArrayList<String>();
            for (var col : columnsStr.split(",")) columns.add(col.trim());
            query.setColumns(columns);
        }
        
        // Parse values
        var valPattern = Pattern.compile("VALUES\\s*\\((.*?)\\)", Pattern.CASE_INSENSITIVE);
        var valMatcher = valPattern.matcher(sql);
        if (valMatcher.find()) {

            var valuesStr = valMatcher.group(1);
            var values = new HashMap<String, Object>();
            var vals = valuesStr.split(",");
            var columns = query.getColumns();
            for (var i = 0; i < vals.length && i < columns.size(); i++) {

                var val = vals[i].trim().replaceAll("^'|'$", "");
                values.put(columns.get(i), val);
            }
            query.setValues(values);
        }
    }
    
    /**
     * Parse an UPDATE query.
     * @param sql the SQL query string
     * @param query the MySqlQuery object to populate
     */
    private void parseUpdateQuery(String sql, MySqlQuery query) {
        
        // UPDATE table SET col1=val1, col2=val2 WHERE condition
        var tablePattern = Pattern.compile("UPDATE\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        var tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTableName(tableMatcher.group(1));
        
        // Parse SET clause
        var setPattern = Pattern.compile("SET\\s+(.+?)(?:WHERE|$)", Pattern.CASE_INSENSITIVE);
        var setMatcher = setPattern.matcher(sql);
        if (setMatcher.find()) {

            var setClause = setMatcher.group(1).trim();
            var values = new HashMap<String, Object>();
            for (var assignment : setClause.split(",")) {
                
                var parts = assignment.split("=");
                if (parts.length == 2) {

                    var col = parts[0].trim();
                    var val = parts[1].trim().replaceAll("^'|'$", "");
                    values.put(col, val);
                }
            }
            query.setValues(values);
        }
        
        // Parse WHERE clause
        var wherePattern = Pattern.compile("WHERE\\s+(.+)$", Pattern.CASE_INSENSITIVE);
        var whereMatcher = wherePattern.matcher(sql);
        if (whereMatcher.find()) query.setWhereClause(whereMatcher.group(1).trim());
    }
    
    /**
     * Parse a DELETE query.
     * @param sql the SQL query string
     * @param query the MySqlQuery object to populate
     */
    private void parseDeleteQuery(String sql, MySqlQuery query) {
        
        // DELETE FROM table WHERE condition
        var tablePattern = Pattern.compile("DELETE\\s+FROM\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        var tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTableName(tableMatcher.group(1));
        
        // Parse WHERE clause
        var wherePattern = Pattern.compile("WHERE\\s+(.+)$", Pattern.CASE_INSENSITIVE);
        var whereMatcher = wherePattern.matcher(sql);
        if (whereMatcher.find()) query.setWhereClause(whereMatcher.group(1).trim());
    }
    
    /**
     * Parse a CREATE query.
     * @param sql the SQL query string
     * @param query the MySqlQuery object to populate
     */
    private void parseCreateQuery(String sql, MySqlQuery query) {
        
        // CREATE TABLE table_name [(col1, col2, ...)]
        var tablePattern = Pattern.compile("CREATE\\s+TABLE\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        var tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTableName(tableMatcher.group(1));
        
        // Parse columns if specified
        var colPattern = Pattern.compile("\\((.*?)\\)", Pattern.CASE_INSENSITIVE);
        var colMatcher = colPattern.matcher(sql);
        if (colMatcher.find()) {

            var columnsStr = colMatcher.group(1);
            var columns = new ArrayList<String>();
            for (var col : columnsStr.split(",")) columns.add(col.trim());
            query.setColumns(columns);
        }
    }
    
    /**
     * Parse a DROP query.
     * @param sql the SQL query string
     * @param query the MySqlQuery object to populate
     */
    private void parseDropQuery(String sql, MySqlQuery query) {
        
        // DROP TABLE table_name
        var tablePattern = Pattern.compile("DROP\\s+TABLE\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        var tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTableName(tableMatcher.group(1));
    }
}
