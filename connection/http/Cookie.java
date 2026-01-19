package com.lucaprevioo.jsi.connection.http;

/**
 * Simple cookie model representing a name/value pair.
 */
public class Cookie {

    private final String name;
    private final String value;

    /**
     * Constructor for Cookie.
     * @param name request cookie name
     * @param value request cookie value
     */
    public Cookie(String name, String value) {

        this.name = name;
        this.value = value;
    }

    /**
     * Get the cookie name.
     * @return the cookie name
     */
    public String getName() { return name; }

    /**
     * Get the cookie value.
     * @return the cookie value
     */
    public String getValue() { return value; }
}
