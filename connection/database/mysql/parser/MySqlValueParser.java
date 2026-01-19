package com.lucaprevioo.jsi.connection.database.mysql.parser;

/**
 * Utility class for parsing and converting MySQL values.
 * Handles removal of quotes and backticks, and type conversion.
 */
public final class MySqlValueParser {

    private MySqlValueParser() {}

    /**
     * Remove backticks from a field name.
     * 
     * @param fieldName the field name
     * @return the field name without backticks
     */
    public static String removeBackticks(String fieldName) {

        if (fieldName != null && fieldName.startsWith("`") && fieldName.endsWith("`") && fieldName.length() > 1)
            return fieldName.substring(1, fieldName.length() - 1);
        return fieldName;
    }

    /**
     * Remove quotes from a value string.
     * 
     * @param value the value string
     * @return the value without quotes
     */
    public static String removeQuotes(String value) {

        if (value == null || value.length() < 2) return value;
        
        if ((value.startsWith("'") && value.endsWith("'")) || (value.startsWith("\"") && value.endsWith("\"")))
            return value.substring(1, value.length() - 1);
        return value;
    }

    /**
     * Parse a value string into its appropriate type.
     * Attempts to convert to: Integer, Double, Boolean, null, or String.
     * 
     * @param valueStr the value string
     * @return the parsed value
     */
    public static Object parseValue(String valueStr) {

        if (valueStr == null) return null;
        
        valueStr = valueStr.trim();
        
        // Remove quotes
        valueStr = removeQuotes(valueStr);

        // Check for NULL
        if (valueStr.equalsIgnoreCase("null")) return null;

        // Check for boolean
        if (valueStr.equalsIgnoreCase("true")) return true;
        if (valueStr.equalsIgnoreCase("false")) return false;
        // Try to parse as integer
        try { return Integer.parseInt(valueStr); }
        catch (NumberFormatException e) {}

        // Try to parse as double
        try { return Double.parseDouble(valueStr); } 
        catch (NumberFormatException e) {}

        // Return as string
        return valueStr;
    }

    /**
     * Parse multiple values from a comma-separated string.
     * 
     * @param valuesStr the comma-separated values string
     * @return array of parsed values
     */
    public static Object[] parseValues(String valuesStr) {

        if (valuesStr == null || valuesStr.trim().isEmpty()) return new Object[0];

        var values = valuesStr.split(",");
        var parsedValues = new Object[values.length];

        for (int i = 0; i < values.length; i++) parsedValues[i] = parseValue(values[i].trim());
        return parsedValues;
    }
}
