package jsi.connection.database.json.storage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jsi.connection.database.storage.StorageEngine;

/**
 * JSON-based storage engine that persists data to the filesystem.
 * Handles serialization, caching, and file I/O.
 * Thread-safe with read-write locks for concurrent access.
 */
public class JsonStorageEngine implements StorageEngine {

    private final Path storageDirectory;
    private final ReadWriteLock lock;

    /**
     * Creates a JsonStorageEngine with the default storage directory "./database".
     * @throws IOException if the directory cannot be created
     */
    public JsonStorageEngine() throws IOException { this(Paths.get("database")); }

    /**
     * Creates a JsonStorageEngine with a custom storage directory.
     * @param storageDirectory the directory where JSON files will be stored
     * @throws IOException if the directory cannot be created
     */
    public JsonStorageEngine(Path storageDirectory) throws IOException {

        this.storageDirectory = storageDirectory;
        this.lock = new ReentrantReadWriteLock();
        
        // Create storage directory if it doesn't exist
        if (!Files.exists(storageDirectory)) Files.createDirectories(storageDirectory);
    }

    /**
     * Writes data to a JSON file identified by the key.
     * The key is used as the filename (with .json extension).
     * @param key the identifier for the data (becomes filename)
     * @param data the byte array to write to the file
     */
    @Override
    public void write(String key, byte[] data) {

        lock.writeLock().lock();
        try {

            var filePath = getFilePath(key);
            Files.write(filePath, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write data for key: " + key, e);
        } finally { lock.writeLock().unlock(); }
    }

    /**
     * Reads data from a JSON file identified by the key.
     * @param key the identifier for the data (filename)
     * @return the byte array read from the file, or null if file doesn't exist
     */
    @Override
    public byte[] read(String key) {

        lock.readLock().lock();
        try {
            var filePath = getFilePath(key);
            if (!Files.exists(filePath)) return null;
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read data for key: " + key, e);
        } finally { lock.readLock().unlock(); }
    }

    /**
     * Deletes the JSON file identified by the key.
     * @param key the identifier for the data (filename to delete)
     */
    @Override
    public void delete(String key) {

        lock.writeLock().lock();
        try {
            var filePath = getFilePath(key);            
            if (Files.exists(filePath)) Files.delete(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete data for key: " + key, e);
        } finally { lock.writeLock().unlock(); }
    }

    /**
     * Checks if a file exists for the given key.
     * @param key the identifier to check
     * @return true if the file exists, false otherwise
     */
    public boolean exists(String key) {
        lock.readLock().lock();
        try {
            return Files.exists(getFilePath(key));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Lists all keys (filenames without .json extension) in the storage directory.
     * @return array of keys, or empty array if none exist
     */
    public String[] listKeys() {

        lock.readLock().lock();
        try {

            return Files.list(storageDirectory)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(path -> {

                        var filename = path.getFileName().toString();
                        return filename.substring(0, filename.length() - 5); // Remove .json
                    })
                    .toArray(String[]::new);
        } catch (IOException e) {
            throw new RuntimeException("Failed to list keys in storage", e);
        } finally { lock.readLock().unlock(); }
    }

    /**
     * Gets the storage directory path.
     * @return the storage directory
     */
    public Path getStorageDirectory() { return storageDirectory; }

    /**
     * Clears all data from the storage directory.
     * WARNING: This deletes all JSON files in the storage directory.
     */
    public void clear() {

        lock.writeLock().lock();
        try {

            Files.list(storageDirectory)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    try { Files.delete(path); }
                    catch (IOException e) {
                        throw new RuntimeException("Failed to delete file: " + path, e);
                    }
                });
        } catch (IOException e) {
            throw new RuntimeException("Failed to clear storage", e);
        } finally { lock.writeLock().unlock(); }
    }

    /**
     * Converts a key to a file path with .json extension.
     * Sanitizes the key to prevent directory traversal attacks.
     * @param key the key to convert
     * @return the full file path
     */
    private Path getFilePath(String key) {

        // Sanitize key to prevent directory traversal
        var sanitizedKey = key.replaceAll("[^a-zA-Z0-9_-]", "_");
        return storageDirectory.resolve(sanitizedKey + ".json");
    }

    // High-level database operations

    /**
     * Loads all table names from storage.
     * @return set of table names
     */
    @Override
    public Set<String> loadTableNames() {

        lock.readLock().lock();
        try {

            var schemaData = read("_schema");
            var json = new String(schemaData, StandardCharsets.UTF_8);
            return parseTableNamesFromJson(json);
        } finally { lock.readLock().unlock(); }
    }

    /**
     * Loads schema information for a table.
     * @param tableName the table name
     * @return set of column names, or empty set if not found
     */
    @Override
    public Set<String> loadTableSchema(String tableName) {

        lock.readLock().lock();
        try {
            
            var schemaData = read("_schema");
            var json = new String(schemaData, StandardCharsets.UTF_8);
            return parseTableSchemaFromJson(json, tableName);
        } finally { lock.readLock().unlock(); }
    }

    /**
     * Loads all rows from a table.
     * @param tableName the table name
     * @return list of rows (each row is a map of column->value)
     */
    @Override
    public List<Map<String, Object>> loadTable(String tableName) {

        lock.readLock().lock();
        try {
            
            var tableData = read("table_" + tableName);
            var json = new String(tableData, StandardCharsets.UTF_8);
            return parseTableDataFromJson(json);
        } finally { lock.readLock().unlock(); }
    }

    /**
     * Saves a table with its rows.
     * @param tableName the table name
     * @param rows the table data
     */
    @Override
    public void saveTable(String tableName, List<Map<String, Object>> rows) {

        lock.writeLock().lock();
        try {
            
            var json = serializeTableToJson(tableName, rows);
            write("table_" + tableName, json.getBytes(StandardCharsets.UTF_8));
        } finally { lock.writeLock().unlock(); }
    }

    /**
     * Saves schema information for all tables.
     * @param schemas map of table name to column names
     */
    @Override
    public void saveSchemas(Map<String, Set<String>> schemas) {

        lock.writeLock().lock();
        try {

            var json = serializeSchemasToJson(schemas);
            write("_schema", json.getBytes(StandardCharsets.UTF_8));
        } finally { lock.writeLock().unlock(); }
    }

    /**
     * Deletes a table from storage.
     * @param tableName the table name to delete
     */
    @Override
    public void deleteTable(String tableName) {

        lock.writeLock().lock();
        try { delete("table_" + tableName); }
        finally { lock.writeLock().unlock(); }
    }

    // JSON serialization/deserialization

    /**
     * Serializes table data to a JSON string.
     * @param tableName the name of the table
     * @param rows the list of rows
     * @return the JSON string representation of the table data
     */
    private String serializeTableToJson(String tableName, List<Map<String, Object>> rows) {

        var json = new StringBuilder();
        json.append("{\"tableName\":\"").append(tableName).append("\",\"rows\":[");
        for (var i = 0; i < rows.size(); i++) {

            if (i > 0) json.append(",");
            json.append("{");
            
            var row = rows.get(i);
            var colIndex = 0;
            for (var entry : row.entrySet()) {

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
     * Serializes schema information to a JSON string.
     * @param schemas map of table name to column names
     * @return the JSON string representation of the schemas
     */
    private String serializeSchemasToJson(Map<String, Set<String>> schemas) {

        var json = new StringBuilder();
        json.append("{\"tables\":{");
        
        var tableIndex = 0;
        for (var entry : schemas.entrySet()) {

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
     * Parses table data from a JSON string.
     * @param json the JSON string representation of the table data
     * @return the list of rows
     */
    private List<Map<String, Object>> parseTableDataFromJson(String json) {

        var rows = new ArrayList<Map<String, Object>>();
        
        var rowsStart = json.indexOf("\"rows\":[") + 8;
        var rowsEnd = json.lastIndexOf("]");
        
        if (rowsStart < 8 || rowsEnd <= rowsStart) return rows;
        
        var rowsJson = json.substring(rowsStart, rowsEnd);
        var rowArray = rowsJson.split("\\},\\{");
        
        for (var rowStr : rowArray) {

            rowStr = rowStr.replace("{", "").replace("}", "");
            if (rowStr.trim().isEmpty()) continue;
            
            var row = new HashMap<String, Object>();
            var fields = rowStr.split(",(?=\")");
            
            for (var field : fields) {

                var colonIndex = field.indexOf(":");
                if (colonIndex > 0) {
                    
                    var key = field.substring(0, colonIndex).replace("\"", "").trim();
                    var value = field.substring(colonIndex + 1).replace("\"", "").trim();
                    row.put(key, value);
                }
            }
            
            if (!row.isEmpty()) rows.add(row);
        }
        
        return rows;
    }

    /**
     * Parses table names from a JSON string.
     * @param json the JSON string representation of the schemas
     * @return the set of table names
     */
    private Set<String> parseTableNamesFromJson(String json) {

        var tableNames = new HashSet<String>();
        
        var tablesStart = json.indexOf("{\"tables\":{") + 11;
        var tablesEnd = json.lastIndexOf("}");
        
        if (tablesStart < 11 || tablesEnd <= tablesStart) return tableNames;
        
        var tablesJson = json.substring(tablesStart, tablesEnd);
        var tableArray = tablesJson.split(",(?=\")");
        
        for (var tableStr : tableArray) {
            
            var colonIndex = tableStr.indexOf(":");
            if (colonIndex > 0) {

                var tableName = tableStr.substring(0, colonIndex).replace("\"", "").trim();
                tableNames.add(tableName);
            }
        }
        
        return tableNames;
    }

    /**
     * Parses table schema from a JSON string.
     * @param json the JSON string representation of the schemas
     * @param tableName the table name to extract schema for
     * @return the set of column names
     */
    private Set<String> parseTableSchemaFromJson(String json, String tableName) {

        var columns = new HashSet<String>();
        
        var tablesStart = json.indexOf("{\"tables\":{") + 11;
        var tablesEnd = json.lastIndexOf("}");
        
        if (tablesStart < 11 || tablesEnd <= tablesStart) return columns;
        
        var tablesJson = json.substring(tablesStart, tablesEnd);
        var tableArray = tablesJson.split(",(?=\")");
        
        for (var tableStr : tableArray) {
            
            var colonIndex = tableStr.indexOf(":");
            if (colonIndex > 0) {

                var currentTableName = tableStr.substring(0, colonIndex).replace("\"", "").trim();
                if (currentTableName.equals(tableName)) {

                    var columnsStr = tableStr.substring(colonIndex + 1)
                        .replace("[", "").replace("]", "").replace("\"", "").trim();
                    
                    if (!columnsStr.isEmpty())
                        for (var col : columnsStr.split(",")) columns.add(col.trim());
                    break;
                }
            }
        }
        
        return columns;
    }
}
