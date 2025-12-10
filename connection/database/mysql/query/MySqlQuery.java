package jsi.connection.database.mysql.query;

import jsi.connection.database.query.Query;
import jsi.connection.database.query.QueryCondition;
import jsi.connection.database.query.QueryType;
import jsi.connection.database.Field;
import java.util.List;

public class MySqlQuery extends Query {

    /**
     * Constructor for MySqlQuery.
     * 
     * @param rawQuery the raw MySQL query string
     */
    public MySqlQuery(String rawQuery) {
        super(rawQuery);
    }

    /**
     * Get the type of the MySQL query.
     * 
     * @return the query type
     * @throws IllegalArgumentException if the query type is unknown
     */
    @Override
    public QueryType getQueryType() {

        for (var type : MySqlQueryType.values())
            if (serialize().toUpperCase().startsWith(type.getKeyword()))
                return type;
        throw new IllegalArgumentException("Unknown MySQL query type.");
    }

    /**
     * Get the target collection (table) for the MySQL query.
     * 
     * @return the target collection
     */
    @Override
    public String getTargetCollection() {
        String query = serialize().trim().replaceAll("\\s+", " ");
        QueryType type = getQueryType();

        if (type == MySqlQueryType.SELECT) {
            // SELECT ... FROM table_name
            int fromIndex = query.toUpperCase().indexOf(" FROM ");
            if (fromIndex == -1)
                return null;

            String afterFrom = query.substring(fromIndex + 6).trim();
            return extractTableName(afterFrom);
        } else if (type == MySqlQueryType.INSERT) {
            // INSERT INTO table_name
            int intoIndex = query.toUpperCase().indexOf(" INTO ");
            if (intoIndex == -1)
                return null;

            String afterInto = query.substring(intoIndex + 6).trim();
            return extractTableName(afterInto);
        } else if (type == MySqlQueryType.UPDATE) {
            // UPDATE table_name
            String afterUpdate = query.substring(type.getKeyword().length()).trim();
            return extractTableName(afterUpdate);
        } else if (type == MySqlQueryType.DELETE) {
            // DELETE FROM table_name
            int fromIndex = query.toUpperCase().indexOf(" FROM ");
            if (fromIndex == -1)
                return null;

            String afterFrom = query.substring(fromIndex + 6).trim();
            return extractTableName(afterFrom);
        } else if (type == MySqlQueryType.CREATE) {
            // CREATE TABLE table_name
            int tableIndex = query.toUpperCase().indexOf(" TABLE ");
            if (tableIndex == -1)
                return null;

            String afterTable = query.substring(tableIndex + 7).trim();
            return extractTableName(afterTable);
        } else if (type == MySqlQueryType.DROP) {
            // DROP TABLE table_name
            int tableIndex = query.toUpperCase().indexOf(" TABLE ");
            if (tableIndex == -1)
                return null;

            String afterTable = query.substring(tableIndex + 7).trim();
            return extractTableName(afterTable);
        } else if (type == MySqlQueryType.ALTER) {
            // ALTER TABLE table_name
            int tableIndex = query.toUpperCase().indexOf(" TABLE ");
            if (tableIndex == -1)
                return null;

            String afterTable = query.substring(tableIndex + 7).trim();
            return extractTableName(afterTable);
        }

        return null;
    }

    /**
     * Extract the table name from a string, handling backticks and stopping at
     * keywords.
     * 
     * @param text the text containing the table name
     * @return the extracted table name
     */
    private String extractTableName(String text) {
        if (text.isEmpty())
            return null;

        // Handle backtick-quoted table names
        if (text.startsWith("`")) {
            int endQuote = text.indexOf("`", 1);
            if (endQuote != -1) {
                return text.substring(1, endQuote);
            }
        }

        // Extract until whitespace or special character
        String[] parts = text.split("[\\s(,;]", 2);
        return parts[0].trim();
    }

