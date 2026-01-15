package jsi.connection.http;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Annotation to define a route for HTTP requests.
 * 
 * Every method annotated as {@code @Route} should specify the path it handles.
 * This annotation can be used to map specific URL paths to handler methods.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Route {
    
    /**
     * The URL path that this route handles.
     * @return the URL path
     */
    String path();

    /**
     * (Optional) Static resource path to serve for this route.
     * If specified, the server will serve the static resource instead of invoking the method.
     * @return the static resource path
     */
    String staticResource() default "";
}
