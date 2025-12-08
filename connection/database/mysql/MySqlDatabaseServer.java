package jsi.connection.database.mysql;

import jsi.connection.database.DatabaseServer;
import jsi.connection.database.mysql.engine.MySqlDatabaseEngine;
import jsi.connection.database.storage.StorageEngine;

public class MySqlDatabaseServer extends DatabaseServer<MySqlDatabaseEngine, MySqlQuery> {
    
    public MySqlDatabaseServer(int port, MySqlDatabaseEngine databaseEngine, StorageEngine storageEngine) { super(port, databaseEngine, storageEngine); }

    /**
     * Parse a SQL query string into a MySqlQuery object.
     * Delegates to MySqlQueryManager for the actual parsing logic.
     * 
     * @param input the SQL query string
     * @return a parsed MySqlQuery object
     */
    @Override
    protected MySqlQuery parseRequest(String input) { return new MySqlQuery(input); }
}