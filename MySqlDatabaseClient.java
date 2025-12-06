package jsi;
import jsi.connection.database.Query;
import jsi.connection.database.QueryResult;
import jsi.connection.database.mysql.MySqlQuery;
import jsi.connection.database.mysql.MySqlQueryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySqlDatabaseClient extends DatabaseClient {
    
    public MySqlDatabaseClient(String host, int port) {
        super(host, port);
    }

    /**
     * Serialize the request by extracting the raw query string.
     * This ensures we send the SQL string, not the object representation.
     */
    @Override
    protected String serializeRequest(Request request) {
        if (request instanceof Query) {
            return ((Query) request).getRawQuery();
        }
        return super.serializeRequest(request);
    }

    /**
     * Execute a raw SQL query string.
     * @param queryString the SQL query string
     * @return the QueryResult from the server
     */
    @Override
    public QueryResult executeQuery(String queryString) {
        MySqlQuery query = MySqlQueryManager.parse(queryString);
        query.setRawQuery(queryString);
        return executeQuery(query);
    }

    /**
     * Parse the response string from the database server into a QueryResult object.
     * Expected format: SUCCESS|message|rowCount|{row1};{row2};...
     * or: ERROR|error message
     * 
     * @param input the response string from the server
     * @return the parsed QueryResult
     */
    @Override
    public QueryResult parseResponse(String input) {
        QueryResult result = new QueryResult();
        
        if (input == null || input.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("Empty response from server");
            return result;
        }

        try {
            // Split response: STATUS|MESSAGE|COUNT|DATA
            String[] parts = input.split("\\|", 4);
            
            if (parts.length < 2) {
                result.setSuccess(false);
                result.setMessage("Invalid response format");
                return result;
            }

            String status = parts[0];
            String message = parts[1];
            
            result.setSuccess(status.equals("SUCCESS"));
            result.setMessage(message);

            // Parse data if present
            if (parts.length >= 4 && status.equals("SUCCESS")) {
                String dataStr = parts[3];
                List<Map<String, Object>> data = parseDataRows(dataStr);
                result.setData(data);
            }

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Error parsing response: " + e.getMessage());
        }

        return result;
    }

    /**
     * Parse the data rows from the response string.
     * Format: {key1=value1, key2=value2};{key1=value1, key2=value2};...
     * 
     * @param dataStr the data string
     * @return list of maps representing rows
     */
    private List<Map<String, Object>> parseDataRows(String dataStr) {
        List<Map<String, Object>> rows = new ArrayList<>();
        
        if (dataStr == null || dataStr.isEmpty()) {
            return rows;
        }

        // Split by };{ to get individual rows
        String[] rowStrings = dataStr.split(";");
        
        for (String rowStr : rowStrings) {
            // Remove { and } from row
            rowStr = rowStr.replace("{", "").replace("}", "").trim();
            
            if (rowStr.isEmpty()) continue;
            
            Map<String, Object> row = new HashMap<>();
            
            // Parse key=value pairs
            String[] fields = rowStr.split(", ");
            
            for (String field : fields) {
                String[] keyValue = field.split("=", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
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
     * Convenience method to execute a SELECT query.
     * @param tableName the table name
     * @param columns the columns to select (use "*" for all)
     * @return the QueryResult
     */
    public QueryResult select(String tableName, String... columns) {
        String cols = columns.length == 0 ? "*" : String.join(", ", columns);
        String query = "SELECT " + cols + " FROM " + tableName;
        return executeQuery(query);
    }

    /**
     * Convenience method to execute a SELECT query with a WHERE clause.
     * @param tableName the table name
     * @param whereClause the WHERE clause (without "WHERE" keyword)
     * @param columns the columns to select
     * @return the QueryResult
     */
    public QueryResult selectWhere(String tableName, String whereClause, String... columns) {
        String cols = columns.length == 0 ? "*" : String.join(", ", columns);
        String query = "SELECT " + cols + " FROM " + tableName + " WHERE " + whereClause;
        return executeQuery(query);
    }

    /**
     * Convenience method to execute an INSERT query.
     * @param tableName the table name
     * @param data the data to insert (column -> value)
     * @return the QueryResult
     */
    public QueryResult insert(String tableName, Map<String, Object> data) {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        
        int i = 0;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (i > 0) {
                columns.append(", ");
                values.append(", ");
            }
            columns.append(entry.getKey());
            values.append("'").append(entry.getValue()).append("'");
            i++;
        }
        
        String query = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
        return executeQuery(query);
    }

    /**
     * Convenience method to execute an UPDATE query.
     * @param tableName the table name
     * @param data the data to update (column -> value)
     * @param whereClause the WHERE clause (without "WHERE" keyword)
     * @return the QueryResult
     */
    public QueryResult update(String tableName, Map<String, Object> data, String whereClause) {
        StringBuilder setClause = new StringBuilder();
        
        int i = 0;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (i > 0) setClause.append(", ");
            setClause.append(entry.getKey()).append(" = '").append(entry.getValue()).append("'");
            i++;
        }
        
        String query = "UPDATE " + tableName + " SET " + setClause + " WHERE " + whereClause;
        return executeQuery(query);
    }

    /**
     * Convenience method to execute a DELETE query.
     * @param tableName the table name
     * @param whereClause the WHERE clause (without "WHERE" keyword)
     * @return the QueryResult
     */
    public QueryResult delete(String tableName, String whereClause) {
        String query = "DELETE FROM " + tableName + " WHERE " + whereClause;
        return executeQuery(query);
    }
}
