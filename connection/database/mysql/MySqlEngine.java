package jsi.connection.database.mysql;

import jsi.connection.database.DatabaseEngine;
import jsi.connection.database.Field;
import jsi.connection.database.StorageEngine;
import jsi.connection.database.mysql.query.MySqlQuery;
import jsi.connection.database.mysql.query.MySqlQueryCondition;
import jsi.connection.database.mysql.query.MySqlQueryType;
import jsi.connection.database.query.Query;
import jsi.connection.database.query.QueryResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MySQL-specific implementation of DatabaseEngine.
 * Provides transaction management with buffering for MySQL databases.
 */
public class MySqlEngine extends DatabaseEngine {
    
    private boolean inTransaction = false;
    private List<TransactionOperation> transactionBuffer;
    private Map<String, List<List<Field>>> snapshotCache;

    /**
     * Constructor for MySqlEngine.
     * @param storageEngine the StorageEngine to use
     */
    public MySqlEngine(StorageEngine storageEngine) { 

        super(storageEngine);
        transactionBuffer = new ArrayList<>();
        snapshotCache = new HashMap<>();
    }

    /**
     * Execute a database query with transaction support.
     * Operations are buffered during transactions and applied on commit.
     * @param query the Query to execute
     * @return the QueryResult of the execution
     */
    @Override
    public QueryResult execute(Query query) {

        if (query instanceof MySqlQuery mySqlQuery) {
        
            if (!inTransaction) {

                // Handle CREATE TABLE outside of transactions
                if (mySqlQuery.getQueryType() == MySqlQueryType.CREATE) return handleCreateTable(mySqlQuery);
                return super.execute(mySqlQuery);
            }
            
            // During transaction: buffer operations
            var type = mySqlQuery.getQueryType();
            var collection = mySqlQuery.getTargetCollection();
            var condition = mySqlQuery.getCondition();
            
            try {
                
                // Create snapshot on first access to collection
                if (!snapshotCache.containsKey(collection)) {

                    var snapshot = getStorageEngine().read(collection, null);
                    snapshotCache.put(collection, new ArrayList<>(snapshot));
                }
                
                // Buffer the operation
                if (type == MySqlQueryType.SELECT) {
                    
                    // Read from snapshot
                    var results = new ArrayList<>(snapshotCache.get(collection));
                    if (condition != null) results.removeIf(record -> !matchesCondition(record, condition));
                    return new QueryResult(true, "Read operation successful (from transaction snapshot).", results);
                } else if (type == MySqlQueryType.INSERT || type == MySqlQueryType.UPDATE) {
                    
                    var affectedFields = mySqlQuery.getAffectedFields();
                    transactionBuffer.add(new TransactionOperation(OperationType.WRITE, collection, affectedFields, null));
                    
                    // Update snapshot
                    snapshotCache.get(collection).add(affectedFields);
                    return new QueryResult(true, "Write operation buffered in transaction.", null);
                } else if (type == MySqlQueryType.DELETE || type == MySqlQueryType.DROP) {
                    
                    transactionBuffer.add(new TransactionOperation(OperationType.DELETE, collection, null, condition));
                    
                    // Update snapshot
                    snapshotCache.get(collection).removeIf(record -> matchesCondition(record, condition));
                    return new QueryResult(true, "Delete operation buffered in transaction.", null);
                }
                
                return new QueryResult(false, "Unsupported operation type.", null);
                
            } catch (Exception e) { return new QueryResult(false, "Transaction operation failed: " + e.getMessage(), null); }
        } else throw new IllegalArgumentException("Invalid query type: Expected MySqlQuery.");
    }

    /**
     * Handle CREATE TABLE operation.
     * Creates a new collection file if it doesn't exist.
     * @param query the CREATE TABLE query
     * @return the QueryResult of the operation
     */
    private QueryResult handleCreateTable(MySqlQuery query) {
        
        try {

            var collection = query.getTargetCollection();
            getStorageEngine().createCollection(collection);
            return new QueryResult(true, "Collection created: " + collection + ".", null);
        } catch (IllegalStateException e) { return new QueryResult(false, "Collection already exists.", null); }
        catch (IOException e) { return new QueryResult(false, "Failed to create collection: " + e.getMessage(), null); }
    }

    /**
     * Begin a MySQL transaction.
     * Creates a new transaction buffer and starts tracking changes.
     */
    @Override
    public void beginTransaction() {

        if (inTransaction) throw new IllegalStateException("Transaction already in progress.");
        inTransaction = true;
        transactionBuffer.clear();
        snapshotCache.clear();
        System.out.println("MySQL transaction started.");
    }

    /**
     * Commit the current MySQL transaction.
     * Applies all buffered operations to the storage engine.
     */
    @Override
    public void commit() {

        if (!inTransaction) throw new IllegalStateException("No active transaction to commit.");
        
        try {
            
            // Apply all buffered operations
            for (var operation : transactionBuffer) {
                
                switch (operation.type) {
                    case WRITE:
                        getStorageEngine().write(operation.collection, operation.fields);
                        break;
                    case DELETE:
                        getStorageEngine().delete(operation.collection, operation.condition);
                        break;
                }
            }
            
            inTransaction = false;
            transactionBuffer.clear();
            snapshotCache.clear();
            System.out.println("MySQL transaction committed successfully.");
        } catch (IOException e) {
            
            System.err.println("Commit failed: " + e.getMessage());
            rollback();
            throw new RuntimeException("Transaction commit failed: " + e.getMessage(), e);
        }
    }

    /**
     * Rollback the current MySQL transaction.
     * Discards all buffered operations without applying them.
     */
    @Override
    public void rollback() {

        if (!inTransaction) throw new IllegalStateException("No active transaction to rollback.");
        inTransaction = false;
        transactionBuffer.clear();
        snapshotCache.clear();
        System.out.println("MySQL transaction rolled back.");
    }

    /**
     * Check if a record matches the given condition.
     */
    private boolean matchesCondition(List<Field> record, MySqlQueryCondition condition) {
        
        if (condition == null) return true;
        
        if (condition.isSimpleCondition()) {
            
            var fieldName = condition.getFieldName();
            var expectedValue = condition.getValue();
            
            for (var field : record)
                if (field.getName().equals(fieldName)) return compareValues(field.getValue(), expectedValue);
            return false;
        }
        
        return true; // Complex conditions require more implementation
    }

    /**
     * Compare two values for equality.
     */
    private boolean compareValues(Object actual, Object expected) {
        
        if (actual == null && expected == null) return true;
        if (actual == null || expected == null) return false;
        
        if (actual instanceof Number && expected instanceof Number)
            return Math.abs(((Number) actual).doubleValue() - ((Number) expected).doubleValue()) < 0.0001;
        
        if (actual instanceof String && expected instanceof String)
            return ((String) actual).equalsIgnoreCase((String) expected);
        
        return actual.equals(expected);
    }

    /**
     * Internal class to represent a buffered transaction operation.
     */
    private static class TransactionOperation {
        
        final OperationType type;
        final String collection;
        final List<Field> fields;
        final MySqlQueryCondition condition;
        
        /**
         * Constructor for TransactionOperation.
         * @param type the type of operation
         * @param collection the target collection
         * @param fields the fields involved (for write operations)
         * @param condition the condition (for delete operations)
         */
        TransactionOperation(OperationType type, String collection, List<Field> fields, MySqlQueryCondition condition) {

            this.type = type;
            this.collection = collection;
            this.fields = fields;
            this.condition = condition;
        }
    }

    /**
     * Enum representing types of operations in a transaction.
     */
    private enum OperationType {
        WRITE,
        DELETE
    }
}
