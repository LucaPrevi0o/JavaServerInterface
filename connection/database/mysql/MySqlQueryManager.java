package JavaServerInterface.connection.database.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Utility class for parsing and managing MySQL queries.
 */
public class MySqlQueryManager {
    
    /**
     * Parse a SQL query string into a MySqlQuery object.
     * Supports basic SELECT, INSERT, UPDATE, and DELETE statements.
     * 
     * @param input the SQL query string
     * @return a parsed MySqlQuery object
     */
    public static MySqlQuery parse(String input) {
        
        if (input == null || input.trim().isEmpty()) throw new IllegalArgumentException("Query cannot be null or empty");

        var query = new MySqlQuery();
        var sql = input.trim();
        query.setRawSql(sql);

        // Determine query type
        var queryType = determineQueryType(sql);
        query.setQueryType(queryType);

        // Parse based on query type
        switch (queryType) {
            case SELECT:
                parseSelect(sql, query);
                break;
            case INSERT:
                parseInsert(sql, query);
                break;
            case UPDATE:
                parseUpdate(sql, query);
                break;
            case DELETE:
                parseDelete(sql, query);
                break;
            default:
                // For CREATE, DROP, ALTER - just store raw SQL
                break;
        }

        return query;
    }

    /**
     * Determine the type of SQL query.
     * @param sql the SQL query string
     * @return the QueryType enum value
     */
    private static QueryType determineQueryType(String sql) {
        
        var upperSql = sql.toUpperCase().trim();
        
        for (var type : QueryType.values())
            if (upperSql.startsWith(type.getSqlKeyword())) return type;
        
        throw new IllegalArgumentException("Unknown query type: " + sql);
    }

    /**
     * Parse a SELECT query: SELECT col1, col2 FROM table WHERE condition
     * @param sql the SQL query string
     * @param query the MySqlQuery object to populate
     */
    private static void parseSelect(String sql, MySqlQuery query) {
        
        // Extract table name
        var tablePattern = Pattern.compile("FROM\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        var tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTableName(tableMatcher.group(1));

        // Extract columns
        var colPattern = Pattern.compile("SELECT\\s+(.+?)\\s+FROM", Pattern.CASE_INSENSITIVE);
        var colMatcher = colPattern.matcher(sql);
        if (colMatcher.find()) {

            var colsStr = colMatcher.group(1).trim();
            var columns = new ArrayList<String>();
            if (colsStr.equals("*")) columns.add("*");
            else for (var col : colsStr.split(","))
                columns.add(col.trim());
            query.setColumns(columns);
        }

        // Extract WHERE clause
        extractWhereClause(sql, query);
    }

    /**
     * Parse an INSERT query: INSERT INTO table (col1, col2) VALUES (val1, val2)
     * @param sql the SQL query string
     * @param query the MySqlQuery object to populate
     */
    private static void parseInsert(String sql, MySqlQuery query) {
        
        // Extract table name
        var tablePattern = Pattern.compile("INSERT\\s+INTO\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        var tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTableName(tableMatcher.group(1));

        // Extract columns
        var colPattern = Pattern.compile("\\(([^)]+)\\)\\s*VALUES", Pattern.CASE_INSENSITIVE);
        var colMatcher = colPattern.matcher(sql);
        if (colMatcher.find()) {

            var colsStr = colMatcher.group(1).trim();
            var columns = new ArrayList<String>();
            for (var col : colsStr.split(",")) columns.add(col.trim());
            query.setColumns(columns);
        }

        // Extract values
        var valPattern = Pattern.compile("VALUES\\s*\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE);
        var valMatcher = valPattern.matcher(sql);
        if (valMatcher.find()) {

            var valsStr = valMatcher.group(1).trim();
            var valArray = valsStr.split(",");
            
            var values = new HashMap<String, Object>();
            var columns = query.getColumns();
            if (columns != null && columns.size() == valArray.length) for (var i = 0; i < columns.size(); i++) {
                    
                var value = valArray[i].trim();
                // Remove quotes if present
                if (value.startsWith("'") && value.endsWith("'")) value = value.substring(1, value.length() - 1);
                values.put(columns.get(i), value);
            }
            query.setValues(values);
        }
    }

    /**
     * Parse an UPDATE query: UPDATE table SET col1=val1, col2=val2 WHERE condition
     * @param sql the SQL query string
     * @param query the MySqlQuery object to populate
     */
    private static void parseUpdate(String sql, MySqlQuery query) {
        
        // Extract table name
        var tablePattern = Pattern.compile("UPDATE\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        var tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTableName(tableMatcher.group(1));

        // Extract SET clause
        var setPattern = Pattern.compile("SET\\s+(.+?)(?:\\s+WHERE|$)", Pattern.CASE_INSENSITIVE);
        var setMatcher = setPattern.matcher(sql);
        if (setMatcher.find()) {

            var setClause = setMatcher.group(1).trim();
            var values = new HashMap<String, Object>();
            for (var assignment : setClause.split(",")) {

                var parts = assignment.split("=");
                if (parts.length == 2) {

                    var col = parts[0].trim();
                    var val = parts[1].trim();
                    // Remove quotes if present
                    if (val.startsWith("'") && val.endsWith("'")) val = val.substring(1, val.length() - 1);
                    values.put(col, val);
                }
            }
            query.setValues(values);
        }

        // Extract WHERE clause
        extractWhereClause(sql, query);
    }

    /**
     * Parse a DELETE query: DELETE FROM table WHERE condition
     * @param sql the SQL query string
     * @param query the MySqlQuery object to populate
     */
    private static void parseDelete(String sql, MySqlQuery query) {
        
        // Extract table name
        var tablePattern = Pattern.compile("DELETE\\s+FROM\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        var tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTableName(tableMatcher.group(1));

        // Extract WHERE clause
        extractWhereClause(sql, query);
    }

    /**
     * Extract WHERE clause from any SQL statement.
     * @param sql the SQL query string
     * @param query the MySqlQuery object to populate
     */
    private static void extractWhereClause(String sql, MySqlQuery query) {

        var wherePattern = Pattern.compile("WHERE\\s+(.+?)(?:;|$)", Pattern.CASE_INSENSITIVE);
        var whereMatcher = wherePattern.matcher(sql);
        if (whereMatcher.find()) query.setWhereClause(whereMatcher.group(1).trim());
    }
}
