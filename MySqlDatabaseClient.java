package jsi;
import jsi.connection.database.Query;
import jsi.connection.database.QueryResult;

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

        if (request instanceof Query<?>) return ((Query<?>) request).getRawQuery();
        return super.serializeRequest(request);
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

        var result = new QueryResult();
        if (input == null || input.isEmpty()) {

            result.setSuccess(false);
            result.setMessage("Empty response from server");
            return result;
        }

        try {

            // Split response: STATUS|MESSAGE|COUNT|DATA
            var parts = input.split("\\|", 4);
            
            if (parts.length < 2) {

                result.setSuccess(false);
                result.setMessage("Invalid response format");
                return result;
            }

            var status = parts[0];
            var message = parts[1];
            
            result.setSuccess(status.equals("SUCCESS"));
            result.setMessage(message);

            // Parse data if present
            if (parts.length >= 4 && status.equals("SUCCESS")) {

                var dataStr = parts[3];
                var data = parseDataRows(dataStr);
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

        var rows = new ArrayList<Map<String, Object>>();
        if (dataStr == null || dataStr.isEmpty()) return rows;

        // Split by };{ to get individual rows
        var rowStrings = dataStr.split(";");
        
        for (var rowStr : rowStrings) {

            // Remove { and } from row
            rowStr = rowStr.replace("{", "").replace("}", "").trim();
            
            if (rowStr.isEmpty()) continue;
            
            var row = new HashMap<String, Object>();
            
            // Parse key=value pairs
            var fields = rowStr.split(", ");
            
            for (var field : fields) {

                var keyValue = field.split("=", 2);
                if (keyValue.length == 2) {

                    var key = keyValue[0].trim();
                    var value = keyValue[1].trim();
                    row.put(key, value);
                }
            }
            
            if (!row.isEmpty()) rows.add(row);
        }
        
        return rows;
    }
}
