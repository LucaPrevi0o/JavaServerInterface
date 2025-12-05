package server.connection.database.mysql;

import server.connection.database.DatabaseEngine;
import server.connection.database.DatabaseServer;
import server.connection.database.QueryResult;
import java.net.Socket;

import common.Response;

public class MySqlDatabaseServer extends DatabaseServer<MySqlDatabaseClientHandler> {

    public MySqlDatabaseServer(int port, DatabaseEngine databaseEngine) { super(port, databaseEngine); }

    protected MySqlDatabaseClientHandler createClientHandler(Socket clientSocket) {
        return new MySqlDatabaseClientHandler(clientSocket, this.getDatabaseEngine());
    }

    @Override
    protected Response serveFile(String filePath, String contentType) {
        
        QueryResult result = new QueryResult();
        result.setSuccess(false);
        result.setMessage("Database server does not support file serving");
        return result;
    }
    
}