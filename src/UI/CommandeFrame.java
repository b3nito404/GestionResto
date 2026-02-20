/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

import DAO.CommandeDAO;
import DAO.ProduitDAO;
import MODEL.Commande;
import MODEL.LigneCommande;
import MODEL.Produit;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import CONNEXION.dbconnection;


/**
 *
 * @author nexa
 */

public class CommandeFrame extends JPanel {

    private static final Color ROSE         = new Color(0xE91E8C);
    private static final Color ROSE_CLAIR   = new Color(0xFCE4F3);
    private static final Color VIOLET       = new Color(0x8B2FC9);
    private static final Color FOND         = new Color(0xF5F5F5);
    private static final Color BLANC        = Color.WHITE;
    private static final Color TEXTE_SOMBRE = new Color(0x1A1A2E);
    private static final Color TEXTE_GRIS   = new Color(0x9E9E9E);
    private static final Color VERT         = new Color(0x4CAF50);
    private static final Color ROUGE        = new Color(0xF44336);
    private static final Color ORANGE       = new Color(0xFFC107);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CommandeDAO commandeDAO = new CommandeDAO();
    private final ProduitDAO  produitDAO  = new ProduitDAO();
    private JTable            table;
    private DefaultTableModel tableModel;
    private JComboBox<String> filtreEtat;
    private JTextField        searchField;
    private JLabel            lblTotal, lblValidees, lblAnnulees;

