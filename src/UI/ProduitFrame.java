/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

import DAO.CategorieDAO;
import DAO.ProduitDAO;
import MODEL.Categorie;
import MODEL.Produit;
import UI.Validateur;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 *
 * @author nexa
 */

public class ProduitFrame extends JPanel {

    // ── Palette (identique au Dashboard) ──────────────────────────────────────
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

    // ── DAO ───────────────────────────────────────────────────────────────────
    private final ProduitDAO   produitDAO   = new ProduitDAO();
    private final CategorieDAO categorieDAO = new CategorieDAO();

    // ── Composants UI ─────────────────────────────────────────────────────────
    private JTable            table;
    private DefaultTableModel tableModel;
    private JTextField        searchField;
    private JLabel            lblTotal, lblAlerte;

    public ProduitFrame() {
        setLayout(new BorderLayout());
        setBackground(FOND);
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        chargerDonnees();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TOOLBAR
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(BLANC);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF0E0EB)),
            new EmptyBorder(12, 18, 12, 18)
        ));

        // Gauche : stats rapides
        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        gauche.setOpaque(false);
        lblTotal  = badgeStat("0 produits", VIOLET);
        lblAlerte = badgeStat("0 alertes",  ROUGE);
        gauche.add(lblTotal);
        gauche.add(lblAlerte);

        // Droite : recherche + bouton ajouter
        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        droite.setOpaque(false);

        searchField = new JTextField(18);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new RoundedLineBorder(new Color(0xDDD0E8), 10, 1),
            new EmptyBorder(6, 12, 6, 12)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Rechercher un produit...");
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filtrer(searchField.getText()); }
        });

        JButton btnAjouter = boutonRose("+ Ajouter produit");
        btnAjouter.addActionListener(e -> ouvrirDialogProduit(null));

        droite.add(searchField);
        droite.add(btnAjouter);

        bar.add(gauche, BorderLayout.WEST);
        bar.add(droite, BorderLayout.EAST);
        return bar;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TABLE
    // ══════════════════════════════════════════════════════════════════════════
    private JScrollPane buildTable() {
        String[] cols = {"ID", "Nom", "Catégorie", "Prix vente (FCFA)", "Stock", "Seuil alerte", "État stock", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 7; }
        };

        table = new JTable(tableModel);
        styliserTable();

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);
        table.getColumnModel().getColumn(7).setPreferredWidth(130);

        // Rendu colonne "État stock"
        table.getColumnModel().getColumn(6).setCellRenderer(new EtatStockRenderer());

        // Boutons Actions
        table.getColumnModel().getColumn(7).setCellRenderer(new BoutonRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new BoutonEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scroll.setBackground(FOND);
        scroll.getViewport().setBackground(FOND);
        return scroll;
    }

    private void styliserTable() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(42);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0xF0F0F0));
        table.setBackground(BLANC);
        table.setSelectionBackground(ROSE_CLAIR);
        table.setSelectionForeground(TEXTE_SOMBRE);
        table.setFocusable(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(0xFDF0FA));
        header.setForeground(ROSE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ROSE_CLAIR));
        header.setReorderingAllowed(false);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DONNÉES
    // ══════════════════════════════════════════════════════════════════════════
    private List<Produit> tousProduits;

    public void chargerDonnees() {
        tousProduits = produitDAO.readAllProduit();
        afficherProduits(tousProduits);
    }

    private void afficherProduits(List<Produit> produits) {
        tableModel.setRowCount(0);
        int alertes = 0;
        for (Produit p : produits) {
            String cat = (p.getCategorie() != null) ? p.getCategorie().getLibelle() : "-";
            String etat;
            if (p.getStockActuel() == 0)              etat = "Rupture";
            else if (p.getStockActuel() <= p.getSeuilAlerte()) etat = "Alerte";
            else                                               etat = "OK";
            if (!etat.equals("OK")) alertes++;
            tableModel.addRow(new Object[]{
                p.getId(), p.getNom(), cat,
                String.format("%,.0f", p.getPrixVente()),
                p.getStockActuel(), p.getSeuilAlerte(),
                etat, "actions"
            });
        }
        lblTotal.setText(produits.size() + " produit(s)");
        lblAlerte.setText(alertes + " alerte(s)");
    }

    private void filtrer(String texte) {
        if (tousProduits == null) return;
        String t = texte.toLowerCase().trim();
        if (t.isEmpty()) { afficherProduits(tousProduits); return; }
        afficherProduits(tousProduits.stream()
            .filter(p -> p.getNom().toLowerCase().contains(t)
                || (p.getCategorie() != null && p.getCategorie().getLibelle().toLowerCase().contains(t)))
            .toList());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DIALOG AJOUT / MODIFICATION
    // ══════════════════════════════════════════════════════════════════════════
    private void ouvrirDialogProduit(Produit produitExistant) {
        boolean modeEdit = (produitExistant != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            modeEdit ? "Modifier le produit" : "Ajouter un produit", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BLANC);
        form.setBorder(new EmptyBorder(24, 28, 16, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 4, 6, 4);

        // ── Champs du formulaire ─────────────────────────────────────────────────
        JTextField fNom   = champ(modeEdit ? produitExistant.getNom() : "");
        JTextField fPrix  = champ(modeEdit ? String.valueOf(produitExistant.getPrixVente()) : "");
        JTextField fStock = champ(modeEdit ? String.valueOf(produitExistant.getStockActuel()) : "");
        JTextField fSeuil = champ(modeEdit ? String.valueOf(produitExistant.getSeuilAlerte()) : "");

        // ── ComboBox catégorie rechargeable + bouton "+" ─────────────────────
        final List<Categorie>[] catHolder = new List[]{ categorieDAO.readAllCategorie() };
        JComboBox<String> fCat = new JComboBox<>();
        fCat.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        Runnable rechargerCats = () -> {
            catHolder[0] = categorieDAO.readAllCategorie();
            fCat.removeAllItems();
            if (catHolder[0].isEmpty()) {
                fCat.addItem("— Aucune catégorie (cliquez + pour en créer) —");
            } else {
                for (Categorie c : catHolder[0]) fCat.addItem(c.getLibelle());
            }
        };
        rechargerCats.run();

        if (modeEdit && produitExistant.getCategorie() != null) {
            for (int i = 0; i < catHolder[0].size(); i++) {
                if (catHolder[0].get(i).getId() == produitExistant.getCategorie().getId()) {
                    fCat.setSelectedIndex(i); break;
                }
            }
        }

        // Bouton + pour créer une catégorie directement depuis le dialog
        JButton btnNouvelleCategorie = new JButton("+");
        btnNouvelleCategorie.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNouvelleCategorie.setForeground(BLANC);
        btnNouvelleCategorie.setBackground(ROSE);
        btnNouvelleCategorie.setOpaque(true);
        btnNouvelleCategorie.setBorderPainted(false);
        btnNouvelleCategorie.setFocusPainted(false);
        btnNouvelleCategorie.setPreferredSize(new Dimension(32, 32));
        btnNouvelleCategorie.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNouvelleCategorie.setToolTipText("Créer une nouvelle catégorie");
        btnNouvelleCategorie.addActionListener(e -> {
            String libelle = JOptionPane.showInputDialog(dialog,
                "Nom de la nouvelle catégorie :", "Nouvelle catégorie", JOptionPane.PLAIN_MESSAGE);
            if (libelle == null || libelle.trim().isEmpty()) return;
            Validateur.Resultat r = Validateur.validerNom(libelle);
            if (!r.ok) { Validateur.afficherErreur(dialog, r.message); return; }
            if (categorieDAO.existsByLibelle(libelle.trim())) {
                Validateur.afficherErreur(dialog, "La catégorie existe déjà."); return;
            }
            categorieDAO.saveCategorie(new Categorie(0, libelle.trim()));
            rechargerCats.run();
            fCat.setSelectedItem(libelle.trim());
        });

        JPanel panCat = new JPanel(new BorderLayout(6, 0));
        panCat.setOpaque(false);
        panCat.add(fCat, BorderLayout.CENTER);
        panCat.add(btnNouvelleCategorie, BorderLayout.EAST);

        int row = 0;
        ajouterLigne(form, g, row++, "Nom",              fNom);
        ajouterLigne(form, g, row++, "Catégorie",         panCat);
        ajouterLigne(form, g, row++, "Prix vente (FCFA)", fPrix);
        ajouterLigne(form, g, row++, "Stock actuel",      fStock);
        ajouterLigne(form, g, row++, "Seuil alerte",      fSeuil);

        // Référence stable aux catégories pour le bouton Enregistrer
        final List<Categorie> categories = catHolder[0];

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(BLANC);
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnAnnuler.addActionListener(e -> dialog.dispose());

        JButton btnSave = boutonRose(modeEdit ? "Modifier" : "Enregistrer");
        btnSave.setPreferredSize(new Dimension(120, 36));
        btnSave.addActionListener(e -> {
            // ── Validation nom ───────────────────────────────────────────────
            Validateur.Resultat rNom = Validateur.validerNom(fNom.getText());
            if (!rNom.ok) { Validateur.afficherErreur(dialog, rNom.message); fNom.requestFocus(); return; }

            // ── Validation prix ──────────────────────────────────────────────
            Validateur.Resultat rPrix = Validateur.validerPrix(fPrix.getText());
            if (!rPrix.ok) { Validateur.afficherErreur(dialog, rPrix.message); fPrix.requestFocus(); return; }

            // ── Validation stock ─────────────────────────────────────────────
            Validateur.Resultat rStock = Validateur.validerStock(fStock.getText());
            if (!rStock.ok) { Validateur.afficherErreur(dialog, rStock.message); fStock.requestFocus(); return; }

            // ── Validation seuil ─────────────────────────────────────────────
            Validateur.Resultat rSeuil = Validateur.validerSeuil(fSeuil.getText());
            if (!rSeuil.ok) { Validateur.afficherErreur(dialog, rSeuil.message); fSeuil.requestFocus(); return; }

            int stockVal = Integer.parseInt(fStock.getText().trim());
            int seuilVal = Integer.parseInt(fSeuil.getText().trim());

            // ── Avertissement cohérence stock/seuil ──────────────────────────
            Validateur.Resultat rCoherence = Validateur.validerCoherenceStockSeuil(stockVal, seuilVal);
            if (!rCoherence.ok) {
                if (!Validateur.confirmerAvertissement(dialog, rCoherence.message)) return;
            }

            try {
                String    nom  = fNom.getText().trim();
                double    prix = Double.parseDouble(fPrix.getText().trim().replace(",", "."));
                // Utiliser catHolder[0] qui est toujours à jour après rechargements
                List<Categorie> catsActuelles = catHolder[0];
                Categorie cat = (catsActuelles.isEmpty() || fCat.getSelectedIndex() < 0)
                    ? null : catsActuelles.get(fCat.getSelectedIndex());

                Produit p = modeEdit ? produitExistant : new Produit();
                p.setNom(nom);
                p.setPrixVente(prix);
                p.setStockActuel(stockVal);
                p.setSeuilAlerte(seuilVal);
                p.setCategorie(cat);

                if (modeEdit) produitDAO.updateProduit(p);
                else          produitDAO.saveProduit(p);

                dialog.dispose();
                chargerDonnees();
            } catch (Exception ex) {
                Validateur.afficherErreur(dialog, "Erreur inattendue : " + ex.getMessage());
            }
        });

        footer.add(btnAnnuler);
        footer.add(btnSave);
        dialog.add(form,   BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void supprimerProduit(int id) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer ce produit ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            produitDAO.deleteProduit(id);
            chargerDonnees();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // RENDERERS & EDITORS
    // ══════════════════════════════════════════════════════════════════════════
    class EtatStockRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            JLabel lbl = new JLabel(String.valueOf(val));
            lbl.setOpaque(true);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setHorizontalAlignment(CENTER);
            lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
            switch (String.valueOf(val)) {
                case "OK"      -> { lbl.setForeground(VERT);   lbl.setBackground(new Color(0xE8F5E9)); }
                case "Alerte"  -> { lbl.setForeground(ORANGE); lbl.setBackground(new Color(0xFFF8E1)); }
                case "Rupture" -> { lbl.setForeground(ROUGE);  lbl.setBackground(new Color(0xFFEBEE)); }
            }
            return lbl;
        }
    }

    class BoutonRenderer extends JPanel implements TableCellRenderer {
        BoutonRenderer() { setOpaque(true); setLayout(new FlowLayout(FlowLayout.CENTER, 4, 6)); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            removeAll();
            add(petitBouton("Modifier", VIOLET));
            add(petitBouton("Supprimer", ROUGE));
            setBackground(sel ? ROSE_CLAIR : BLANC);
            return this;
        }
    }

    class BoutonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
        private int currentRow;

        BoutonEditor() {
            JButton btnEdit = petitBouton("Modifier", VIOLET);
            JButton btnDel  = petitBouton("Supprimer", ROUGE);
            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) tableModel.getValueAt(currentRow, 0);
                Produit p = produitDAO.readProduit(id);
                if (p != null) ouvrirDialogProduit(p);
            });
            btnDel.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) tableModel.getValueAt(currentRow, 0);
                supprimerProduit(id);
            });
            panel.add(btnEdit);
            panel.add(btnDel);
            panel.setBackground(BLANC);
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object val, boolean sel, int row, int col) {
            currentRow = row;
            return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // UTILITAIRES UI
    // ══════════════════════════════════════════════════════════════════════════
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
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
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

    private JTextField champ(String val) {
        JTextField f = new JTextField(val);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedLineBorder(new Color(0xDDD0E8), 8, 1),
            new EmptyBorder(7, 10, 7, 10)
        ));
        return f;
    }

    private void ajouterLigne(JPanel form, GridBagConstraints g, int row, String label, JComponent comp) {
        g.gridx = 0; g.gridy = row; g.weightx = 0.3;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXTE_GRIS);
        form.add(l, g);
        g.gridx = 1; g.weightx = 0.7;
        form.add(comp, g);
    }

    // Bordure arrondie simple
    static class RoundedLineBorder extends AbstractBorder {
        private final Color color; private final int radius, thickness;
        RoundedLineBorder(Color c, int r, int t) { color = c; radius = r; thickness = t; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(thickness+2, thickness+2, thickness+2, thickness+2); }
    }
}