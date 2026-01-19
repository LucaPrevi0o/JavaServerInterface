package com.lucaprevioo.jsi.connection.database.json;

import java.util.ArrayList;
import java.util.List;

import com.lucaprevioo.jsi.connection.database.Field;
import com.lucaprevioo.jsi.connection.database.StorageEngine;

/**
 * Concrete implementation of JsonStorageEngine.
 * Provides JSON parsing and serialization functionality.
 */
public class JsonStorageEngine extends StorageEngine {

    /**
     * Constructor for JsonStorageEngine.
     * @param storagePath the path where JSON files are stored
     */
    public JsonStorageEngine(String storagePath) { super(storagePath); }

    /**
     * Serializes a Field object into a JSON key-value pair string.
     * @param field the Field to serialize
     * @return the JSON string representation of the field
     */
    @Override
    protected String fieldToString(Field field) {
    
        var sb = new StringBuilder();
        sb.append("\"").append(field.getName()).append("\":");
    
        var value = field.getValue();
    
        if (value == null) sb.append("null");
        else if (value instanceof String) {

            sb.append("\"")
                .append(((String) value)
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\t", "\\t"))
                .append("\"");
        } else if (value instanceof Number || value instanceof Boolean) sb.append(value.toString());
        else sb.append("\"").append(value.toString()).append("\"");
        return sb.toString();
    }

    /**
     * Serializes a list of Field objects into a JSON object string.
     * @param fields the list of Fields to serialize
     * @return the JSON string representation of the record
     */
    @Override
    protected String recordToString(List<Field> fields) {

        var sb = new StringBuilder();
        sb.append("{");
        
        for (var i = 0; i < fields.size(); i++) {

            if (i > 0) sb.append(", ");
            sb.append(fieldToString(fields.get(i)));
        }
        
        sb.append("}");
        return sb.toString();
    }

    /**
     * Serializes a list of records into a JSON array string.
     * @param records the list of records to serialize
     * @return the JSON string representation of the array
     */
    @Override
    protected String recordsToArray(List<List<Field>> records) {

        var sb = new StringBuilder();
        sb.append("[\n");
        
        for (var i = 0; i < records.size(); i++) {

            sb.append("  ").append(recordToString(records.get(i)));
            if (i < records.size() - 1) sb.append(",");
            sb.append("\n");
        }
        
        sb.append("]\n");
        return sb.toString();
    }

    /**
     * Parses a JSON array string into a list of records.
     * @param jsonArray the JSON array string to parse
     * @return the list of records represented as lists of Fields
     */
    @Override
    protected List<List<Field>> arrayToRecords(String jsonArray) {
        
        var results = new ArrayList<List<Field>>();
        jsonArray = jsonArray.trim();
        
        if (!jsonArray.startsWith("[") || !jsonArray.endsWith("]"))
            throw new IllegalArgumentException("Invalid JSON array: " + jsonArray);
        
        jsonArray = jsonArray.substring(1, jsonArray.length() - 1).trim();
        
        if (jsonArray.isEmpty()) return results;
        
        var objects = splitJsonObjects(jsonArray);
        for (var obj : objects) results.add(jsonToRecord(obj));
        
        return results;
    }

    /**
     * Get the file path for a given collection.
     * @param collection the collection name
     * @return the file path
     */
    @Override
    protected String getCollectionPath(String collection) { return storagePath + "/" + collection + ".json"; }

    // ============= Private helper methods =============

    /**
     * Parse a single JSON key-value pair into a Field object.
     * @param jsonPair the JSON key-value pair string
     * @return the Field object represented by the JSON pair
     * @throws IllegalArgumentException if the JSON pair is invalid
     */
    private Field jsonToField(String jsonPair) {
        
        jsonPair = jsonPair.trim();
        
        var colonIndex = jsonPair.indexOf(':');
        if (colonIndex == -1) throw new IllegalArgumentException("Invalid JSON pair: " + jsonPair);
        
        var name = jsonPair.substring(0, colonIndex).trim();
        if (name.startsWith("\"") && name.endsWith("\""))
            name = name.substring(1, name.length() - 1);
        
        var valueStr = jsonPair.substring(colonIndex + 1).trim();
        var value = parseJsonValue(valueStr);
        
        return new Field(name, value);
    }

    /**
     * Parse a JSON value and convert it to the appropriate Java type.
     * @param valueStr the JSON value string
     * @return the parsed Java object
     */
    private Object parseJsonValue(String valueStr) {
        
        valueStr = valueStr.trim();
        
        if (valueStr.equals("null")) return null;
        if (valueStr.equals("true")) return true;
        if (valueStr.equals("false")) return false;
        
        if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
            return valueStr.substring(1, valueStr.length() - 1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\t", "\t");
        }
        
        try {

            if (valueStr.contains(".")) return Double.parseDouble(valueStr);
            else return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) { return valueStr; }
    }

    /**
     * Parse a JSON object into a list of Field objects.
     * @param jsonObject the JSON object string
     * @return the list of Fields represented by the JSON object
     * @throws IllegalArgumentException if the JSON object is invalid
     */
    private List<Field> jsonToRecord(String jsonObject) {
        
        var fields = new ArrayList<Field>();
        jsonObject = jsonObject.trim();
        
        if (!jsonObject.startsWith("{") || !jsonObject.endsWith("}"))
            throw new IllegalArgumentException("Invalid JSON object: " + jsonObject);
        
        jsonObject = jsonObject.substring(1, jsonObject.length() - 1).trim();
        
        if (jsonObject.isEmpty()) return fields;
        
        var pairs = splitJsonPairs(jsonObject);
        for (var pair : pairs) fields.add(jsonToField(pair));
        
        return fields;
    }

    /**
     * Split a JSON content into individual key-value pairs.
     * @param jsonContent the JSON content string
     * @return the list of key-value pair strings
     */
    private List<String> splitJsonPairs(String jsonContent) {
        
        var pairs = new ArrayList<String>();
        var inString = false;
        var braceDepth = 0;
        var bracketDepth = 0;
        var start = 0;
        
        for (var i = 0; i < jsonContent.length(); i++) {
            var c = jsonContent.charAt(i);
            
            if (c == '"' && (i == 0 || jsonContent.charAt(i - 1) != '\\')) inString = !inString;
            else if (!inString) {

                if (c == '{' || c == '[') braceDepth++;
                else if (c == '}' || c == ']') bracketDepth--;
                else if (c == ',' && braceDepth == 0 && bracketDepth == 0) {

                    pairs.add(jsonContent.substring(start, i).trim());
                    start = i + 1;
                }
            }
        }
        
        if (start < jsonContent.length()) pairs.add(jsonContent.substring(start).trim());
        return pairs;
    }

    /**
     * Split a JSON array content into individual object strings.
     * @param jsonArrayContent the JSON array content string
     * @return the list of JSON object strings
     */
    private List<String> splitJsonObjects(String jsonArrayContent) {
        
        var objects = new ArrayList<String>();
        var inString = false;
        var braceDepth = 0;
        var start = 0;
        
        for (var i = 0; i < jsonArrayContent.length(); i++) {

            var c = jsonArrayContent.charAt(i);
            if (c == '"' && (i == 0 || jsonArrayContent.charAt(i - 1) != '\\')) inString = !inString;
            else if (!inString) {

                if (c == '{') {

                    if (braceDepth == 0) start = i;
                    braceDepth++;
                } else if (c == '}') {

                    braceDepth--;
                    if (braceDepth == 0) objects.add(jsonArrayContent.substring(start, i + 1).trim());
                }
            }
        }
        
        return objects;
    }
}