    public CommandeFrame() {
        setLayout(new BorderLayout());
        setBackground(FOND);
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        chargerDonnees();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(BLANC);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF0E0EB)),
            new EmptyBorder(12, 18, 12, 18)
        ));

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        gauche.setOpaque(false);
        lblTotal    = badgeStat("0 commandes", VIOLET);
        lblValidees = badgeStat("0 validées",  VERT);
        lblAnnulees = badgeStat("0 annulées",  ROUGE);
        gauche.add(lblTotal);
        gauche.add(lblValidees);
        gauche.add(lblAnnulees);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        droite.setOpaque(false);

        filtreEtat = new JComboBox<>(new String[]{"Toutes", "EN_COURS", "VALIDE", "ANNULEE"});
        filtreEtat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filtreEtat.addActionListener(e -> chargerDonnees());

        searchField = new JTextField(14);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new ProduitFrame.RoundedLineBorder(new Color(0xDDD0E8), 10, 1),
            new EmptyBorder(6, 12, 6, 12)
        ));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filtrerParRecherche(searchField.getText()); }
        });

        JButton btnNouvelle = boutonRose("+ Nouvelle commande");
        btnNouvelle.addActionListener(e -> ouvrirDialogNouvelleCommande());

        droite.add(searchField);
        droite.add(new JLabel("Filtre : "));
        droite.add(filtreEtat);
        droite.add(btnNouvelle);

        bar.add(gauche, BorderLayout.WEST);
        bar.add(droite, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        String[] cols = {"N° Commande", "Date", "Total (FCFA)", "État", "Détails", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4 || c == 5; }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(44);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0xF0F0F0));
        table.setBackground(BLANC);
        table.setSelectionBackground(ROSE_CLAIR);
        table.setSelectionForeground(TEXTE_SOMBRE);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(0xFDF0FA));
        header.setForeground(ROSE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ROSE_CLAIR));
        header.setReorderingAllowed(false);

        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(160);

        table.getColumnModel().getColumn(3).setCellRenderer(new EtatRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new DetailsBoutonRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new DetailsBoutonEditor());
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scroll.getViewport().setBackground(FOND);
        return scroll;
    }

   
    private List<Object[]> toutesCommandes = new ArrayList<>();

    public void chargerDonnees() {
        toutesCommandes = new ArrayList<>();
        String filtre = (String) filtreEtat.getSelectedItem();

        String sql = "SELECT id, date_commande, etat, total FROM commande ORDER BY date_commande DESC";
        try (Connection con = dbconnection.getConnection();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            while (rs.next()) {
                toutesCommandes.add(new Object[]{
                    rs.getInt("id"),
                    rs.getTimestamp("date_commande"),
                    rs.getDouble("total"),
                    rs.getString("etat")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        afficherCommandes(toutesCommandes, filtre);
    }
    private void afficherCommandes(List<Object[]> commandes, String filtre) {
        tableModel.setRowCount(0);
        long total = 0, validees = 0, annulees = 0;

        for (Object[] row : commandes) {
            int    id   = (int)    row[0];
            Object tsObj = row[1];
            double mont = (double) row[2];
            String etat = (String) row[3];

            if (!"Toutes".equals(filtre) && !etat.equals(filtre)) continue;

            total++;
            if ("VALIDE".equals(etat))  validees++;
            if ("ANNULEE".equals(etat)) annulees++;

            String dateStr = "-";
            if (tsObj instanceof Timestamp) {
                dateStr = ((Timestamp) tsObj).toLocalDateTime().format(FMT);
            }

            tableModel.addRow(new Object[]{
                String.format("N°%05d", id),
                dateStr,
                String.format("%,.0f FCFA", mont),
                etat,
                id,  
                id  
            });
        }
        lblTotal.setText(total + " commande(s)");
        lblValidees.setText(validees + " validée(s)");
        lblAnnulees.setText(annulees + " annulée(s)");
    }

  
    private void ouvrirDialogNouvelleCommande() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Nouvelle commande", true);
        dialog.setSize(620, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BLANC);

      
        List<Produit> produits = produitDAO.readAllProduit();
        String[] nomsProduits  = produits.stream().map(Produit::getNom).toArray(String[]::new);

        String[] colsLignes = {"Produit", "Quantité", "Prix unit. (FCFA)", "Total ligne (FCFA)", ""};
        DefaultTableModel lignesModel = new DefaultTableModel(colsLignes, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        JTable tableLignes = new JTable(lignesModel);
        tableLignes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableLignes.setRowHeight(38);
        tableLignes.setGridColor(new Color(0xF0F0F0));
        tableLignes.setShowVerticalLines(false);
        tableLignes.getColumnModel().getColumn(4).setPreferredWidth(60);
        tableLignes.getColumnModel().getColumn(4).setCellRenderer(new SupprimerLigneRenderer());
        tableLignes.getColumnModel().getColumn(4).setCellEditor(new SupprimerLigneEditor(lignesModel));

        JLabel lblTotalCmd = new JLabel("Total : 0 FCFA");
        lblTotalCmd.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalCmd.setForeground(ROSE);

        // Fonction recalcul total
        Runnable recalcTotal = () -> {
            double t = 0;
            for (int i = 0; i < lignesModel.getRowCount(); i++) {
                try { t += Double.parseDouble(lignesModel.getValueAt(i, 3).toString().replace(" ", "").replace("FCFA", "").replace(",", "")); }
                catch (Exception ignored) {}
            }
            lblTotalCmd.setText(String.format("Total : %,.0f FCFA", t));
        };

       
        JPanel ajoutLigne = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        ajoutLigne.setBackground(new Color(0xFDF0FA));
        ajoutLigne.setBorder(new EmptyBorder(4, 10, 4, 10));

        JComboBox<String> cbProduit = new JComboBox<>(nomsProduits);
        cbProduit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbProduit.setPreferredSize(new Dimension(200, 34));

        JTextField fQte = new JTextField("1", 5);
        fQte.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fQte.setBorder(BorderFactory.createCompoundBorder(
            new ProduitFrame.RoundedLineBorder(new Color(0xDDD0E8), 8, 1),
            new EmptyBorder(5, 8, 5, 8)
        ));

        JButton btnAjoutLigne = petitBouton("+ Ajouter", VERT);
        btnAjoutLigne.addActionListener(e -> {
            if (produits.isEmpty()) return;

            
            Validateur.Resultat rQte = Validateur.validerQuantite(fQte.getText());
            if (!rQte.ok) { Validateur.afficherErreur(dialog, rQte.message); fQte.requestFocus(); return; }

            int qty     = Integer.parseInt(fQte.getText().trim());
            Produit p   = produits.get(cbProduit.getSelectedIndex());

            //ici ce qu'on on av  faire est de verifier que la quantité demandée par rapport au stock
            if (qty > p.getStockActuel()) {
                boolean continuer = Validateur.confirmerAvertissement(dialog,
                    "La quantité demandée (" + qty + ") dépasse le stock disponible de \"" + p.getNom()
                    + "\" (" + p.getStockActuel() + ").\n"
                    + "La validation de cette commande sera impossible.\nAjouter quand même ?");
                if (!continuer) return;
            }

            double total_ligne = qty * p.getPrixVente();
            lignesModel.addRow(new Object[]{
                p.getNom(), qty,
                String.format("%,.0f", p.getPrixVente()),
                String.format("%,.0f", total_ligne),
                "✕"
            });
            recalcTotal.run();
            fQte.setText("1");
        });

        ajoutLigne.add(new JLabel("Produit :"));
        ajoutLigne.add(cbProduit);
        ajoutLigne.add(new JLabel("Qté :"));
        ajoutLigne.add(fQte);
        ajoutLigne.add(btnAjoutLigne);

        JPanel centre = new JPanel(new BorderLayout());
        centre.setBackground(BLANC);
        centre.setBorder(new EmptyBorder(10, 14, 0, 14));
        centre.add(ajoutLigne, BorderLayout.NORTH);
        JScrollPane scrollLignes = new JScrollPane(tableLignes);
        scrollLignes.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(0xF0E0EB)));
        centre.add(scrollLignes, BorderLayout.CENTER);

        JPanel bas = new JPanel(new BorderLayout(0, 0));
        bas.setBackground(BLANC);
        bas.setBorder(new EmptyBorder(10, 18, 14, 18));
        bas.add(lblTotalCmd, BorderLayout.WEST);

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        boutons.setOpaque(false);
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnAnnuler.addActionListener(e -> dialog.dispose());

        JButton btnCreer = boutonRose("Créer la commande");
        btnCreer.setPreferredSize(new Dimension(170, 36));
        btnCreer.addActionListener(e -> {
           
            if (lignesModel.getRowCount() == 0) {
                Validateur.afficherErreur(dialog, "Ajoutez au moins un produit à la commande.");
                return;
            }

            java.util.Set<String> nomsSeen = new java.util.HashSet<>();
            for (int i = 0; i < lignesModel.getRowCount(); i++) {
                String nom = (String) lignesModel.getValueAt(i, 0);
                if (!nomsSeen.add(nom)) {
                    Validateur.afficherErreur(dialog,
                        "Le produit \"" + nom + "\" apparaît plusieurs fois.\n"
                        + "Regroupez les quantités sur une seule ligne.");
                    return;
                }
            }

            Commande commande = new Commande();
            commande.setDateCommande(LocalDateTime.now());
            commande.setEtat(Commande.Etat.EN_COURS);
            List<LigneCommande> lignes = new ArrayList<>();
            for (int i = 0; i < lignesModel.getRowCount(); i++) {
                String nomP = (String) lignesModel.getValueAt(i, 0);
                int qty     = (int)   lignesModel.getValueAt(i, 1);
                Produit p   = produits.stream().filter(pr -> pr.getNom().equals(nomP)).findFirst().orElse(null);
                if (p == null) continue;
                LigneCommande lc = new LigneCommande();
                lc.setProduit(p);
                lc.setPrixUnitaire(p.getPrixVente());
                lc.setQuantite(qty);
                lignes.add(lc);
            }
            commande.setLignes(lignes);
            int id = commandeDAO.createCommande(commande);
            if (id > 0) {
                JOptionPane.showMessageDialog(dialog,
                    "Commande N°" + String.format("%05d", id) + " créée avec succès !",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                chargerDonnees();
            } else {
                Validateur.afficherErreur(dialog, "Erreur lors de la création de la commande.");
            }
        });

        boutons.add(btnAnnuler);
        boutons.add(btnCreer);
        bas.add(boutons, BorderLayout.EAST);

        dialog.add(centre, BorderLayout.CENTER);
        dialog.add(bas,    BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

 
    private void validerCommande(int id) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Valider la commande N°" + String.format("%05d", id) + " ?\nCela décrémentera les stocks.",
            "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        boolean ok = commandeDAO.validerCommande(id);
        if (ok) JOptionPane.showMessageDialog(this, "Commande validée avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
        else     JOptionPane.showMessageDialog(this, "Validation impossible (stock insuffisant ?).", "Erreur", JOptionPane.ERROR_MESSAGE);
        chargerDonnees();
    }

    private void annulerCommande(int id) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Annuler la commande N°" + String.format("%05d", id) + " ?",
            "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        commandeDAO.annulerCommande(id);
        chargerDonnees();
    }


    private void filtrerParRecherche(String texte) {
        if (toutesCommandes == null) return;
        String t = texte.toLowerCase().trim();
        String filtre = (String) filtreEtat.getSelectedItem();
        if (t.isEmpty()) { afficherCommandes(toutesCommandes, filtre); return; }
        List<Object[]> filtrees = toutesCommandes.stream()
            .filter(row -> {
                int id = (int) row[0];
                String etat = (String) row[3];
                return String.format("%05d", id).contains(t)
                    || etat.toLowerCase().contains(t);
            }).toList();
        afficherCommandes(filtrees, filtre);
    }

    private void afficherDetailsCommande(int commandeId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Détails commande N°" + String.format("%05d", commandeId), true);
        dialog.setSize(540, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BLANC);
        JPanel entete = new JPanel(new BorderLayout());
        entete.setBackground(new Color(0xFDF0FA));
        entete.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel titreDetail = new JLabel("Commande N°" + String.format("%05d", commandeId));
        titreDetail.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titreDetail.setForeground(ROSE);
        entete.add(titreDetail, BorderLayout.WEST);

 
        String[] cols = {"Produit", "Quantité", "Prix unitaire (FCFA)", "Total ligne (FCFA)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tableLignes = new JTable(model);
        tableLignes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableLignes.setRowHeight(36);
        tableLignes.setShowVerticalLines(false);
        tableLignes.setGridColor(new Color(0xF0F0F0));
        tableLignes.setBackground(BLANC);
        tableLignes.setSelectionBackground(ROSE_CLAIR);
        JTableHeader header = tableLignes.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(0xFDF0FA));
        header.setForeground(ROSE);

       
        double totalGlobal = 0;
        String sql = "SELECT p.nom, lc.quantite, lc.prix_unitaire, lc.montant_ligne "
                   + "FROM ligne_commande lc JOIN produit p ON lc.produit_id = p.id "
                   + "WHERE lc.commande_id = ? ORDER BY p.nom";
        try (java.sql.Connection con = CONNEXION.dbconnection.getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double montantLigne = rs.getDouble("montant_ligne");
                totalGlobal += montantLigne;
                model.addRow(new Object[]{
                    rs.getString("nom"),
                    rs.getInt("quantite"),
                    String.format("%,.0f", rs.getDouble("prix_unitaire")),
                    String.format("%,.0f", montantLigne)
                });
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            Validateur.afficherErreur(this, "Impossible de charger les détails : " + e.getMessage());
            return;
        }

        JScrollPane scroll = new JScrollPane(tableLignes);
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 12, 0, 12));


        JPanel pied = new JPanel(new BorderLayout());
        pied.setBackground(BLANC);
        pied.setBorder(new EmptyBorder(10, 18, 14, 18));
        JLabel lblTotalDetail = new JLabel(String.format("Total : %,.0f FCFA", totalGlobal));
        lblTotalDetail.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalDetail.setForeground(ROSE);
        JButton btnFermer = new JButton("Fermer");
        btnFermer.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnFermer.addActionListener(e -> dialog.dispose());
        pied.add(lblTotalDetail, BorderLayout.WEST);
        pied.add(btnFermer, BorderLayout.EAST);

        dialog.add(entete, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(pied,   BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    
    class DetailsBoutonRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
            p.setBackground(sel ? ROSE_CLAIR : BLANC);
            JButton btn = petitBouton("Détails", VIOLET);
            p.add(btn);
            return p;
        }
    }

    class DetailsBoutonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        private int currentRow;
        DetailsBoutonEditor() {
            panel.setBackground(BLANC);
            JButton btn = petitBouton("Détails", VIOLET);
            btn.addActionListener(e -> {
                fireEditingStopped();
                int id = extraireIdDepuisColonne(currentRow, 4);
                afficherDetailsCommande(id);
            });
            panel.add(btn);
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int row, int col) {
            currentRow = row; return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }

    class EtatRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            JLabel lbl = new JLabel(String.valueOf(val));
            lbl.setOpaque(true);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setHorizontalAlignment(CENTER);
            lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
            switch (String.valueOf(val)) {
                case "VALIDE"   -> { lbl.setForeground(VERT);   lbl.setBackground(new Color(0xE8F5E9)); }
                case "ANNULEE"  -> { lbl.setForeground(ROUGE);  lbl.setBackground(new Color(0xFFEBEE)); }
                default         -> { lbl.setForeground(ORANGE); lbl.setBackground(new Color(0xFFF8E1)); }
            }
            return lbl;
        }
    }

    class ActionRenderer extends JPanel implements TableCellRenderer {
        ActionRenderer() { setOpaque(true); setLayout(new FlowLayout(FlowLayout.CENTER, 4, 6)); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            removeAll();
            String etat = (String) tableModel.getValueAt(row, 3);
            if ("EN_COURS".equals(etat)) {
                add(petitBouton("Valider",  VERT));
                add(petitBouton("Annuler",  ROUGE));
            } else {
                JLabel lbl = new JLabel("—");
                lbl.setForeground(TEXTE_GRIS);
                add(lbl);
            }
            setBackground(sel ? ROSE_CLAIR : BLANC);
            return this;
        }
    }

    class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
        private int currentRow;

        ActionEditor() { panel.setBackground(BLANC); }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object val, boolean sel, int row, int col) {
            currentRow = row;
            panel.removeAll();
            String etat = (String) tableModel.getValueAt(row, 3);

            if ("EN_COURS".equals(etat)) {
                JButton btnVal = petitBouton("Valider", VERT);
                JButton btnAnn = petitBouton("Annuler", ROUGE);
                btnVal.addActionListener(e -> {
                    fireEditingStopped();
                    // Retrouver l'id depuis la colonne 4 stockée
                    int id = extraireId(currentRow);
                    validerCommande(id);
                });
                btnAnn.addActionListener(e -> {
                    fireEditingStopped();
                    int id = extraireId(currentRow);
                    annulerCommande(id);
                });
                panel.add(btnVal);
                panel.add(btnAnn);
            } else {
                JLabel lbl = new JLabel("—");
                lbl.setForeground(TEXTE_GRIS);
                panel.add(lbl);
            }
            return panel;
        }

        private int extraireId(int row) {
            return extraireIdDepuisColonne(row, 5);
        }

        @Override public Object getCellEditorValue() { return ""; }
    }

    // Renderer / Editor pour supprimer une ligne dans le dialog
    class SupprimerLigneRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            JButton btn = new JButton("✕");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(ROUGE);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            return btn;
        }
    }

    class SupprimerLigneEditor extends AbstractCellEditor implements TableCellEditor {
        private final DefaultTableModel model;
        private final JButton btn = new JButton("✕");
        private int row;
        SupprimerLigneEditor(DefaultTableModel m) {
            model = m;
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(ROUGE);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.addActionListener(e -> { fireEditingStopped(); model.removeRow(row); });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { row = r; return btn; }
        @Override public Object getCellEditorValue() { return ""; }
    }

    // Helper partagé pour extraire l'id stocké dans une colonne
    private int extraireIdDepuisColonne(int row, int col) {
        Object val = tableModel.getValueAt(row, col);
        if (val instanceof Integer) return (Integer) val;
        // Fallback: depuis la colonne N°XXXXX
        String numStr = (String) tableModel.getValueAt(row, 0);
        return Integer.parseInt(numStr.replace("N°", ""));
    }

    private JLabel badgeStat(String text, Color couleur) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(couleur);
        l.setOpaque(true);
        l.setBackground(new Color(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), 22));
        l.setBorder(new EmptyBorder(5, 12, 5, 12));
        return l;
    }

    private JButton boutonRose(String texte) {
        JButton btn = new JButton(texte) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ROSE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(BLANC);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private JButton petitBouton(String texte, Color couleur) {
        JButton btn = new JButton(texte) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(couleur);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(BLANC);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(72, 26));
        return btn;
    }
}