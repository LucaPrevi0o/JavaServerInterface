package jsi.connection.database.mysql.engine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import jsi.connection.database.mysql.MySqlQueryCondition;
import jsi.connection.database.QueryResult;
import jsi.connection.database.Schema;
import jsi.connection.database.engine.DatabaseEngine;
import jsi.connection.database.mysql.MySqlQuery;
import jsi.connection.database.storage.StorageEngine;

/**
 * MySQL Database Engine implementation.
 * Supports basic SQL operations: SELECT, INSERT, UPDATE, DELETE, CREATE, DROP.
 * Uses in-memory storage with optional persistence via StorageEngine.
 */
public class MySqlDatabaseEngine implements DatabaseEngine<MySqlQuery> {

    private final Map<String, List<Map<String, Object>>> tables;
    private final Map<String, Set<String>> tableSchemas;
    private final StorageEngine storageEngine;
    private boolean inTransaction = false;
    private Map<String, List<Map<String, Object>>> transactionSnapshot;
    private Map<String, Set<String>> schemaSnapshot;

    /**
     * Creates a MySqlDatabaseEngine with optional persistence.
     * @param storageEngine the storage engine for persistence, or null for in-memory only
     */
    public MySqlDatabaseEngine(StorageEngine storageEngine) {

        this.tables = new ConcurrentHashMap<>();
        this.tableSchemas = new ConcurrentHashMap<>();
        this.storageEngine = storageEngine;
    }
    /**
     * Executes a database query.
     * @param query the Query object
     * @return the QueryResult object
     */
    @Override
    public QueryResult execute(MySqlQuery query) {

        if (!(query instanceof MySqlQuery)) return createErrorResult("Invalid query type");
        loadFromStorage();
        
        try {

            return switch (query.getQueryType().getOperationType()) {
                case READ -> executeSelect(query);
                case CREATE -> executeInsert(query);
                case UPDATE -> executeUpdate(query);
                case DELETE -> executeDelete(query);
                default -> createErrorResult("Unsupported query type: " + query.getQueryType());
            };
        } catch (Exception e) { return createErrorResult("Query execution error: " + e.getMessage()); }
    }

    /**
     * Executes a SELECT query.
     * @param query the MySqlQuery object
     * @return the QueryResult object
     */
    private QueryResult executeSelect(MySqlQuery query) {

        var tableName = query.getTargetCollection();
        if (!tables.containsKey(tableName)) return createErrorResult("Table '" + tableName + "' does not exist");

        var rows = tables.get(tableName);
        var result = new ArrayList<Map<String, Object>>();

        // Filter rows by WHERE condition
        for (var row : rows) {

            if (query.getWhereCondition() == null || evaluateCondition(row, (MySqlQueryCondition)query.getWhereCondition())) {

                var selectedRow = new HashMap<String, Object>();
                
                // Select specific columns or all (*)
                var affectedFields = query.getAffectedFields();
                if (affectedFields != null && !affectedFields.isEmpty())
                    for (var col : affectedFields) {

                        if (col.equals("*")) {

                            selectedRow.putAll(row);
                            break;
                        } else if (row.containsKey(col)) selectedRow.put(col, row.get(col));
                    }
                else selectedRow.putAll(row);
                result.add(selectedRow);
            }
        }

        return createSuccessResult("SELECT successful", result);
    }

    /**
     * Executes an INSERT query.
     * @param query the MySqlQuery object
     * @return the QueryResult object
     */
    private QueryResult executeInsert(MySqlQuery query) {

        var tableName = query.getTargetCollection();
        var isNewTable = false;
        
        // Create table if it doesn't exist
        if (!tables.containsKey(tableName)) {

            tables.put(tableName, new ArrayList<>());
            tableSchemas.put(tableName, new HashSet<>());
            isNewTable = true;
        }

        var newRow = new HashMap<String, Object>();
        var dataValues = query.getDataValues();

        if (dataValues != null) {

            newRow.putAll(dataValues);
            // Update schema with new columns
            tableSchemas.get(tableName).addAll(dataValues.keySet());
        }

        tables.get(tableName).add(newRow);
        
        // Persist to storage
        saveTableToStorage(tableName);
        
        // Save schema if table was created or columns were added
        if (isNewTable || (dataValues != null && !dataValues.isEmpty())) saveSchemaToStorage();
        return createSuccessResult("INSERT successful. 1 row affected.", null);
    }

