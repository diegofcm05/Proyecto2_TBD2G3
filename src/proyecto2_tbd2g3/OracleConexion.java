package proyecto2_tbd2g3;

import java.security.Timestamp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 *
 * @author dfcm9
 */
public class OracleConexion {

    private String ogurl = "jdbc:oracle:thin:@localhost:1521:XE"; // Reemplaza con tu URL de conexión
    private String oguser = "system"; // Reemplaza con tu usuario
    private String ogpassword = "18273645"; // Reemplaza con tu contraseña
    private String ogport = "1521";

    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;

    public boolean Conectar(String url, String user, String password, String port) {//Agregar los parametros de url, puerto, user y password, asi como esta en la clase de mysql
        boolean success = false;
        try {

            ogurl = url;
            oguser = user;
            ogpassword = password;
            ogport = port;

            String fullurl = url + ":" + port + ":" + "XE";
            // Conexión a la base de datos
            connection = DriverManager.getConnection(fullurl, user, password);
            success = true;
            System.out.println("Conexión exitosa a la base de datos Oracle!");

            /*
            //Codigo que funciona como prueba de una query
            
            
            // Crear la consulta
            String query = "SELECT IDNO, LASTNAME, FIRSTNAME, USER_NAME, USER_PASSWORD FROM USERLOGIN";

            // Ejecutar la consulta
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            // Mostrar los resultados
            System.out.println("Resultados de la tabla USERLOGIN:");
            while (resultSet.next()) {
                //Estas son las columnas de la tabla
                int idno = resultSet.getInt("IDNO");
                String lastName = resultSet.getString("LASTNAME");
                String firstName = resultSet.getString("FIRSTNAME");
                String userName = resultSet.getString("USER_NAME");
                String userPassword = resultSet.getString("USER_PASSWORD");

                System.out.println("ID: " + idno + 
                                   ", Apellido: " + lastName + 
                                   ", Nombre: " + firstName + 
                                   ", Usuario: " + userName + 
                                   ", Contraseña: " + userPassword);
            }
             */
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            /*
            // Cerrar recursos
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
             */
        }

        return success;
    }

    public List<String> getNombresTabla() {
        List<String> tableNames = new ArrayList<>();
        try {
            // Verificar si hay conexión activa
            if (connection == null || connection.isClosed()) {
                throw new SQLException("No hay una conexión activa con la base de datos.");
            }

            // Consulta para obtener los nombres de las tablas
            String sql = "SELECT table_name FROM user_tables ORDER BY table_name";

            try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
                // Agregar los nombres de las tablas a la lista
                while (resultSet.next()) {
                    tableNames.add(resultSet.getString("table_name"));
                }
            }
            System.out.println("Nombres de tablas obtenidos exitosamente.");
        } catch (SQLException e) {
            System.err.println("Error al obtener los nombres de las tablas: " + e.getMessage());
        }
        return tableNames;
    }

    public String Ingreso(String table_name) {
        String ddlResult = null;

        try {
            // Usar PreparedStatement para obtener el DDL de la tabla en Oracle
            String query = "SELECT DBMS_METADATA.GET_DDL('TABLE', ?) FROM DUAL";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, table_name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                ddlResult = rs.getString(1);  // Obtener el DDL de Oracle
                System.out.println("DDL de Oracle: \n" + ddlResult);
            }

            rs.close();
            pstmt.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return ddlResult;
    }

    public String getBitacoraCompleta() {
        StringBuilder bitacora = new StringBuilder();
        String sql = "SELECT us_id, tabla, operacion, dato, hora FROM bitacora";

        try {

            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

                // Iterar sobre los resultados y construir el string
                while (rs.next()) {
                    int usId = rs.getInt("us_id");
                    String tabla = rs.getString("tabla");
                    String operacion = rs.getString("operacion");
                    String dato = rs.getString("dato");
                    String hora = rs.getString("hora");

                    bitacora.append(String.format("Usuario: %d, Tabla: %s, Operación: %s, Dato: %s, Hora: %s%n",
                            usId, tabla, operacion, dato, hora));
                }
            }

            System.out.println("Bitácora obtenida exitosamente.");
        } catch (SQLException e) {
            System.err.println("Error al obtener la bitácora completa: " + e.getMessage());
        }

        return bitacora.toString();
    }
    
    
    public String translateQuery(String mysqlQuery) {
        
        // Identificar el tipo de consulta y procesarla
        if (mysqlQuery.startsWith("CREATE")) {
            return translateCreateQuery(mysqlQuery);
        } else if (mysqlQuery.startsWith("INSERT")) {
            return translateInsertQuery(mysqlQuery);
        } else if (mysqlQuery.startsWith("UPDATE")) {
            return translateUpdateQuery(mysqlQuery);
        } else if (mysqlQuery.startsWith("DELETE")) {
            return translateDeleteQuery(mysqlQuery);
        }
        // Si no es ninguna de las anteriores, retornamos la query original (o lanzamos un error)
        return mysqlQuery;
    }

    private String translateCreateQuery(String mysqlQuery) {
        // Básica traducción para `CREATE TABLE`
        // Adaptar tipos de datos, AUTO_INCREMENT y engine-specific properties
        String oracleQuery = mysqlQuery
                .replace("AUTO_INCREMENT", "GENERATED BY DEFAULT AS IDENTITY") // MySQL a Oracle
                .replace("ENGINE=InnoDB", "") // Oracle no utiliza ENGINE
                .replace("TINYINT", "NUMBER(1)") // Ejemplo: TINYINT a NUMBER(1)
                .replace("DATETIME", "TIMESTAMP") // MySQL usa DATETIME, Oracle usa TIMESTAMP
                .replace("TEXT", "CLOB") // MySQL usa TEXT, Oracle usa CLOB
                .replace("CHARSET=utf8mb4", ""); // Charset no es necesario en Oracle

        return oracleQuery;
    }

    private String translateInsertQuery(String mysqlQuery) {
        // Los `INSERT` suelen ser compatibles, pero aseguramos que las comillas simples estén bien formateadas
        return mysqlQuery.replace("`", "\""); // Cambiar comillas invertidas (MySQL) a dobles (Oracle)
    }

    private String translateUpdateQuery(String mysqlQuery) {
        // Los `UPDATE` suelen ser compatibles directamente entre MySQL y Oracle
        return mysqlQuery.replace("`", "\""); // Cambiar comillas invertidas (MySQL) a dobles (Oracle)
    }

    private String translateDeleteQuery(String mysqlQuery) {
        // Los `DELETE` suelen ser compatibles directamente entre MySQL y Oracle
        return mysqlQuery.replace("`", "\""); // Cambiar comillas invertidas (MySQL) a dobles (Oracle)
    }
    
    
    
    public boolean ejecutarQuery(String query) {
    boolean success = false;
    try (Connection conn = DriverManager.getConnection(ogurl, oguser, ogpassword);
         Statement stmt = conn.createStatement()) {
        
        // Ejecutar la consulta
        stmt.execute(query);
        success = true;
        System.out.println("Query ejecutada con éxito: " + query);

    } catch (SQLException e) {
        System.err.println("Error al ejecutar la query: " + query);
        e.printStackTrace();
    }
    return success;
}
    
    
    

}
