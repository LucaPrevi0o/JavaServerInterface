package server.connection.database;

public interface StorageEngine {
    
    void write(String key, byte[] data);
    byte[] read(String key);
    void delete(String key);
}