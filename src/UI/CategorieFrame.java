/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

import DAO.CategorieDAO;
import MODEL.Categorie;

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

public class CategorieFrame extends JPanel {

    private static final Color ROSE         = new Color(0xE91E8C);
    private static final Color ROSE_CLAIR   = new Color(0xFCE4F3);
    private static final Color VIOLET       = new Color(0x8B2FC9);
    private static final Color FOND         = new Color(0xF5F5F5);
    private static final Color BLANC        = Color.WHITE;
    private static final Color TEXTE_SOMBRE = new Color(0x1A1A2E);
    private static final Color TEXTE_GRIS   = new Color(0x9E9E9E);
    private static final Color ROUGE        = new Color(0xF44336);
    private static final Color VERT         = new Color(0x4CAF50);

    private final CategorieDAO categorieDAO = new CategorieDAO();
    private JTable            table;
    private DefaultTableModel tableModel;
    private JTextField        searchField;
    private JLabel            lblTotal;

    public CategorieFrame() {
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

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        gauche.setOpaque(false);
        lblTotal = badgeStat("0 catégorie(s)", VIOLET);
        gauche.add(lblTotal);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        droite.setOpaque(false);

        searchField = new JTextField(18);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new ProduitFrame.RoundedLineBorder(new Color(0xDDD0E8), 10, 1),
            new EmptyBorder(6, 12, 6, 12)
        ));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filtrer(searchField.getText()); }
        });

        JButton btnAjouter = boutonRose("+ Ajouter catégorie");
        btnAjouter.addActionListener(e -> ouvrirDialog(null));

        droite.add(searchField);
        droite.add(btnAjouter);

        bar.add(gauche, BorderLayout.WEST);
        bar.add(droite, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID", "Libellé", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 2; }
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

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(400);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);

        table.getColumnModel().getColumn(2).setCellRenderer(new BoutonRenderer());
        table.getColumnModel().getColumn(2).setCellEditor(new BoutonEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scroll.getViewport().setBackground(FOND);
        return scroll;
    }

    private List<Categorie> toutesCategories;

    public void chargerDonnees() {
        toutesCategories = categorieDAO.readAllCategorie();
        afficherCategories(toutesCategories);
    }

    private void afficherCategories(List<Categorie> categories) {
        tableModel.setRowCount(0);
        for (Categorie c : categories) {
            tableModel.addRow(new Object[]{ c.getId(), c.getLibelle(), "actions" });
        }
        lblTotal.setText(categories.size() + " catégorie(s)");
    }

    private void filtrer(String texte) {
        if (toutesCategories == null) return;
        String t = texte.toLowerCase().trim();
        if (t.isEmpty()) { afficherCategories(toutesCategories); return; }
        afficherCategories(toutesCategories.stream()
            .filter(c -> c.getLibelle().toLowerCase().contains(t))
            .toList());
    }

   
    public void ouvrirDialog(Categorie existante) {
        boolean modeEdit = (existante != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            modeEdit ? "Modifier la catégorie" : "Ajouter une catégorie", true);
        dialog.setSize(380, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BLANC);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BLANC);
        form.setBorder(new EmptyBorder(24, 28, 10, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 4, 6, 4);

        JTextField fLibelle = champ(modeEdit ? existante.getLibelle() : "");

        g.gridx = 0; g.gridy = 0; g.weightx = 0.3;
        JLabel lbl = new JLabel("Libellé");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXTE_GRIS);
        form.add(lbl, g);
        g.gridx = 1; g.weightx = 0.7;
        form.add(fLibelle, g);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(BLANC);

        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnAnnuler.addActionListener(e -> dialog.dispose());

        JButton btnSave = boutonRose(modeEdit ? "Modifier" : "Enregistrer");
        btnSave.setPreferredSize(new Dimension(120, 36));
        btnSave.addActionListener(e -> {
            // ── Validation ────────────────────────────────────────────────────
            Validateur.Resultat r = Validateur.validerNom(fLibelle.getText());
            if (!r.ok) { Validateur.afficherErreur(dialog, r.message); fLibelle.requestFocus(); return; }

            String libelle = fLibelle.getText().trim();

            // ── Vérifier doublon ──────────────────────────────────────────────
            if (!modeEdit && categorieDAO.existsByLibelle(libelle)) {
                Validateur.afficherErreur(dialog, "La catégorie \"" + libelle + "\" existe déjà.");
                return;
            }

            if (modeEdit) {
                existante.setLibelle(libelle);
                categorieDAO.updateCategorie(existante);
            } else {
                categorieDAO.saveCategorie(new Categorie(0, libelle));
            }
            dialog.dispose();
            chargerDonnees();
        });

        footer.add(btnAnnuler);
        footer.add(btnSave);
        dialog.add(form,   BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);

        // Focus automatique sur le champ
        dialog.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) { fLibelle.requestFocusInWindow(); }
        });

        dialog.setVisible(true);
    }

    private void supprimerCategorie(int id, String libelle) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer la catégorie \"" + libelle + "\" ?\n"
            + "Attention : les produits liés perdront leur catégorie.",
            "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            categorieDAO.deleteCategorie(id);
            chargerDonnees();
        }
    }

    class BoutonRenderer extends JPanel implements TableCellRenderer {
        BoutonRenderer() { setOpaque(true); setLayout(new FlowLayout(FlowLayout.CENTER, 4, 8)); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            removeAll();
            add(petitBouton("Modifier",   VIOLET));
            add(petitBouton("Supprimer",  ROUGE));
            setBackground(sel ? ROSE_CLAIR : BLANC);
            return this;
        }
    }

    class BoutonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 8));
        private int currentRow;

        BoutonEditor() {
            JButton btnEdit = petitBouton("Modifier",  VIOLET);
            JButton btnDel  = petitBouton("Supprimer", ROUGE);

            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                int id          = (int)    tableModel.getValueAt(currentRow, 0);
                String libelle  = (String) tableModel.getValueAt(currentRow, 1);
                ouvrirDialog(new Categorie(id, libelle));
            });

            btnDel.addActionListener(e -> {
                fireEditingStopped();
                int id         = (int)    tableModel.getValueAt(currentRow, 0);
                String libelle = (String) tableModel.getValueAt(currentRow, 1);
                supprimerCategorie(id, libelle);
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
        btn.setPreferredSize(new Dimension(76, 26));
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
}