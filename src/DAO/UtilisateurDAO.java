/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import MODEL.Utilisateur;
import CONNEXION.dbconnection;
import java.sql.*;

/**
 *
 * @author nexa
 */
public class UtilisateurDAO {

    public int saveUtilisateur(Utilisateur u) {
        String sql = "INSERT INTO utilisateur(login, mot_de_passe) VALUES (?, ?)";
        int generatedId = -1;
        try (Connection con = dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getLogin());
            ps.setString(2, u.getMotDePasse()); 
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) generatedId = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return generatedId;
    }

    public Utilisateur authenticate(String login, String motDePasse) {
        String sql = "SELECT * FROM utilisateur WHERE login = ? AND mot_de_passe = ?";
        try (Connection con = dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, motDePasse); 
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Utilisateur u = new Utilisateur();
                u.setId(rs.getInt("id"));
                u.setLogin(rs.getString("login"));
                u.setMotDePasse(rs.getString("mot_de_passe"));
                return u;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
