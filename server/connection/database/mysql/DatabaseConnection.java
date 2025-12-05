package server.connection.database.mysql;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseConnection {
    
    /**
     * The database host to connect to.
     */
    String host();

    /**
     * The database port to connect to.
     */
    int port();

    /**
     * The database query to execute.
     * If provided, this query will be executed when the hook is triggered.
     * 
     */
    String query() default "";
}
