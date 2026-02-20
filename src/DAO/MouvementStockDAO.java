/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import MODEL.MouvementStock;
import MODEL.Produit;
import CONNEXION.dbconnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author nexa
 */
public class MouvementStockDAO {

    public int saveMouvementAndApply(MouvementStock m) {
        String insertSql = "INSERT INTO mouvement_stock(type, produit_id, quantite, date_mouvement, motif) VALUES (?, ?, ?, ?, ?)";
        String selectProduitForUpdate = "SELECT stock FROM produit WHERE id = ? FOR UPDATE";
        String updateProduit = "UPDATE produit SET stock = ? WHERE id = ?";

        int generatedId = -1;

        try (Connection con = dbconnection.getConnection()) {
            con.setAutoCommit(false);

            int produitId = m.getProduit().getId();
            int currentStock;
            try (PreparedStatement psSel = con.prepareStatement(selectProduitForUpdate)) {
                psSel.setInt(1, produitId);
                try (ResultSet rs = psSel.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return -1; // produit inexistant
                    }
                    currentStock = rs.getInt("stock");
                }
            }

            int newStock = currentStock + (m.getType() == MouvementStock.MouvementType.ENTREE ? m.getQuantite() : -m.getQuantite());

            if (newStock < 0) {
                con.rollback();
                return -1;
            }

            try (PreparedStatement psIns = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                psIns.setString(1, m.getType().name());
                psIns.setInt(2, produitId);
                psIns.setInt(3, m.getQuantite());
                LocalDateTime dt = m.getDateMouvement() == null ? LocalDateTime.now() : m.getDateMouvement();
                psIns.setTimestamp(4, Timestamp.valueOf(dt));
                psIns.setString(5, m.getMotif());
                psIns.executeUpdate();

                try (ResultSet rs = psIns.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                    } else {
                        con.rollback();
                        return -1;
                    }
                }
            }

            try (PreparedStatement psUpd = con.prepareStatement(updateProduit)) {
                psUpd.setInt(1, newStock);
                psUpd.setInt(2, produitId);
                psUpd.executeUpdate();
            }