    /**
     * Executes an UPDATE query.
     * @param query the MySqlQuery object
     * @return the QueryResult object
     */
    private QueryResult executeUpdate(MySqlQuery query) {

        var tableName = query.getTargetCollection();
        if (!tables.containsKey(tableName)) return createErrorResult("Table '" + tableName + "' does not exist");

        var rows = tables.get(tableName);
        var updates = query.getDataValues();
        var affectedRows = 0;
        var schemaChanged = false;

        if (updates != null) {

            // Check if we're adding new columns
            var currentSchema = tableSchemas.get(tableName);
            for (var key : updates.keySet()) if (!currentSchema.contains(key)) {

                currentSchema.add(key);
                schemaChanged = true;
            }
            
            for (var row : rows) if (query.getWhereCondition() == null || evaluateCondition(row, (MySqlQueryCondition)query.getWhereCondition())) {

                row.putAll(updates);
                affectedRows++;
            }
        }

        // Persist to storage
        if (affectedRows > 0) saveTableToStorage(tableName);
        if (schemaChanged) saveSchemaToStorage();
        
        return createSuccessResult("UPDATE successful. " + affectedRows + " row(s) affected.", null);
    }

    /**
     * Executes a DELETE query.
     * @param query the MySqlQuery object
     * @return the QueryResult object
     */
    private QueryResult executeDelete(MySqlQuery query) {

        var tableName = query.getTargetCollection();
        if (!tables.containsKey(tableName)) return createErrorResult("Table '" + tableName + "' does not exist");

        var rows = tables.get(tableName);
        var originalSize = rows.size();

        if (query.getWhereCondition() != null) rows.removeIf(row -> evaluateCondition(row, (MySqlQueryCondition)query.getWhereCondition()));
        else rows.clear();

        var affectedRows = originalSize - rows.size();
        
        // Persist to storage
        if (affectedRows > 0) saveTableToStorage(tableName);
        return createSuccessResult("DELETE successful. " + affectedRows + " row(s) affected.", null);
    }

    /**
     * Evaluates a QueryCondition against a row.
     * Supports both simple and complex conditions.
     * 
     * @param row the row to evaluate
     * @param condition the QueryCondition to evaluate
     * @return true if the condition is satisfied, false otherwise
     */
    private boolean evaluateCondition(Map<String, Object> row, MySqlQueryCondition condition) {

        if (condition == null) return true;
        
        // Handle simple conditions
        if (condition.isSimpleCondition()) return evaluateSimpleCondition(row, condition);
        
        // Handle complex conditions (AND/OR/NOT)
        if (condition.isComplexCondition()) {

            var logicalOp = condition.getLogicalOperator();
            var subConditions = condition.getSubConditions();
            
            return switch (logicalOp) {

                case AND -> {
                    for (var subCondition : subConditions) {
                        if (!evaluateCondition(row, (MySqlQueryCondition)subCondition)) {
                            yield false;
                        }
                    }
                    yield true;
                }
                case OR -> {
                    for (var subCondition : subConditions) {
                        if (evaluateCondition(row, (MySqlQueryCondition)subCondition)) {
                            yield true;
                        }
                    }
                    yield false;
                }
                case NOT -> {
                    if (!subConditions.isEmpty()) {
                        yield !evaluateCondition(row, (MySqlQueryCondition)subConditions.get(0));
                    }
                    yield true;
                }
            };
        }
        
        return true;
    }

    /**
     * Evaluates a simple condition against a row.
     * 
     * @param row the row to evaluate
     * @param condition the simple QueryCondition
     * @return true if the condition is satisfied, false otherwise
     */
    private boolean evaluateSimpleCondition(Map<String, Object> row, MySqlQueryCondition condition) {

        var fieldName = condition.getFieldName();
        var operator = condition.getComparisonOperator();
        var conditionValue = condition.getValue();
        var rowValue = row.get(fieldName);

        return switch (operator) {

            case EQUALS -> Objects.equals(rowValue, conditionValue);
            case NOT_EQUALS -> !Objects.equals(rowValue, conditionValue);
            case GREATER_THAN -> compareValues(rowValue, conditionValue) > 0;
            case LESS_THAN -> compareValues(rowValue, conditionValue) < 0;
            case GREATER_THAN_OR_EQUAL -> compareValues(rowValue, conditionValue) >= 0;
            case LESS_THAN_OR_EQUAL -> compareValues(rowValue, conditionValue) <= 0;
            case LIKE -> {

                if (rowValue == null || conditionValue == null) yield false;
                var pattern = conditionValue.toString()
                    .replace("%", ".*")
                    .replace("_", ".");
                yield rowValue.toString().matches(pattern);
            }
            case IN -> {

                if (conditionValue instanceof Object[]) {

                    var values = (Object[]) conditionValue;
                    for (var value : values)
                        if (Objects.equals(rowValue, value)) yield true;
                }
                yield false;
            }
            case IS_NULL -> rowValue == null;
            case IS_NOT_NULL -> rowValue != null;
        };
    }

