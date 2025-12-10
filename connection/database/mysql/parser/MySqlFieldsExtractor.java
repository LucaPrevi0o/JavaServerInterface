package jsi.connection.database.mysql.parser;

import jsi.connection.database.Field;
import jsi.connection.database.mysql.query.MySqlQueryType;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for extracting fields from MySQL queries.
 * Handles various query types including SELECT, INSERT, UPDATE, CREATE TABLE, and ALTER TABLE.
 */
public final class MySqlFieldsExtractor {

    private MySqlFieldsExtractor() {}

    /**
     * Extract the fields affected by a MySQL query.
     * 
     * @param query the normalized query string
     * @param queryType the type of query
     * @return list of Field objects (with null values for most cases)
     */
    public static List<Field> extractFields(String query, MySqlQueryType queryType) {

        if (query == null || queryType == null) return new ArrayList<>();
        var normalizedQuery = query.trim().replaceAll("\\s+", " ");
        return switch (queryType) {

            case SELECT -> extractSelectFields(normalizedQuery);
            case INSERT -> extractInsertFields(normalizedQuery);
            case UPDATE -> extractUpdateFields(normalizedQuery);
            case CREATE -> extractCreateTableFields(normalizedQuery);
            case ALTER -> extractAlterTableFields(normalizedQuery);
            default -> new ArrayList<>();
        };
    }

    /**
     * Extract fields from SELECT query.
     * Pattern: SELECT col1, col2, col3 FROM table
     * @param query the normalized query string
     * @return list of Field objects (with null values)
     */
    private static List<Field> extractSelectFields(String query) {

        var selectIndex = query.toUpperCase().indexOf("SELECT");
        var fromIndex = query.toUpperCase().indexOf(" FROM ");

        if (selectIndex == -1 || fromIndex == -1) return new ArrayList<>();

        var columnsPart = query.substring(selectIndex + 6, fromIndex).trim();

        // Handle SELECT *
        if (columnsPart.equals("*")) return new ArrayList<>();
        return parseColumnNames(columnsPart);
    }

    /**
     * Extract fields from INSERT query.
     * Pattern: INSERT INTO table (col1, col2, col3) VALUES (...)
     * @param query the normalized query string
     * @return list of Field objects (with null values)
     */
    private static List<Field> extractInsertFields(String query) {

        var leftParen = query.indexOf("(");
        var rightParen = query.indexOf(")");

        if (leftParen == -1 || rightParen == -1) return new ArrayList<>();

        var columnsPart = query.substring(leftParen + 1, rightParen).trim();
        return parseColumnNames(columnsPart);
    }

    /**
     * Extract fields from UPDATE query.
     * Pattern: UPDATE table SET col1 = val1, col2 = val2
     * @param query the normalized query string
     * @return list of Field objects (with null values)
     */
    private static List<Field> extractUpdateFields(String query) {

        var setIndex = query.toUpperCase().indexOf(" SET ");
        if (setIndex == -1) return new ArrayList<>();

        var afterSet = query.substring(setIndex + 5);

        // Find WHERE clause if exists
        var whereIndex = afterSet.toUpperCase().indexOf(" WHERE ");
        var setPart = whereIndex == -1 ? afterSet : afterSet.substring(0, whereIndex);

        return parseSetClause(setPart.trim());
    }

    /**
     * Extract fields from CREATE TABLE query.
     * Pattern: CREATE TABLE table (col1 TYPE, col2 TYPE, ...)
     * @param query the normalized query string
     * @return list of Field objects (with null values)
     */
    private static List<Field> extractCreateTableFields(String query) {

        var leftParen = query.indexOf("(");
        var rightParen = query.lastIndexOf(")");

        if (leftParen == -1 || rightParen == -1) return new ArrayList<>();

        var columnsPart = query.substring(leftParen + 1, rightParen).trim();
        return parseCreateTableColumns(columnsPart);
    }

