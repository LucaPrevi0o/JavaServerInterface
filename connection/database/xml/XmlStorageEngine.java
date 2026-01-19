package com.lucaprevioo.jsi.connection.database.xml;

import java.util.ArrayList;
import java.util.List;

import com.lucaprevioo.jsi.connection.database.Field;
import com.lucaprevioo.jsi.connection.database.StorageEngine;

/**
 * Concrete implementation of XmlStorageEngine.
 * Provides XML parsing and serialization functionality without using pre-built XML libraries.
 */
public class XmlStorageEngine extends StorageEngine {

    /**
     * Constructor for XmlStorageEngine.
     * @param storagePath the path where XML files are stored
     */
    public XmlStorageEngine(String storagePath) { super(storagePath); }

    /**
     * Serializes a Field object into an XML element string.
     * @param field the Field to serialize
     * @return the XML string representation of the field
     */
    @Override
    protected String fieldToString(Field field) {

        var sb = new StringBuilder();
        var tagName = sanitizeTagName(field.getName());
        var value = field.getValue();

        sb.append("<").append(tagName).append(">");

        if (value == null) sb.append("<null/>");
        else if (value instanceof String) sb.append(escapeXml((String) value));
        else if (value instanceof Number || value instanceof Boolean) sb.append(value.toString());
        else sb.append(escapeXml(value.toString()));

        sb.append("</").append(tagName).append(">");
        return sb.toString();
    }

    /**
     * Serializes a list of Field objects into an XML record element string.
     * @param fields the list of Fields to serialize
     * @return the XML string representation of the record
     */
    @Override
    protected String recordToString(List<Field> fields) {

        var sb = new StringBuilder();
        sb.append("<record>");
        for (var field : fields) sb.append("\n    ").append(fieldToString(field));
        sb.append("\n  </record>");
        return sb.toString();
    }

    /**
     * Serializes a list of records into an XML root element string.
     * @param records the list of records to serialize
     * @return the XML string representation of the collection
     */
    @Override
    protected String recordsToArray(List<List<Field>> records) {

        var sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<collection>\n");
        for (var record : records) sb.append("  ").append(recordToString(record)).append("\n");
        sb.append("</collection>\n");
        return sb.toString();
    }

    /**
     * Parses an XML collection string into a list of records.
     * @param xmlArray the XML collection string to parse
     * @return the list of records represented as lists of Fields
     */
    @Override
    protected List<List<Field>> arrayToRecords(String xmlArray) {

        var results = new ArrayList<List<Field>>();
        xmlArray = xmlArray.trim();

        // Remove XML declaration if present
        if (xmlArray.startsWith("<?xml")) {

            var declarationEnd = xmlArray.indexOf("?>");
            if (declarationEnd != -1) xmlArray = xmlArray.substring(declarationEnd + 2).trim();
        }

        // Verify root element
        if (!xmlArray.startsWith("<collection>") || !xmlArray.endsWith("</collection>"))
            throw new IllegalArgumentException("Invalid XML collection: must have <collection> root element");

        // Extract content between collection tags
        var content = extractTagContent(xmlArray, "collection");
        if (content.isEmpty()) return results;

        // Split into individual records
        var records = extractAllElements(content, "record");
        for (var recordXml : records) results.add(xmlToRecord(recordXml));

        return results;
    }

    /**
     * Get the file path for a given collection.
     * @param collection the collection name
     * @return the file path
     */
    @Override
    protected String getCollectionPath(String collection) { return storagePath + "/" + collection + ".xml"; }

    // ============= Private helper methods =============

    /**
     * Escape special XML characters in a string.
     * @param text the text to escape
     * @return the escaped text
     */
    private String escapeXml(String text) {

        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }

    /**
     * Unescape XML entities in a string.
     * @param text the text to unescape
     * @return the unescaped text
     */
    private String unescapeXml(String text) {

        return text.replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&");
    }

    /**
     * Sanitize a field name to be a valid XML tag name.
     * @param name the field name
     * @return the sanitized tag name
     */
    private String sanitizeTagName(String name) {

        // Replace invalid characters with underscores
        var sanitized = name.replaceAll("[^a-zA-Z0-9_-]", "_");
        
        // Ensure it starts with a letter or underscore
        if (!sanitized.isEmpty() && !Character.isLetter(sanitized.charAt(0)) && sanitized.charAt(0) != '_')
            sanitized = "_" + sanitized;
        return sanitized.isEmpty() ? "_field" : sanitized;
    }

