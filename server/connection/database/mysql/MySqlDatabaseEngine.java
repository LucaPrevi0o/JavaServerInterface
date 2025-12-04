package server.connection.database.mysql;

import server.connection.database.DatabaseEngine;
import server.connection.database.Query;
import server.connection.database.QueryResult;
import server.connection.database.Schema;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MySqlDatabaseEngine implements DatabaseEngine {

    // In-memory storage: table -> rows
    private final Map<String, List<Map<String, Object>>> tables;
    // Schema information: table -> columns
    private final Map<String, Set<String>> tableSchemas;
    // Transaction state
    private boolean inTransaction = false;
    private Map<String, List<Map<String, Object>>> transactionSnapshot;
    private Map<String, Set<String>> schemaSnapshot;

    public MySqlDatabaseEngine() {
        this.tables = new ConcurrentHashMap<>();
        this.tableSchemas = new ConcurrentHashMap<>();
    }

    @Override
    public QueryResult execute(Query query) {
        if (!(query instanceof MySqlQuery)) {
            return createErrorResult("Invalid query type");
        }

        MySqlQuery mySqlQuery = (MySqlQuery) query;
        
        try {
            switch (mySqlQuery.getQueryType()) {
                case SELECT:
                    return executeSelect(mySqlQuery);
                case INSERT:
                    return executeInsert(mySqlQuery);
                case UPDATE:
                    return executeUpdate(mySqlQuery);
                case DELETE:
                    return executeDelete(mySqlQuery);
                case CREATE:
                    return executeCreate(mySqlQuery);
                case DROP:
                    return executeDrop(mySqlQuery);
                default:
                    return createErrorResult("Unsupported query type: " + mySqlQuery.getQueryType());
            }
        } catch (Exception e) {
            return createErrorResult("Query execution error: " + e.getMessage());
        }
    }

    private QueryResult executeSelect(MySqlQuery query) {
        String tableName = query.getTableName();
        if (!tables.containsKey(tableName)) {
            return createErrorResult("Table '" + tableName + "' does not exist");
        }

        List<Map<String, Object>> rows = tables.get(tableName);
        List<Map<String, Object>> result = new ArrayList<>();

        // Filter rows by WHERE clause
        for (Map<String, Object> row : rows) {
            if (query.getWhereClause() == null || evaluateWhereClause(row, query.getWhereClause())) {
                Map<String, Object> selectedRow = new HashMap<>();
                
                // Select specific columns or all (*)
                if (query.getColumns() != null && !query.getColumns().isEmpty()) {
                    for (String col : query.getColumns()) {
                        if (col.equals("*")) {
                            selectedRow.putAll(row);
                            break;
                        } else if (row.containsKey(col)) {
                            selectedRow.put(col, row.get(col));
                        }
                    }
                } else {
                    selectedRow.putAll(row);
                }
                
                result.add(selectedRow);
            }
        }

        return createSuccessResult("SELECT successful", result);
    }

    private QueryResult executeInsert(MySqlQuery query) {
        String tableName = query.getTableName();
        
        // Create table if it doesn't exist
        if (!tables.containsKey(tableName)) {
            tables.put(tableName, new ArrayList<>());
            tableSchemas.put(tableName, new HashSet<>());
        }

        Map<String, Object> newRow = new HashMap<>();
        List<String> columns = query.getColumns();
        Map<String, Object> values = query.getValues();

        if (columns != null && values != null) {
            for (String col : columns) {
                newRow.put(col, values.get(col));
                tableSchemas.get(tableName).add(col);
            }
        }

        tables.get(tableName).add(newRow);
        return createSuccessResult("INSERT successful. 1 row affected.", null);
    }

    private QueryResult executeUpdate(MySqlQuery query) {
        String tableName = query.getTableName();
        if (!tables.containsKey(tableName)) {
            return createErrorResult("Table '" + tableName + "' does not exist");
        }

        List<Map<String, Object>> rows = tables.get(tableName);
        Map<String, Object> updates = query.getValues();
        int affectedRows = 0;

        for (Map<String, Object> row : rows) {
            if (query.getWhereClause() == null || evaluateWhereClause(row, query.getWhereClause())) {
                row.putAll(updates);
                affectedRows++;
            }
        }

        return createSuccessResult("UPDATE successful. " + affectedRows + " row(s) affected.", null);
    }

    private QueryResult executeDelete(MySqlQuery query) {
        String tableName = query.getTableName();
        if (!tables.containsKey(tableName)) {
            return createErrorResult("Table '" + tableName + "' does not exist");
        }

        List<Map<String, Object>> rows = tables.get(tableName);
        int originalSize = rows.size();

        if (query.getWhereClause() != null) {
            rows.removeIf(row -> evaluateWhereClause(row, query.getWhereClause()));
        } else {
            rows.clear();
        }

        int affectedRows = originalSize - rows.size();
        return createSuccessResult("DELETE successful. " + affectedRows + " row(s) affected.", null);
    }

    private QueryResult executeCreate(MySqlQuery query) {
        String tableName = query.getTableName();
        
        if (tables.containsKey(tableName)) {
            return createErrorResult("Table '" + tableName + "' already exists");
        }

        tables.put(tableName, new ArrayList<>());
        Set<String> columns = query.getColumns() != null ? 
            new HashSet<>(query.getColumns()) : new HashSet<>();
        tableSchemas.put(tableName, columns);

        return createSuccessResult("Table '" + tableName + "' created successfully.", null);
    }

    private QueryResult executeDrop(MySqlQuery query) {
        String tableName = query.getTableName();
        
        if (!tables.containsKey(tableName)) {
            return createErrorResult("Table '" + tableName + "' does not exist");
        }

        tables.remove(tableName);
        tableSchemas.remove(tableName);

        return createSuccessResult("Table '" + tableName + "' dropped successfully.", null);
    }

    /**
     * Simple WHERE clause evaluation.
     * Supports: column = value, column > value, column < value
     */
    private boolean evaluateWhereClause(Map<String, Object> row, String whereClause) {
        // Simple implementation for basic comparisons
        if (whereClause.contains("=")) {
            String[] parts = whereClause.split("=");
            if (parts.length == 2) {
                String column = parts[0].trim();
                String value = parts[1].trim().replace("'", "").replace("\"", "");
                Object rowValue = row.get(column);
                return rowValue != null && rowValue.toString().equals(value);
            }
        }
        // Add more operators as needed (>, <, !=, LIKE, etc.)
        return true;
    }

    @Override
    public void beginTransaction() {
        if (inTransaction) {
            throw new IllegalStateException("Transaction already in progress");
        }
        
        // Create deep copy of current state
        transactionSnapshot = new HashMap<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : tables.entrySet()) {
            List<Map<String, Object>> tableCopy = new ArrayList<>();
            for (Map<String, Object> row : entry.getValue()) {
                tableCopy.add(new HashMap<>(row));
            }
            transactionSnapshot.put(entry.getKey(), tableCopy);
        }
        
        schemaSnapshot = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : tableSchemas.entrySet()) {
            schemaSnapshot.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        
        inTransaction = true;
    }

    @Override
    public void commit() {
        if (!inTransaction) {
            throw new IllegalStateException("No transaction in progress");
        }
        
        // Clear snapshots and commit changes
        transactionSnapshot = null;
        schemaSnapshot = null;
        inTransaction = false;
    }

    @Override
    public void rollback() {
        if (!inTransaction) {
            throw new IllegalStateException("No transaction in progress");
        }
        
        // Restore from snapshot
        tables.clear();
        tables.putAll(transactionSnapshot);
        tableSchemas.clear();
        tableSchemas.putAll(schemaSnapshot);
        
        transactionSnapshot = null;
        schemaSnapshot = null;
        inTransaction = false;
    }

    @Override
    public Schema getSchema() {
        Schema schema = new Schema();
        schema.setTables(new HashMap<>(tableSchemas));
        return schema;
    }

    private QueryResult createSuccessResult(String message, List<Map<String, Object>> data) {
        QueryResult result = new QueryResult();
        result.setSuccess(true);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    private QueryResult createErrorResult(String errorMessage) {
        QueryResult result = new QueryResult();
        result.setSuccess(false);
        result.setMessage(errorMessage);
        return result;
    }
}
