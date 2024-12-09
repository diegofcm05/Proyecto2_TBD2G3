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
            //prueba
            filterGeneralLog(url, User, Password);
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
    public static void filterGeneralLog(String jdbcUrl, String username, String password) {
        String query = "SELECT event_time, user_host, command_type, argument " +
                       "FROM mysql.general_log " +
                       "WHERE command_type = 'Query' " +
                       "ORDER BY event_time DESC";
        
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("Filtered General Log Entries:");
            System.out.println("---------------------------------------");

            while (rs.next()) {
                String eventTime = rs.getString("event_time");
                String userHost = rs.getString("user_host");
                String commandType = rs.getString("command_type");
                String argument = rs.getString("argument");

                // Filtrar consultas ocultas o internas
                if (isInternalQuery(argument)) {
                    System.out.println("Time: " + eventTime);
                    System.out.println("User: " + userHost);
                    System.out.println("Query: " + argument);
                    System.out.println("---------------------------------------");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para identificar consultas internas que queremos ignorar
    private static boolean isInternalQuery(String argument) {
        boolean bien=false;
        boolean good=false;
        if(argument.startsWith("INSERT") || 
               argument.startsWith("DELETE") || 
               argument.startsWith("CREATE") || 
               argument.startsWith("UPDATE")){
            bien=true;
        }
        if(!argument.startsWith("INSERT INTO mysql.rds")&&!argument.startsWith("DELETE FROM mysql.rds")){
            good=true;
        }
        return bien && good;
    }
    
    // Método para adaptar el DDL de Oracle a MySQL
    public String adaptDDLForMySQL(String ddl) {
        // Aquí se pueden agregar más adaptaciones si es necesario
        // Ejemplo de reemplazo de tipos de datos específicos de Oracle a MySQL
        ddl = ddl.replace("NUMBER", "INT");  // Reemplazar NUMBER por INT
        ddl = ddl.replace("VARCHAR2", "VARCHAR");  // Reemplazar VARCHAR2 por VARCHAR
        ddl = ddl.replace("DATE", "DATETIME");  // Reemplazar DATE por DATETIME (si es necesario)

        // Agregar cualquier otro reemplazo o ajuste de sintaxis aquí
        return ddl;
    }

    // Método para crear la tabla en MySQL
    public void createTableInMySQL(String ddl) {
        try {
            Statement stmt = connection.createStatement();

            // Ejecutar el DDL adaptado para MySQL
            stmt.executeUpdate(ddl);  // Ejecutar el DDL en MySQL
            System.out.println("Tabla creada en MySQL exitosamente.");

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
