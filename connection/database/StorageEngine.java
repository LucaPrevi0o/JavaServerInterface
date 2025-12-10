package jsi.connection.database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import jsi.connection.database.query.QueryCondition;

/**
 * Abstract JSON-based implementation of StorageEngine.
 * Stores data in JSON files, one file per collection.
 * Subclasses must implement JSON serialization and parsing methods.
 */
public abstract class StorageEngine {
    
    protected String storagePath;

    /**
     * Constructor for StorageEngine.
     * @param storagePath the path where files are stored
     */
    public StorageEngine(String storagePath) { this.storagePath = storagePath; }

    /**
     * Create a new collection.
     * Creates an empty JSON array file for the collection.
     * @param collection the name of the collection to create
     * @throws IOException if an I/O error occurs
     * @throws IllegalStateException if the collection already exists
     */
    public void createCollection(String collection) throws IOException {
        
        var filePath = getCollectionPath(collection);
        var file = new File(filePath);
        
        // Verifica che il file non esista già
        if (file.exists()) throw new IllegalStateException("Collection already exists: " + collection + ".");
        
        // Crea la directory se non esiste
        var parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) parentDir.mkdirs();
        
        // Crea un file vuoto con un array JSON vuoto
        var writer = new BufferedWriter(new FileWriter(file));
        writer.write("[\n]\n");
        writer.close();
    }

    /**
     * Write data to the storage.
     * Appends a new record to the collection file.
     * @param collection the target collection
     * @param data the data to write
     * @throws FileNotFoundException if the collection file does not exist
     * @throws IOException if an I/O error occurs
     */
    public void write(String collection, List<Field> data) throws FileNotFoundException, IOException {

        var filePath = getCollectionPath(collection);
        var file = new File(filePath);
        
        // Verifica che il file esista
        if (!file.exists()) throw new FileNotFoundException("Collection does not exist: " + collection + ".");
        
        // Leggi tutti i record esistenti
        var allRecords = read(collection, null);
        
        // Aggiungi il nuovo record
        allRecords.add(data);
        
        // Scrivi il file aggiornato
        writeRecordsToFile(file, allRecords);
    }

    /**
     * Read data from the storage.
     * @param collection the target collection
     * @param condition the query condition
     * @return the list of fields read
     * @throws FileNotFoundException if the collection file does not exist
     * @throws IOException if an I/O error occurs
     */
    public List<List<Field>> read(String collection, QueryCondition condition) throws FileNotFoundException, IOException {

        var filePath = getCollectionPath(collection);
        var file = new File(filePath);
        
        if (!file.exists()) throw new FileNotFoundException("Collection file not found: " + filePath);
        
        // Leggi il contenuto del file
        var fileContent = readFileContent(file);
        
        // Parse dell'array
        var allRecords = arrayToRecords(fileContent);
        
        // Se non c'è condizione, ritorna tutti i record
        if (condition == null) return allRecords;
        
        // Rimuovi i record che non matchano la condizione
        allRecords.removeIf(record -> !matchesCondition(record, condition));
        
        return allRecords;
    }

    /**
     * Delete data from the JSON storage.
     * Removes all records matching the condition from the collection.
     * @param collection the target collection
     * @param condition the query condition
     * @throws FileNotFoundException if the collection file does not exist
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if condition is null
     */
    public void delete(String collection, QueryCondition condition) throws FileNotFoundException, IOException {

        var filePath = getCollectionPath(collection);
        var file = new File(filePath);
        
        // Verifica che il file esista
        if (!file.exists()) throw new FileNotFoundException("Collection does not exist: " + collection + ".");
        
        // La condizione non può essere null per delete
        if (condition == null) throw new IllegalArgumentException("Cannot delete without a condition");
        
        // Leggi tutti i record
        var allRecords = read(collection, null);
        
        // Rimuovi i record che matchano la condizione
        allRecords.removeIf(record -> matchesCondition(record, condition));
        
        // Scrivi il file aggiornato
        writeRecordsToFile(file, allRecords);
    }

    // ============= Helper methods =============

    /**
     * Get the file path for a given collection.
     * @param collection the collection name
     * @return the file path
     */
    protected abstract String getCollectionPath(String collection);

    /**
     * Read the entire content of a file.
     * @param file the file to read
     * @return the file content as a string
     * @throws IOException if an I/O error occurs
     */
    private String readFileContent(File file) throws IOException {

        var reader = new BufferedReader(new FileReader(file));
        var content = new StringBuilder();
        for (var line = ""; (line = reader.readLine()) != null;) content.append(line);
        reader.close();
        return content.toString();
    }

    /**
     * Write records to a JSON file.
     * @param file the file to write to
     * @param records the records to write
     * @throws IOException if an I/O error occurs
     */
    private void writeRecordsToFile(File file, List<List<Field>> records) throws IOException {

        var fileContent = recordsToArray(records);
        var writer = new BufferedWriter(new FileWriter(file));
        writer.write(fileContent);
        writer.close();
    }
    
    /**
     * Check if a record matches the given condition.
     * @param record the record as a list of Field objects
     * @param condition the QueryCondition to evaluate
     * @return true if the record matches the condition
     */
    private boolean matchesCondition(List<Field> record, QueryCondition condition) {
        
        if (condition == null) return true;
        if (condition.isSimpleCondition()) return evaluateSimpleCondition(record, condition);
        if (condition.isComplexCondition()) return evaluateComplexCondition(record, condition);
        return true;
    }

    /**
     * Evaluate a simple condition (field comparison).
     * @param record the record to check
     * @param condition the simple condition
     * @return true if the condition is satisfied
     */
    private boolean evaluateSimpleCondition(List<Field> record, QueryCondition condition) {
        
        var fieldName = condition.getFieldName();
        var expectedValue = condition.getValue();
        
        for (var field : record)
            if (field.getName().equals(fieldName)) return compareValues(field.getValue(), expectedValue);
        
        return false;
    }

    /**
     * Evaluate a complex condition (logical combination of conditions).
     * @param record the record to check
     * @param condition the complex condition
     * @return true if the condition is satisfied
     */
    private boolean evaluateComplexCondition(List<Field> record, QueryCondition condition) {
        
        var operator = condition.getLogicalOperator();
        var subConditions = condition.getSubConditions();
        
        if (operator == QueryCondition.LogicalOperator.AND) {

            for (var subCondition : subConditions)
                if (!matchesCondition(record, subCondition)) return false;
            return true;
        } else if (operator == QueryCondition.LogicalOperator.OR) {

            for (var subCondition : subConditions)
                if (matchesCondition(record, subCondition)) return true;
            return false;
        } else if (operator == QueryCondition.LogicalOperator.NOT) {

            if (!subConditions.isEmpty()) return !matchesCondition(record, subConditions.get(0));
            return true;
        }
        
        return false;
    }

    /**
     * Compare two values for equality, handling different types.
     * @param actualValue the actual value from the record
     * @param expectedValue the expected value from the condition
     * @return true if values match
     */
    private boolean compareValues(Object actualValue, Object expectedValue) {
        
        if (actualValue == null && expectedValue == null) return true;
        if (actualValue == null || expectedValue == null) return false;
        
        if (actualValue instanceof Number && expectedValue instanceof Number) {

            var actual = ((Number) actualValue).doubleValue();
            var expected = ((Number) expectedValue).doubleValue();
            return Math.abs(actual - expected) < 0.0001;
        }
        
        if (actualValue instanceof String && expectedValue instanceof String)
            return ((String) actualValue).equalsIgnoreCase((String) expectedValue);
        
        if (actualValue instanceof Boolean && expectedValue instanceof Boolean)
            return actualValue.equals(expectedValue);
        
        return actualValue.equals(expectedValue);
    }

    // ============= Abstract methods for JSON serialization/parsing =============

    /**
     * Convert a Field object to its string representation.
     * @param field the Field object to convert
     * @return the string representation
     */
    protected abstract String fieldToString(Field field);

    /**
     * Convert a list of Field objects to a string representation of an object.
     * @param fields the list of Field objects
     * @return the string representation of the object
     */
    protected abstract String recordToString(List<Field> fields);

    /**
     * Convert a list of records to a string representation of an array.
     * @param records the list of records
     * @return the string representation of the array
     */
    protected abstract String recordsToArray(List<List<Field>> records);

    /**
     * Parse a string representation of an array into a list of Field lists.
     * @param fieldArray the string representation of the array
     * @return list of lists, where each inner list represents one object
     */
    protected abstract List<List<Field>> arrayToRecords(String fieldArray);
}
