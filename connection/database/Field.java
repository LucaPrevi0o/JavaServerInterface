package jsi.connection.database;

/**
 * Class representing a field in a database query.
 * The field name represents the column name, and the value represents the data stored in that column.
 */
public class Field {

    private String name;
    private Object value;

    /**
     * Constructor for Field.
     * @param name the name of the field
     * @param value the value of the field
     */
    public Field(String name, Object value) {

        this.name = name;
        this.value = value;
    }

    /**
     * Get the name of the field.
     * @return the field name
     */
    public String getName() { return name; }

    /**
     * Get the value of the field.
     * @return the field value
     */
    public Object getValue() { return value; }
}
