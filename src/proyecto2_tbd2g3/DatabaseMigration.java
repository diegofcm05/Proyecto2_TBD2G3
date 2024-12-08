import java.sql.*;

public class DatabaseMigration {

    private String mysqlUrl;
    private String mysqlUser;
    private String mysqlPass;

    private String oracleUrl;
    private String oracleUser;
    private String oraclePass;

    private String tableName;

    public DatabaseMigration(String mysqlUrl, String mysqlUser, String mysqlPass,
                             String oracleUrl, String oracleUser, String oraclePass,
                             String tableName) {
        this.mysqlUrl = mysqlUrl;
        this.mysqlUser = mysqlUser;
        this.mysqlPass = mysqlPass;
        this.oracleUrl = oracleUrl;
        this.oracleUser = oracleUser;
        this.oraclePass = oraclePass;
        this.tableName = tableName;
    }

    // Conectar a MySQL
    private Connection connectToMySQL() throws SQLException {
        return DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
    }

    // Conectar a Oracle
    private Connection connectToOracle() throws SQLException {
        return DriverManager.getConnection(oracleUrl, oracleUser, oraclePass);
    }

    // Migracion de la tabla
    public void migrateTable() {
        Connection mysqlConn = null;
        Connection oracleConn = null;
        PreparedStatement oracleInsertStmt = null;
        Statement mysqlSelectStmt = null;
        ResultSet mysqlResultSet = null;

        try {
            // Establecer conexiones
            mysqlConn = connectToMySQL();
            System.out.println("Conectado a MySQL");

            oracleConn = connectToOracle();
            System.out.println("Conectado a Oracle");

            // Leer datos de MySQL
            String mysqlQuery = "SELECT * FROM " + tableName;
            mysqlSelectStmt = mysqlConn.createStatement();
            mysqlResultSet = mysqlSelectStmt.executeQuery(mysqlQuery);

            // INSERT a Oracle
            ResultSetMetaData metaData = mysqlResultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            StringBuilder oracleInsertQuery = new StringBuilder("INSERT INTO " + tableName + " VALUES (");
            for (int i = 1; i <= columnCount; i++) {
                oracleInsertQuery.append("?");
                if (i < columnCount) oracleInsertQuery.append(", ");
            }
            oracleInsertQuery.append(")");

            oracleInsertStmt = oracleConn.prepareStatement(oracleInsertQuery.toString());

            // Migrar datos
            int rowCount = 0;
            while (mysqlResultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    oracleInsertStmt.setObject(i, mysqlResultSet.getObject(i));
                }
                oracleInsertStmt.addBatch();
                rowCount++;
            }
            oracleInsertStmt.executeBatch();
            System.out.println("Migracion completa. Total de filas migradas: " + rowCount);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Cerrar recursos
            try {
                if (mysqlResultSet != null) mysqlResultSet.close();
                if (mysqlSelectStmt != null) mysqlSelectStmt.close();
                if (oracleInsertStmt != null) oracleInsertStmt.close();
                if (mysqlConn != null) mysqlConn.close();
                if (oracleConn != null) oracleConn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}