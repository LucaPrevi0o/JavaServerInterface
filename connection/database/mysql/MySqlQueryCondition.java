package jsi.connection.database.mysql;

import jsi.connection.database.QueryCondition;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * MySQL-specific implementation of QueryCondition.
 * Provides functionality to parse SQL WHERE clauses into QueryCondition objects.
 */
public class MySqlQueryCondition extends QueryCondition {
    
    /**
     * Enum representing comparison operators for simple conditions.
     */
    public enum ComparisonOperator {

        EQUALS("="),
        NOT_EQUALS("!="),
        GREATER_THAN(">"),
        LESS_THAN("<"),
        GREATER_THAN_OR_EQUAL(">="),
        LESS_THAN_OR_EQUAL("<="),
        LIKE("LIKE"),
        IN("IN"),
        IS_NULL("IS NULL"),
        IS_NOT_NULL("IS NOT NULL");
        
        private final String sqlOperator;
        
        /**
         * Constructor for ComparisonOperator.
         * @param sqlOperator the SQL representation of the operator
         */
        ComparisonOperator(String sqlOperator) { this.sqlOperator = sqlOperator; }
        
        /**
         * Get the SQL representation of the operator.
         * @return the SQL operator as a string
         */
        public String getSqlOperator() { return sqlOperator; }
    }
    
    protected ComparisonOperator comparisonOperator;
    public static final MySqlQueryCondition EMPTY = new MySqlQueryCondition();

    /**
     * Get the comparison operator for simple conditions.
     * @return the ComparisonOperator
     */
    public ComparisonOperator getComparisonOperator() { return comparisonOperator; }
    
    /**
     * Creates an empty MySqlQueryCondition.
     */
    public MySqlQueryCondition() { super(); }
    
    /**
     * Creates a simple MySqlQueryCondition.
     * @param fieldName the field name
     * @param operator the comparison operator
     * @param value the value to compare against
     */
    public MySqlQueryCondition(String fieldName, ComparisonOperator operator, Object value) {

        super(fieldName, value);
        this.comparisonOperator = operator;
    }
    
    /**
     * Creates a complex MySqlQueryCondition.
     * @param operator the logical operator
     * @param conditions the sub-conditions to combine
     */
    public MySqlQueryCondition(LogicalOperator operator, QueryCondition... conditions) { super(operator, conditions); }
    
    // ===== Implementation of abstract factory methods =====
    
