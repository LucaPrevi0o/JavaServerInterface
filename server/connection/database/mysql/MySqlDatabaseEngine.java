package server.connection.database.mysql;

import server.connection.database.DatabaseEngine;
import server.connection.database.Query;
import server.connection.database.QueryResult;
import server.connection.database.Schema;
import server.connection.database.StorageEngine;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;

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
     * Creates a MySqlDatabaseEngine without persistence (in-memory only).
     */
    public MySqlDatabaseEngine() {
        this(null);
    }

    /**
     * Creates a MySqlDatabaseEngine with optional persistence.
     * @param storageEngine the storage engine for persistence, or null for in-memory only
     */
    public MySqlDatabaseEngine(StorageEngine storageEngine) {
        this.tables = new ConcurrentHashMap<>();
        this.tableSchemas = new ConcurrentHashMap<>();
        this.storageEngine = storageEngine;
        
        // Load existing data from storage if available
        if (storageEngine != null) {
            loadFromStorage();
        }
    }

    @Override
    public QueryResult execute(Query query) {
        if (!(query instanceof MySqlQuery)) {
            return createErrorResult("Invalid query type");
        }

        MySqlQuery mySqlQuery = (MySqlQuery) query;
        
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

    private QueryResult executeSelect(MySqlQuery query) {

        String tableName = query.getTableName();
        if (!tables.containsKey(tableName)) return createErrorResult("Table '" + tableName + "' does not exist");

        List<Map<String, Object>> rows = tables.get(tableName);
        List<Map<String, Object>> result = new ArrayList<>();

        // Filter rows by WHERE clause
        for (Map<String, Object> row : rows) {

            if (query.getWhereClause() == null || evaluateWhereClause(row, query.getWhereClause())) {

                Map<String, Object> selectedRow = new HashMap<>();
                
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

        if (columns != null && values != null) for (String col : columns) {

            newRow.put(col, values.get(col));
            tableSchemas.get(tableName).add(col);
        }

        tables.get(tableName).add(newRow);
        
        // Persist to storage
        saveTableToStorage(tableName);
        
        return createSuccessResult("INSERT successful. 1 row affected.", null);
    }

    private QueryResult executeUpdate(MySqlQuery query) {

        String tableName = query.getTableName();
        if (!tables.containsKey(tableName)) return createErrorResult("Table '" + tableName + "' does not exist");

        List<Map<String, Object>> rows = tables.get(tableName);
        Map<String, Object> updates = query.getValues();
        int affectedRows = 0;

        for (Map<String, Object> row : rows) if (query.getWhereClause() == null || evaluateWhereClause(row, query.getWhereClause())) {

            row.putAll(updates);
            affectedRows++;
        }

        // Persist to storage
        if (affectedRows > 0) {
            saveTableToStorage(tableName);
        }
        
        return createSuccessResult("UPDATE successful. " + affectedRows + " row(s) affected.", null);
    }

    private QueryResult executeDelete(MySqlQuery query) {

        String tableName = query.getTableName();
        if (!tables.containsKey(tableName)) return createErrorResult("Table '" + tableName + "' does not exist");

        List<Map<String, Object>> rows = tables.get(tableName);
        int originalSize = rows.size();

        if (query.getWhereClause() != null) rows.removeIf(row -> evaluateWhereClause(row, query.getWhereClause()));
        else rows.clear();

        int affectedRows = originalSize - rows.size();
        
        // Persist to storage
        if (affectedRows > 0) {
            saveTableToStorage(tableName);
        }
        
        return createSuccessResult("DELETE successful. " + affectedRows + " row(s) affected.", null);
    }

    private QueryResult executeCreate(MySqlQuery query) {

        String tableName = query.getTableName();
        
        if (tables.containsKey(tableName))return createErrorResult("Table '" + tableName + "' already exists");

        tables.put(tableName, new ArrayList<>());
        Set<String> columns = query.getColumns() != null ? 
            new HashSet<>(query.getColumns()) : new HashSet<>();
        tableSchemas.put(tableName, columns);

        // Persist to storage
        saveTableToStorage(tableName);
        saveSchemaToStorage();

        return createSuccessResult("Table '" + tableName + "' created successfully.", null);
    }

    private QueryResult executeDrop(MySqlQuery query) {

        String tableName = query.getTableName();
        
        if (!tables.containsKey(tableName)) return createErrorResult("Table '" + tableName + "' does not exist");

        tables.remove(tableName);
        tableSchemas.remove(tableName);

        // Remove from storage
        if (storageEngine != null) {
            storageEngine.delete("table_" + tableName);
        }
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

        if (inTransaction) throw new IllegalStateException("Transaction already in progress");
        
        // Create deep copy of current state
        transactionSnapshot = new HashMap<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : tables.entrySet()) {

            List<Map<String, Object>> tableCopy = new ArrayList<>();
            for (Map<String, Object> row : entry.getValue()) tableCopy.add(new HashMap<>(row));
            transactionSnapshot.put(entry.getKey(), tableCopy);
        }
        
        schemaSnapshot = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : tableSchemas.entrySet()) schemaSnapshot.put(entry.getKey(), new HashSet<>(entry.getValue()));
        
        inTransaction = true;
    }

    @Override
    public void commit() {

        if (!inTransaction) throw new IllegalStateException("No transaction in progress");
        
        // Clear snapshots and commit changes
        transactionSnapshot = null;
        schemaSnapshot = null;
        inTransaction = false;
    }

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

    /**
     * Loads all tables and schema from storage.
     */
    private void loadFromStorage() {
        if (storageEngine == null) return;

        try {
            // Load schema
            byte[] schemaData = storageEngine.read("_schema");
            if (schemaData != null) {
                String schemaJson = new String(schemaData, StandardCharsets.UTF_8);
                parseSchemaFromJson(schemaJson);
            }

            // Load each table
            for (String tableName : tableSchemas.keySet()) {
                loadTableFromStorage(tableName);
            }
        } catch (Exception e) {
            System.err.println("Error loading from storage: " + e.getMessage());
        }
    }

    /**
     * Loads a specific table from storage.
     */
    private void loadTableFromStorage(String tableName) {
        if (storageEngine == null) return;

        try {
            byte[] tableData = storageEngine.read("table_" + tableName);
            if (tableData != null) {
                String tableJson = new String(tableData, StandardCharsets.UTF_8);
                List<Map<String, Object>> rows = parseTableFromJson(tableJson);
                tables.put(tableName, rows);
            }
        } catch (Exception e) {
            System.err.println("Error loading table '" + tableName + "': " + e.getMessage());
        }
    }

    /**
     * Saves a specific table to storage.
     */
    private void saveTableToStorage(String tableName) {
        if (storageEngine == null) return;

        try {
            List<Map<String, Object>> rows = tables.get(tableName);
            String tableJson = serializeTableToJson(tableName, rows);
            storageEngine.write("table_" + tableName, tableJson.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("Error saving table '" + tableName + "': " + e.getMessage());
        }
    }

    /**
     * Saves the schema to storage.
     */
    private void saveSchemaToStorage() {
        if (storageEngine == null) return;

        try {
            String schemaJson = serializeSchemaToJson();
            storageEngine.write("_schema", schemaJson.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("Error saving schema: " + e.getMessage());
        }
    }

    /**
     * Serializes a table to JSON format.
     */
    private String serializeTableToJson(String tableName, List<Map<String, Object>> rows) {
        StringBuilder json = new StringBuilder();
        json.append("{\"tableName\":\"").append(tableName).append("\",\"rows\":[");
        
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) json.append(",");
            json.append("{");
            
            Map<String, Object> row = rows.get(i);
            int colIndex = 0;
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (colIndex > 0) json.append(",");
                json.append("\"").append(entry.getKey()).append("\":\"");
                json.append(entry.getValue() != null ? entry.getValue().toString() : "");
                json.append("\"");
                colIndex++;
            }
            
            json.append("}");
        }
        
        json.append("]}");
        return json.toString();
    }

    /**
     * Serializes the schema to JSON format.
     */
    private String serializeSchemaToJson() {
        StringBuilder json = new StringBuilder();
        json.append("{\"tables\":{");
        
        int tableIndex = 0;
        for (Map.Entry<String, Set<String>> entry : tableSchemas.entrySet()) {
            if (tableIndex > 0) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":[\"")
                .append(String.join("\",\"", entry.getValue()))
                .append("\"]");
            tableIndex++;
        }
        
        json.append("}}");
        return json.toString();
    }

    /**
     * Parses table data from JSON.
     */
    private List<Map<String, Object>> parseTableFromJson(String json) {
        List<Map<String, Object>> rows = new ArrayList<>();
        
        // Simple JSON parsing (rows array)
        int rowsStart = json.indexOf("\"rows\":[") + 8;
        int rowsEnd = json.lastIndexOf("]");
        
        if (rowsStart < 8 || rowsEnd <= rowsStart) return rows;
        
        String rowsJson = json.substring(rowsStart, rowsEnd);
        String[] rowArray = rowsJson.split("\\},\\{");
        
        for (String rowStr : rowArray) {
            rowStr = rowStr.replace("{", "").replace("}", "");
            if (rowStr.trim().isEmpty()) continue;
            
            Map<String, Object> row = new HashMap<>();
            String[] fields = rowStr.split(",(?=\")");
            
            for (String field : fields) {
                int colonIndex = field.indexOf(":");
                if (colonIndex > 0) {
                    String key = field.substring(0, colonIndex).replace("\"", "").trim();
                    String value = field.substring(colonIndex + 1).replace("\"", "").trim();
                    row.put(key, value);
                }
            }
            
            if (!row.isEmpty()) {
                rows.add(row);
            }
        }
        
        return rows;
    }

    /**
     * Parses schema from JSON.
     */
    private void parseSchemaFromJson(String json) {
        // Simple JSON parsing (tables object)
        int tablesStart = json.indexOf("{\"tables\":{") + 11;
        int tablesEnd = json.lastIndexOf("}");
        
        if (tablesStart < 11 || tablesEnd <= tablesStart) return;
        
        String tablesJson = json.substring(tablesStart, tablesEnd);
        String[] tableArray = tablesJson.split(",(?=\")");
        
        for (String tableStr : tableArray) {
            int colonIndex = tableStr.indexOf(":");
            if (colonIndex > 0) {
                String tableName = tableStr.substring(0, colonIndex).replace("\"", "").trim();
                String columnsStr = tableStr.substring(colonIndex + 1)
                    .replace("[", "").replace("]", "").replace("\"", "").trim();
                
                Set<String> columns = new HashSet<>();
                if (!columnsStr.isEmpty()) {
                    for (String col : columnsStr.split(",")) {
                        columns.add(col.trim());
                    }
                }
                
                tableSchemas.put(tableName, columns);
            }
        }
    }
}
