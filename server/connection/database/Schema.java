package server.connection.database;

import java.util.Map;
import java.util.Set;

/**
 * Class representing the database schema.
 */
public class Schema {
    
    private Map<String, Set<String>> tables;

    /**
     * Constructor for Schema.
     */
    public Schema() { this.tables = null; }

    /**
     * Gets the tables in the schema.
     * @return a map of table names to their columns
     */
    public Map<String, Set<String>> getTables() { return tables; }

    /**
     * Sets the tables in the schema.
     * @param tables a map of table names to their columns
     */
    public void setTables(Map<String, Set<String>> tables) { this.tables = tables; }
}