    /**
     * Extract the content between opening and closing tags.
     * @param xml the XML string
     * @param tagName the tag name to extract
     * @return the content between the tags
     */
    private String extractTagContent(String xml, String tagName) {

        var openTag = "<" + tagName + ">";
        var closeTag = "</" + tagName + ">";

        var startIndex = xml.indexOf(openTag);
        if (startIndex == -1) return "";

        var contentStart = startIndex + openTag.length();
        var endIndex = xml.lastIndexOf(closeTag);

        if (endIndex == -1 || endIndex < contentStart) return "";
        return xml.substring(contentStart, endIndex).trim();
    }

    /**
     * Extract all elements with a given tag name from XML content.
     * @param xml the XML content
     * @param tagName the tag name to search for
     * @return list of XML strings for each element
     */
    private List<String> extractAllElements(String xml, String tagName) {

        var elements = new ArrayList<String>();
        var openTag = "<" + tagName + ">";
        var closeTag = "</" + tagName + ">";

        var searchStart = 0;
        while (true) {

            var startIndex = xml.indexOf(openTag, searchStart);
            if (startIndex == -1) break;

            var endIndex = xml.indexOf(closeTag, startIndex);
            if (endIndex == -1) break;

            var element = xml.substring(startIndex + openTag.length(), endIndex).trim();
            elements.add(element);
            searchStart = endIndex + closeTag.length();
        }

        return elements;
    }

    /**
     * Parse an XML record into a list of Field objects.
     * @param recordXml the XML record content
     * @return the list of Fields
     */
    private List<Field> xmlToRecord(String recordXml) {

        var fields = new ArrayList<Field>();
        recordXml = recordXml.trim();

        if (recordXml.isEmpty()) return fields;

        // Extract all field elements
        var fieldElements = extractFieldElements(recordXml);
        
        for (var fieldElement : fieldElements) {

            var field = xmlToField(fieldElement);
            if (field != null) fields.add(field);
        }

        return fields;
    }

    /**
     * Extract all field elements from a record.
     * @param recordXml the record XML content
     * @return list of field elements with their tag names and values
     */
    private List<FieldElement> extractFieldElements(String recordXml) {

        var elements = new ArrayList<FieldElement>();
        var position = 0;

        while (position < recordXml.length()) {

            // Skip whitespace
            while (position < recordXml.length() && Character.isWhitespace(recordXml.charAt(position))) position++;
            if (position >= recordXml.length()) break;

            // Find opening tag
            if (recordXml.charAt(position) != '<') {

                position++;
                continue;
            }

            var tagStart = position + 1;
            var tagEnd = recordXml.indexOf('>', tagStart);
            if (tagEnd == -1) break;

            var tagName = recordXml.substring(tagStart, tagEnd).trim();
            
            // Skip closing tags
            if (tagName.startsWith("/")) {

                position = tagEnd + 1;
                continue;
            }

            // Find closing tag
            var closeTag = "</" + tagName + ">";
            var contentStart = tagEnd + 1;
            var contentEnd = recordXml.indexOf(closeTag, contentStart);
            
            if (contentEnd == -1) break;

            var content = recordXml.substring(contentStart, contentEnd);
            elements.add(new FieldElement(tagName, content));
            
            position = contentEnd + closeTag.length();
        }

        return elements;
    }

    /**
     * Parse an XML field element into a Field object.
     * @param element the field element
     * @return the Field object
     */
    private Field xmlToField(FieldElement element) {

        var name = element.tagName;
        var valueStr = element.content.trim();
        var value = parseXmlValue(valueStr);
        return new Field(name, value);
    }

    /**
     * Parse an XML value and convert it to the appropriate Java type.
     * @param valueStr the XML value string
     * @return the parsed Java object
     */
    private Object parseXmlValue(String valueStr) {

        valueStr = valueStr.trim();

        // Check for null
        if (valueStr.equals("<null/>") || valueStr.isEmpty()) return null;

        // Check for boolean
        if (valueStr.equals("true")) return true;
        if (valueStr.equals("false")) return false;

        // Try to parse as number
        try {

            if (valueStr.contains(".")) return Double.parseDouble(valueStr);
            else return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {}

        // Unescape and return as string
        return unescapeXml(valueStr);
    }

    /**
     * Helper class to represent a field element (tag name + content).
     */
    private static class FieldElement {

        String tagName;
        String content;

        /**
         * Constructor for FieldElement.
         * @param tagName the tag name
         * @param content the content between the tags
         */
        FieldElement(String tagName, String content) {

            this.tagName = tagName;
            this.content = content;
        }
    }
}
