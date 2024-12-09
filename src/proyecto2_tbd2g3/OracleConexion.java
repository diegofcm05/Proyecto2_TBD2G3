package proyecto2_tbd2g3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

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
    
    public boolean Conectar(String url, String user, String password, String port){//Agregar los parametros de url, puerto, user y password, asi como esta en la clase de mysql
        boolean success = false;
        try {
            
            ogurl = url;
            oguser = user;
            ogpassword = password;
            ogport = port;
            
            String fullurl = url+":"+port+":"+"XE"; 
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
            
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
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
    
    
    
    
}
