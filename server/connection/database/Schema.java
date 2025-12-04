package server.connection.database;

import java.util.Map;
import java.util.Set;

public class Schema {
    
    private Map<String, Set<String>> tables;

    public Schema() {
        this.tables = null;
    }

    public Map<String, Set<String>> getTables() {
        return tables;
    }

    public void setTables(Map<String, Set<String>> tables) {
        this.tables = tables;
    }
}