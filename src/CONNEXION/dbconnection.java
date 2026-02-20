/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CONNEXION;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 *
 * @author nexa
 */
public class dbconnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/resto_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private dbconnection() {}

    // Méthode principale pour obtenir la connexion
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Méthode de test
    public static void main(String[] args) {

        try {
            // Charger explicitement le driver (sécurité)
            Class.forName("com.mysql.jdbc.Driver");

            Connection con = getConnection();

            if (con != null && !con.isClosed()) {
                System.out.println("Connexion à la base de données réussie !");
                con.close();
            } else {
                System.out.println("Connexion échouée.");
            }

        } catch (ClassNotFoundException e) {
            System.out.println("Driver MySQL introuvable !");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println(" Erreur SQL lors de la connexion !");
            e.printStackTrace();
        }
    }
}

    
   