    /**
     * Creates an EQUALS condition.
     * @param fieldName the field name
     * @param value the value to compare against
     * @return a MySqlQueryCondition representing the EQUALS condition
     */
    @Override
    protected MySqlQueryCondition createEquals(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.EQUALS, value);
    }
    
    /**
     * Creates a NOT_EQUALS condition.
     * @param fieldName the field name
     * @param value the value to compare against
     * @return a MySqlQueryCondition representing the NOT_EQUALS condition
     */
    @Override
    protected MySqlQueryCondition createNotEquals(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.NOT_EQUALS, value);
    }
    
    /**
     * Creates a GREATER_THAN condition.
     * @param fieldName the field name
     * @param value the value to compare against
     * @return a MySqlQueryCondition representing the GREATER_THAN condition
     */
    @Override
    protected MySqlQueryCondition createGreaterThan(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.GREATER_THAN, value);
    }
    
    /**
     * Creates a LESS_THAN condition.
     * @param fieldName the field name
     * @param value the value to compare against
     * @return a MySqlQueryCondition representing the LESS_THAN condition
     */
    @Override
    protected MySqlQueryCondition createLessThan(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.LESS_THAN, value);
    }
    
    /**
     * Creates a GREATER_THAN_OR_EQUAL condition.
     * @param fieldName the field name
     * @param value the value to compare against
     * @return a MySqlQueryCondition representing the GREATER_THAN_OR_EQUAL condition
     */
    @Override
    protected MySqlQueryCondition createGreaterThanOrEqual(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.GREATER_THAN_OR_EQUAL, value);
    }
    
    /**
     * Creates a LESS_THAN_OR_EQUAL condition.
     * @param fieldName the field name
     * @param value the value to compare against
     * @return a MySqlQueryCondition representing the LESS_THAN_OR_EQUAL condition
     */
    @Override
    protected MySqlQueryCondition createLessThanOrEqual(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.LESS_THAN_OR_EQUAL, value);
    }
    
    /**
     * Creates a LIKE condition.
     * @param fieldName the field name
     * @param pattern the pattern to match
     * @return a MySqlQueryCondition representing the LIKE condition
     */
    @Override
    protected MySqlQueryCondition createLike(String fieldName, String pattern) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.LIKE, pattern);
    }
    
    /**
     * Creates an IN condition.
     * @param fieldName the field name
     * @param values the values to check for inclusion
     * @return a MySqlQueryCondition representing the IN condition
     */
    @Override
    protected MySqlQueryCondition createIn(String fieldName, Object... values) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.IN, values);
    }
    
    /**
     * Creates an IS_NULL condition.
     * @param fieldName the field name
     * @return a MySqlQueryCondition representing the IS_NULL condition
     */
    @Override
    protected MySqlQueryCondition createIsNull(String fieldName) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.IS_NULL, null);
    }
    
    /**
     * Creates an IS_NOT_NULL condition.
     * @param fieldName the field name
     * @return a MySqlQueryCondition representing the IS_NOT_NULL condition
     */
    @Override
    protected MySqlQueryCondition createIsNotNull(String fieldName) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.IS_NOT_NULL, null);
    }
    
    /**
     * Creates an AND condition.
     * @param conditions the conditions to combine with AND
     * @return a MySqlQueryCondition representing the AND condition
     */
    @Override
    protected MySqlQueryCondition createAnd(QueryCondition... conditions) {
        return new MySqlQueryCondition(LogicalOperator.AND, conditions);
    }
    
    /**
     * Creates an OR condition.
     * @param conditions the conditions to combine with OR
     * @return a MySqlQueryCondition representing the OR condition
     */
    @Override
    protected MySqlQueryCondition createOr(QueryCondition... conditions) {
        return new MySqlQueryCondition(LogicalOperator.OR, conditions);
    }
    
    /**
     * Creates a NOT condition.
     * @param condition the condition to negate
     * @return a MySqlQueryCondition representing the NOT condition
     */
    @Override
    protected MySqlQueryCondition createNot(QueryCondition condition) {
        return new MySqlQueryCondition(LogicalOperator.NOT, condition);
    }
    
    // ===== Static factory methods for convenient usage =====
    
    /**
     * Creates an EQUALS condition.
     * @param fieldName the field name
     * @param value the value to compare against
     * @return a MySqlQueryCondition representing the EQUALS condition
     */
    public static MySqlQueryCondition equals(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.EQUALS, value);
    }
    
    /**
     * Creates a NOT_EQUALS condition.
     * @param fieldName the field name
     * @param value the value to compare against
     * @return a MySqlQueryCondition representing the NOT_EQUALS condition
     */
    public static MySqlQueryCondition notEquals(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.NOT_EQUALS, value);
    }
    
    /**
     * Creates a GREATER_THAN condition.
     * @param fieldName the field name
     * @param value the value to compare against
     * @return a MySqlQueryCondition representing the GREATER_THAN condition
     */
    public static MySqlQueryCondition greaterThan(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.GREATER_THAN, value);
    }
    
    /**
     * Creates a LESS_THAN condition.
     * @param fieldName the field name
     * @param value the value to compare against
     * @return a MySqlQueryCondition representing the LESS_THAN condition
     */
    public static MySqlQueryCondition lessThan(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.LESS_THAN, value);
    }
    
    /**
     * Creates a GREATER_THAN_OR_EQUAL condition.
     * @param fieldName the field name
     * @param value the value to compare against
     * @return a MySqlQueryCondition representing the GREATER_THAN_OR_EQUAL condition
     */
    public static MySqlQueryCondition greaterThanOrEqual(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.GREATER_THAN_OR_EQUAL, value);
    }
    
    /**
     * Creates a LESS_THAN_OR_EQUAL condition.
     * @param fieldName the field name
     * @param value the value to compare against
     * @return a MySqlQueryCondition representing the LESS_THAN_OR_EQUAL condition
     */
    public static MySqlQueryCondition lessThanOrEqual(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.LESS_THAN_OR_EQUAL, value);
    }
    
    /**
     * Creates a LIKE condition.
     * @param fieldName the field name
     * @param pattern the pattern to match
     * @return a MySqlQueryCondition representing the LIKE condition
     */
    public static MySqlQueryCondition like(String fieldName, String pattern) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.LIKE, pattern);
    }
    
    /**
     * Creates an IN condition.
     * @param fieldName the field name
     * @param values the values to check for inclusion
     * @return a MySqlQueryCondition representing the IN condition
     */
    public static MySqlQueryCondition in(String fieldName, Object... values) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.IN, values);
    }
    
    /**
     * Creates an IS_NULL condition.
     * @param fieldName the field name
     * @return a MySqlQueryCondition representing the IS_NULL condition
     */
    public static MySqlQueryCondition isNull(String fieldName) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.IS_NULL, null);
    }
    
    /**
     * Creates an IS_NOT_NULL condition.
     * @param fieldName the field name
     * @return a MySqlQueryCondition representing the IS_NOT_NULL condition
     */
    public static MySqlQueryCondition isNotNull(String fieldName) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.IS_NOT_NULL, null);
    }

    /**
     * Creates an AND condition.
     * @param conditions the conditions to combine with AND
     * @return a MySqlQueryCondition representing the AND condition
     */
    public static MySqlQueryCondition and(QueryCondition... conditions) {
        return new MySqlQueryCondition(LogicalOperator.AND, conditions);
    }
    
    /**
     * Creates an OR condition.
     * @param conditions the conditions to combine with OR
     * @return a MySqlQueryCondition representing the OR condition
     */
    public static MySqlQueryCondition or(QueryCondition... conditions) {
        return new MySqlQueryCondition(LogicalOperator.OR, conditions);
    }
    
    /**
     * Creates a NOT condition.
     * @param condition the condition to negate
     * @return a MySqlQueryCondition representing the NOT condition
     */
    public static MySqlQueryCondition not(QueryCondition condition) {
        return new MySqlQueryCondition(LogicalOperator.NOT, condition);
    }
    
    // ===== MySQL-specific parsing logic =====
    
    /**
     * Parse a SQL WHERE clause string into a MySqlQueryCondition object.
     * Supports basic conditions, AND/OR operators, and parentheses.
     * 
     * @param whereClause the WHERE clause string (without the "WHERE" keyword)
     * @return a MySqlQueryCondition object representing the parsed condition
     */
    public static MySqlQueryCondition parse(String whereClause) {
        
        if (whereClause == null || whereClause.trim().isEmpty()) return EMPTY;
        
        whereClause = whereClause.trim();
        
        // Remove outer parentheses if they wrap the entire expression
        whereClause = removeOuterParentheses(whereClause);
        
        // Try to split by OR (lowest precedence)
        var orParts = splitByOperator(whereClause, "OR");
        if (orParts.size() > 1) {

            var conditions = new QueryCondition[orParts.size()];
            for (var i = 0; i < orParts.size(); i++) conditions[i] = parse(orParts.get(i));
            return or(conditions);
        }
        
        // Try to split by AND (higher precedence than OR)
        var andParts = splitByOperator(whereClause, "AND");
        if (andParts.size() > 1) {

            var conditions = new QueryCondition[andParts.size()];
            for (var i = 0; i < andParts.size(); i++) conditions[i] = parse(andParts.get(i));
            return and(conditions);
        }
        
        // Parse as simple condition
        return parseSimpleCondition(whereClause);
    }
    
    /**
     * Remove outer parentheses from a string if they wrap the entire expression.
     * @param str the input string
     * @return the string without outer parentheses
     */
    private static String removeOuterParentheses(String str) {
        
        str = str.trim();
        if (!str.startsWith("(") || !str.endsWith(")")) return str;
        
        var depth = 0;
        for (var i = 0; i < str.length(); i++) {

            if (str.charAt(i) == '(') depth++;
            else if (str.charAt(i) == ')') depth--;
            if (depth == 0 && i < str.length() - 1) return str;
        }
        
        return removeOuterParentheses(str.substring(1, str.length() - 1));
    }
    
    /**
     * Split a string by a logical operator (AND/OR) while respecting parentheses.
     * @param str the input string
     * @param operator the logical operator to split by ("AND" or "OR")
     * @return list of split parts
     */
    private static List<String> splitByOperator(String str, String operator) {
        
        var parts = new ArrayList<String>();
        var depth = 0;
        var lastSplit = 0;
        
        var pattern = Pattern.compile("\\b" + operator + "\\b", Pattern.CASE_INSENSITIVE);
        var matcher = pattern.matcher(str);
        
        while (matcher.find()) {

            depth = 0;
            for (var i = lastSplit; i < matcher.start(); i++) {

                if (str.charAt(i) == '(') depth++;
                else if (str.charAt(i) == ')') depth--;
            }
            
            if (depth == 0) {

                parts.add(str.substring(lastSplit, matcher.start()).trim());
                lastSplit = matcher.end();
            }
        }
        
        if (lastSplit < str.length()) parts.add(str.substring(lastSplit).trim());
        if (parts.isEmpty()) parts.add(str);
        return parts;
    }
    
    /**
     * Parse a simple condition (e.g., "age > 18", "name = 'John'").
     * @param condition the simple condition string
     * @return a MySqlQueryCondition representing the simple condition
     */
    private static MySqlQueryCondition parseSimpleCondition(String condition) {
        
        condition = condition.trim();
        
        // Check for IS NOT NULL
        if (Pattern.compile("\\bIS\\s+NOT\\s+NULL\\b", Pattern.CASE_INSENSITIVE).matcher(condition).find()) {

            var field = condition.replaceAll("(?i)\\s+IS\\s+NOT\\s+NULL.*", "").trim();
            return isNotNull(field);
        }
        
        // Check for IS NULL
        if (Pattern.compile("\\bIS\\s+NULL\\b", Pattern.CASE_INSENSITIVE).matcher(condition).find()) {

            var field = condition.replaceAll("(?i)\\s+IS\\s+NULL.*", "").trim();
            return isNull(field);
        }
        
        // Check for LIKE
        var likePattern = Pattern.compile("(.+?)\\s+LIKE\\s+(.+)", Pattern.CASE_INSENSITIVE);
        var likeMatcher = likePattern.matcher(condition);
        if (likeMatcher.matches()) {

            var field = likeMatcher.group(1).trim();
            var value = cleanValue(likeMatcher.group(2).trim());
            return like(field, value);
        }
        
        // Check for IN
        var inPattern = Pattern.compile("(.+?)\\s+IN\\s+\\((.+?)\\)", Pattern.CASE_INSENSITIVE);
        var inMatcher = inPattern.matcher(condition);
        if (inMatcher.matches()) {

            var field = inMatcher.group(1).trim();
            var valuesList = inMatcher.group(2).trim();
            var values = valuesList.split(",");
            var parsedValues = new Object[values.length];
            for (var i = 0; i < values.length; i++) parsedValues[i] = parseValue(values[i].trim());
            return in(field, parsedValues);
        }
        
        // Check for comparison operators
        var operators = new String[]{">=", "<=", "!=", "<>", "=", ">", "<"};
        for (var op : operators) {

            var index = condition.indexOf(op);
            if (index > 0) {

                var field = condition.substring(0, index).trim();
                var valueStr = condition.substring(index + op.length()).trim();
                var value = parseValue(valueStr);
                
                switch (op) {

                    case ">=": return greaterThanOrEqual(field, value);
                    case "<=": return lessThanOrEqual(field, value);
                    case "!=":
                    case "<>": return notEquals(field, value);
                    case "=": return equals(field, value);
                    case ">": return greaterThan(field, value);
                    case "<": return lessThan(field, value);
                }
            }
        }
        
        return equals(condition, null);
    }
    
    /**
     * Parse a value from a SQL string.
     */
    private static Object parseValue(String valueStr) {
        
        valueStr = valueStr.trim();
        
        if ((valueStr.startsWith("'") && valueStr.endsWith("'")) ||
            (valueStr.startsWith("\"") && valueStr.endsWith("\""))) {
            return valueStr.substring(1, valueStr.length() - 1);
        }
        
        try {
            if (valueStr.contains(".")) return Double.parseDouble(valueStr);
            else return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            // Not a number
        }
        
        if (valueStr.equalsIgnoreCase("true") || valueStr.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(valueStr);
        }
        
        return valueStr;
    }
    
    /**
     * Clean a value string by removing quotes.
     */
    private static String cleanValue(String value) {

        value = value.trim();
        if ((value.startsWith("'") && value.endsWith("'")) ||
            (value.startsWith("\"") && value.endsWith("\""))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
