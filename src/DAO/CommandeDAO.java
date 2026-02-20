/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import MODEL.Commande;
import MODEL.LigneCommande;
import MODEL.Produit;
import CONNEXION.dbconnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author nexa
 */

public class CommandeDAO {

    private ProduitDAO produitDAO = new ProduitDAO();

    public int createCommande(Commande commande) {
        String insertCommande = "INSERT INTO commande(date_commande, etat, total) VALUES (?, ?, ?)";
        String insertLigne = "INSERT INTO ligne_commande(commande_id, produit_id, quantite, prix_unitaire, montant_ligne) VALUES (?, ?, ?, ?, ?)";
        int commandeId = -1;

        try (Connection con = dbconnection.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement psCmd = con.prepareStatement(insertCommande, Statement.RETURN_GENERATED_KEYS)) {
                LocalDateTime dt = commande.getDateCommande() == null ? LocalDateTime.now() : commande.getDateCommande();
                psCmd.setTimestamp(1, Timestamp.valueOf(dt));
                psCmd.setString(2, commande.getEtat().name());
                commande.recalcTotal();
                psCmd.setDouble(3, commande.getTotal());
                psCmd.executeUpdate();

                ResultSet rs = psCmd.getGeneratedKeys();
                if (rs.next()) commandeId = rs.getInt(1);
            }

            try (PreparedStatement psL = con.prepareStatement(insertLigne, Statement.RETURN_GENERATED_KEYS)) {
                for (LigneCommande l : commande.getLignes()) {
                    psL.setInt(1, commandeId);
                    psL.setInt(2, l.getProduit().getId());
                    psL.setInt(3, l.getQuantite());
                    psL.setDouble(4, l.getPrixUnitaire());
                    psL.setDouble(5, l.getMontantLigne());
                    psL.executeUpdate();
                }
            }

            con.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return commandeId;
    }

    public boolean validerCommande(int commandeId) {
        String selectLignes = "SELECT * FROM ligne_commande WHERE commande_id = ?";
        String updateStock = "UPDATE produit SET stock = stock - ? WHERE id = ?";
        String updateCommande = "UPDATE commande SET etat = ?, total = ? WHERE id = ?";

        try (Connection con = dbconnection.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(selectLignes)) {
                ps.setInt(1, commandeId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int produitId = rs.getInt("produit_id");
                    int qty = rs.getInt("quantite");

                    String chk = "SELECT stock FROM produit WHERE id = ? FOR UPDATE";
                    try (PreparedStatement psChk = con.prepareStatement(chk)) {
                        psChk.setInt(1, produitId);
                        ResultSet r2 = psChk.executeQuery();
                        if (r2.next()) {
                            int stock = r2.getInt("stock");
                            if (stock < qty) {
                                con.rollback();
                                return false;
                            }
                        } else {
                            con.rollback();
                            return false;
                        }
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(selectLignes);
                 PreparedStatement psUpd = con.prepareStatement(updateStock)) {

                ps.setInt(1, commandeId);
                ResultSet rs = ps.executeQuery();
                double total = 0;
                while (rs.next()) {
                    int produitId = rs.getInt("produit_id");
                    int qty = rs.getInt("quantite");
                    double montant = rs.getDouble("montant_ligne");
                    total += montant;

                    psUpd.setInt(1, qty);
                    psUpd.setInt(2, produitId);
                    psUpd.executeUpdate();
                }

                try (PreparedStatement psCmd = con.prepareStatement(updateCommande)) {
                    psCmd.setString(1, "VALIDE");
                    psCmd.setDouble(2, total);
                    psCmd.setInt(3, commandeId);
                    psCmd.executeUpdate();
                }
            }

            con.commit();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean annulerCommande(int commandeId) {
        String selectCmd = "SELECT etat FROM commande WHERE id = ?";
        String selectLignes = "SELECT * FROM ligne_commande WHERE commande_id = ?";
        String updateStock = "UPDATE produit SET stock = stock + ? WHERE id = ?";
        String updateCmd = "UPDATE commande SET etat = 'ANNULEE' WHERE id = ?";

        try (Connection con = dbconnection.getConnection()) {
            con.setAutoCommit(false);

            String etat = null;
            try (PreparedStatement ps = con.prepareStatement(selectCmd)) {
                ps.setInt(1, commandeId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) etat = rs.getString("etat");
                else { con.rollback(); return false; }
            }

            if ("VALIDE".equals(etat)) {
                try (PreparedStatement ps = con.prepareStatement(selectLignes);
                     PreparedStatement psUpd = con.prepareStatement(updateStock)) {

                    ps.setInt(1, commandeId);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        int produitId = rs.getInt("produit_id");
                        int qty = rs.getInt("quantite");
                        psUpd.setInt(1, qty);
                        psUpd.setInt(2, produitId);
                        psUpd.executeUpdate();
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(updateCmd)) {
                ps.setInt(1, commandeId);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
