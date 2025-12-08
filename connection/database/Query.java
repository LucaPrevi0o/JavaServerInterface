package jsi.connection.database;

import jsi.Request;
import java.util.List;
import java.util.Map;

/**
 * Abstract class representing a database query.
 * Extends Request to be used in client-server communication.
 */
public abstract class Query<T extends QueryCondition> extends Request {

    protected String rawQuery;
    protected QueryType queryType; // READ, WRITE, UPDATE, DELETE
    protected String targetCollection;
    
    /**
     * The fields to be selected (for READ) or affected (for WRITE/UPDATE operations).
     * For SELECT: the columns to retrieve
     * For INSERT/UPDATE: the columns to insert/modify
     */
    protected List<String> affectedFields;
    
    /**
     * The data values for WRITE and UPDATE operations.
     * Maps field names to their values.
     * For INSERT: the values to insert
     * For UPDATE: the new values to set
     */
    protected Map<String, Object> dataValues;
    
    /**
     * The WHERE condition for filtering records.
     * Used in SELECT, UPDATE, and DELETE operations.
     */
    protected T whereCondition;

    /**
     * Get the raw query string.
     * @return the raw query string
     */
    public String getRawQuery() { return rawQuery; }

    /**
     * Get the type of operation for the query.
     * @return the QueryType
     */
    public QueryType getQueryType() { return queryType; }

    /**
     * Get the target collection (table) for the query.
     * @return the target collection name
     */
    public String getTargetCollection() { return targetCollection; }

    /**
     * Get the fields affected by this query.
     * @return the list of affected fields
     */
    public List<String> getAffectedFields() { return affectedFields; }

    /**
     * Get the data values for WRITE/UPDATE operations.
     * @return the data values as a map
     */
    public Map<String, Object> getDataValues() { return dataValues; }

    /**
     * Get the WHERE condition for filtering.
     * @return the QueryCondition object
     */
    public T getWhereCondition() { return whereCondition; }
    
    /**
     * Set the raw query string.
     * @param rawQuery the raw query string
     */
    public void setRawQuery(String rawQuery) { this.rawQuery = rawQuery; }

    /**
     * Set the type of operation for the query.
     * @param queryType the QueryType
     */
    public void setQueryType(QueryType queryType) { this.queryType = queryType; }

    /**
     * Set the target collection (table) for the query.
     * @param targetCollection the target collection name
     */
    public void setTargetCollection(String targetCollection) { this.targetCollection = targetCollection; }

    /**
     * Set the fields affected by this query.
     * @param affectedFields the list of affected fields
     */
    public void setAffectedFields(List<String> affectedFields) { this.affectedFields = affectedFields; }

    /**
     * Set the data values for WRITE/UPDATE operations.
     * @param dataValues the data values as a map
     */
    public void setDataValues(Map<String, Object> dataValues) { this.dataValues = dataValues; }

    /**
     * Set the WHERE condition for filtering.
     * @param whereCondition the QueryCondition object
     */
    public void setWhereCondition(T whereCondition) { this.whereCondition = whereCondition; }
}