    @Override
    public List<Field> getAffectedFields() {

        var query = serialize().trim().replaceAll("\\s+", " ");
        var type = getQueryType();
        var fields = new java.util.ArrayList<Field>();

        if (type == MySqlQueryType.SELECT) {
            // SELECT col1, col2, col3 FROM table
            int selectIndex = query.toUpperCase().indexOf("SELECT");
            int fromIndex = query.toUpperCase().indexOf(" FROM ");

            if (selectIndex == -1 || fromIndex == -1)
                return fields;

            String columnsPart = query.substring(selectIndex + 6, fromIndex).trim();

            // Handle SELECT *
            if (columnsPart.equals("*"))
                return fields;

            return parseColumnNames(columnsPart);

        } else if (type == MySqlQueryType.INSERT) {
            // INSERT INTO table (col1, col2, col3) VALUES (...)
            int leftParen = query.indexOf("(");
            int rightParen = query.indexOf(")");

            if (leftParen == -1 || rightParen == -1)
                return fields;

            String columnsPart = query.substring(leftParen + 1, rightParen).trim();
            return parseColumnNames(columnsPart);

        } else if (type == MySqlQueryType.UPDATE) {
            // UPDATE table SET col1 = val1, col2 = val2
            int setIndex = query.toUpperCase().indexOf(" SET ");
            if (setIndex == -1)
                return fields;

            String afterSet = query.substring(setIndex + 5);

            // Find WHERE clause if exists
            int whereIndex = afterSet.toUpperCase().indexOf(" WHERE ");
            String setPart = whereIndex == -1 ? afterSet : afterSet.substring(0, whereIndex);

            return parseSetClause(setPart.trim());

        } else if (type == MySqlQueryType.CREATE) {
            // CREATE TABLE table (col1 TYPE, col2 TYPE, ...)
            int leftParen = query.indexOf("(");
            int rightParen = query.lastIndexOf(")");

            if (leftParen == -1 || rightParen == -1)
                return fields;

            String columnsPart = query.substring(leftParen + 1, rightParen).trim();
            return parseCreateTableColumns(columnsPart);

        } else if (type == MySqlQueryType.ALTER) {
            // ALTER TABLE table ADD/MODIFY/DROP column ...
            return parseAlterTableColumns(query);
        }

        return fields;
    }

    /**
     * Parse comma-separated column names.
     * 
     * @param columnsPart the string containing column names
     * @return list of Field objects (with null values)
     */
    private List<Field> parseColumnNames(String columnsPart) {
        var fields = new java.util.ArrayList<Field>();
        String[] columns = columnsPart.split(",");

        for (String col : columns) {
            String columnName = col.trim();

            // Remove backticks if present
            if (columnName.startsWith("`") && columnName.endsWith("`")) {
                columnName = columnName.substring(1, columnName.length() - 1);
            }

            // Handle aliases (col AS alias or col alias)
            String[] parts = columnName.split("\\s+");
            if (parts.length > 0) {
                columnName = parts[0];
                // Remove backticks again if needed
                if (columnName.startsWith("`") && columnName.endsWith("`")) {
                    columnName = columnName.substring(1, columnName.length() - 1);
                }
            }

            if (!columnName.isEmpty()) {
                fields.add(new Field(columnName, null));
            }
        }

        return fields;
    }

    /**
     * Parse SET clause from UPDATE query.
     * 
     * @param setPart the SET clause string
     * @return list of Field objects with column names
     */
    private List<Field> parseSetClause(String setPart) {
        var fields = new java.util.ArrayList<Field>();
        String[] assignments = setPart.split(",");

        for (String assignment : assignments) {
            String[] parts = assignment.split("=", 2);
            if (parts.length > 0) {
                String columnName = parts[0].trim();

                // Remove backticks if present
                if (columnName.startsWith("`") && columnName.endsWith("`")) {
                    columnName = columnName.substring(1, columnName.length() - 1);
                }

                if (!columnName.isEmpty()) {
                    fields.add(new Field(columnName, null));
                }
            }
        }

        return fields;
    }

    /**
     * Parse column definitions from CREATE TABLE statement.
     * 
     * @param columnsPart the column definitions string
     * @return list of Field objects with column names
     */
    private List<Field> parseCreateTableColumns(String columnsPart) {
        var fields = new java.util.ArrayList<Field>();

        // Split by comma, but be careful with constraints
        String[] definitions = columnsPart.split(",");

        for (String def : definitions) {
            String trimmed = def.trim();

            // Skip constraints (PRIMARY KEY, FOREIGN KEY, etc.)
            String upper = trimmed.toUpperCase();
            if (upper.startsWith("PRIMARY KEY") || upper.startsWith("FOREIGN KEY") ||
                    upper.startsWith("UNIQUE") || upper.startsWith("CHECK") ||
                    upper.startsWith("INDEX") || upper.startsWith("KEY")) {
                continue;
            }

            // Extract column name (first word)
            String[] parts = trimmed.split("\\s+", 2);
            if (parts.length > 0) {
                String columnName = parts[0];

                // Remove backticks if present
                if (columnName.startsWith("`") && columnName.endsWith("`")) {
                    columnName = columnName.substring(1, columnName.length() - 1);
                }

                if (!columnName.isEmpty()) {
                    fields.add(new Field(columnName, null));
                }
            }
        }

        return fields;
    }

