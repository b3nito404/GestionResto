/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;
import MODEL.Categorie;
import CONNEXION.dbconnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nexa
 */
public class CategorieDAO {
    
    public int saveCategorie(Categorie categorie) {
        String sql = "INSERT INTO categorie(libelle) VALUES (?)";
        int generatedId = -1;

        try (Connection con = dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, categorie.getLibelle());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                generatedId = rs.getInt(1);
                categorie.setId(generatedId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return generatedId;
    }

    public boolean existsByLibelle(String libelle) {
        String sql = "SELECT COUNT(*) FROM categorie WHERE libelle = ?";
        try (Connection con = dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, libelle);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    
     public Categorie readCategorie(int id) {
        String sql = "SELECT * FROM categorie WHERE id=?";
        Categorie c = null;

        try (Connection con = dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                c = new Categorie(
                        rs.getInt("id"),
                        rs.getString("libelle")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return c;
    }

    public void updateCategorie(Categorie categorie) {
        String sql = "UPDATE categorie SET libelle=? WHERE id=?";

        try (Connection con = dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categorie.getLibelle());
            ps.setInt(2, categorie.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCategorie(int id) {
        String sql = "DELETE FROM categorie WHERE id=?";

        try (Connection con = dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Categorie> readAllCategorie() {
        List<Categorie> categories = new ArrayList<>();
        String sql = "SELECT * FROM categorie";

        try (Connection con = dbconnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(
                        new Categorie(
                                rs.getInt("id"),
                                rs.getString("libelle")
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
    
}
