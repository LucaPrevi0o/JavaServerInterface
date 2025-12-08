package jsi.connection.database;

import jsi.Request;
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
    protected abstract QueryType getQueryType();

    /**
     * Get the target collection for the query.
     * @return the target collection
     */
    protected abstract String getTargetCollection();

    /**
     * Get the fields affected by the query.
     * @return the list of affected fields
     */
    protected abstract List<Field> getAffectedFields();

    /**
     * Get the where condition for the query.
     * @return the where condition
     */
    protected abstract QueryCondition getCondition();
    
    /**
     * Serialize the request into a string format.
     * @return the serialized request string (the raw query)
     */
    @Override
    public String serialize() { return rawQuery; }
}
