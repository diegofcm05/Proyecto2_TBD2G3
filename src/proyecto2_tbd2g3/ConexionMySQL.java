/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyecto2_tbd2g3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author Junior
 */
public class ConexionMySQL {
    static String url="jdbc:mysql://";
    static String urlOG="jdbc:mysql://tbd2g3.c1ci2a0qwno2.us-east-1.rds.amazonaws.com:3306/tbd2g3";
    
    public static Connection conectar(String Url,String User,String Password,String Port,String NombreDB){
        Connection conexion=null;
        try {
            url="jdbc:mysql://"+Url+":"+Port+"/"+NombreDB;
            conexion= DriverManager.getConnection(url, User, Password);
            JOptionPane.showMessageDialog(null, "Se ha conectado correctamente con la Base de Datos");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Ha ocurrido un error con la conexion");
        }
        return conexion;
    }
}
