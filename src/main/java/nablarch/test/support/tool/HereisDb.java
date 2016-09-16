package nablarch.test.support.tool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class
HereisDb {

    public static int
    table (Connection conn, Object... embedParams) 
    { assert conn != null;
        // Very ugly... must be rewritten.

        int result = 0;
        JvmFrame callerFrame = JvmFrame.getCallerFrame();
        String literal = Hereis.__string(callerFrame, 1, embedParams);
        Scanner s = new Scanner(literal);

        while (s.hasNextLine()) {
            String tableName = null;
            StringBuilder buff = new StringBuilder();

            while (s.hasNextLine()) {
                String line = s.nextLine();
                if (line.trim().length() == 0) continue;
                Matcher m = identifier.matcher(line);
                if (!m.find()) {
                    throw new RuntimeException(
                      "The name of the table to which you want to insert "
                    + "records must be specified."
                    );
                }
                tableName = m.group().trim();
                break;
            }

            while (s.hasNextLine()) {
                String line = s.nextLine();
                if (line.trim().length() == 0) continue;
                if (!barLine.matcher(line).matches()) {
                    throw new RuntimeException(
                      "A table name must be followed by a separator line. "
                    + "(like \"=====+\")."
                    );
                }
                break;
            }

            while (s.hasNextLine()) {
                String line = s.nextLine();
                if (line.trim().length() == 0) {
                    break; // Having read a blank line, go to the next table.
                }
                buff.append(line).append("\n");
            }

            Table table = new Table(buff.toString());
            String cols = Builder.join(table.cols(), ", ");
            Statement stmt = null;
            try {
                // 事前にテーブルデータを削除
                final Statement del = conn.createStatement();
                del.executeUpdate("delete from " + tableName);
                conn.commit();
                del.close();
                stmt = conn.createStatement();
                for (Map<String, Object> row : table.rows()) {
                    List<Object> values = new ArrayList<Object>();

                    ResultSet metaData = conn.getMetaData().getColumns(
                            null, null, tableName.toUpperCase(), null
                    );

                    Set stringTypeColumns = new HashSet();
                    while (metaData.next()) {
                        int sqlType = metaData.getInt("DATA_TYPE");
                        switch (sqlType) {
                            case Types.CHAR:
                            case Types.VARCHAR:
                            case Types.LONGVARCHAR:
                            case Types.DATE:
                            case Types.TIMESTAMP:
                            /* @since JDK1.6
                            case Types.NCHAR:
                            case Types.NVARCHAR:
                            case Types.LONGNVARCHAR:
                            */
                                stringTypeColumns.add(
                                        metaData.getString("COLUMN_NAME").toUpperCase()
                                );
                                break;
                            default: // Nothing to do.
                        }
                    }
                    
                    metaData.close();
                    
                    for (Table.Column col : table.cols()) {
                        String val = row.get(col.getName()).toString();
                        if (stringTypeColumns.contains(
                                col.getName().toUpperCase())) {
                            if (!val.equalsIgnoreCase("null")) {
                                val = "'" + val + "'";
                            }
                        }
                        values.add(val);
                    }
                    String insertCommand
                            = "INSERT INTO " + tableName + " ("
                            + cols
                            + ") VALUES ("
                            + Builder.join(values, ", ")
                            + ")";
                    stmt.addBatch(insertCommand);
                    result++;
                }
                stmt.executeBatch();
            }
            catch (SQLException sqle) {
                throw new RuntimeException(sqle);
            }
            finally {
                try {
                    stmt.close();
                } catch (Exception ex) {
                }
            }
        }
        return result;
    }

    static Pattern identifier = Pattern.compile (
        "\\b[_a-zA-Z][_a-zA-Z0-9]*\\b"
    );
    static Pattern barLine = Pattern.compile (
        "^\\s*(-+|=+)\\s*$"
    );

    public static int
    sql (Connection conn, Object... embedParams)
    { assert conn != null;

        int result = 0;
        JvmFrame callerFrame = JvmFrame.getCallerFrame();
        String sql = Hereis.__string(callerFrame, 1, embedParams).trim();
        return __sql(conn, sql);
    }

    public static int
    sql (Object... embedParams)
    {
        Connection conn = Rdb.connection();
        if (conn == null) {
            throw new RuntimeException (
            "There is no db connection to use."
            );
        }
        JvmFrame callerFrame = JvmFrame.getCallerFrame();
        String sql = Hereis.__string(callerFrame, 0, embedParams).trim();
        return __sql(conn, sql);
    }

    static int
    __sql (Connection conn, String sql) 
    { assert conn != null;
        int result = 0;
        Scanner statements = new Scanner(sql).useDelimiter(";");
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            while (statements.hasNext()) {
                stmt.addBatch(statements.next());
                result++;
            }
            stmt.executeBatch();
        }
        catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
        finally {
            try {
                stmt.close();
            } catch (Exception e) {/* Nothing to do. */}
        }
        return result;
    }


    public static List<Map<String, Object>>
    query (Object... embedParams)
    {
        Connection conn = Rdb.connection();
        if (conn == null) {
            throw new RuntimeException (
            "There is no db connection to use."
            );
        }
        JvmFrame callerFrame = JvmFrame.getCallerFrame();
        String sql = Hereis.__string(callerFrame, 0, embedParams);
        return __query(conn, sql);
    }


    public static List<Map<String, Object>>
    query (Connection conn, Object... embedParams)
    {
        JvmFrame callerFrame = JvmFrame.getCallerFrame();
        String sql = Hereis.__string(callerFrame, 1, embedParams);
        return __query(conn, sql);
    }


    static List<Map<String, Object>>
    __query (Connection conn, String sql)
    { assert conn != null;
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        ResultSet resultSet = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);

            ResultSetMetaData meta = resultSet.getMetaData();
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<String, Object>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    Object val = resultSet.getObject(i);
                    if (val instanceof java.lang.String) {
                        val = val.toString().trim();
                    }
                    row.put(meta.getColumnName(i), val);
                }
                rows.add(row);
            }
        }
        catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
        finally {
            try {
                stmt.close();
            } catch (Exception e) {/* Nothing to do. */}
        }
        return rows;
    }

    public static List<Map<String, Object>>
    prepare (Connection conn, Object... embedParams)
    { assert conn != null;
        return new ArrayList<Map<String, Object>>(); 
    }
}
