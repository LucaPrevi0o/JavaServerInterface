package common;
/**
 * Abstract class representing a generic request.
 */
public abstract class Request {

    /**
     * Parse the input string to create a Request object.
     * @param input the input string representing the request
     * @return a Request object
     */
    public abstract Request parse(String input);
}
