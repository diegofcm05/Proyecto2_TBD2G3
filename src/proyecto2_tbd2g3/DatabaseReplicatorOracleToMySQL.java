package proyecto2_tbd2g3;

import java.sql.*;
import javax.swing.JLabel;

public class DatabaseReplicatorOracleToMySQL {

    private Connection originConnection; // Conexión a Oracle
    private Connection destinationConnection; // Conexión a MySQL

    // Conexión a la base de datos de origen (Oracle)
    public void connectToOrigin(String host, String user, String pass, String port, String dbName) throws SQLException {
        String url = "jdbc:oracle:thin:@oracledb.cv8gq8u2msgm.us-east-1.rds.amazonaws.com:1521:orcl";
        originConnection = DriverManager.getConnection(url, user, pass);
        System.out.println("Conexión establecida con la base de datos Oracle de origen.");
    }

    // Conexión a la base de datos de destino (MySQL)
    public void connectToDestination(String host, String user, String pass, String port, String dbName) throws SQLException {
        String url = "jdbc:mysql://tbd2g3.c1ci2a0qwno2.us-east-1.rds.amazonaws.com:3306/tbd2g3";
        destinationConnection = DriverManager.getConnection(url, user, pass);
        System.out.println("Conexión establecida con la base de datos MySQL de destino.");
    }

    // Método para replicar de Oracle a MySQL
    public void replicateOriginToDestination() throws SQLException {
        replicate("bitacora_origen", originConnection, destinationConnection);
    }

    // Lógica genérica de replicación
    private void replicate(String bitacoraTable, Connection source, Connection target) throws SQLException {
        Statement sourceStmt = null;
        ResultSet resultSet = null;

        try {
            sourceStmt = originConnection.createStatement();
            resultSet = sourceStmt.executeQuery("SELECT * FROM " + bitacoraTable);

            while (resultSet.next()) {
                String table = resultSet.getString("tabla");
                String operation = resultSet.getString("operacion");
                String data = resultSet.getString("dato");

                processOperation(target, table, operation, data);
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (sourceStmt != null) {
                sourceStmt.close();
            }
        }
    }

    // Método para procesar las operaciones
    private void processOperation(Connection target, String table, String operation, String data) throws SQLException {
        Statement targetStmt = null;

        try {
            // Convierte nombres de tabla y columnas para que sean compatibles con MySQL
            table = table.replace("\"", ""); // Elimina comillas dobles
            data = data.replace("\"", ""); // Elimina comillas dobles en los datos

            targetStmt = target.createStatement();

            switch (operation.toUpperCase()) {
                case "INSERT":
                    String insertQuery = "INSERT INTO " + table + " VALUES (" + data + ")";
                    targetStmt.executeUpdate(insertQuery);
                    System.out.println("Insertado en " + table + ": " + data);
                    break;

                case "UPDATE":
                    String updateQuery = "UPDATE " + table + " SET " + data;
                    targetStmt.executeUpdate(updateQuery);
                    System.out.println("Actualizado en " + table + ": " + data);
                    break;

                case "DELETE":
                    String deleteQuery = "DELETE FROM " + table + " WHERE " + data;
                    targetStmt.executeUpdate(deleteQuery);
                    System.out.println("Eliminado en " + table + " con condición: " + data);
                    break;

                default:
                    System.out.println("Operación no reconocida: " + operation);
                    break;
            }
        } finally {
            if (targetStmt != null) {
                targetStmt.close();
            }
        }
    }

    // Método para cerrar las conexiones
    public void closeConnections() {
        try {
            if (originConnection != null && !originConnection.isClosed()) {
                originConnection.close();
                System.out.println("Conexión a la base de datos Oracle cerrada.");
            }
            if (destinationConnection != null && !destinationConnection.isClosed()) {
                destinationConnection.close();
                System.out.println("Conexión a la base de datos MySQL cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexiones: " + e.getMessage());
        }
    }

    // Métodos para actualizar el estado de las conexiones en las etiquetas de la interfaz
    public void updateConnectionStatus(boolean isOriginConnected, boolean isDestinationConnected, JLabel lblConexion1, JLabel lblConexion2) {
        if (isOriginConnected) {
            lblConexion1.setText("Conexión con Oracle exitosa");
        } else {
            lblConexion1.setText("Conexión con Oracle fallida");
        }

        if (isDestinationConnected) {
            lblConexion2.setText("Conexión con MySQL exitosa");
        } else {
            lblConexion2.setText("Conexión con MySQL fallida");
        }
    }
}