    /**
     * Extract fields from ALTER TABLE query.
     * Pattern: ALTER TABLE table ADD/MODIFY/DROP COLUMN col_name
     * @param query the normalized query string
     * @return list of Field objects (with null values)
     */
    private static List<Field> extractAlterTableFields(String query) {

        var fields = new ArrayList<Field>();
        var upper = query.toUpperCase();

        // ALTER TABLE table ADD COLUMN col_name
        if (upper.contains(" ADD ")) {

            var field = extractAlterColumnName(query, upper, " ADD ", 5);
            if (field != null) fields.add(field);
        }

        // ALTER TABLE table DROP COLUMN col_name
        if (upper.contains(" DROP ")) {

            var field = extractAlterColumnName(query, upper, " DROP ", 6);
            if (field != null) fields.add(field);
        }

        // ALTER TABLE table MODIFY COLUMN col_name
        if (upper.contains(" MODIFY ")) {

            var field = extractAlterColumnName(query, upper, " MODIFY ", 8);
            if (field != null) fields.add(field);
        }

        return fields;
    }

    /**
     * Extract column name from ALTER TABLE statement.
     * @param query the normalized query string
     * @param upperQuery the uppercase version of the query
     * @param keyword the ALTER operation keyword (e.g., " ADD ", " DROP ", " MODIFY ")
     * @param keywordLength the length of the keyword
     * @return Field object with the column name (null value)
     */
    private static Field extractAlterColumnName(String query, String upperQuery, String keyword, int keywordLength) {

        var keywordIndex = upperQuery.indexOf(keyword);
        var afterKeyword = query.substring(keywordIndex + keywordLength).trim();

        // Skip COLUMN keyword if present
        if (afterKeyword.toUpperCase().startsWith("COLUMN ")) afterKeyword = afterKeyword.substring(7).trim();

        var parts = afterKeyword.split("\\s+", 2);
        if (parts.length > 0) {

            var columnName = MySqlValueParser.removeBackticks(parts[0]);
            if (!columnName.isEmpty()) return new Field(columnName, null);
        }

        return null;
    }

    /**
     * Parse comma-separated column names.
     * Handles aliases (col AS alias or col alias) and backticks.
     * 
     * @param columnsPart the string containing column names
     * @return list of Field objects (with null values)
     */
    private static List<Field> parseColumnNames(String columnsPart) {

        var fields = new ArrayList<Field>();
        var columns = columnsPart.split(",");

        for (var col : columns) {

            var columnName = col.trim();

            // Remove backticks if present
            columnName = MySqlValueParser.removeBackticks(columnName);

            // Handle aliases (col AS alias or col alias)
            var parts = columnName.split("\\s+");
            if (parts.length > 0) {

                columnName = parts[0];
                // Remove backticks again if needed
                columnName = MySqlValueParser.removeBackticks(columnName);
            }

            if (!columnName.isEmpty()) fields.add(new Field(columnName, null));
        }

        return fields;
    }

    /**
     * Parse SET clause from UPDATE query.
     * Pattern: col1 = val1, col2 = val2
     * 
     * @param setPart the SET clause string
     * @return list of Field objects with column names
     */
    private static List<Field> parseSetClause(String setPart) {

        var fields = new ArrayList<Field>();
        var assignments = setPart.split(",");

        for (var assignment : assignments) {

            var parts = assignment.split("=", 2);
            if (parts.length > 0) {

                var columnName = MySqlValueParser.removeBackticks(parts[0].trim());
                if (!columnName.isEmpty()) fields.add(new Field(columnName, null));
            }
        }

        return fields;
    }

    /**
     * Parse column definitions from CREATE TABLE statement.
     * Filters out constraints like PRIMARY KEY, FOREIGN KEY, etc.
     * 
     * @param columnsPart the column definitions string
     * @return list of Field objects with column names
     */
    private static List<Field> parseCreateTableColumns(String columnsPart) {

        var fields = new ArrayList<Field>();

        // Split by comma, but be careful with constraints
        var definitions = columnsPart.split(",");

        for (var def : definitions) {

            var trimmed = def.trim();
            var upper = trimmed.toUpperCase();
            if (upper.startsWith("PRIMARY KEY") || upper.startsWith("FOREIGN KEY") ||
                upper.startsWith("UNIQUE") || upper.startsWith("CHECK") ||
                upper.startsWith("INDEX") || upper.startsWith("KEY")) continue;

            // Extract column name (first word)
            var parts = trimmed.split("\\s+", 2);
            if (parts.length > 0) {

                var columnName = MySqlValueParser.removeBackticks(parts[0]);
                if (!columnName.isEmpty()) fields.add(new Field(columnName, null));
            }
        }

        return fields;
    }
}
