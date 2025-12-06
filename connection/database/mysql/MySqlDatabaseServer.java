package jsi.connection.database.mysql;

import jsi.connection.database.DatabaseServer;
import jsi.connection.database.engine.DatabaseEngine;
import jsi.connection.database.storage.StorageEngine;

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