    /**
     * Parse column names from ALTER TABLE statement.
     * 
     * @param query the ALTER TABLE query
     * @return list of Field objects with affected column names
     */
    private List<Field> parseAlterTableColumns(String query) {
        var fields = new java.util.ArrayList<Field>();
        String upper = query.toUpperCase();

        // ALTER TABLE table ADD COLUMN col_name
        if (upper.contains(" ADD ")) {
            int addIndex = upper.indexOf(" ADD ");
            String afterAdd = query.substring(addIndex + 5).trim();

            // Skip COLUMN keyword if present
            if (afterAdd.toUpperCase().startsWith("COLUMN ")) {
                afterAdd = afterAdd.substring(7).trim();
            }

            String[] parts = afterAdd.split("\\s+", 2);
            if (parts.length > 0) {
                String columnName = parts[0];
                if (columnName.startsWith("`") && columnName.endsWith("`")) {
                    columnName = columnName.substring(1, columnName.length() - 1);
                }
                fields.add(new Field(columnName, null));
            }
        }

        // ALTER TABLE table DROP COLUMN col_name
        if (upper.contains(" DROP ")) {
            int dropIndex = upper.indexOf(" DROP ");
            String afterDrop = query.substring(dropIndex + 6).trim();

            // Skip COLUMN keyword if present
            if (afterDrop.toUpperCase().startsWith("COLUMN ")) {
                afterDrop = afterDrop.substring(7).trim();
            }

            String[] parts = afterDrop.split("\\s+", 2);
            if (parts.length > 0) {
                String columnName = parts[0];
                if (columnName.startsWith("`") && columnName.endsWith("`")) {
                    columnName = columnName.substring(1, columnName.length() - 1);
                }
                fields.add(new Field(columnName, null));
            }
        }

        // ALTER TABLE table MODIFY COLUMN col_name
        if (upper.contains(" MODIFY ")) {
            int modifyIndex = upper.indexOf(" MODIFY ");
            String afterModify = query.substring(modifyIndex + 8).trim();

            // Skip COLUMN keyword if present
            if (afterModify.toUpperCase().startsWith("COLUMN ")) {
                afterModify = afterModify.substring(7).trim();
            }

            String[] parts = afterModify.split("\\s+", 2);
            if (parts.length > 0) {
                String columnName = parts[0];
                if (columnName.startsWith("`") && columnName.endsWith("`")) {
                    columnName = columnName.substring(1, columnName.length() - 1);
                }
                fields.add(new Field(columnName, null));
            }
        }

        return fields;

    }

    @Override
    public MySqlQueryCondition getCondition() {

        var query = serialize().trim().replaceAll("\\s+", " ");
        var upper = query.toUpperCase();

        // Find WHERE clause
        var whereIndex = upper.indexOf(" WHERE ");
        if (whereIndex == -1)
            return null;

        // Extract WHERE condition part
        var afterWhere = query.substring(whereIndex + 7).trim();

        // Remove trailing clauses (ORDER BY, GROUP BY, LIMIT, etc.)
        var orderByIndex = afterWhere.toUpperCase().indexOf(" ORDER BY");
        var groupByIndex = afterWhere.toUpperCase().indexOf(" GROUP BY");
        var limitIndex = afterWhere.toUpperCase().indexOf(" LIMIT");
        var havingIndex = afterWhere.toUpperCase().indexOf(" HAVING");

        var endIndex = afterWhere.length();

        if (orderByIndex != -1)
            endIndex = Math.min(endIndex, orderByIndex);
        if (groupByIndex != -1)
            endIndex = Math.min(endIndex, groupByIndex);
        if (limitIndex != -1)
            endIndex = Math.min(endIndex, limitIndex);
        if (havingIndex != -1)
            endIndex = Math.min(endIndex, havingIndex);

        var conditionStr = afterWhere.substring(0, endIndex).trim();

        return parseCondition(conditionStr);
    }

