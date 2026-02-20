/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

import DAO.CategorieDAO;
import DAO.CommandeDAO;
import DAO.MouvementStockDAO;
import DAO.ProduitDAO;
import CONNEXION.dbconnection;
import MODEL.Commande;
import MODEL.Produit;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 *
 * @author nexa
 */

    
public class DashboardDataImpl implements DashboardFrame.DashboardData {

    // ── Nom de l'utilisateur connecté ─────────────────────────────────────────
    private final String username;

    // ── DAO ───────────────────────────────────────────────────────────────────
    private final ProduitDAO        produitDAO        = new ProduitDAO();
    private final MouvementStockDAO mouvementDAO      = new MouvementStockDAO();

    // ── Cache des données (rechargé via reload()) ─────────────────────────────
    private int     totalCommandes      = 0;
    private int     totalProduits       = 0;
    private int     alertCount          = 0;
    private int     commandesValidees   = 0;
    private double  caJour              = 0;
    private double  caMois              = 0;
    private double  objectifJour        = 150_000; // Modifiez selon votre objectif

    private double[]                caMensuel       = new double[12];
    private Map<String, Double>     categoriesVentes = new LinkedHashMap<>();
    private List<CommandeRecente>   commandesRecentes = new ArrayList<>();
    private List<TopProduit>        topProduits       = new ArrayList<>();
    private List<AlerteProduit>     alerteProduits    = new ArrayList<>();

