/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;
import MODEL.Categorie;
import MODEL.Produit;
import CONNEXION.dbconnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author nexa
 */
public class ProduitDAO {

    public int saveProduit(Produit produit) {
        String sql = "INSERT INTO produit(nom, prix_vente, stock, seuil_alerte, categorie_id) VALUES (?, ?, ?, ?, ?)";
        int generatedId = -1;

        try (Connection con = dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, produit.getNom());
            ps.setDouble(2, produit.getPrixVente());
            ps.setInt(3, produit.getStockActuel());
            ps.setInt(4, produit.getSeuilAlerte());
            if (produit.getCategorie() != null)
                ps.setInt(5, produit.getCategorie().getId());
            else
                ps.setNull(5, Types.INTEGER);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                generatedId = rs.getInt(1);
                produit.setId(generatedId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return generatedId;
    }

    public Produit readProduit(int id) {
        String sql = "SELECT p.*, c.libelle FROM produit p LEFT JOIN categorie c ON p.categorie_id = c.id WHERE p.id = ?";
        Produit produit = null;

        try (Connection con = dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Categorie categorie = null;
                int catId = rs.getInt("categorie_id");
                if (!rs.wasNull()) {
                    categorie = new Categorie(catId, rs.getString("libelle"));
                }

                produit = new Produit();
                produit.setId(rs.getInt("id"));
                produit.setNom(rs.getString("nom"));
                produit.setPrixVente(rs.getDouble("prix_vente"));
                produit.setStockActuel(rs.getInt("stock"));
                produit.setSeuilAlerte(rs.getInt("seuil_alerte"));
                produit.setCategorie(categorie);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produit;
    }

    public void updateProduit(Produit produit) {
        String sql = "UPDATE produit SET nom=?, prix_vente=?, stock=?, seuil_alerte=?, categorie_id=? WHERE id=?";

        try (Connection con = dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, produit.getNom());
            ps.setDouble(2, produit.getPrixVente());
            ps.setInt(3, produit.getStockActuel());
            ps.setInt(4, produit.getSeuilAlerte());
            if (produit.getCategorie() != null)
                ps.setInt(5, produit.getCategorie().getId());
            else
                ps.setNull(5, Types.INTEGER);
            ps.setInt(6, produit.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteProduit(int id) {
        String sql = "DELETE FROM produit WHERE id=?";

        try (Connection con = dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Produit> readAllProduit() {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT p.*, c.libelle FROM produit p LEFT JOIN categorie c ON p.categorie_id = c.id";

        try (Connection con = dbconnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Categorie categorie = null;
                int catId = rs.getInt("categorie_id");
                if (!rs.wasNull()) {
                    categorie = new Categorie(catId, rs.getString("libelle"));
                }
                Produit produit = new Produit();
                produit.setId(rs.getInt("id"));
                produit.setNom(rs.getString("nom"));
                produit.setPrixVente(rs.getDouble("prix_vente"));
                produit.setStockActuel(rs.getInt("stock"));
                produit.setSeuilAlerte(rs.getInt("seuil_alerte"));
                produit.setCategorie(categorie);

                produits.add(produit);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produits;
    }

    // Méthode utilitaire: decrement stock (utilisée par CommandeDAO)
    public boolean decrementStock(Connection con, int produitId, int qty) throws SQLException {
        String checkSql = "SELECT stock FROM produit WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(checkSql)) {
            ps.setInt(1, produitId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int stock = rs.getInt("stock");
                if (stock < qty) return false;
                String update = "UPDATE produit SET stock = stock - ? WHERE id = ?";
                try (PreparedStatement ps2 = con.prepareStatement(update)) {
                    ps2.setInt(1, qty);
                    ps2.setInt(2, produitId);
                    ps2.executeUpdate();
                    return true;
                }
            } else {
                return false;
            }
        }
    }
}