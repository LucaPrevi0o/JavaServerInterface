package server.connection.database.json;

import server.connection.database.StorageEngine;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * JSON-based storage engine that persists data to the filesystem.
 * Each key corresponds to a separate JSON file in the storage directory.
 * Thread-safe with read-write locks for concurrent access.
 */
public class JsonStorageEngine implements StorageEngine {

    private final Path storageDirectory;
    private final ReadWriteLock lock;

    /**
     * Creates a JsonStorageEngine with the default storage directory "./database".
     * @throws IOException if the directory cannot be created
     */
    public JsonStorageEngine() throws IOException {
        this(Paths.get("database"));
    }

    /**
     * Creates a JsonStorageEngine with a custom storage directory.
     * @param storageDirectory the directory where JSON files will be stored
     * @throws IOException if the directory cannot be created
     */
    public JsonStorageEngine(Path storageDirectory) throws IOException {
        this.storageDirectory = storageDirectory;
        this.lock = new ReentrantReadWriteLock();
        
        // Create storage directory if it doesn't exist
        if (!Files.exists(storageDirectory)) {
            Files.createDirectories(storageDirectory);
        }
    }

    /**
     * Writes data to a JSON file identified by the key.
     * The key is used as the filename (with .json extension).
     * @param key the identifier for the data (becomes filename)
     * @param data the byte array to write to the file
     */
    @Override
    public void write(String key, byte[] data) {
        lock.writeLock().lock();
        try {
            Path filePath = getFilePath(key);
            Files.write(filePath, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write data for key: " + key, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Reads data from a JSON file identified by the key.
     * @param key the identifier for the data (filename)
     * @return the byte array read from the file, or null if file doesn't exist
     */
    @Override
    public byte[] read(String key) {
        lock.readLock().lock();
        try {
            Path filePath = getFilePath(key);
            
            if (!Files.exists(filePath)) {
                return null;
            }
            
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read data for key: " + key, e);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Deletes the JSON file identified by the key.
     * @param key the identifier for the data (filename to delete)
     */
    @Override
    public void delete(String key) {
        lock.writeLock().lock();
        try {
            Path filePath = getFilePath(key);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete data for key: " + key, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Checks if a file exists for the given key.
     * @param key the identifier to check
     * @return true if the file exists, false otherwise
     */
    public boolean exists(String key) {
        lock.readLock().lock();
        try {
            return Files.exists(getFilePath(key));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Lists all keys (filenames without .json extension) in the storage directory.
     * @return array of keys, or empty array if none exist
     */
    public String[] listKeys() {
        lock.readLock().lock();
        try {
            return Files.list(storageDirectory)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(path -> {
                        String filename = path.getFileName().toString();
                        return filename.substring(0, filename.length() - 5); // Remove .json
                    })
                    .toArray(String[]::new);
        } catch (IOException e) {
            throw new RuntimeException("Failed to list keys in storage", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets the storage directory path.
     * @return the storage directory
     */
    public Path getStorageDirectory() {
        return storageDirectory;
    }

    /**
     * Clears all data from the storage directory.
     * WARNING: This deletes all JSON files in the storage directory.
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            Files.list(storageDirectory)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete file: " + path, e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Failed to clear storage", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Converts a key to a file path with .json extension.
     * Sanitizes the key to prevent directory traversal attacks.
     * @param key the key to convert
     * @return the full file path
     */
    private Path getFilePath(String key) {
        // Sanitize key to prevent directory traversal
        String sanitizedKey = key.replaceAll("[^a-zA-Z0-9_-]", "_");
        return storageDirectory.resolve(sanitizedKey + ".json");
    }
}
