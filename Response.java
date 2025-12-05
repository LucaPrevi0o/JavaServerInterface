package JavaServerInterface;

/**
 * Abstract class representing a generic response.
 */
public abstract class Response {


    /**
     * Serialize the response to a string format suitable for transmission.
     * @return the serialized response
     */
    public abstract String serialize();
}
