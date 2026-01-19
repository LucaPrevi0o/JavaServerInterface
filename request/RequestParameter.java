package com.lucaprevioo.jsi.request;

public interface RequestParameter {
    
    /**
     * Get the name of the request parameter.
     * @return the name of the request parameter
     */
    public String getName();

    /**
     * Get the value of the request parameter.
     * @return the value of the request parameter
     */
    public Object getValue();
}