    // Sparkline par carte (derniers 12 jours)
    private int[] sparkCommandes = new int[12];
    private int[] sparkProduits  = new int[12];
    private int[] sparkAlertes   = new int[12];
    private int[] sparkCA        = new int[12];

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ══════════════════════════════════════════════════════════════════════════
    public DashboardDataImpl(String username) {
        this.username = username;
        reload();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // RECHARGEMENT COMPLET DEPUIS LA BDD
    // ══════════════════════════════════════════════════════════════════════════
    @Override
    public void reload() {
        try {
            loadStats();
            loadCaMensuel();
            loadCategoriesVentes();
            loadCommandesRecentes();
            loadTopProduits();
            loadAlerteProduits();
            loadSparklines();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Stats globales ────────────────────────────────────────────────────────
    private void loadStats() {
        String sqlTotalCmd  = "SELECT COUNT(*) FROM commande";
        String sqlValidees  = "SELECT COUNT(*) FROM commande WHERE etat = 'VALIDE'";
        String sqlCAJour    = "SELECT COALESCE(SUM(total),0) FROM commande WHERE etat='VALIDE' AND DATE(date_commande)=CURDATE()";
        String sqlCAMois    = "SELECT COALESCE(SUM(total),0) FROM commande WHERE etat='VALIDE' AND MONTH(date_commande)=MONTH(CURDATE()) AND YEAR(date_commande)=YEAR(CURDATE())";

        try (Connection con = dbconnection.getConnection()) {

            try (Statement st = con.createStatement()) {
                // Total commandes
                try (ResultSet rs = st.executeQuery(sqlTotalCmd)) {
                    if (rs.next()) totalCommandes = rs.getInt(1);
                }
                // Commandes validées
                try (ResultSet rs = st.executeQuery(sqlValidees)) {
                    if (rs.next()) commandesValidees = rs.getInt(1);
                }
                // CA jour
                try (ResultSet rs = st.executeQuery(sqlCAJour)) {
                    if (rs.next()) caJour = rs.getDouble(1);
                }
                // CA mois
                try (ResultSet rs = st.executeQuery(sqlCAMois)) {
                    if (rs.next()) caMois = rs.getDouble(1);
                }
            }

            // Total produits
            totalProduits = produitDAO.readAllProduit().size();

            // Alertes stock
            alertCount = 0;
            for (Produit p : produitDAO.readAllProduit()) {
                if (p.getStockActuel() <= p.getSeuilAlerte()) alertCount++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── CA par mois (12 mois de l'année en cours) ─────────────────────────────
    private void loadCaMensuel() {
        caMensuel = new double[12];
        String sql = "SELECT MONTH(date_commande) AS mois, COALESCE(SUM(total),0) AS ca "
                   + "FROM commande "
                   + "WHERE etat='VALIDE' AND YEAR(date_commande)=YEAR(CURDATE()) "
                   + "GROUP BY MONTH(date_commande)";
        try (Connection con = dbconnection.getConnection();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            while (rs.next()) {
                int mois = rs.getInt("mois"); // 1=Janvier ... 12=Décembre
                caMensuel[mois - 1] = rs.getDouble("ca");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Ventes par catégorie ──────────────────────────────────────────────────
    private void loadCategoriesVentes() {
        categoriesVentes = new LinkedHashMap<>();
        String sql = "SELECT c.libelle, COALESCE(SUM(lc.montant_ligne),0) AS total "
                   + "FROM ligne_commande lc "
                   + "JOIN produit p ON lc.produit_id = p.id "
                   + "JOIN categorie c ON p.categorie_id = c.id "
                   + "JOIN commande cmd ON lc.commande_id = cmd.id "
                   + "WHERE cmd.etat = 'VALIDE' "
                   + "GROUP BY c.libelle "
                   + "ORDER BY total DESC";
        try (Connection con = dbconnection.getConnection();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            while (rs.next()) {
                categoriesVentes.put(rs.getString("libelle"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Dernières commandes ───────────────────────────────────────────────────
    private void loadCommandesRecentes() {
        commandesRecentes = new ArrayList<>();
        String sql = "SELECT id, date_commande, etat, total "
                   + "FROM commande "
                   + "ORDER BY date_commande DESC "
                   + "LIMIT 10";
        try (Connection con = dbconnection.getConnection();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            while (rs.next()) {
                int    id    = rs.getInt("id");
                String date  = rs.getTimestamp("date_commande") != null
                             ? rs.getTimestamp("date_commande").toLocalDateTime().format(FMT_DATE)
                             : "-";
                // Traduction des états BDD → français affiché
                String etatBDD = rs.getString("etat");
                String etatFR  = switch (etatBDD) {
                    case "VALIDE"   -> "VALIDEE";
                    case "ANNULEE"  -> "ANNULEE";
                    default         -> "EN_COURS";
                };
                double total = rs.getDouble("total");
                commandesRecentes.add(new CommandeRecente(id, date, etatFR, total));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Top produits vendus ───────────────────────────────────────────────────
    private void loadTopProduits() {
        topProduits = new ArrayList<>();
        String sql = "SELECT p.nom, SUM(lc.quantite) AS qte, SUM(lc.montant_ligne) AS ca "
                   + "FROM ligne_commande lc "
                   + "JOIN produit p ON lc.produit_id = p.id "
                   + "JOIN commande cmd ON lc.commande_id = cmd.id "
                   + "WHERE cmd.etat = 'VALIDE' "
                   + "GROUP BY p.nom "
                   + "ORDER BY qte DESC "
                   + "LIMIT 10";
        try (Connection con = dbconnection.getConnection();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            while (rs.next()) {
                topProduits.add(new TopProduit(
                    rs.getString("nom"),
                    rs.getInt("qte"),
                    rs.getDouble("ca")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Produits sous seuil d'alerte ──────────────────────────────────────────
    private void loadAlerteProduits() {
        alerteProduits = new ArrayList<>();
        for (Produit p : produitDAO.readAllProduit()) {
            if (p.getStockActuel() <= p.getSeuilAlerte()) {
                alerteProduits.add(new AlerteProduit(
                    p.getNom(),
                    p.getStockActuel(),
                    p.getSeuilAlerte()
                ));
            }
        }
    }

    // ── Sparklines : commandes par jour sur les 12 derniers jours ─────────────
    private void loadSparklines() {
        sparkCommandes = new int[12];
        sparkCA        = new int[12];

        String sqlCmd = "SELECT DAY(date_commande) AS j, COUNT(*) AS nb "
                      + "FROM commande "
                      + "WHERE date_commande >= DATE_SUB(CURDATE(), INTERVAL 12 DAY) "
                      + "GROUP BY DAY(date_commande) "
                      + "ORDER BY date_commande";

        String sqlCA2 = "SELECT DAY(date_commande) AS j, COALESCE(SUM(total),0) AS ca "
                      + "FROM commande "
                      + "WHERE etat='VALIDE' AND date_commande >= DATE_SUB(CURDATE(), INTERVAL 12 DAY) "
                      + "GROUP BY DAY(date_commande) "
                      + "ORDER BY date_commande";

        try (Connection con = dbconnection.getConnection()) {
            int idx = 0;
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlCmd)) {
                while (rs.next() && idx < 12) sparkCommandes[idx++] = rs.getInt("nb");
            }
            idx = 0;
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlCA2)) {
                while (rs.next() && idx < 12) sparkCA[idx++] = (int) rs.getDouble("ca") / 1000;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Sparklines produits et alertes (statiques relatives aux données chargées)
        sparkProduits = buildFlatSparkline(totalProduits);
        sparkAlertes  = buildFlatSparkline(alertCount);
    }

    private int[] buildFlatSparkline(int valeur) {
        int[] sp = new int[12];
        Random r = new Random(valeur); // graine fixe = courbe stable entre 2 rechargements
        for (int i = 0; i < 12; i++) {
            sp[i] = Math.max(0, valeur + r.nextInt(Math.max(1, valeur / 4)) - valeur / 8);
        }
        sp[11] = valeur; // dernier point = valeur réelle
        return sp;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // IMPLÉMENTATION DE L'INTERFACE
    // ══════════════════════════════════════════════════════════════════════════

    //@Override public void reload() { loadStats(); loadCaMensuel(); loadCategoriesVentes(); loadCommandesRecentes(); loadTopProduits(); loadAlerteProduits(); loadSparklines(); }
    @Override public String getUsername()          { return username; }
    @Override public int    getTotalCommandes()    { return totalCommandes; }
    @Override public int    getTotalProduits()     { return totalProduits; }
    @Override public int    getAlertCount()        { return alertCount; }
    @Override public int    getCommandesValidees() { return commandesValidees; }
    @Override public double getCaJour()            { return caJour; }
    @Override public double getCaMois()            { return caMois; }
    @Override public double getObjectifJour()      { return objectifJour; }

    @Override
    public String getCaJourFormate() {
        return String.format("%,.0f FCFA", caJour);
    }

    @Override
    public String getCaMoisFormate() {
        return String.format("%,.0f FCFA", caMois);
    }

    @Override
    public String getObjectifFormate() {
        return String.format("%,.0f FCFA", objectifJour);
    }

    @Override public double[]            getCaMensuel()        { return caMensuel; }
    @Override public Map<String, Double> getCategoriesVentes() { return categoriesVentes; }

    @Override
    public int[] getSparklineData(String cardLabel) {
        return switch (cardLabel) {
            case "Commandes"     -> sparkCommandes;
            case "Produits"      -> sparkProduits;
            case "Alertes Stock" -> sparkAlertes;
            case "CA du jour"    -> sparkCA;
            default              -> new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        };
    }

    @Override
    public List<CommandeRecente> getCommandesRecentes(int limit) {
        return commandesRecentes.subList(0, Math.min(limit, commandesRecentes.size()));
    }

    @Override
    public List<TopProduit> getTopProduits(int limit) {
        return topProduits.subList(0, Math.min(limit, topProduits.size()));
    }

    @Override
    public List<AlerteProduit> getAlerteProduits() {
        return alerteProduits;
    }

   
    public void setObjectifJour(double objectif) {
        this.objectifJour = objectif;
    }
}

