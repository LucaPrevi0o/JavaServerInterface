package server.connection.database.mysql;

import server.connection.database.DatabaseEngine;
import server.connection.database.DatabaseServer;
import server.connection.database.StorageEngine;
import java.net.Socket;

public class MySqlDatabaseServer extends DatabaseServer<MySqlDatabaseClientHandler> {

    public MySqlDatabaseServer(int port, DatabaseEngine databaseEngine, StorageEngine storageEngine) { super(port, databaseEngine, storageEngine); }

    protected MySqlDatabaseClientHandler createClientHandler(Socket clientSocket) {
        return new MySqlDatabaseClientHandler(clientSocket, this.getDatabaseEngine());
    }
}