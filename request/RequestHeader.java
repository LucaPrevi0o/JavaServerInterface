package jsi.request;

public interface RequestHeader {
    
    /**
     * Get the name of the request header.
     * @return the name of the request header
     */
    public String getName();

    /**
     * Get the value of the request header.
     * @return the value of the request header
     */
    public String getValue();
}
