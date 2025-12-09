package jsi.connection.database.query;

import java.util.List;

import jsi.Response;
import jsi.connection.database.Field;

/**
 * Class representing the result of a database query.
 * Extends Response to be used in client-server communication.
 */
public class QueryResult implements Response {

    private boolean success;
    private String message;
    private List<Field> data;

    /**
     * Constructor for QueryResult.
     * @param success indicates if the query was successful
     * @param message the message associated with the result
     * @param data the data returned by the query
     */
    public QueryResult(boolean success, String message, List<Field> data) {

        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * Indicates whether the query was successful.
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() { return success; }

    /**
     * Gets the message associated with the query result.
     * @return the message
     */
    public String getMessage() { return message; }

    /**
     * Gets the data returned by the query.
     * @return the data as a list of maps
     */
    public List<Field> getData() { return data; }

    /**  
     * Serializes the QueryResult into a string format for transmission.
     * Uses a single-line format with pipe separators for network transmission.
     * Format: STATUS|MESSAGE|ROW_COUNT|DATA
     * @return the serialized string representation of the QueryResult
     */
    @Override
    public String serialize() {

        var sb = new StringBuilder();
        sb.append(success ? "SUCCESS" : "ERROR").append("|");
        sb.append(message != null ? message : "").append("|");
        
        if (data != null && !data.isEmpty()) {

            sb.append(data.size()).append("|");
            
            for (int i = 0; i < data.size(); i++) {

                if (i > 0) sb.append(";");
                var row = data.get(i);
                sb.append(row.getName()).append("=").append(row.getValue());
            }
        } else sb.append("0|");
        return sb.toString();
    }
    
}