    /**
     * Compares two values numerically.
     * Returns negative if v1 < v2, zero if equal, positive if v1 > v2.
     */
    private int compareValues(Object v1, Object v2) {

        if (v1 == null || v2 == null) return 0;
        
        // Try numeric comparison
        try {

            if (v1 instanceof Number && v2 instanceof Number) {
                var d1 = ((Number) v1).doubleValue();
                var d2 = ((Number) v2).doubleValue();
                return Double.compare(d1, d2);
            }
        } catch (Exception e) { /* Ignore and fall back */ }
        
        // Fall back to string comparison
        return v1.toString().compareTo(v2.toString());
    }

    /**
     * Begins a transaction.
     */
    @Override
    public void beginTransaction() {

        if (inTransaction) throw new IllegalStateException("Transaction already in progress");
        
        // Create deep copy of current state
        transactionSnapshot = new HashMap<>();
        for (var entry : tables.entrySet()) {

            var tableCopy = new ArrayList<Map<String, Object>>();
            for (var row : entry.getValue()) tableCopy.add(new HashMap<>(row));
            transactionSnapshot.put(entry.getKey(), tableCopy);
        }
        
        schemaSnapshot = new HashMap<>();
        for (var entry : tableSchemas.entrySet()) schemaSnapshot.put(entry.getKey(), new HashSet<>(entry.getValue()));
        inTransaction = true;
    }

    /**
     * Commits the current transaction.
     */
    @Override
    public void commit() {

        if (!inTransaction) throw new IllegalStateException("No transaction in progress");
        
        // Clear snapshots and commit changes
        transactionSnapshot = null;
        schemaSnapshot = null;
        inTransaction = false;
    }

    /**
     * Rolls back the current transaction.
     */
    @Override
    public void rollback() {
        
        if (!inTransaction) throw new IllegalStateException("No transaction in progress");
        
        // Restore from snapshot
        tables.clear();
        tables.putAll(transactionSnapshot);
        tableSchemas.clear();
        tableSchemas.putAll(schemaSnapshot);
        
        transactionSnapshot = null;
        schemaSnapshot = null;
        inTransaction = false;
    }

    /**
     * Gets the database schema.
     * @return the Schema object
     */
    @Override
    public Schema getSchema() {

        var schema = new Schema();
        schema.setTables(new HashMap<>(tableSchemas));
        return schema;
    }

    /**
     * Creates a successful QueryResult.
     * @param message the success message
     * @param data the data to include in the result
     * @return the QueryResult object
     */
    private QueryResult createSuccessResult(String message, List<Map<String, Object>> data) {

        var result = new QueryResult();
        result.setSuccess(true);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * Creates an error QueryResult.
     */
    private QueryResult createErrorResult(String errorMessage) {

        var result = new QueryResult();
        result.setSuccess(false);
        result.setMessage(errorMessage);
        return result;
    }

    /**
     * Loads all tables and schema from storage.
     */
    private void loadFromStorage() {

        if (storageEngine == null) return;

        try {

            // Load all table names
            var tableNames = storageEngine.loadTableNames();
            tableNames.add("schema");
            
            // Load each table's schema and data
            for (var tableName : tableNames) {

                var columns = storageEngine.loadTableSchema(tableName);
                var rows = storageEngine.loadTable(tableName);
                
                tableSchemas.put(tableName, columns);
                tables.put(tableName, rows);
            }
        } catch (Exception e) { System.err.println("Error loading from storage: " + e.getMessage()); }
    }

    /**
     * Saves a specific table to storage.
     * @param tableName the name of the table to save
     */
    private void saveTableToStorage(String tableName) {

        if (storageEngine == null) return;

        try {

            var rows = tables.get(tableName);
            storageEngine.saveTable(tableName, rows);
        } catch (Exception e) { System.err.println("Error saving table '" + tableName + "': " + e.getMessage()); }
    }

    /**
     * Saves the schema to storage.
     */
    private void saveSchemaToStorage() {

        if (storageEngine == null) return;

        try { storageEngine.saveSchemas(tableSchemas); }
        catch (Exception e) { System.err.println("Error saving schema: " + e.getMessage()); }
    }
}
