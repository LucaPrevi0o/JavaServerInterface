package server.connection.database;

import common.Response;
import java.util.List;
import java.util.Map;

public class QueryResult extends Response {

    private boolean success;
    private String message;
    private List<Map<String, Object>> data;

    public QueryResult() {
        this.success = false;
        this.message = "";
        this.data = null;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    @Override
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append("Status: ").append(success ? "SUCCESS" : "ERROR").append("\n");
        sb.append("Message: ").append(message).append("\n");
        
        if (data != null && !data.isEmpty()) {
            sb.append("Rows: ").append(data.size()).append("\n");
            sb.append("Data:\n");
            
            for (int i = 0; i < data.size(); i++) {
                Map<String, Object> row = data.get(i);
                sb.append("  Row ").append(i + 1).append(": ");
                sb.append(row.toString()).append("\n");
            }
        }
        
        return sb.toString();
    }
    
}
