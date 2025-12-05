package server.connection.database.mysql;

import server.connection.database.DatabaseClientHandler;
import server.connection.database.DatabaseEngine;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MySqlDatabaseClientHandler extends DatabaseClientHandler {
    
    public MySqlDatabaseClientHandler(Socket clientSocket, DatabaseEngine databaseEngine) { super(clientSocket, databaseEngine); }

    @Override
    public void run() {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            String line;
            while ((line = in.readLine()) != null) {

                if (line.trim().isEmpty()) continue;
                try {

                    MySqlQuery query = parseRequest(line);
                    var response = query.execute(getDatabaseEngine());
                    out.println(response.serialize());
                } catch (Exception e) { out.println("ERROR: " + e.getMessage()); }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    protected MySqlQuery parseRequest(String input) {

        String sql = input.trim().toUpperCase();
        MySqlQuery query = new MySqlQuery();
        
        boolean matched = false;
        for (QueryType type : QueryType.values()) if (sql.startsWith(type.getSqlKeyword())) {

            query.setRawSql(input);
            query.setQueryType(type);
            matched = true;
            
            switch (type) {
                case SELECT -> parseSelectQuery(input, query);
                case INSERT -> parseInsertQuery(input, query);
                case UPDATE -> parseUpdateQuery(input, query);
                case DELETE -> parseDeleteQuery(input, query);
                case CREATE -> parseCreateQuery(input, query);
                case DROP -> parseDropQuery(input, query);
                default -> {} // Other types can be implemented similarly
            }
            break;
        }
        
        if (!matched) throw new IllegalArgumentException("Unsupported SQL query: " + input);
        return query;
    }
    
    private void parseSelectQuery(String sql, MySqlQuery query) {
        
        // Simple regex-based parsing
        Pattern fromPattern = Pattern.compile("FROM\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher fromMatcher = fromPattern.matcher(sql);
        if (fromMatcher.find()) query.setTableName(fromMatcher.group(1));
        
        // Parse columns (between SELECT and FROM)
        Pattern colPattern = Pattern.compile("SELECT\\s+(.+?)\\s+FROM", Pattern.CASE_INSENSITIVE);
        Matcher colMatcher = colPattern.matcher(sql);
        if (colMatcher.find()) {

            String columnsStr = colMatcher.group(1).trim();
            List<String> columns = new ArrayList<>();
            for (String col : columnsStr.split(",")) columns.add(col.trim());
            query.setColumns(columns);
        }
        
        // Parse WHERE clause
        Pattern wherePattern = Pattern.compile("WHERE\\s+(.+?)(?:ORDER BY|LIMIT|$)", Pattern.CASE_INSENSITIVE);
        Matcher whereMatcher = wherePattern.matcher(sql);
        if (whereMatcher.find()) query.setWhereClause(whereMatcher.group(1).trim());
    }
    
    private void parseInsertQuery(String sql, MySqlQuery query) {
        
        // INSERT INTO table (col1, col2) VALUES (val1, val2)
        Pattern tablePattern = Pattern.compile("INSERT\\s+INTO\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTableName(tableMatcher.group(1));
        
        // Parse columns
        Pattern colPattern = Pattern.compile("\\((.*?)\\)\\s*VALUES", Pattern.CASE_INSENSITIVE);
        Matcher colMatcher = colPattern.matcher(sql);
        if (colMatcher.find()) {

            String columnsStr = colMatcher.group(1);
            List<String> columns = new ArrayList<>();
            for (String col : columnsStr.split(",")) columns.add(col.trim());
            query.setColumns(columns);
        }
        
        // Parse values
        Pattern valPattern = Pattern.compile("VALUES\\s*\\((.*?)\\)", Pattern.CASE_INSENSITIVE);
        Matcher valMatcher = valPattern.matcher(sql);
        if (valMatcher.find()) {

            String valuesStr = valMatcher.group(1);
            Map<String, Object> values = new HashMap<>();
            String[] vals = valuesStr.split(",");
            List<String> columns = query.getColumns();
            for (int i = 0; i < vals.length && i < columns.size(); i++) {

                String val = vals[i].trim().replaceAll("^'|'$", "");
                values.put(columns.get(i), val);
            }
            query.setValues(values);
        }
    }
    
    private void parseUpdateQuery(String sql, MySqlQuery query) {
        
        // UPDATE table SET col1=val1, col2=val2 WHERE condition
        Pattern tablePattern = Pattern.compile("UPDATE\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTableName(tableMatcher.group(1));
        
        // Parse SET clause
        Pattern setPattern = Pattern.compile("SET\\s+(.+?)(?:WHERE|$)", Pattern.CASE_INSENSITIVE);
        Matcher setMatcher = setPattern.matcher(sql);
        if (setMatcher.find()) {
            String setClause = setMatcher.group(1).trim();
            Map<String, Object> values = new HashMap<>();
            for (String assignment : setClause.split(",")) {
                String[] parts = assignment.split("=");
                if (parts.length == 2) {
                    String col = parts[0].trim();
                    String val = parts[1].trim().replaceAll("^'|'$", "");
                    values.put(col, val);
                }
            }
            query.setValues(values);
        }
        
        // Parse WHERE clause
        Pattern wherePattern = Pattern.compile("WHERE\\s+(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher whereMatcher = wherePattern.matcher(sql);
        if (whereMatcher.find()) {
            query.setWhereClause(whereMatcher.group(1).trim());
        }
    }
    
    private void parseDeleteQuery(String sql, MySqlQuery query) {
        
        // DELETE FROM table WHERE condition
        Pattern tablePattern = Pattern.compile("DELETE\\s+FROM\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTableName(tableMatcher.group(1));
        
        // Parse WHERE clause
        Pattern wherePattern = Pattern.compile("WHERE\\s+(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher whereMatcher = wherePattern.matcher(sql);
        if (whereMatcher.find()) query.setWhereClause(whereMatcher.group(1).trim());
    }
    
    private void parseCreateQuery(String sql, MySqlQuery query) {
        
        // CREATE TABLE table_name [(col1, col2, ...)]
        Pattern tablePattern = Pattern.compile("CREATE\\s+TABLE\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTableName(tableMatcher.group(1));
        
        // Parse columns if specified
        Pattern colPattern = Pattern.compile("\\((.*?)\\)", Pattern.CASE_INSENSITIVE);
        Matcher colMatcher = colPattern.matcher(sql);
        if (colMatcher.find()) {
            String columnsStr = colMatcher.group(1);
            List<String> columns = new ArrayList<>();
            for (String col : columnsStr.split(",")) {
                columns.add(col.trim());
            }
            query.setColumns(columns);
        }
    }
    
    private void parseDropQuery(String sql, MySqlQuery query) {
        
        // DROP TABLE table_name
        Pattern tablePattern = Pattern.compile("DROP\\s+TABLE\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher tableMatcher = tablePattern.matcher(sql);
        if (tableMatcher.find()) query.setTableName(tableMatcher.group(1));
    }
}
