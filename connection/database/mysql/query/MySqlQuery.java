package jsi.connection.database.mysql.query;

import jsi.connection.database.query.Query;
import jsi.connection.database.Field;
import jsi.connection.database.mysql.parser.MySqlConditionParser;
import jsi.connection.database.mysql.parser.MySqlFieldsExtractor;
import jsi.connection.database.mysql.parser.MySqlTableNameExtractor;

import java.util.List;

/**
 * MySQL-specific implementation of Query.
 * Delegates parsing responsibilities to specialized utility classes.
 */
public class MySqlQuery extends Query {

    /**
     * Constructor for MySqlQuery.
     * 
     * @param rawQuery the raw MySQL query string
     */
    public MySqlQuery(String rawQuery) { super(rawQuery); }

    /**
     * Get the type of the MySQL query.
     * 
     * @return the query type
     * @throws IllegalArgumentException if the query type is unknown
     */
    @Override
    public MySqlQueryType getQueryType() {

        var queryUpper = serialize().toUpperCase().trim();
        
        for (var type : MySqlQueryType.values())
            if (queryUpper.startsWith(type.getKeyword())) return type;
        throw new IllegalArgumentException("Unknown MySQL query type: " + serialize());
    }

    /**
     * Get the target collection (table) for the MySQL query.
     * Delegates to MySqlTableNameExtractor.
     * 
     * @return the target collection
     */
    @Override
    public String getTargetCollection() { return MySqlTableNameExtractor.extractTableName(serialize(), getQueryType()); }

    /**
     * Get the fields affected by the query.
     * Delegates to MySqlFieldsExtractor.
     * 
     * @return the list of affected fields
     */
    @Override
    public List<Field> getAffectedFields() { return MySqlFieldsExtractor.extractFields(serialize(), getQueryType()); }

    /**
     * Get the WHERE condition for the query.
     * Delegates to MySqlConditionParser.
     * 
     * @return the WHERE condition as MySqlQueryCondition
     */
    @Override
    public MySqlQueryCondition getCondition() { return MySqlConditionParser.extractCondition(serialize()); }
}
