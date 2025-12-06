package jsi.connection.database;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class representing a query condition that can be used to filter database records.
 * Supports simple conditions (field comparisons) and complex conditions (logical combinations).
 * Subclasses must implement the factory methods to create database-specific conditions.
 */
public abstract class QueryCondition {
    
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
        
        ComparisonOperator(String sqlOperator) { this.sqlOperator = sqlOperator; }
        
        public String getSqlOperator() { return sqlOperator; }
    }
    
    /**
     * Enum representing logical operators for combining conditions.
     */
    public enum LogicalOperator {
        AND,
        OR,
        NOT
    }
    
    // Fields for simple conditions
    protected String fieldName;
    protected ComparisonOperator comparisonOperator;
    protected Object value;
    
    // Fields for complex conditions
    protected LogicalOperator logicalOperator;
    protected List<QueryCondition> subConditions;
    
    /**
     * Creates an empty QueryCondition.
     */
    protected QueryCondition() { this.subConditions = new ArrayList<>(); }
    
    /**
     * Creates a simple condition with a field, operator, and value.
     * 
     * @param fieldName the name of the field to compare
     * @param operator the comparison operator
     * @param value the value to compare against
     */
    protected QueryCondition(String fieldName, ComparisonOperator operator, Object value) {

        this.fieldName = fieldName;
        this.comparisonOperator = operator;
        this.value = value;
        this.subConditions = new ArrayList<>();
    }
    
    /**
     * Creates a complex condition by combining multiple conditions with a logical operator.
     * 
     * @param operator the logical operator (AND, OR, NOT)
     * @param conditions the sub-conditions to combine
     */
    protected QueryCondition(LogicalOperator operator, QueryCondition... conditions) {

        this.logicalOperator = operator;
        this.subConditions = new ArrayList<>();
        for (QueryCondition condition : conditions)
            if (condition != null) this.subConditions.add(condition);
    }
    
    /**
     * Checks if this is a simple condition (field comparison).
     * 
     * @return true if this is a simple condition, false if it's a complex condition
     */
    public boolean isSimpleCondition() { return fieldName != null && comparisonOperator != null; }
    
    /**
     * Checks if this is a complex condition (logical combination).
     * 
     * @return true if this is a complex condition, false if it's a simple condition
     */
    public boolean isComplexCondition() { return logicalOperator != null && !subConditions.isEmpty(); }
    
    // Getters for simple conditions
    public String getFieldName() { return fieldName; }
    
    public ComparisonOperator getComparisonOperator() { return comparisonOperator; }
    
    public Object getValue() { return value; }
    
    // Getters for complex conditions
    public LogicalOperator getLogicalOperator() { return logicalOperator; }
    
    public List<QueryCondition> getSubConditions() { return new ArrayList<>(subConditions); }
    
    // Setters for simple conditions
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    
    public void setComparisonOperator(ComparisonOperator comparisonOperator) { this.comparisonOperator = comparisonOperator; }
    
    public void setValue(Object value) { this.value = value; }
    
    // Setters for complex conditions
    public void setLogicalOperator(LogicalOperator logicalOperator) { this.logicalOperator = logicalOperator; }
    
    public void setSubConditions(List<QueryCondition> subConditions) {
        this.subConditions = subConditions != null ? new ArrayList<>(subConditions) : new ArrayList<>();
    }
    
    public void addSubCondition(QueryCondition condition) {
        if (condition != null) this.subConditions.add(condition);
    }
    
    // ===== Abstract factory methods to be implemented by subclasses =====
    
    /**
     * Creates a condition: field = value
     * Must be implemented by subclasses to return their specific QueryCondition type.
     */
    protected abstract QueryCondition createEquals(String fieldName, Object value);
    
    /**
     * Creates a condition: field != value
     * Must be implemented by subclasses to return their specific QueryCondition type.
     */
    protected abstract QueryCondition createNotEquals(String fieldName, Object value);
    
    /**
     * Creates a condition: field > value
     * Must be implemented by subclasses to return their specific QueryCondition type.
     */
    protected abstract QueryCondition createGreaterThan(String fieldName, Object value);
    
    /**
     * Creates a condition: field < value
     * Must be implemented by subclasses to return their specific QueryCondition type.
     */
    protected abstract QueryCondition createLessThan(String fieldName, Object value);
    
    /**
     * Creates a condition: field >= value
     * Must be implemented by subclasses to return their specific QueryCondition type.
     */
    protected abstract QueryCondition createGreaterThanOrEqual(String fieldName, Object value);
    
    /**
     * Creates a condition: field <= value
     * Must be implemented by subclasses to return their specific QueryCondition type.
     */
    protected abstract QueryCondition createLessThanOrEqual(String fieldName, Object value);
    
    /**
     * Creates a condition: field LIKE pattern
     * Must be implemented by subclasses to return their specific QueryCondition type.
     */
    protected abstract QueryCondition createLike(String fieldName, String pattern);
    
    /**
     * Creates a condition: field IN (values)
     * Must be implemented by subclasses to return their specific QueryCondition type.
     */
    protected abstract QueryCondition createIn(String fieldName, Object... values);
    
    /**
     * Creates a condition: field IS NULL
     * Must be implemented by subclasses to return their specific QueryCondition type.
     */
    protected abstract QueryCondition createIsNull(String fieldName);
    
    /**
     * Creates a condition: field IS NOT NULL
     * Must be implemented by subclasses to return their specific QueryCondition type.
     */
    protected abstract QueryCondition createIsNotNull(String fieldName);
    
    /**
     * Combines multiple conditions with AND.
     * Must be implemented by subclasses to return their specific QueryCondition type.
     */
    protected abstract QueryCondition createAnd(QueryCondition... conditions);
    
    /**
     * Combines multiple conditions with OR.
     * Must be implemented by subclasses to return their specific QueryCondition type.
     */
    protected abstract QueryCondition createOr(QueryCondition... conditions);
    
    /**
     * Negates a condition with NOT.
     * Must be implemented by subclasses to return their specific QueryCondition type.
     */
    protected abstract QueryCondition createNot(QueryCondition condition);
    
    // ===== Instance methods for chaining (concrete, rely on abstract factory methods) =====
    
    /**
     * Chains this condition with another using AND.
     */
    public QueryCondition and(QueryCondition other) {
        return createAnd(this, other);
    }
    
    /**
     * Chains this condition with another using OR.
     */
    public QueryCondition or(QueryCondition other) {
        return createOr(this, other);
    }
    
    /**
     * Returns a string representation of this condition (useful for debugging).
     */
    @Override
    public String toString() {

        if (isSimpleCondition())
            return String.format("%s %s %s", fieldName, comparisonOperator.getSqlOperator(), value);
        else if (isComplexCondition()) {

            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i = 0; i < subConditions.size(); i++) {

                if (i > 0) sb.append(" ").append(logicalOperator).append(" ");
                sb.append(subConditions.get(i).toString());
            }
            sb.append(")");
            return sb.toString();
        }
        return "EMPTY_CONDITION";
    }
}
