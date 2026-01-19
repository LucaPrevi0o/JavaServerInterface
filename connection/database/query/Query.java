package com.lucaprevioo.jsi.connection.database.query;

import com.lucaprevioo.jsi.Request;
import com.lucaprevioo.jsi.connection.database.Field;

import java.util.List;

/**
 * Abstract class representing a database query.
 * Extends Request to be used in client-server communication.
 */
public abstract class Query implements Request {

    private String rawQuery;

    /**
     * Constructor for Query.
     * @param rawQuery the raw query string
     */
    public Query(String rawQuery) { this.rawQuery = rawQuery; }
    
    /**
     * Get the type of the query.
     * @return the query type
     */
    public abstract QueryType getQueryType();

    /**
     * Get the target collection for the query.
     * @return the target collection
     */
    public abstract String getTargetCollection();

    /**
     * Get the fields affected by the query.
     * @return the list of affected fields
     */
    public abstract List<Field> getAffectedFields();

    /**
     * Get the where condition for the query.
     * @return the where condition
     */
    public abstract QueryCondition getCondition();
    
    /**
     * Serialize the request into a string format.
     * @return the serialized request string (the raw query)
     */
    @Override
    public String serialize() { return rawQuery; }
}
