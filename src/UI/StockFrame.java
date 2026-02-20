/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

import DAO.MouvementStockDAO;
import DAO.ProduitDAO;
import MODEL.MouvementStock;
import MODEL.Produit;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *
 * @author nexa
 */

public class StockFrame extends JPanel {

    // ── Palette ───────────────────────────────────────────────────────────────
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

    // ── DAO ───────────────────────────────────────────────────────────────────
    private final MouvementStockDAO mouvementDAO = new MouvementStockDAO();
    private final ProduitDAO        produitDAO   = new ProduitDAO();

    // ── UI ────────────────────────────────────────────────────────────────────
    private JTable            table;
    private DefaultTableModel tableModel;
    private JComboBox<String> filtreType;
    private JTextField        searchField;
    private JLabel            lblEntrees, lblSorties;

    public StockFrame() {
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

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        gauche.setOpaque(false);
        lblEntrees = badgeStat("0 entrées", VERT);
        lblSorties = badgeStat("0 sorties", ROUGE);
        gauche.add(lblEntrees);
        gauche.add(lblSorties);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        droite.setOpaque(false);

        filtreType = new JComboBox<>(new String[]{"Tous", "ENTREE", "SORTIE"});
        filtreType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filtreType.addActionListener(e -> chargerDonnees());

        searchField = new JTextField(16);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new ProduitFrame.RoundedLineBorder(new Color(0xDDD0E8), 10, 1),
            new EmptyBorder(6, 12, 6, 12)
        ));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filtrer(searchField.getText()); }
        });

        JButton btnAjouter = boutonRose("+ Enregistrer mouvement");
        btnAjouter.addActionListener(e -> ouvrirDialogMouvement());

        droite.add(new JLabel("Type :"));
        droite.add(filtreType);
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
        String[] cols = {"ID", "Date", "Produit", "Type", "Quantité", "Motif"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(40);
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

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(250);

        // Rendu colonne "Type"
        table.getColumnModel().getColumn(3).setCellRenderer(new TypeMouvementRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scroll.setBackground(FOND);
        scroll.getViewport().setBackground(FOND);
        return scroll;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DONNÉES
    // ══════════════════════════════════════════════════════════════════════════
    private List<MouvementStock> tousMouvements;

    public void chargerDonnees() {
        tousMouvements = mouvementDAO.readAll();
        afficherMouvements(tousMouvements);
    }

    private void afficherMouvements(List<MouvementStock> mouvements) {
        tableModel.setRowCount(0);
        String filtre = (String) filtreType.getSelectedItem();
        long nbEntrees = 0, nbSorties = 0;

        for (MouvementStock m : mouvements) {
            String type = m.getType().name();
            if (!"Tous".equals(filtre) && !type.equals(filtre)) continue;

            if (type.equals("ENTREE")) nbEntrees++;
            else                       nbSorties++;

            String date    = m.getDateMouvement() != null ? m.getDateMouvement().format(FMT) : "-";
            String produit = m.getProduit() != null ? m.getProduit().getNom() : "-";

            tableModel.addRow(new Object[]{
                m.getId(), date, produit, type, m.getQuantite(), m.getMotif()
            });
        }
        lblEntrees.setText(nbEntrees + " entrée(s)");
        lblSorties.setText(nbSorties + " sortie(s)");
    }

    private void filtrer(String texte) {
        if (tousMouvements == null) return;
        String t = texte.toLowerCase().trim();
        if (t.isEmpty()) { afficherMouvements(tousMouvements); return; }
        afficherMouvements(tousMouvements.stream()
            .filter(m -> m.getProduit() != null
                && m.getProduit().getNom().toLowerCase().contains(t))
            .toList());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DIALOG ENREGISTRER MOUVEMENT
    // ══════════════════════════════════════════════════════════════════════════
    private void ouvrirDialogMouvement() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Enregistrer un mouvement de stock", true);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BLANC);
        form.setBorder(new EmptyBorder(24, 28, 16, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 4, 6, 4);

        List<Produit> produits = produitDAO.readAllProduit();
        String[] nomsProduits = produits.stream().map(Produit::getNom).toArray(String[]::new);

        JComboBox<String> fProduit = new JComboBox<>(nomsProduits);
        fProduit.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JComboBox<String> fType = new JComboBox<>(new String[]{"ENTREE", "SORTIE"});
        fType.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTextField fQte   = champ("");
        JTextField fMotif = champ("");

        int row = 0;
        ajouterLigne(form, g, row++, "Produit",   fProduit);
        ajouterLigne(form, g, row++, "Type",       fType);
        ajouterLigne(form, g, row++, "Quantité",   fQte);
        ajouterLigne(form, g, row++, "Motif",      fMotif);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(BLANC);
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnAnnuler.addActionListener(e -> dialog.dispose());

        JButton btnSave = boutonRose("Enregistrer");
        btnSave.setPreferredSize(new Dimension(130, 36));
        btnSave.addActionListener(e -> {
            // ── Validation quantité ──────────────────────────────────────────
            Validateur.Resultat rQte = Validateur.validerQuantite(fQte.getText());
            if (!rQte.ok) { Validateur.afficherErreur(dialog, rQte.message); fQte.requestFocus(); return; }

            // ── Validation motif ─────────────────────────────────────────────
            Validateur.Resultat rMotif = Validateur.validerMotif(fMotif.getText());
            if (!rMotif.ok) { Validateur.afficherErreur(dialog, rMotif.message); fMotif.requestFocus(); return; }

            try {
                if (produits.isEmpty()) throw new Exception("Aucun produit disponible.");

                int qte    = Integer.parseInt(fQte.getText().trim());
                Produit produit = produits.get(fProduit.getSelectedIndex());
                MouvementStock.MouvementType type = MouvementStock.MouvementType
                    .valueOf((String) fType.getSelectedItem());

                // ── Avertissement sortie > stock disponible ──────────────────
                if (type == MouvementStock.MouvementType.SORTIE && qte > produit.getStockActuel()) {
                    Validateur.afficherErreur(dialog,
                        "Stock insuffisant pour \"" + produit.getNom() + "\".\n"
                        + "Stock actuel : " + produit.getStockActuel()
                        + "  |  Quantité demandée : " + qte);
                    return;
                }

                MouvementStock m = new MouvementStock();
                m.setProduit(produit);
                m.setType(type);
                m.setQuantite(qte);
                m.setMotif(fMotif.getText().trim());
                m.setDateMouvement(java.time.LocalDateTime.now());

                int id = mouvementDAO.saveMouvementAndApply(m);
                if (id < 0) {
                    Validateur.afficherErreur(dialog,
                        "Impossible d'enregistrer ce mouvement.\nStock insuffisant ou produit invalide.");
                    return;
                }
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

    // ══════════════════════════════════════════════════════════════════════════
    // RENDERERS
    // ══════════════════════════════════════════════════════════════════════════
    class TypeMouvementRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            JLabel lbl = new JLabel(String.valueOf(val));
            lbl.setOpaque(true);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setHorizontalAlignment(CENTER);
            lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
            if ("ENTREE".equals(val)) { lbl.setForeground(VERT);  lbl.setBackground(new Color(0xE8F5E9)); }
            else                      { lbl.setForeground(ROUGE); lbl.setBackground(new Color(0xFFEBEE)); }
            return lbl;
        }
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
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private JTextField champ(String val) {
        JTextField f = new JTextField(val);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            new ProduitFrame.RoundedLineBorder(new Color(0xDDD0E8), 8, 1),
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
}