package jsi.connection.database.mysql.parser;

import jsi.connection.database.mysql.query.MySqlQueryType;

/**
 * Utility class for extracting table names from MySQL queries.
 * Handles various query types and table name formats including backticks.
 */
public final class MySqlTableNameExtractor {

    private MySqlTableNameExtractor() {}

    /**
     * Extract the target table name from a MySQL query.
     * 
     * @param query the normalized query string
     * @param queryType the type of query
     * @return the table name, or null if not found
     */
    public static String extractTableName(String query, MySqlQueryType queryType) {

        if (query == null || queryType == null) return null;
        var normalizedQuery = query.trim().replaceAll("\\s+", " ");
        return switch (queryType) {

            case SELECT -> extractFromSelectQuery(normalizedQuery);
            case INSERT -> extractFromInsertQuery(normalizedQuery);
            case UPDATE -> extractFromUpdateQuery(normalizedQuery, queryType);
            case DELETE -> extractFromDeleteQuery(normalizedQuery);
            case CREATE -> extractFromCreateQuery(normalizedQuery);
            case DROP -> extractFromDropQuery(normalizedQuery);
            case ALTER -> extractFromAlterQuery(normalizedQuery);
            default -> null;
        };
    }

    /**
     * Extract table name from SELECT query.
     * Pattern: SELECT ... FROM table_name
     * @param query the normalized query string
     * @return the table name
     */
    private static String extractFromSelectQuery(String query) {

        var fromIndex = query.toUpperCase().indexOf(" FROM ");
        if (fromIndex == -1) return null;

        var afterFrom = query.substring(fromIndex + 6).trim();
        return extractTableNameFromText(afterFrom);
    }

    /**
     * Extract table name from INSERT query.
     * Pattern: INSERT INTO table_name
     * @param query the normalized query string
     * @return the table name
     */
    private static String extractFromInsertQuery(String query) {

        var intoIndex = query.toUpperCase().indexOf(" INTO ");
        if (intoIndex == -1) return null;

        var afterInto = query.substring(intoIndex + 6).trim();
        return extractTableNameFromText(afterInto);
    }

    /**
     * Extract table name from UPDATE query.
     * Pattern: UPDATE table_name
     * @param query the normalized query string
     * @param queryType the type of query
     * @return the table name
     */
    private static String extractFromUpdateQuery(String query, MySqlQueryType queryType) {

        var afterUpdate = query.substring(queryType.getKeyword().length()).trim();
        return extractTableNameFromText(afterUpdate);
    }

    /**
     * Extract table name from DELETE query.
     * Pattern: DELETE FROM table_name
     * @param query the normalized query string
     * @return the table name
     */
    private static String extractFromDeleteQuery(String query) {

        var fromIndex = query.toUpperCase().indexOf(" FROM ");
        if (fromIndex == -1) return null;

        var afterFrom = query.substring(fromIndex + 6).trim();
        return extractTableNameFromText(afterFrom);
    }

    /**
     * Extract table name from CREATE query.
     * Pattern: CREATE TABLE table_name
     * @param query the normalized query string
     * @return the table name
     */
    private static String extractFromCreateQuery(String query) {

        var tableIndex = query.toUpperCase().indexOf(" TABLE ");
        if (tableIndex == -1) return null;

        var afterTable = query.substring(tableIndex + 7).trim();
        return extractTableNameFromText(afterTable);
    }

    /**
     * Extract table name from DROP query.
     * Pattern: DROP TABLE table_name
     * @param query the normalized query string
     * @return the table name
     */
    private static String extractFromDropQuery(String query) {

        var tableIndex = query.toUpperCase().indexOf(" TABLE ");
        if (tableIndex == -1) return null;

        var afterTable = query.substring(tableIndex + 7).trim();
        return extractTableNameFromText(afterTable);
    }

    /**
     * Extract table name from ALTER query.
     * Pattern: ALTER TABLE table_name
     * @param query the normalized query string
     * @return the table name
     */
    private static String extractFromAlterQuery(String query) {

        var tableIndex = query.toUpperCase().indexOf(" TABLE ");
        if (tableIndex == -1) return null;

        var afterTable = query.substring(tableIndex + 7).trim();
        return extractTableNameFromText(afterTable);
    }

    /**
     * Extract the table name from a string, handling backticks and stopping at keywords.
     * Supports formats like: table, `table`, schema.table, `schema`.`table`
     * 
     * @param text the text containing the table name
     * @return the extracted table name (without schema prefix if present)
     */
    private static String extractTableNameFromText(String text) {

        if (text == null || text.isEmpty()) return null;

        // Handle backtick-quoted table names
        if (text.startsWith("`")) {

            var endQuote = text.indexOf("`", 1);
            if (endQuote != -1) {

                var tableName = text.substring(1, endQuote);
                
                // Check if there's a schema prefix (e.g., schema.table or `schema`.`table`)
                var dotIndex = text.indexOf(".", endQuote);
                if (dotIndex != -1 && dotIndex < text.length() - 1) {

                    // There's a schema, extract the table name after the dot
                    var afterDot = text.substring(dotIndex + 1).trim();
                    if (afterDot.startsWith("`")) {

                        var tableEndQuote = afterDot.indexOf("`", 1);
                        if (tableEndQuote != -1) return afterDot.substring(1, tableEndQuote);
                    }
                }
                
                return tableName;
            }
        }

        // Extract until whitespace or special character
        var parts = text.split("[\\s(,;]", 2);
        var tableName = parts[0].trim();
        
        // Handle schema.table format
        var dotIndex = tableName.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < tableName.length() - 1) tableName = tableName.substring(dotIndex + 1);
        
        // Remove any remaining backticks
        return MySqlValueParser.removeBackticks(tableName);
    }
}