            con.commit();
            return generatedId;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saveMouvementOnly(MouvementStock m) {
        String sql = "INSERT INTO mouvement_stock(type, produit_id, quantite, date_mouvement, motif) VALUES (?, ?, ?, ?, ?)";
        int id = -1;
        try (Connection con = dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, m.getType().name());
            ps.setInt(2, m.getProduit().getId());
            ps.setInt(3, m.getQuantite());
            LocalDateTime dt = m.getDateMouvement() == null ? LocalDateTime.now() : m.getDateMouvement();
            ps.setTimestamp(4, Timestamp.valueOf(dt));
            ps.setString(5, m.getMotif());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) id = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public MouvementStock getById(int id) {
        String sql = "SELECT ms.*, p.nom, p.prix_vente, p.stock, p.categorie_id FROM mouvement_stock ms JOIN produit p ON ms.produit_id = p.id WHERE ms.id = ?";
        try (Connection con = dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMouvement(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<MouvementStock> readAll() {
        List<MouvementStock> list = new ArrayList<>();
        String sql = "SELECT ms.*, p.nom, p.prix_vente, p.stock, p.categorie_id FROM mouvement_stock ms JOIN produit p ON ms.produit_id = p.id ORDER BY ms.date_mouvement DESC";
        try (Connection con = dbconnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRowToMouvement(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<MouvementStock> readByProduct(int produitId) {
        List<MouvementStock> list = new ArrayList<>();
        String sql = "SELECT ms.*, p.nom, p.prix_vente, p.stock, p.categorie_id FROM mouvement_stock ms JOIN produit p ON ms.produit_id = p.id WHERE ms.produit_id = ? ORDER BY ms.date_mouvement DESC";
        try (Connection con = dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, produitId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToMouvement(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<MouvementStock> readByDateRange(LocalDateTime from, LocalDateTime to) {
        List<MouvementStock> list = new ArrayList<>();
        String sql = "SELECT ms.*, p.nom, p.prix_vente, p.stock, p.categorie_id FROM mouvement_stock ms JOIN produit p ON ms.produit_id = p.id WHERE ms.date_mouvement BETWEEN ? AND ? ORDER BY ms.date_mouvement DESC";
        try (Connection con = dbconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(from));
            ps.setTimestamp(2, Timestamp.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToMouvement(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteMouvement(int id, boolean revertStock) {
        String selectMvt = "SELECT * FROM mouvement_stock WHERE id = ?";
        String deleteMvt = "DELETE FROM mouvement_stock WHERE id = ?";
        String selectProduitForUpdate = "SELECT stock FROM produit WHERE id = ? FOR UPDATE";
        String updateProduit = "UPDATE produit SET stock = ? WHERE id = ?";

        try (Connection con = dbconnection.getConnection()) {
            con.setAutoCommit(false);

            int produitId;
            MouvementStock.MouvementType type;
            int quantite;
            try (PreparedStatement psSel = con.prepareStatement(selectMvt)) {
                psSel.setInt(1, id);
                try (ResultSet rs = psSel.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                    produitId = rs.getInt("produit_id");
                    type = MouvementStock.MouvementType.valueOf(rs.getString("type"));
                    quantite = rs.getInt("quantite");
                }
            }

            if (revertStock) {
                int currentStock;
                try (PreparedStatement psLock = con.prepareStatement(selectProduitForUpdate)) {
                    psLock.setInt(1, produitId);
                    try (ResultSet rs = psLock.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                        currentStock = rs.getInt("stock");
                    }
                }

                int adjusted = currentStock + (type == MouvementStock.MouvementType.ENTREE ? -quantite : +quantite);
                if (adjusted < 0) {
                    con.rollback();
                    return false;
                }

                try (PreparedStatement psUpd = con.prepareStatement(updateProduit)) {
                    psUpd.setInt(1, adjusted);
                    psUpd.setInt(2, produitId);
                    psUpd.executeUpdate();
                }
            }

            try (PreparedStatement psDel = con.prepareStatement(deleteMvt)) {
                psDel.setInt(1, id);
                psDel.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateMouvement(MouvementStock m, boolean adjustStock) {
        if (m.getId() <= 0) return false;
        String selectMvt = "SELECT * FROM mouvement_stock WHERE id = ?";
        String updateMvtSql = "UPDATE mouvement_stock SET type = ?, produit_id = ?, quantite = ?, date_mouvement = ?, motif = ? WHERE id = ?";
        String selectProduitForUpdate = "SELECT stock FROM produit WHERE id = ? FOR UPDATE";
        String updateProduit = "UPDATE produit SET stock = ? WHERE id = ?";

        try (Connection con = dbconnection.getConnection()) {
            con.setAutoCommit(false);

            int oldProduitId;
            MouvementStock.MouvementType oldType;
            int oldQty;
            try (PreparedStatement psSel = con.prepareStatement(selectMvt)) {
                psSel.setInt(1, m.getId());
                try (ResultSet rs = psSel.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                    oldProduitId = rs.getInt("produit_id");
                    oldType = MouvementStock.MouvementType.valueOf(rs.getString("type"));
                    oldQty = rs.getInt("quantite");
                }
            }

            if (adjustStock) {
                if (oldProduitId == m.getProduit().getId()) {
                    int currentStock;
                    try (PreparedStatement psLock = con.prepareStatement(selectProduitForUpdate)) {
                        psLock.setInt(1, oldProduitId);
                        try (ResultSet rs = psLock.executeQuery()) {
                            if (!rs.next()) { con.rollback(); return false; }
                            currentStock = rs.getInt("stock");
                        }
                    }

                    int oldEffect = (oldType == MouvementStock.MouvementType.ENTREE) ? oldQty : -oldQty;
                    int newEffect = (m.getType() == MouvementStock.MouvementType.ENTREE) ? m.getQuantite() : -m.getQuantite();
                    int newStock = currentStock + (newEffect - oldEffect);

                    if (newStock < 0) { con.rollback(); return false; }

                    try (PreparedStatement psUpd = con.prepareStatement(updateProduit)) {
                        psUpd.setInt(1, newStock);
                        psUpd.setInt(2, oldProduitId);
                        psUpd.executeUpdate();
                    }
                } else {
                    int stockOld;
                    try (PreparedStatement psLockOld = con.prepareStatement(selectProduitForUpdate)) {
                        psLockOld.setInt(1, oldProduitId);
                        try (ResultSet rs = psLockOld.executeQuery()) {
                            if (!rs.next()) { con.rollback(); return false; }
                            stockOld = rs.getInt("stock");
                        }
                    }
                    int effectOld = (oldType == MouvementStock.MouvementType.ENTREE) ? -oldQty : +oldQty;
                    int newStockOld = stockOld + effectOld;
                    if (newStockOld < 0) { con.rollback(); return false; }
                    try (PreparedStatement psUpdOld = con.prepareStatement(updateProduit)) {
                        psUpdOld.setInt(1, newStockOld);
                        psUpdOld.setInt(2, oldProduitId);
                        psUpdOld.executeUpdate();
                    }

                    int newProduitId = m.getProduit().getId();
                    int stockNew;
                    try (PreparedStatement psLockNew = con.prepareStatement(selectProduitForUpdate)) {
                        psLockNew.setInt(1, newProduitId);
                        try (ResultSet rs = psLockNew.executeQuery()) {
                            if (!rs.next()) { con.rollback(); return false; }
                            stockNew = rs.getInt("stock");
                        }
                    }
                    int effectNew = (m.getType() == MouvementStock.MouvementType.ENTREE) ? m.getQuantite() : -m.getQuantite();
                    int newStockNew = stockNew + effectNew;
                    if (newStockNew < 0) { con.rollback(); return false; }
                    try (PreparedStatement psUpdNew = con.prepareStatement(updateProduit)) {
                        psUpdNew.setInt(1, newStockNew);
                        psUpdNew.setInt(2, newProduitId);
                        psUpdNew.executeUpdate();
                    }
                }
            }

            try (PreparedStatement psUpdMvt = con.prepareStatement(updateMvtSql)) {
                psUpdMvt.setString(1, m.getType().name());
                psUpdMvt.setInt(2, m.getProduit().getId());
                psUpdMvt.setInt(3, m.getQuantite());
                LocalDateTime dt = m.getDateMouvement() == null ? LocalDateTime.now() : m.getDateMouvement();
                psUpdMvt.setTimestamp(4, Timestamp.valueOf(dt));
                psUpdMvt.setString(5, m.getMotif());
                psUpdMvt.setInt(6, m.getId());
                psUpdMvt.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private MouvementStock mapRowToMouvement(ResultSet rs) throws SQLException {
        MouvementStock m = new MouvementStock();
        m.setId(rs.getInt("id"));
        m.setType(MouvementStock.MouvementType.valueOf(rs.getString("type")));

        Produit p = new Produit();
        p.setId(rs.getInt("produit_id"));
        try {
            p.setNom(rs.getString("nom"));
            p.setPrixVente(rs.getDouble("prix_vente"));
            p.setStockActuel(rs.getInt("stock"));
        } catch (SQLException ignored) { }

        m.setProduit(p);
        m.setQuantite(rs.getInt("quantite"));

        Timestamp ts = rs.getTimestamp("date_mouvement");
        if (ts != null) m.setDateMouvement(ts.toLocalDateTime());

        m.setMotif(rs.getString("motif"));
        return m;
    }
}
