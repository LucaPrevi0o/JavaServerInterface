package jsi.connection.database.mysql;

import jsi.connection.database.QueryCondition;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * MySQL-specific implementation of QueryCondition.
 * Provides functionality to parse SQL WHERE clauses into QueryCondition objects.
 */
public class MySqlQueryCondition extends QueryCondition {
    
    public static final MySqlQueryCondition EMPTY = new MySqlQueryCondition();
    
    /**
     * Creates an empty MySqlQueryCondition.
     */
    public MySqlQueryCondition() {
        super();
    }
    
    /**
     * Creates a simple MySqlQueryCondition.
     */
    public MySqlQueryCondition(String fieldName, ComparisonOperator operator, Object value) {
        super(fieldName, operator, value);
    }
    
    /**
     * Creates a complex MySqlQueryCondition.
     */
    public MySqlQueryCondition(LogicalOperator operator, QueryCondition... conditions) {
        super(operator, conditions);
    }
    
    // ===== Implementation of abstract factory methods =====
    
    @Override
    protected QueryCondition createEquals(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.EQUALS, value);
    }
    
    @Override
    protected QueryCondition createNotEquals(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.NOT_EQUALS, value);
    }
    
    @Override
    protected QueryCondition createGreaterThan(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.GREATER_THAN, value);
    }
    
    @Override
    protected QueryCondition createLessThan(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.LESS_THAN, value);
    }
    
    @Override
    protected QueryCondition createGreaterThanOrEqual(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.GREATER_THAN_OR_EQUAL, value);
    }
    
    @Override
    protected QueryCondition createLessThanOrEqual(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.LESS_THAN_OR_EQUAL, value);
    }
    
    @Override
    protected QueryCondition createLike(String fieldName, String pattern) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.LIKE, pattern);
    }
    
    @Override
    protected QueryCondition createIn(String fieldName, Object... values) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.IN, values);
    }
    
    @Override
    protected QueryCondition createIsNull(String fieldName) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.IS_NULL, null);
    }
    
    @Override
    protected QueryCondition createIsNotNull(String fieldName) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.IS_NOT_NULL, null);
    }
    
    @Override
    protected QueryCondition createAnd(QueryCondition... conditions) {
        return new MySqlQueryCondition(LogicalOperator.AND, conditions);
    }
    
    @Override
    protected QueryCondition createOr(QueryCondition... conditions) {
        return new MySqlQueryCondition(LogicalOperator.OR, conditions);
    }
    
    @Override
    protected QueryCondition createNot(QueryCondition condition) {
        return new MySqlQueryCondition(LogicalOperator.NOT, condition);
    }
    
    // ===== Static factory methods for convenient usage =====
    
    public static MySqlQueryCondition equals(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.EQUALS, value);
    }
    
    public static MySqlQueryCondition notEquals(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.NOT_EQUALS, value);
    }
    
    public static MySqlQueryCondition greaterThan(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.GREATER_THAN, value);
    }
    
    public static MySqlQueryCondition lessThan(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.LESS_THAN, value);
    }
    
    public static MySqlQueryCondition greaterThanOrEqual(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.GREATER_THAN_OR_EQUAL, value);
    }
    
    public static MySqlQueryCondition lessThanOrEqual(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.LESS_THAN_OR_EQUAL, value);
    }
    
    public static MySqlQueryCondition like(String fieldName, String pattern) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.LIKE, pattern);
    }
    
    public static MySqlQueryCondition in(String fieldName, Object... values) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.IN, values);
    }
    
    public static MySqlQueryCondition isNull(String fieldName) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.IS_NULL, null);
    }
    
    public static MySqlQueryCondition isNotNull(String fieldName) {
        return new MySqlQueryCondition(fieldName, ComparisonOperator.IS_NOT_NULL, null);
    }
    
    public static MySqlQueryCondition and(QueryCondition... conditions) {
        return new MySqlQueryCondition(LogicalOperator.AND, conditions);
    }
    
    public static MySqlQueryCondition or(QueryCondition... conditions) {
        return new MySqlQueryCondition(LogicalOperator.OR, conditions);
    }
    
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
        
        if (whereClause == null || whereClause.trim().isEmpty()) {
            return EMPTY;
        }
        
        whereClause = whereClause.trim();
        
        // Remove outer parentheses if they wrap the entire expression
        whereClause = removeOuterParentheses(whereClause);
        
        // Try to split by OR (lowest precedence)
        List<String> orParts = splitByOperator(whereClause, "OR");
        if (orParts.size() > 1) {
            QueryCondition[] conditions = new QueryCondition[orParts.size()];
            for (int i = 0; i < orParts.size(); i++) {
                conditions[i] = parse(orParts.get(i));
            }
            return or(conditions);
        }
        
        // Try to split by AND (higher precedence than OR)
        List<String> andParts = splitByOperator(whereClause, "AND");
        if (andParts.size() > 1) {
            QueryCondition[] conditions = new QueryCondition[andParts.size()];
            for (int i = 0; i < andParts.size(); i++) {
                conditions[i] = parse(andParts.get(i));
            }
            return and(conditions);
        }
        
        // Parse as simple condition
        return parseSimpleCondition(whereClause);
    }
    
    /**
     * Remove outer parentheses from a string if they wrap the entire expression.
     */
    private static String removeOuterParentheses(String str) {
        
        str = str.trim();
        
        if (!str.startsWith("(") || !str.endsWith(")")) {
            return str;
        }
        
        int depth = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '(') depth++;
            else if (str.charAt(i) == ')') depth--;
            
            if (depth == 0 && i < str.length() - 1) {
                return str;
            }
        }
        
        return removeOuterParentheses(str.substring(1, str.length() - 1));
    }
    
    /**
     * Split a string by a logical operator (AND/OR) while respecting parentheses.
     */
    private static List<String> splitByOperator(String str, String operator) {
        
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int lastSplit = 0;
        
        Pattern pattern = Pattern.compile("\\b" + operator + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        
        while (matcher.find()) {
            depth = 0;
            for (int i = lastSplit; i < matcher.start(); i++) {
                if (str.charAt(i) == '(') depth++;
                else if (str.charAt(i) == ')') depth--;
            }
            
            if (depth == 0) {
                parts.add(str.substring(lastSplit, matcher.start()).trim());
                lastSplit = matcher.end();
            }
        }
        
        if (lastSplit < str.length()) {
            parts.add(str.substring(lastSplit).trim());
        }
        
        if (parts.isEmpty()) {
            parts.add(str);
        }
        
        return parts;
    }
    
    /**
     * Parse a simple condition (e.g., "age > 18", "name = 'John'").
     */
    private static MySqlQueryCondition parseSimpleCondition(String condition) {
        
        condition = condition.trim();
        
        // Check for IS NOT NULL
        if (Pattern.compile("\\bIS\\s+NOT\\s+NULL\\b", Pattern.CASE_INSENSITIVE).matcher(condition).find()) {
            String field = condition.replaceAll("(?i)\\s+IS\\s+NOT\\s+NULL.*", "").trim();
            return isNotNull(field);
        }
        
        // Check for IS NULL
        if (Pattern.compile("\\bIS\\s+NULL\\b", Pattern.CASE_INSENSITIVE).matcher(condition).find()) {
            String field = condition.replaceAll("(?i)\\s+IS\\s+NULL.*", "").trim();
            return isNull(field);
        }
        
        // Check for LIKE
        Pattern likePattern = Pattern.compile("(.+?)\\s+LIKE\\s+(.+)", Pattern.CASE_INSENSITIVE);
        Matcher likeMatcher = likePattern.matcher(condition);
        if (likeMatcher.matches()) {
            String field = likeMatcher.group(1).trim();
            String value = cleanValue(likeMatcher.group(2).trim());
            return like(field, value);
        }
        
        // Check for IN
        Pattern inPattern = Pattern.compile("(.+?)\\s+IN\\s+\\((.+?)\\)", Pattern.CASE_INSENSITIVE);
        Matcher inMatcher = inPattern.matcher(condition);
        if (inMatcher.matches()) {
            String field = inMatcher.group(1).trim();
            String valuesList = inMatcher.group(2).trim();
            String[] values = valuesList.split(",");
            Object[] parsedValues = new Object[values.length];
            for (int i = 0; i < values.length; i++) {
                parsedValues[i] = parseValue(values[i].trim());
            }
            return in(field, parsedValues);
        }
        
        // Check for comparison operators
        String[] operators = {">=", "<=", "!=", "<>", "=", ">", "<"};
        for (String op : operators) {
            int index = condition.indexOf(op);
            if (index > 0) {
                String field = condition.substring(0, index).trim();
                String valueStr = condition.substring(index + op.length()).trim();
                Object value = parseValue(valueStr);
                
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
            if (valueStr.contains(".")) {
                return Double.parseDouble(valueStr);
            } else {
                return Integer.parseInt(valueStr);
            }
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
