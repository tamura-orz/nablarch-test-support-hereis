package nablarch.test.support.tool;

import java.sql.*;

public class
Rdb
{
    public static void
    connectTo (String url)
    {
        try {
            connect(DriverManager.getConnection(url));
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void
    connect (Connection conn)
    {
        connectionPerThread.set(conn);
    }

    public static Connection
    connection ()
    {
        return connectionPerThread.get();
    }
    
    static final ThreadLocal<Connection>
    connectionPerThread = new ThreadLocal<Connection>();


    public static TableMetaData
    table (String tableName)
    {
        Connection conn = connection();
        if (conn == null) {
            throw new RuntimeException (
                "There is no db connection to use."
            );
        }
        return new TableMetaData(tableName);
    }
 
    public static class
    TableMetaData
    {
        public
        TableMetaData (String name)
        { 
            this.name = name;
        }
        final String name;

        public boolean
        exists ()
        {
            ResultSet meta = null;
            try {
                meta = connection()
                       .getMetaData()
                       .getTables (null, null, name.toUpperCase(), null);
                return meta.next();
            }
            catch (SQLException e) {throw new RuntimeException(e);}
            finally {
                try {meta.close();} catch(SQLException e) {}
            }
        }

        public ColumnMetaData
        column (String columnName)
        {
            return new ColumnMetaData(columnName);
        }
    }

    public static class
    ColumnMetaData
    {
        public
        ColumnMetaData (String name)
        {
            this.name = name;
        }
        final String name;

        public boolean
        exists ()
        {
            return true; 
        }
    }
}
