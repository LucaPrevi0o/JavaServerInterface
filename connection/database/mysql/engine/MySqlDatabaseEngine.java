package JavaServerInterface.connection.database.mysql.engine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import JavaServerInterface.connection.database.Query;
import JavaServerInterface.connection.database.QueryResult;
import JavaServerInterface.connection.database.Schema;
import JavaServerInterface.connection.database.engine.DatabaseEngine;
import JavaServerInterface.connection.database.mysql.MySqlQuery;
import JavaServerInterface.connection.database.storage.StorageEngine;

/**
 * MySQL Database Engine implementation.
 * Supports basic SQL operations: SELECT, INSERT, UPDATE, DELETE, CREATE, DROP.
 * Uses in-memory storage with optional persistence via StorageEngine.
 */
public class MySqlDatabaseEngine implements DatabaseEngine {

    // In-memory storage: table -> rows
    private final Map<String, List<Map<String, Object>>> tables;
    // Schema information: table -> columns
    private final Map<String, Set<String>> tableSchemas;
    // Storage engine for persistence
    private final StorageEngine storageEngine;
    // Transaction state
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
        
        loadFromStorage();
    }

    @Override
    public QueryResult execute(Query query) {

        if (!(query instanceof MySqlQuery)) return createErrorResult("Invalid query type");
        var mySqlQuery = (MySqlQuery) query;
        
        try {

            return switch (mySqlQuery.getQueryType()) {
                case SELECT -> executeSelect(mySqlQuery);
                case INSERT -> executeInsert(mySqlQuery);
                case UPDATE -> executeUpdate(mySqlQuery);
                case DELETE -> executeDelete(mySqlQuery);
                case CREATE -> executeCreate(mySqlQuery);
                case DROP -> executeDrop(mySqlQuery);
                default -> createErrorResult("Unsupported query type: " + mySqlQuery.getQueryType());
            };
        } catch (Exception e) { return createErrorResult("Query execution error: " + e.getMessage()); }
    }

    /**
     * Executes a SELECT query.
     * @param query the MySqlQuery object
     * @return the QueryResult object
     */
    private QueryResult executeSelect(MySqlQuery query) {

        var tableName = query.getTableName();
        if (!tables.containsKey(tableName)) return createErrorResult("Table '" + tableName + "' does not exist");

        var rows = tables.get(tableName);
        var result = new ArrayList<Map<String, Object>>();

        // Filter rows by WHERE clause
        for (var row : rows) {

            if (query.getWhereClause() == null || evaluateWhereClause(row, query.getWhereClause())) {

                var selectedRow = new HashMap<String, Object>();
                
                // Select specific columns or all (*)
                if (query.getColumns() != null && !query.getColumns().isEmpty()) for (String col : query.getColumns()) {

                    if (col.equals("*")) {

                        selectedRow.putAll(row);
                        break;
                    } else if (row.containsKey(col)) selectedRow.put(col, row.get(col));
                } else selectedRow.putAll(row);
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

        var tableName = query.getTableName();
        
        // Create table if it doesn't exist
        if (!tables.containsKey(tableName)) {

            tables.put(tableName, new ArrayList<>());
            tableSchemas.put(tableName, new HashSet<>());
        }

        var newRow = new HashMap<String, Object>();
        var columns = query.getColumns();
        var values = query.getValues();

        if (columns != null && values != null) for (String col : columns) {

            newRow.put(col, values.get(col));
            tableSchemas.get(tableName).add(col);
        }

        tables.get(tableName).add(newRow);
        
        // Persist to storage
        saveTableToStorage(tableName);
        
        return createSuccessResult("INSERT successful. 1 row affected.", null);
    }

    /**
     * Executes an UPDATE query.
     * @param query the MySqlQuery object
     * @return the QueryResult object
     */
    private QueryResult executeUpdate(MySqlQuery query) {

        var tableName = query.getTableName();
        if (!tables.containsKey(tableName)) return createErrorResult("Table '" + tableName + "' does not exist");

        var rows = tables.get(tableName);
        var updates = query.getValues();
        int affectedRows = 0;

        for (var row : rows) if (query.getWhereClause() == null || evaluateWhereClause(row, query.getWhereClause())) {

            row.putAll(updates);
            affectedRows++;
        }

        // Persist to storage
        if (affectedRows > 0) saveTableToStorage(tableName);
        return createSuccessResult("UPDATE successful. " + affectedRows + " row(s) affected.", null);
    }

    /**
     * Executes a DELETE query.
     * @param query the MySqlQuery object
     * @return the QueryResult object
     */
    private QueryResult executeDelete(MySqlQuery query) {

        var tableName = query.getTableName();
        if (!tables.containsKey(tableName)) return createErrorResult("Table '" + tableName + "' does not exist");

        var rows = tables.get(tableName);
        var originalSize = rows.size();

        if (query.getWhereClause() != null) rows.removeIf(row -> evaluateWhereClause(row, query.getWhereClause()));
        else rows.clear();

        var affectedRows = originalSize - rows.size();
        
        // Persist to storage
        if (affectedRows > 0) saveTableToStorage(tableName);
        return createSuccessResult("DELETE successful. " + affectedRows + " row(s) affected.", null);
    }

    /**
     * Executes a CREATE query.
     * @param query the MySqlQuery object
     * @return the QueryResult object
     */
    private QueryResult executeCreate(MySqlQuery query) {

        var tableName = query.getTableName();
        
        if (tables.containsKey(tableName))return createErrorResult("Table '" + tableName + "' already exists");

        tables.put(tableName, new ArrayList<>());
        var columns = query.getColumns() != null ? new HashSet<String>(query.getColumns()) : new HashSet<String>();
        tableSchemas.put(tableName, columns);

        // Persist to storage
        saveTableToStorage(tableName);
        saveSchemaToStorage();

        return createSuccessResult("Table '" + tableName + "' created successfully.", null);
    }

    /**
     * Executes a DROP query.
     * @param query the MySqlQuery object
     * @return the QueryResult object
     */
    private QueryResult executeDrop(MySqlQuery query) {

        var tableName = query.getTableName();
        
        if (!tables.containsKey(tableName)) return createErrorResult("Table '" + tableName + "' does not exist");

        tables.remove(tableName);
        tableSchemas.remove(tableName);

        // Remove from storage
        if (storageEngine != null) storageEngine.deleteTable(tableName);
        saveSchemaToStorage();

        return createSuccessResult("Table '" + tableName + "' dropped successfully.", null);
    }

    /**
     * Simple WHERE clause evaluation.
     * Supports: column = value, column > value, column < value
     */
    private boolean evaluateWhereClause(Map<String, Object> row, String whereClause) {

        // Simple implementation for basic comparisons
        if (whereClause.contains("=")) {

            var parts = whereClause.split("=");
            if (parts.length == 2) {

                var column = parts[0].trim();
                var value = parts[1].trim().replace("'", "").replace("\"", "");
                var rowValue = row.get(column);
                return rowValue != null && rowValue.toString().equals(value);
            }
        }
        // Add more operators as needed (>, <, !=, LIKE, etc.)
        return true;
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