    /**
     * Parse a condition string into a MySqlQueryCondition object.
     * 
     * @param conditionStr the condition string to parse
     * @return the parsed MySqlQueryCondition
     */
    private MySqlQueryCondition parseCondition(String conditionStr) {

        if (conditionStr.isEmpty())
            return null;

        // Handle logical operators (AND, OR) - simple approach
        // Find the top-level logical operator (not inside parentheses)
        var andIndex = findTopLevelOperator(conditionStr, " AND ");
        var orIndex = findTopLevelOperator(conditionStr, " OR ");

        if (andIndex != -1) {
            // Split by AND
            var left = conditionStr.substring(0, andIndex).trim();
            var right = conditionStr.substring(andIndex + 5).trim();

            var leftCondition = parseCondition(left);
            var rightCondition = parseCondition(right);

            if (leftCondition == null || rightCondition == null)
                return null;
            return new MySqlQueryCondition(QueryCondition.LogicalOperator.AND, leftCondition, rightCondition);
        }

        if (orIndex != -1) {
            // Split by OR
            var left = conditionStr.substring(0, orIndex).trim();
            var right = conditionStr.substring(orIndex + 4).trim();

            var leftCondition = parseCondition(left);
            var rightCondition = parseCondition(right);

            if (leftCondition == null || rightCondition == null)
                return null;
            return new MySqlQueryCondition(QueryCondition.LogicalOperator.OR, leftCondition, rightCondition);
        }

        // Handle parentheses
        if (conditionStr.startsWith("(") && conditionStr.endsWith(")")) {
            var inner = conditionStr.substring(1, conditionStr.length() - 1).trim();
            return parseCondition(inner);
        }

        // Handle NOT operator
        if (conditionStr.toUpperCase().startsWith("NOT ")) {
            var innerCondition = parseCondition(conditionStr.substring(4).trim());
            if (innerCondition == null)
                return null;
            return new MySqlQueryCondition(QueryCondition.LogicalOperator.NOT, innerCondition);
        }

        // Parse simple condition
        return parseSimpleCondition(conditionStr);
    }

    /**
     * Find a logical operator at the top level (not inside parentheses).
     * 
     * @param str      the string to search in
     * @param operator the operator to find
     * @return the index of the operator, or -1 if not found
     */
    private int findTopLevelOperator(String str, String operator) {

        var parenthesesLevel = 0;
        var upper = str.toUpperCase();

        for (var i = 0; i <= str.length() - operator.length(); i++) {

            if (str.charAt(i) == '(')
                parenthesesLevel++;
            else if (str.charAt(i) == ')')
                parenthesesLevel--;

            if (parenthesesLevel == 0 && upper.substring(i).startsWith(operator))
                return i;
        }

        return -1;
    }

