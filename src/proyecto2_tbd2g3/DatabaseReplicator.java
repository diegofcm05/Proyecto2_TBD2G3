import java.sql.*;
import javax.swing.JLabel;

public class DatabaseReplicator {

    private Connection originConnection;
    private Connection destinationConnection;

    // Conexión a la base de datos de origen (MySQL)
    public void connectToOrigin(String host, String user, String pass, String port, String dbName) throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
        originConnection = DriverManager.getConnection(url, user, pass);
        System.out.println("Conexión establecida con la base de datos de origen.");
    }

    // Conexión a la base de datos de destino (Oracle)
    public void connectToDestination(String host, String user, String pass, String port, String dbName) throws SQLException {
        String url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName;
        destinationConnection = DriverManager.getConnection(url, user, pass);
        System.out.println("Conexión establecida con la base de datos de destino.");
    }

    // Método para replicar de origen a destino
    public void replicateOriginToDestination() throws SQLException {
        replicate("bitacora_origen", originConnection, destinationConnection);
    }

    // Método para replicar de destino a origen
    public void replicateDestinationToOrigin() throws SQLException {
        replicate("bitacora_destino", destinationConnection, originConnection);
    }

    // Lógica genérica de replicación
    private void replicate(String bitacoraTable, Connection source, Connection target) throws SQLException {
        Statement sourceStmt = null;
        ResultSet resultSet = null;

        try {
            sourceStmt = source.createStatement();
            resultSet = sourceStmt.executeQuery("SELECT * FROM " + bitacoraTable);

            while (resultSet.next()) {
                String table = resultSet.getString("tabla");
                String operation = resultSet.getString("operacion");
                String data = resultSet.getString("dato");

                processOperation(target, table, operation, data);
            }
        } finally {
            if (resultSet != null) resultSet.close();
            if (sourceStmt != null) sourceStmt.close();
        }
    }

    // Método para procesar las operaciones
    private void processOperation(Connection target, String table, String operation, String data) throws SQLException {
        Statement targetStmt = null;

        try {
            targetStmt = target.createStatement();

            switch (operation.toUpperCase()) {
                case "INSERT":
                    // Suponiendo que `data` contiene una lista de valores separados por comas
                    String insertQuery = "INSERT INTO " + table + " VALUES (" + data + ")";
                    targetStmt.executeUpdate(insertQuery);
                    System.out.println("Insertado en " + table + ": " + data);
                    break;

                case "UPDATE":
                    // Suponiendo que `data` contiene pares clave=valor separados por comas
                    // y que la clave primaria está al final (p.ej., "campo1=valor1, campo2=valor2 WHERE id=1")
                    String updateQuery = "UPDATE " + table + " SET " + data;
                    targetStmt.executeUpdate(updateQuery);
                    System.out.println("Actualizado en " + table + ": " + data);
                    break;

                case "DELETE":
                    // Suponiendo que `data` contiene una condición WHERE (p.ej., "id=1")
                    String deleteQuery = "DELETE FROM " + table + " WHERE " + data;
                    targetStmt.executeUpdate(deleteQuery);
                    System.out.println("Eliminado en " + table + " con condición: " + data);
                    break;

                default:
                    System.out.println("Operación no reconocida: " + operation);
                    break;
            }
        } finally {
            if (targetStmt != null) targetStmt.close();
        }
    }

    // Método para cerrar las conexiones
    public void closeConnections() {
        try {
            if (originConnection != null && !originConnection.isClosed()) {
                originConnection.close();
                System.out.println("Conexión a la base de datos de origen cerrada.");
            }
            if (destinationConnection != null && !destinationConnection.isClosed()) {
                destinationConnection.close();
                System.out.println("Conexión a la base de datos de destino cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexiones: " + e.getMessage());
        }
    }

    // Métodos para actualizar el estado de las conexiones en las etiquetas de la interfaz
    public void updateConnectionStatus(boolean isOriginConnected, boolean isDestinationConnected, JLabel lblConexion1, JLabel lblConexion2) {
        if (isOriginConnected) {
            lblConexion1.setText("Conexión con MySQL exitosa");
        } else {
            lblConexion1.setText("Conexión con MySQL fallida");
        }

        if (isDestinationConnected) {
            lblConexion2.setText("Conexión con Oracle exitosa");
        } else {
            lblConexion2.setText("Conexión con Oracle fallida");
        }
    }
}
