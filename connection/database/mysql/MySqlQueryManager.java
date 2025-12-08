package jsi.connection.database.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Utility class for parsing and managing MySQL queries.
 */
public class MySqlQueryManager {

    /**
     * Determine the type of SQL query.
     * @param sql the SQL query string
     * @return the QueryType enum value
     */
    public static MySqlQueryType determineQueryType(String sql) {
        
        var upperSql = sql.toUpperCase().trim();
        
        for (var type : MySqlQueryType.values())
            if (upperSql.startsWith(type.getSqlKeyword())) return type;
        
        throw new IllegalArgumentException("Unknown query type: " + sql);
    }

    /**
     * Parse a SELECT query: SELECT col1, col2 FROM table WHERE condition
     * @param sql the SQL query string
     * @param query the MySqlQuery object to populate
     */
    public static void parseSelect(String sql, MySqlQuery query) {
        
        // Extract table name
        var tablePattern = Pattern.compile("FROM\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        var tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTargetCollection(tableMatcher.group(1));
        
        // Extract columns
        var colPattern = Pattern.compile("SELECT\\s+(.+?)\\s+FROM", Pattern.CASE_INSENSITIVE);
        var colMatcher = colPattern.matcher(sql);
        if (colMatcher.find()) {

            var colsStr = colMatcher.group(1).trim();
            var columns = new ArrayList<String>();
            if (colsStr.equals("*")) columns.add("*");
            else for (var col : colsStr.split(","))
                columns.add(col.trim());
            query.setAffectedFields(columns);
        }

        // Extract WHERE clause
        extractWhereClause(sql, query);
    }

    /**
     * Parse an INSERT query: INSERT INTO table (col1, col2) VALUES (val1, val2)
     * @param sql the SQL query string
     * @param query the MySqlQuery object to populate
     */
    public static void parseInsert(String sql, MySqlQuery query) {
        
        // Extract table name
        var tablePattern = Pattern.compile("INSERT\\s+INTO\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        var tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTargetCollection(tableMatcher.group(1));

        // Extract columns
        var colPattern = Pattern.compile("\\(([^)]+)\\)\\s*VALUES", Pattern.CASE_INSENSITIVE);
        var colMatcher = colPattern.matcher(sql);
        if (colMatcher.find()) {

            var colsStr = colMatcher.group(1).trim();
            var columns = new ArrayList<String>();
            for (var col : colsStr.split(",")) columns.add(col.trim());
            query.setAffectedFields(columns);
        }

        // Extract values
        var valPattern = Pattern.compile("VALUES\\s*\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE);
        var valMatcher = valPattern.matcher(sql);
        if (valMatcher.find()) {

            var valsStr = valMatcher.group(1).trim();
            var valArray = valsStr.split(",");
            
            var values = new HashMap<String, Object>();
            var columns = query.getAffectedFields();
            if (columns != null && columns.size() == valArray.length) for (var i = 0; i < columns.size(); i++) {
                    
                var value = valArray[i].trim();
                // Remove quotes if present
                if (value.startsWith("'") && value.endsWith("'")) value = value.substring(1, value.length() - 1);
                values.put(columns.get(i), value);
            }
            query.setDataValues(values);
        }
    }

    /**
     * Parse an UPDATE query: UPDATE table SET col1=val1, col2=val2 WHERE condition
     * @param sql the SQL query string
     * @param query the MySqlQuery object to populate
     */
    public static void parseUpdate(String sql, MySqlQuery query) {
        
        // Extract table name
        var tablePattern = Pattern.compile("UPDATE\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        var tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTargetCollection(tableMatcher.group(1));

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
            query.setDataValues(values);
        }

        // Extract WHERE clause
        extractWhereClause(sql, query);
    }

    /**
     * Parse a DELETE query: DELETE FROM table WHERE condition
     * @param sql the SQL query string
     * @param query the MySqlQuery object to populate
     */
    public static void parseDelete(String sql, MySqlQuery query) {
        
        // Extract table name
        var tablePattern = Pattern.compile("DELETE\\s+FROM\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        var tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTargetCollection(tableMatcher.group(1));

        // Extract WHERE clause
        extractWhereClause(sql, query);
    }

    /**
     * Extract WHERE clause from any SQL statement and parse it into a QueryCondition.
     * @param sql the SQL query string
     * @param query the MySqlQuery object to populate
     */
    public static void extractWhereClause(String sql, MySqlQuery query) {

        var wherePattern = Pattern.compile("WHERE\\s+(.+?)(?:;|$)", Pattern.CASE_INSENSITIVE);
        var whereMatcher = wherePattern.matcher(sql);
        if (whereMatcher.find()) {

            var whereClause = whereMatcher.group(1).trim();
            var condition = MySqlQueryCondition.parse(whereClause);
            query.setWhereCondition(condition);
        }
    }
}
