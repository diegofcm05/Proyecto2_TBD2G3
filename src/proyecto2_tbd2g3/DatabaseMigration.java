import java.sql.*;

public class DatabaseMigration {

    private String mysql_url = "jdbc:mysql://";
    private String mysql_user;
    private String mysql_pass;

    private String oracle_url = "";
    private String oracle_user;
    private String oracle_pass;

    private String tableName;

    public void migrateTable_MySQL_to_Oracle() {
        Connection mysqlConn = null;
        Connection oracleConn = null;
        PreparedStatement oracleInsertStmt = null;
        Statement mysqlSelectStmt = null;
        ResultSet mysqlResultSet = null;
        Statement oracleCreateStmt = null;

        try {
            mysqlConn = DriverManager.getConnection(this.mysql_url, this.mysql_user, this.mysql_pass);
            System.out.println("Conectado a MySQL");

            oracleConn = DriverManager.getConnection(this.oracle_url, this.oracle_user, this.oracle_pass);
            System.out.println("Conectado a Oracle");

            String mysqlQuery = "SELECT * FROM " + this.tableName;
            mysqlSelectStmt = mysqlConn.createStatement();
            mysqlResultSet = mysqlSelectStmt.executeQuery(mysqlQuery);

            ResultSetMetaData metaData = mysqlResultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            StringBuilder createTableQuery = new StringBuilder("CREATE TABLE " + this.tableName + " (");

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                String columnType = metaData.getColumnTypeName(i).toUpperCase();
                columnType = convertMySQLTypeToOracleType(columnType);
                createTableQuery.append(columnName + " " + columnType);
                if (i < columnCount) {
                    createTableQuery.append(", ");
                }
            }

            createTableQuery.append(")");

            Statement oracleCheckStmt = oracleConn.createStatement();
            ResultSet oracleCheckResultSet = oracleCheckStmt.executeQuery("SELECT COUNT(*) FROM all_tables WHERE table_name = '" + this.tableName.toUpperCase() + "'");

            if (oracleCheckResultSet.next() && oracleCheckResultSet.getInt(1) == 0) {
                oracleCreateStmt = oracleConn.createStatement();
                oracleCreateStmt.executeUpdate(createTableQuery.toString());
                System.out.println("Tabla " + this.tableName + " creada en Oracle.");
            } else {
                System.out.println("La tabla " + this.tableName + " ya existe en Oracle.");
            }

            String primaryKeyQuery = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE "
                    + "WHERE TABLE_NAME = '" + this.tableName + "' AND CONSTRAINT_NAME = 'PRIMARY'";
            Statement primaryKeyStmt = mysqlConn.createStatement();
            ResultSet primaryKeyResultSet = primaryKeyStmt.executeQuery(primaryKeyQuery);

            StringBuilder primaryKeyBuilder = new StringBuilder();
            while (primaryKeyResultSet.next()) {
                if (primaryKeyBuilder.length() > 0) {
                    primaryKeyBuilder.append(", ");
                }
                primaryKeyBuilder.append(primaryKeyResultSet.getString("COLUMN_NAME"));
            }

            if (primaryKeyBuilder.length() > 0) {
                String createPrimaryKeyQuery = "ALTER TABLE " + this.tableName + " ADD CONSTRAINT " 
                        + this.tableName + "_PK PRIMARY KEY (" + primaryKeyBuilder.toString() + ")";
                oracleCreateStmt.executeUpdate(createPrimaryKeyQuery);
                System.out.println("Clave primaria creada en Oracle");
            }

            String foreignKeyQuery = "SELECT COLUMN_NAME, CONSTRAINT_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME "
                    + "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE "
                    + "WHERE TABLE_NAME = '" + this.tableName + "' AND REFERENCED_TABLE_NAME IS NOT NULL";
            Statement foreignKeyStmt = mysqlConn.createStatement();
            ResultSet foreignKeyResultSet = foreignKeyStmt.executeQuery(foreignKeyQuery);

            while (foreignKeyResultSet.next()) {
                String columnName = foreignKeyResultSet.getString("COLUMN_NAME");
                String constraintName = foreignKeyResultSet.getString("CONSTRAINT_NAME");
                String referencedTable = foreignKeyResultSet.getString("REFERENCED_TABLE_NAME");
                String referencedColumn = foreignKeyResultSet.getString("REFERENCED_COLUMN_NAME");

                String createForeignKeyQuery = "ALTER TABLE " + this.tableName + " ADD CONSTRAINT " 
                        + this.tableName + "_" + constraintName + "_FK FOREIGN KEY (" + columnName + ") "
                        + "REFERENCES " + referencedTable + "(" + referencedColumn + ")";
                
                oracleCreateStmt.executeUpdate(createForeignKeyQuery);
                System.out.println("Clave foránea creada en Oracle para " + columnName);
            }

            StringBuilder oracleInsertQuery = new StringBuilder("INSERT INTO " + this.tableName + " VALUES (");

            for (int i = 1; i <= columnCount; i++) {
                oracleInsertQuery.append("?");  
                if (i < columnCount) {
                    oracleInsertQuery.append(", ");
                }
            }
            oracleInsertQuery.append(")");

            oracleInsertStmt = oracleConn.prepareStatement(oracleInsertQuery.toString());

            int rowCount = 0;
            while (mysqlResultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    oracleInsertStmt.setObject(i, mysqlResultSet.getObject(i));
                }
                oracleInsertStmt.addBatch();
                rowCount++;
            }
            oracleInsertStmt.executeBatch();
            System.out.println("Migración completa. Total de filas migradas: " + rowCount);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (mysqlResultSet != null) mysqlResultSet.close();
                if (mysqlSelectStmt != null) mysqlSelectStmt.close();
                if (oracleInsertStmt != null) oracleInsertStmt.close();
                if (oracleCreateStmt != null) oracleCreateStmt.close();
                if (mysqlConn != null) mysqlConn.close();
                if (oracleConn != null) oracleConn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private String convertMySQLTypeToOracleType(String mysqlType) {
        switch (mysqlType) {
            case "VARCHAR":
            case "CHAR":
                return "VARCHAR2";
            case "TEXT":
                return "CLOB";
            case "INT":
            case "INTEGER":
                return "NUMBER";
            case "DECIMAL":
            case "FLOAT":
                return "NUMBER";
            case "DATE":
            case "DATETIME":
                return "DATE";
            case "TINYINT":
                return "NUMBER(1)";
            default:
                return mysqlType;
        }
    }
}
