package com.lucaprevioo.jsi.connection.database.mysql.query;

import com.lucaprevioo.jsi.connection.database.query.QueryCondition;

public class MySqlQueryCondition extends QueryCondition {

    public enum MySqlOperator {

        EQUALS("="),
        NOT_EQUALS("<>"),
        GREATER_THAN(">"),
        LESS_THAN("<"),
        GREATER_THAN_OR_EQUALS(">="),
        LESS_THAN_OR_EQUALS("<="),
        IS_NULL("IS NULL"),
        IS_NOT_NULL("IS NOT NULL"),
        LIKE("LIKE"),
        IN("IN");

        private final String symbol;

        MySqlOperator(String symbol) { this.symbol = symbol; }

        public String getSymbol() { return symbol; }
    }

    private MySqlOperator operator;

    public MySqlOperator getOperator() { return operator; }

    public MySqlQueryCondition(String fieldName, Object value, MySqlOperator operator) { 
        
        super(fieldName, value); 
        this.operator = operator;
    }

    public MySqlQueryCondition(LogicalOperator operator, QueryCondition... conditions) { super(operator, conditions); }
    
    @Override
    public MySqlQueryCondition createAnd(QueryCondition... conditions) {
        return new MySqlQueryCondition(LogicalOperator.AND, conditions);
    }

    @Override
    public MySqlQueryCondition createOr(QueryCondition... conditions) {
        return new MySqlQueryCondition(LogicalOperator.OR, conditions);
    }

    @Override
    protected QueryCondition createNot(QueryCondition condition) {
        return new MySqlQueryCondition(LogicalOperator.NOT, condition);
    }

    @Override
    protected QueryCondition createEquals(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, value, MySqlOperator.EQUALS);
    }

    @Override
    protected QueryCondition createNotEquals(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, value, MySqlOperator.NOT_EQUALS);
    }

    @Override
    protected QueryCondition createGreaterThan(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, value, MySqlOperator.GREATER_THAN);
    }

    @Override
    protected QueryCondition createLessThan(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, value, MySqlOperator.LESS_THAN);
    }

    @Override
    protected QueryCondition createGreaterThanOrEqual(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, value, MySqlOperator.GREATER_THAN_OR_EQUALS);
    }

    @Override
    protected QueryCondition createLessThanOrEqual(String fieldName, Object value) {
        return new MySqlQueryCondition(fieldName, value, MySqlOperator.LESS_THAN_OR_EQUALS);
    }

    @Override
    protected QueryCondition createLike(String fieldName, String pattern) {
        return new MySqlQueryCondition(fieldName, pattern, MySqlOperator.LIKE);
    }

    @Override
    protected QueryCondition createIn(String fieldName, Object... values) {
        return new MySqlQueryCondition(fieldName, values, MySqlOperator.IN);
    }

    @Override
    protected QueryCondition createIsNull(String fieldName) {
        return new MySqlQueryCondition(fieldName, null, MySqlOperator.IS_NULL);
    }

    @Override
    protected QueryCondition createIsNotNull(String fieldName) {
        return new MySqlQueryCondition(fieldName, null, MySqlOperator.IS_NOT_NULL);
    }
}
