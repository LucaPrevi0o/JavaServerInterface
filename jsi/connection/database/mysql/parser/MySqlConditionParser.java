package jsi.connection.database.mysql.parser;

import jsi.connection.database.mysql.query.MySqlQueryCondition;
import jsi.connection.database.query.QueryCondition;

/**
 * Utility class for parsing WHERE conditions from MySQL queries.
 * Handles logical operators (AND, OR, NOT), comparison operators, and special operators (LIKE, IN, IS NULL).
 */
public final class MySqlConditionParser {

    private MySqlConditionParser() {}

    /**
     * Extract and parse the WHERE condition from a MySQL query.
     * 
     * @param query the normalized query string
     * @return the parsed MySqlQueryCondition, or null if no WHERE clause
     */
    public static MySqlQueryCondition extractCondition(String query) {

        if (query == null) return null;

        var normalizedQuery = query.trim().replaceAll("\\s+", " ");
        var upper = normalizedQuery.toUpperCase();

        // Find WHERE clause
        var whereIndex = upper.indexOf(" WHERE ");
        if (whereIndex == -1) return null;

        // Extract WHERE condition part
        var afterWhere = normalizedQuery.substring(whereIndex + 7).trim();

        // Remove trailing clauses (ORDER BY, GROUP BY, LIMIT, etc.)
        var endIndex = findEndOfWhereClause(afterWhere);
        var conditionStr = afterWhere.substring(0, endIndex).trim();
        return parseCondition(conditionStr);
    }

    /**
     * Find the end of WHERE clause by looking for subsequent clauses.
     * @param afterWhere the string after WHERE
     * @return the end index of the WHERE clause
     */
    private static int findEndOfWhereClause(String afterWhere) {

        var upper = afterWhere.toUpperCase();
        var endIndex = afterWhere.length();

        var orderByIndex = upper.indexOf(" ORDER BY");
        var groupByIndex = upper.indexOf(" GROUP BY");
        var limitIndex = upper.indexOf(" LIMIT");
        var havingIndex = upper.indexOf(" HAVING");

        if (orderByIndex != -1) endIndex = Math.min(endIndex, orderByIndex);
        if (groupByIndex != -1) endIndex = Math.min(endIndex, groupByIndex);
        if (limitIndex != -1) endIndex = Math.min(endIndex, limitIndex);
        if (havingIndex != -1) endIndex = Math.min(endIndex, havingIndex);

        return endIndex;
    }

    /**
     * Parse a condition string into a MySqlQueryCondition object.
     * Uses recursive descent parsing for handling nested conditions.
     * 
     * @param conditionStr the condition string to parse
     * @return the parsed MySqlQueryCondition
     */
    private static MySqlQueryCondition parseCondition(String conditionStr) {

        if (conditionStr == null || conditionStr.isEmpty()) return null;

        // Handle logical operators (AND, OR) - find top-level operators
        var andIndex = findTopLevelOperator(conditionStr, " AND ");
        var orIndex = findTopLevelOperator(conditionStr, " OR ");

        // Process AND (higher precedence in SQL)
        if (andIndex != -1) {

            var left = conditionStr.substring(0, andIndex).trim();
            var right = conditionStr.substring(andIndex + 5).trim();

            var leftCondition = parseCondition(left);
            var rightCondition = parseCondition(right);

            if (leftCondition == null || rightCondition == null) return null;
            return new MySqlQueryCondition(QueryCondition.LogicalOperator.AND, leftCondition, rightCondition);
        }

        // Process OR
        if (orIndex != -1) {

            var left = conditionStr.substring(0, orIndex).trim();
            var right = conditionStr.substring(orIndex + 4).trim();

            var leftCondition = parseCondition(left);
            var rightCondition = parseCondition(right);
            if (leftCondition == null || rightCondition == null) return null;
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
            if (innerCondition == null) return null;
            return new MySqlQueryCondition(QueryCondition.LogicalOperator.NOT, innerCondition);
        }

        // Parse simple condition
        return parseSimpleCondition(conditionStr);
    }

    /**
     * Find a logical operator at the top level (not inside parentheses).
     * 
     * @param str the string to search in
     * @param operator the operator to find
     * @return the index of the operator, or -1 if not found
     */
    private static int findTopLevelOperator(String str, String operator) {

        var parenthesesLevel = 0;
        var upper = str.toUpperCase();

        for (var i = 0; i <= str.length() - operator.length(); i++) {

            var c = str.charAt(i);
            
            if (c == '(') parenthesesLevel++;
            else if (c == ')') parenthesesLevel--;

            if (parenthesesLevel == 0 && upper.substring(i).startsWith(operator)) return i;
        }

        return -1;
    }

    /**
     * Parse a simple condition (field operator value).
     * 
     * @param conditionStr the condition string to parse
     * @return the parsed MySqlQueryCondition
     */
    private static MySqlQueryCondition parseSimpleCondition(String conditionStr) {

        var upper = conditionStr.toUpperCase();

        // Handle IS NULL
        if (upper.contains(" IS NULL")) {

            var fieldName = conditionStr.substring(0, upper.indexOf(" IS NULL")).trim();
            fieldName = MySqlValueParser.removeBackticks(fieldName);
            return new MySqlQueryCondition(fieldName, null, MySqlQueryCondition.MySqlOperator.IS_NULL);
        }

        // Handle IS NOT NULL
        if (upper.contains(" IS NOT NULL")) {

            var fieldName = conditionStr.substring(0, upper.indexOf(" IS NOT NULL")).trim();
            fieldName = MySqlValueParser.removeBackticks(fieldName);
            return new MySqlQueryCondition(fieldName, null, MySqlQueryCondition.MySqlOperator.IS_NOT_NULL);
        }

        // Handle LIKE
        if (upper.contains(" LIKE ")) {

            var likeIndex = upper.indexOf(" LIKE ");
            var fieldName = conditionStr.substring(0, likeIndex).trim();
            var value = conditionStr.substring(likeIndex + 6).trim();

            fieldName = MySqlValueParser.removeBackticks(fieldName);
            value = MySqlValueParser.removeQuotes(value);

            return new MySqlQueryCondition(fieldName, value, MySqlQueryCondition.MySqlOperator.LIKE);
        }

        // Handle IN
        if (upper.contains(" IN ")) {

            var inIndex = upper.indexOf(" IN ");
            var fieldName = conditionStr.substring(0, inIndex).trim();
            var valuesPart = conditionStr.substring(inIndex + 4).trim();

            fieldName = MySqlValueParser.removeBackticks(fieldName);

            // Extract values from (val1, val2, val3)
            if (valuesPart.startsWith("(") && valuesPart.endsWith(")")) {

                var valuesStr = valuesPart.substring(1, valuesPart.length() - 1);
                var parsedValues = MySqlValueParser.parseValues(valuesStr);
                return new MySqlQueryCondition(fieldName, parsedValues, MySqlQueryCondition.MySqlOperator.IN);
            }
        }

        // Handle comparison operators (check longest first to avoid conflicts)
        MySqlQueryCondition.MySqlOperator operator = null;
        var operatorIndex = -1;

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

            fieldName = MySqlValueParser.removeBackticks(fieldName);
            var parsedValue = MySqlValueParser.parseValue(value);
            return new MySqlQueryCondition(fieldName, parsedValue, operator);
        }

        return null;
    }
}
