package JavaServerInterface.connection.database.mysql;

import JavaServerInterface.connection.database.DatabaseServer;
import JavaServerInterface.connection.database.engine.DatabaseEngine;
import JavaServerInterface.connection.database.storage.StorageEngine;

public class MySqlDatabaseServer extends DatabaseServer {

    public MySqlDatabaseServer(int port, DatabaseEngine databaseEngine, StorageEngine storageEngine) { super(port, databaseEngine, storageEngine); }

    /**
     * Parse a SQL query string into a MySqlQuery object.
     * Delegates to MySqlQueryManager for the actual parsing logic.
     * 
     * @param input the SQL query string
     * @return a parsed MySqlQuery object
     */
    @Override
    protected MySqlQuery parseRequest(String input) { return MySqlQueryManager.parse(input); }
}