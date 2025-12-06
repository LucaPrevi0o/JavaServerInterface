package jsi.connection.database.mysql;

public enum QueryType {
    
    SELECT("SELECT"),
    INSERT("INSERT"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    CREATE("CREATE"),
    DROP("DROP"),
    ALTER("ALTER");

    private final String sqlKeyword;

    QueryType(String sqlKeyword) { this.sqlKeyword = sqlKeyword; }

    public String getSqlKeyword() { return sqlKeyword; }
}
