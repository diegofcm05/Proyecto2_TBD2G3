/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_tbd2g3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class OracleConexion {
    
    private String url = "jdbc:oracle:thin:@localhost:1521:XE"; // Reemplaza con tu URL de conexión
    private String user = "system"; // Reemplaza con tu usuario
    private String password = "18273645"; // Reemplaza con tu contraseña
    
    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;
    
    public void Conectar(){//Agregar los parametros de url, puerto, user y password, asi como esta en la clase de mysql
        try {
            // Conexión a la base de datos
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Conexión exitosa a la base de datos Oracle!");

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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Cerrar recursos
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        
        
    }
    
}