    /**
     * Parse a simple condition (field operator value).
     * 
     * @param conditionStr the condition string to parse
     * @return the parsed MySqlQueryCondition
     */
    private MySqlQueryCondition parseSimpleCondition(String conditionStr) {

        var upper = conditionStr.toUpperCase();

        // Handle IS NULL
        if (upper.contains(" IS NULL")) {
            var fieldName = conditionStr.substring(0, upper.indexOf(" IS NULL")).trim();
            fieldName = removeBackticks(fieldName);
            return new MySqlQueryCondition(fieldName, null, MySqlQueryCondition.MySqlOperator.IS_NULL);
        }

        // Handle IS NOT NULL
        if (upper.contains(" IS NOT NULL")) {
            var fieldName = conditionStr.substring(0, upper.indexOf(" IS NOT NULL")).trim();
            fieldName = removeBackticks(fieldName);
            return new MySqlQueryCondition(fieldName, null, MySqlQueryCondition.MySqlOperator.IS_NOT_NULL);
        }

        // Handle LIKE
        if (upper.contains(" LIKE ")) {
            var likeIndex = upper.indexOf(" LIKE ");
            var fieldName = conditionStr.substring(0, likeIndex).trim();
            var value = conditionStr.substring(likeIndex + 6).trim();

            fieldName = removeBackticks(fieldName);
            value = removeQuotes(value);

            return new MySqlQueryCondition(fieldName, value, MySqlQueryCondition.MySqlOperator.LIKE);
        }

        // Handle IN
        if (upper.contains(" IN ")) {
            var inIndex = upper.indexOf(" IN ");
            var fieldName = conditionStr.substring(0, inIndex).trim();
            var valuesPart = conditionStr.substring(inIndex + 4).trim();

            fieldName = removeBackticks(fieldName);

            // Extract values from (val1, val2, val3)
            if (valuesPart.startsWith("(") && valuesPart.endsWith(")")) {
                var valuesStr = valuesPart.substring(1, valuesPart.length() - 1);
                var values = valuesStr.split(",");
                var parsedValues = new Object[values.length];

                for (var i = 0; i < values.length; i++) {
                    parsedValues[i] = parseValue(values[i].trim());
                }

                return new MySqlQueryCondition(fieldName, parsedValues, MySqlQueryCondition.MySqlOperator.IN);
            }
        }

        // Handle comparison operators
        MySqlQueryCondition.MySqlOperator operator = null;
        var operatorIndex = -1;

        // Check operators in order of length (longest first to avoid conflicts)
        if (upper.contains(" >= ")) {
            operator = MySqlQueryCondition.MySqlOperator.GREATER_THAN_OR_EQUALS;
            operatorIndex = upper.indexOf(" >= ");
        } else if (upper.contains(" <= ")) {
            operator = MySqlQueryCondition.MySqlOperator.LESS_THAN_OR_EQUALS;
            operatorIndex = upper.indexOf(" <= ");
        } else if (upper.contains(" <> ")) {
            operator = MySqlQueryCondition.MySqlOperator.NOT_EQUALS;
            operatorIndex = upper.indexOf(" <> ");
        } else if (upper.contains(" != ")) {
            operator = MySqlQueryCondition.MySqlOperator.NOT_EQUALS;
            operatorIndex = upper.indexOf(" != ");
        } else if (upper.contains(" > ")) {
            operator = MySqlQueryCondition.MySqlOperator.GREATER_THAN;
            operatorIndex = upper.indexOf(" > ");
        } else if (upper.contains(" < ")) {
            operator = MySqlQueryCondition.MySqlOperator.LESS_THAN;
            operatorIndex = upper.indexOf(" < ");
        } else if (upper.contains(" = ")) {
            operator = MySqlQueryCondition.MySqlOperator.EQUALS;
            operatorIndex = upper.indexOf(" = ");
        }

        if (operator != null && operatorIndex != -1) {
            var fieldName = conditionStr.substring(0, operatorIndex).trim();
            var value = conditionStr.substring(operatorIndex + operator.getSymbol().length() + 2).trim();

            fieldName = removeBackticks(fieldName);
            var parsedValue = parseValue(value);

            return new MySqlQueryCondition(fieldName, parsedValue, operator);
        }

        return null;
    }

    /**
     * Remove backticks from a field name.
     * 
     * @param fieldName the field name
     * @return the field name without backticks
     */
    private String removeBackticks(String fieldName) {
        if (fieldName.startsWith("`") && fieldName.endsWith("`")) {
            return fieldName.substring(1, fieldName.length() - 1);
        }
        return fieldName;
    }

    /**
     * Remove quotes from a value string.
     * 
     * @param value the value string
     * @return the value without quotes
     */
    private String removeQuotes(String value) {
        if ((value.startsWith("'") && value.endsWith("'")) ||
                (value.startsWith("\"") && value.endsWith("\""))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    /**
     * Parse a value string into its appropriate type.
     * 
     * @param valueStr the value string
     * @return the parsed value
     */
    private Object parseValue(String valueStr) {
        valueStr = valueStr.trim();

        // Remove quotes
        valueStr = removeQuotes(valueStr);

        // Try to parse as integer
        try {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            // Not an integer
        }

        // Try to parse as double
        try {
            return Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            // Not a double
        }

        // Check for boolean
        if (valueStr.equalsIgnoreCase("true"))
            return true;
        if (valueStr.equalsIgnoreCase("false"))
            return false;

        // Check for NULL
        if (valueStr.equalsIgnoreCase("null"))
            return null;

        // Return as string
        return valueStr;
    }
}
