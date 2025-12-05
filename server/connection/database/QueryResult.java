package server.connection.database;

import common.Response;
import java.util.List;
import java.util.Map;

/**
 * Class representing the result of a database query.
 * Extends Response to be used in client-server communication.
 */
public class QueryResult extends Response {

    private boolean success;
    private String message;
    private List<Map<String, Object>> data;

    /**
     * Constructor for QueryResult.
     */
    public QueryResult() {

        this.success = false;
        this.message = "";
        this.data = null;
    }

    /**
     * Indicates whether the query was successful.
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() { return success; }

    /**     
     * * Sets the success status of the query.
     * @param success true if successful, false otherwise
     */
    public void setSuccess(boolean success) { this.success = success; }

    /**
     * Gets the message associated with the query result.
     * @return the message
     */
    public String getMessage() { return message; }

    /**
     * Sets the message associated with the query result.
     * @param message the message to set
     */
    public void setMessage(String message) { this.message = message; }

    /**
     * Gets the data returned by the query.
     * @return the data as a list of maps
     */
    public List<Map<String, Object>> getData() { return data; }

    /**
     * Sets the data returned by the query.
     * @param data the data to set as a list of maps
     */
    public void setData(List<Map<String, Object>> data) { this.data = data; }

    /**  
     * * Serializes the QueryResult into a string format for transmission.
     * Uses a single-line format with pipe separators for network transmission.
     * Format: STATUS|MESSAGE|ROW_COUNT|DATA
     * @return the serialized string representation of the QueryResult
     */
    @Override
    public String serialize() {

        StringBuilder sb = new StringBuilder();
        sb.append(success ? "SUCCESS" : "ERROR").append("|");
        sb.append(message != null ? message : "").append("|");
        
        if (data != null && !data.isEmpty()) {

            sb.append(data.size()).append("|");
            
            for (int i = 0; i < data.size(); i++) {

                if (i > 0) sb.append(";");
                Map<String, Object> row = data.get(i);
                sb.append(row.toString());
            }
        } else {
            sb.append("0|");
        }
        
        return sb.toString();
    }
    
}
