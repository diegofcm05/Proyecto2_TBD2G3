/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_tbd2g3;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Junior
 */
public class ConexionMySQL {
    static String url="jdbc:mysql://";
    static String urlOG="jdbc:mysql://tbd2g3.c1ci2a0qwno2.us-east-1.rds.amazonaws.com:3306/tbd2g3";
    private static Connection connection=null;
    
    public static boolean conectar(String Url,String User,String Password,String Port,String NombreDB){
        boolean conectado=false;
        try {
            url="jdbc:mysql://"+Url+":"+Port+"/"+NombreDB;
            connection= DriverManager.getConnection(url, User, Password);
            conectado=true;
            JOptionPane.showMessageDialog(null, "Se ha conectado correctamente con la Base de Datos");
        } catch (SQLException e) {
            e.printStackTrace();
            conectado=false;
            JOptionPane.showMessageDialog(null, "Ha ocurrido un error con la conexion");
        }
        return conectado;
    }
    
    public List<String> getNombresTabla(String databaseName) {
        List<String> tableNames = new ArrayList<>();
        try {
            // Verificar si hay conexión activa
            if (connection == null || connection.isClosed()) {
                throw new SQLException("No hay una conexión activa con la base de datos.");
            }

            // Consulta para obtener los nombres de las tablas
            String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = ? ORDER BY table_name";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Establecer el nombre de la base de datos en el parámetro
                statement.setString(1, databaseName);

                try (ResultSet resultSet = statement.executeQuery()) {
                    // Agregar los nombres de las tablas a la lista
                    while (resultSet.next()) {
                        tableNames.add(resultSet.getString("table_name"));
                    }
                }
            }
            System.out.println("Nombres de tablas obtenidos exitosamente.");
        } catch (SQLException e) {
            System.err.println("Error al obtener los nombres de las tablas: " + e.getMessage());
        }
        return tableNames;
    }
}
