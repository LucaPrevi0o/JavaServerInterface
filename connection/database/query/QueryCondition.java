package com.lucaprevioo.jsi.connection.database.query;

import java.util.List;

/**
 * Abstract class representing a query condition that can be used to filter database records.
 * Supports simple conditions (field comparisons) and complex conditions (logical combinations).
 * Subclasses must implement the factory methods to create database-specific conditions.
 */
public abstract class QueryCondition {
    
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
    protected Object value;
    
    // Fields for complex conditions
    protected LogicalOperator logicalOperator;
    protected List<QueryCondition> subConditions;
    
    /**
     * Creates a simple condition with a field, operator, and value.
     * 
     * @param fieldName the name of the field to compare
     * @param operator the comparison operator
     * @param value the value to compare against
     */
    protected QueryCondition(String fieldName, Object value) {

        this.fieldName = fieldName;
        this.value = value;
    }
    
    /**
     * Creates a complex condition by combining multiple conditions with a logical operator.
     * 
     * @param operator the logical operator (AND, OR, NOT)
     * @param conditions the sub-conditions to combine
     */
    protected QueryCondition(LogicalOperator operator, QueryCondition... conditions) {

        this.logicalOperator = operator;
        this.subConditions = List.of(conditions);
    }
    
    /**
     * Checks if this is a simple condition (field comparison).
     * @return true if this is a simple condition, false if it's a complex condition
     */
    public boolean isSimpleCondition() { return fieldName != null; }
    
    /**
     * Checks if this is a complex condition (logical combination).
     * @return true if this is a complex condition, false if it's a simple condition
     */
    public boolean isComplexCondition() { return logicalOperator != null && !subConditions.isEmpty(); }
    
    /**
     * Get the field name for simple conditions.
     * @return the field name
     */
    public String getFieldName() { return fieldName; }
    
    /**
     * Get the value for simple conditions.
     * @return the value
     */
    public Object getValue() { return value; }
    
    /**
     * Get the logical operator for complex conditions.
     * @return the LogicalOperator
     */
    public LogicalOperator getLogicalOperator() { return logicalOperator; }
    
    /**
     * Get the sub-conditions for complex conditions.
     * @return the list of sub-conditions
     */
    public List<QueryCondition> getSubConditions() { return subConditions; }
    
    /**
     * Set the field name for simple conditions. 
     * @param fieldName the field name to set
     */
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    
    /**
     * Set the value for simple conditions.
     * @param value the value to set
     */
    public void setValue(Object value) { this.value = value; }
    
    /**
     * Set the logical operator for complex conditions.
     * @param logicalOperator the LogicalOperator to set
     */
    public void setLogicalOperator(LogicalOperator logicalOperator) { this.logicalOperator = logicalOperator; }
    /**
     * Set the sub-conditions for complex conditions.
     * @param subConditions the list of sub-conditions to set
     */
    public void setSubConditions(List<QueryCondition> subConditions) { this.subConditions = subConditions; }
    
    /**
     * Adds a sub-condition to the list of sub-conditions for complex conditions.
     * @param condition the QueryCondition to add
     */
    public void addSubCondition(QueryCondition condition) { if (condition != null) this.subConditions.add(condition); }
    
    // ===== Abstract factory methods to be implemented by subclasses =====
    
    /**
     * Creates a condition: field = value
     * Must be implemented by subclasses to return their specific QueryCondition type.
     * @param fieldName the name of the field
     * @param value the value to compare
     */
    protected abstract QueryCondition createEquals(String fieldName, Object value);
    
    /**
     * Creates a condition: field != value
     * Must be implemented by subclasses to return their specific QueryCondition type.
     * @param fieldName the name of the field
     * @param value the value to compare
     */
    protected abstract QueryCondition createNotEquals(String fieldName, Object value);
    
    /**
     * Creates a condition: field > value
     * Must be implemented by subclasses to return their specific QueryCondition type.
     * @param fieldName the name of the field
     * @param value the value to compare
     */
    protected abstract QueryCondition createGreaterThan(String fieldName, Object value);
    
    /**
     * Creates a condition: field < value
     * Must be implemented by subclasses to return their specific QueryCondition type.
     * @param fieldName the name of the field
     * @param value the value to compare
     */
    protected abstract QueryCondition createLessThan(String fieldName, Object value);
    
    /**
     * Creates a condition: field >= value
     * Must be implemented by subclasses to return their specific QueryCondition type.
     * @param fieldName the name of the field
     * @param value the value to compare
     */
    protected abstract QueryCondition createGreaterThanOrEqual(String fieldName, Object value);
    
    /**
     * Creates a condition: field <= value
     * Must be implemented by subclasses to return their specific QueryCondition type.
     * @param fieldName the name of the field
     * @param value the value to compare
     */
    protected abstract QueryCondition createLessThanOrEqual(String fieldName, Object value);
    
    /**
     * Creates a condition: field LIKE pattern
     * Must be implemented by subclasses to return their specific QueryCondition type.
     * @param fieldName the name of the field
     * @param pattern the pattern to match
     */
    protected abstract QueryCondition createLike(String fieldName, String pattern);
    
    /**
     * Creates a condition: field IN (values)
     * Must be implemented by subclasses to return their specific QueryCondition type.
     * @param fieldName the name of the field
     * @param values the values to include
     */
    protected abstract QueryCondition createIn(String fieldName, Object... values);
    
    /**
     * Creates a condition: field IS NULL
     * Must be implemented by subclasses to return their specific QueryCondition type.
     * @param fieldName the name of the field
     */
    protected abstract QueryCondition createIsNull(String fieldName);
    
    /**
     * Creates a condition: field IS NOT NULL
     * Must be implemented by subclasses to return their specific QueryCondition type.
     * @param fieldName the name of the field
     */
    protected abstract QueryCondition createIsNotNull(String fieldName);
    
    /**
     * Combines multiple conditions with AND.
     * Must be implemented by subclasses to return their specific QueryCondition type.
     * @param conditions the conditions to combine
     */
    protected abstract QueryCondition createAnd(QueryCondition... conditions);
    
    /**
     * Combines multiple conditions with OR.
     * Must be implemented by subclasses to return their specific QueryCondition type.
     * @param conditions the conditions to combine
     */
    protected abstract QueryCondition createOr(QueryCondition... conditions);
    
    /**
     * Negates a condition with NOT.
     * Must be implemented by subclasses to return their specific QueryCondition type.
     * @param condition the condition to negate
     */
    protected abstract QueryCondition createNot(QueryCondition condition);
    
    // ===== Instance methods for chaining (concrete, rely on abstract factory methods) =====
    
    /**
     * Chains this condition with another using AND.
     * @param other the other QueryCondition to combine with
     * @return the combined QueryCondition
     */
    public QueryCondition and(QueryCondition other) { return createAnd(this, other); }
    
    /**
     * Chains this condition with another using OR.
     * @param other the other QueryCondition to combine with
     * @return the combined QueryCondition
     */
    public QueryCondition or(QueryCondition other) { return createOr(this, other); }
    
    /**
     * Returns a string representation of this condition (useful for debugging).
     */
    @Override
    public String toString() {

        if (isSimpleCondition()) return String.format("%s %s", fieldName, value);
        else if (isComplexCondition()) {

            var sb = new StringBuilder();
            sb.append("(");
            for (var i = 0; i < subConditions.size(); i++) {

                if (i > 0) sb.append(" ").append(logicalOperator).append(" ");
                sb.append(subConditions.get(i).toString());
            }
            sb.append(")");
            return sb.toString();
        }
        return "EMPTY_CONDITION";
    }
}
