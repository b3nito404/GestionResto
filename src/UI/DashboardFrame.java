/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

import UI.DashboardFrame.DashboardData.CommandeRecente;
import UI.DashboardFrame.DashboardData.TopProduit;
import UI.DashboardFrame.DashboardData.AlerteProduit;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;


/**
 *
 * @author nexa
 */

public class DashboardFrame extends JFrame {

    // ── Palette couleurs ───────────────────────────────────────────────────────
    private static final Color ROSE         = new Color(0xE91E8C);
    private static final Color ROSE_CLAIR   = new Color(0xFCE4F3);
    private static final Color VIOLET       = new Color(0x8B2FC9);
    private static final Color FOND         = new Color(0xF5F5F5);
    private static final Color BLANC        = Color.WHITE;
    private static final Color TEXTE_SOMBRE = new Color(0x1A1A2E);
    private static final Color TEXTE_GRIS   = new Color(0x9E9E9E);
    private static final Color ORANGE       = new Color(0xFFC107);
    private static final Color VERT         = new Color(0x4CAF50);
    private static final Color ROUGE        = new Color(0xF44336);

    // ── Donnees dynamiques ────────────────────────────────────────────────────
    private final DashboardData data;

    // ── Navigation ────────────────────────────────────────────────────────────
    private String navActive = "Tableau de bord";
    private JPanel centerArea;

    // ── Rafraichissement auto ─────────────────────────────────────────────────
   private javax.swing.Timer timerRefresh;
   
   private ProduitFrame   produitFrame;
   private StockFrame     stockFrame;
   private CommandeFrame  commandeFrame;
   private CategorieFrame categorieFrame;

    // ══════════════════════════════════════════════════════════════════════════
    // CONSTRUCTEUR
    // ══════════════════════════════════════════════════════════════════════════
    public DashboardFrame(DashboardData data) {
        this.data = data;

        setTitle("GoFood — Tableau de bord");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 860);
        setMinimumSize(new Dimension(1060, 720));
        setLocationRelativeTo(null);
        setBackground(FOND);

        JPanel racine = new JPanel(new BorderLayout());
        racine.setBackground(FOND);
        racine.add(buildSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(FOND);
        main.add(buildBarreHaut(), BorderLayout.NORTH);

        centerArea = new JPanel(new BorderLayout());
        centerArea.setBackground(FOND);
        afficherTableauDeBord();
        main.add(centerArea, BorderLayout.CENTER);

        racine.add(main, BorderLayout.CENTER);
        setContentPane(racine);

        timerRefresh = new javax.swing.Timer(30_000, e -> rafraichir());
        timerRefresh.start();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SIDEBAR
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(BLANC);
        sb.setPreferredSize(new Dimension(205, 0));
        sb.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0xF0E0EB)));

        // Logo
        JPanel logoP = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        logoP.setBackground(BLANC);
        logoP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        JLabel logo = new JLabel("GoFood.");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(ROSE);
        logoP.add(logo);
        sb.add(logoP);
        sb.add(separateur());
        sb.add(Box.createVerticalStrut(8));

        sb.add(etiquetteSection("GESTION"));
        sb.add(itemNav("Tableau de bord"));
        sb.add(itemNav("Produits"));
        sb.add(itemNav("Stock"));
        sb.add(itemNav("Commandes"));
        sb.add(Box.createVerticalStrut(6));
        sb.add(separateur());
        sb.add(Box.createVerticalStrut(6));

        sb.add(etiquetteSection("RAPPORTS"));
        sb.add(itemNav("Statistiques"));

        sb.add(Box.createVerticalGlue());
        sb.add(separateur());
        sb.add(itemDeconnexion());
        sb.add(Box.createVerticalStrut(10));
        return sb;
    }

    private JLabel etiquetteSection(String texte) {
        JLabel l = new JLabel("   " + texte);
        l.setFont(new Font("Segoe UI", Font.BOLD, 9));
        l.setForeground(new Color(0xBBBBBB));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        return l;
    }

    private JSeparator separateur() {
        JSeparator s = new JSeparator();
        s.setForeground(new Color(0xF0E0EB));
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    private JPanel itemNav(String label) {
        boolean actif = navActive.equals(label);
        JPanel p = new JPanel(new BorderLayout());
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel indicateur = new JPanel();
        indicateur.setPreferredSize(new Dimension(4, 46));
        indicateur.setBackground(actif ? ROSE : BLANC);
        p.add(indicateur, BorderLayout.WEST);

        JPanel contenu = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        contenu.setBackground(actif ? ROSE_CLAIR : BLANC);

        JPanel pastille = new JPanel();
        pastille.setPreferredSize(new Dimension(7, 7));
        pastille.setBackground(actif ? ROSE : TEXTE_GRIS);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", actif ? Font.BOLD : Font.PLAIN, 13));
        lbl.setForeground(actif ? ROSE : TEXTE_SOMBRE);

        contenu.add(pastille);
        contenu.add(lbl);
        p.add(contenu, BorderLayout.CENTER);

        if (!actif) {
            MouseAdapter ma = new MouseAdapter() {
                public void mouseEntered(MouseEvent e)  { contenu.setBackground(new Color(0xFFF5FB)); }
                public void mouseExited(MouseEvent e)   { contenu.setBackground(BLANC); }
                public void mouseClicked(MouseEvent e)  { changerPage(label); }
            };
            p.addMouseListener(ma);
            contenu.addMouseListener(ma);
        }
        return p;
    }

    private JPanel itemDeconnexion() {
        JPanel p = new JPanel(new BorderLayout());
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel contenu = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        contenu.setBackground(BLANC);

        JPanel pastille = new JPanel();
        pastille.setPreferredSize(new Dimension(7, 7));
        pastille.setBackground(ROUGE);

        JLabel lbl = new JLabel("Deconnexion");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(ROUGE);

        contenu.add(pastille);
        contenu.add(lbl);
        p.add(contenu, BorderLayout.CENTER);

        MouseAdapter ma = new MouseAdapter() {
            public void mouseEntered(MouseEvent e)  { contenu.setBackground(new Color(0xFFF0F0)); }
            public void mouseExited(MouseEvent e)   { contenu.setBackground(BLANC); }
            public void mouseClicked(MouseEvent e)  {
                timerRefresh.stop();
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            }
        };
        p.addMouseListener(ma);
        contenu.addMouseListener(ma);
        return p;
    }

    private void changerPage(String label) {
    navActive = label;

    JPanel racine = (JPanel) getContentPane();
    Component sidebarActuel = ((BorderLayout) racine.getLayout()).getLayoutComponent(BorderLayout.WEST);
    if (sidebarActuel != null) racine.remove(sidebarActuel);
    racine.add(buildSidebar(), BorderLayout.WEST);

    centerArea.removeAll();

    switch (label) {
        case "Tableau de bord" -> afficherTableauDeBord();

        case "Produits" -> {
            if (produitFrame == null) produitFrame = new ProduitFrame();
            else produitFrame.chargerDonnees();
            centerArea.add(produitFrame, BorderLayout.CENTER);
        }

        case "Catégories" -> {
            if (categorieFrame == null) categorieFrame = new CategorieFrame();
            else categorieFrame.chargerDonnees();
            centerArea.add(categorieFrame, BorderLayout.CENTER);
        }

        case "Stock" -> {
            if (stockFrame == null) stockFrame = new StockFrame();
            else stockFrame.chargerDonnees();
            centerArea.add(stockFrame, BorderLayout.CENTER);
        }

        case "Commandes" -> {
            if (commandeFrame == null) commandeFrame = new CommandeFrame();
            else commandeFrame.chargerDonnees();
            centerArea.add(commandeFrame, BorderLayout.CENTER);
        }

        case "Statistiques" -> afficherStatistiques();
    }

    racine.revalidate();
    racine.repaint();
}


    private void rafraichir() {
        data.reload();
        if (navActive.equals("Tableau de bord")) {
            centerArea.removeAll();
            afficherTableauDeBord();
            centerArea.revalidate();
            centerArea.repaint();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BARRE DU HAUT
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildBarreHaut() {
        JPanel barre = new JPanel(new BorderLayout());
        barre.setBackground(BLANC);
        barre.setPreferredSize(new Dimension(0, 62));
        barre.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF0E0EB)));

        JLabel titre = new JLabel("   Tableau de bord");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titre.setForeground(TEXTE_SOMBRE);
        barre.add(titre, BorderLayout.WEST);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        droite.setBackground(BLANC);

        int nb = data.getAlertCount();
        JLabel badgeAlerte = new JLabel(nb > 0 ? nb + " alerte(s) stock" : "Stock suffisant");
        badgeAlerte.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badgeAlerte.setForeground(nb > 0 ? ROUGE : VERT);
        badgeAlerte.setOpaque(true);
        badgeAlerte.setBackground(nb > 0 ? new Color(0xFFEBEE) : new Color(0xE8F5E9));
        badgeAlerte.setBorder(new EmptyBorder(4, 10, 4, 10));
        droite.add(badgeAlerte);

        JLabel userLabel = new JLabel("Utilisateur : " + data.getUsername());
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLabel.setForeground(TEXTE_SOMBRE);
        droite.add(userLabel);

        barre.add(droite, BorderLayout.EAST);
        return barre;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PAGE TABLEAU DE BORD
    // ══════════════════════════════════════════════════════════════════════════
    private void afficherTableauDeBord() {
        JScrollPane scroll = new JScrollPane(construireContenuTableauDeBord());
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        centerArea.add(scroll, BorderLayout.CENTER);
    }

    private JPanel construireContenuTableauDeBord() {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(FOND);
        c.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        c.add(ligneCartesStats());
        c.add(Box.createVerticalStrut(16));
        c.add(ligneGraphes());
        c.add(Box.createVerticalStrut(16));
        c.add(ligneJaugeEtAlertes());
        c.add(Box.createVerticalStrut(16));
        c.add(ligneCommandesEtTop());
        c.add(Box.createVerticalStrut(20));
        return c;
    }

    // ── Ligne 1 : 4 cartes stat ────────────────────────────────────────────────
    private JPanel ligneCartesStats() {
        JPanel ligne = new JPanel(new GridLayout(1, 4, 14, 0));
        ligne.setOpaque(false);
        ligne.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));

        ligne.add(carteStat("Commandes",    String.valueOf(data.getTotalCommandes()), new Color(0xFFE4F3), ROSE));
        ligne.add(carteStat("Produits",     String.valueOf(data.getTotalProduits()),  new Color(0xFDE8F5), VIOLET));
        ligne.add(carteStat("Alertes Stock",String.valueOf(data.getAlertCount()),     new Color(0xFFF8E1), ORANGE));
        ligne.add(carteStat("CA du jour",   data.getCaJourFormate(),                 new Color(0xE8F5E9), VERT));
        return ligne;
    }

    private JPanel carteStat(String label, String valeur, Color bgIcone, Color couleur) {
        JPanel card = new CartePanel();
        card.setLayout(new BorderLayout());
        card.setBackground(BLANC);

        JPanel haut = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        haut.setOpaque(false);

        // Icone : boite coloree avec initiale — sans emojis
        JPanel iconeBox = new JPanel(new GridBagLayout());
        iconeBox.setBackground(bgIcone);
        iconeBox.setPreferredSize(new Dimension(44, 44));
        JLabel initiale = new JLabel(String.valueOf(label.charAt(0)));
        initiale.setFont(new Font("Segoe UI", Font.BOLD, 18));
        initiale.setForeground(couleur);
        iconeBox.add(initiale);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblLabel.setForeground(TEXTE_GRIS);

        JLabel lblValeur = new JLabel(valeur);
        lblValeur.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValeur.setForeground(TEXTE_SOMBRE);

        info.add(lblLabel);
        info.add(lblValeur);
        haut.add(iconeBox);
        haut.add(info);
        card.add(haut, BorderLayout.NORTH);

        PanneauSparkline spark = new PanneauSparkline(data.getSparklineData(label), couleur);
        spark.setPreferredSize(new Dimension(0, 42));
        card.add(spark, BorderLayout.CENTER);
        return card;
    }

    // ── Ligne 2 : Graphe CA + Donut categories ─────────────────────────────────
    private JPanel ligneGraphes() {
        JPanel ligne = new JPanel(new BorderLayout(14, 0));
        ligne.setOpaque(false);
        ligne.setMaximumSize(new Dimension(Integer.MAX_VALUE, 310));

        // Graphe CA mensuel
        JPanel carteCA = new CartePanel();
        carteCA.setLayout(new BorderLayout());
        carteCA.setBackground(BLANC);

        JPanel enteteCA = new JPanel(new BorderLayout());
        enteteCA.setOpaque(false);
        enteteCA.setBorder(new EmptyBorder(14, 16, 0, 16));
        JLabel titreCA = new JLabel("Chiffre d'affaires mensuel");
        titreCA.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titreCA.setForeground(TEXTE_SOMBRE);
        JLabel sousCA = new JLabel("Commandes validees — annee en cours");
        sousCA.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sousCA.setForeground(TEXTE_GRIS);
        enteteCA.add(titreCA, BorderLayout.WEST);
        enteteCA.add(sousCA, BorderLayout.EAST);
        carteCA.add(enteteCA, BorderLayout.NORTH);
        carteCA.add(new PanneauGrapheCA(data.getCaMensuel()), BorderLayout.CENTER);
        ligne.add(carteCA, BorderLayout.CENTER);

        // Donut ventes par categorie
        JPanel carteDonut = new CartePanel();
        carteDonut.setPreferredSize(new Dimension(245, 0));
        carteDonut.setLayout(new BorderLayout());
        carteDonut.setBackground(BLANC);

        JPanel enteteDonut = new JPanel(new BorderLayout());
        enteteDonut.setOpaque(false);
        enteteDonut.setBorder(new EmptyBorder(14, 14, 0, 14));
        JLabel titreDonut = new JLabel("Ventes par categorie");
        titreDonut.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titreDonut.setForeground(TEXTE_SOMBRE);
        enteteDonut.add(titreDonut, BorderLayout.WEST);
        carteDonut.add(enteteDonut, BorderLayout.NORTH);
        carteDonut.add(new PanneauDonut(data.getCategoriesVentes()), BorderLayout.CENTER);
        ligne.add(carteDonut, BorderLayout.EAST);

        return ligne;
    }

    // ── Ligne 3 : Jauge objectif + Alertes stock ──────────────────────────────
    private JPanel ligneJaugeEtAlertes() {
        JPanel ligne = new JPanel(new BorderLayout(14, 0));
        ligne.setOpaque(false);
        ligne.setMaximumSize(new Dimension(Integer.MAX_VALUE, 225));

        // Jauge objectif journalier
        JPanel carteJauge = new CartePanel();
        carteJauge.setPreferredSize(new Dimension(245, 0));
        carteJauge.setLayout(new BorderLayout());
        carteJauge.setBackground(BLANC);

        JPanel enteteJauge = new JPanel(new BorderLayout());
        enteteJauge.setOpaque(false);
        enteteJauge.setBorder(new EmptyBorder(14, 14, 0, 14));
        JLabel titreJauge = new JLabel("Objectif journalier");
        titreJauge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titreJauge.setForeground(TEXTE_SOMBRE);
        enteteJauge.add(titreJauge, BorderLayout.WEST);
        carteJauge.add(enteteJauge, BorderLayout.NORTH);

        double caJ = data.getCaJour();
        double obj  = data.getObjectifJour();
        float  pct  = obj > 0 ? (float) Math.min(caJ / obj, 1.0) : 0f;
        carteJauge.add(
            new PanneauJauge(pct, data.getCaJourFormate(), "Objectif : " + data.getObjectifFormate()),
            BorderLayout.CENTER
        );
        ligne.add(carteJauge, BorderLayout.WEST);

        // Alertes stock
        JPanel carteAlertes = new CartePanel();
        carteAlertes.setLayout(new BorderLayout());
        carteAlertes.setBackground(BLANC);

        JPanel enteteAlertes = new JPanel(new BorderLayout());
        enteteAlertes.setOpaque(false);
        enteteAlertes.setBorder(new EmptyBorder(14, 16, 8, 16));
        JLabel titreAlertes = new JLabel("Produits en dessous du seuil d'alerte");
        titreAlertes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titreAlertes.setForeground(TEXTE_SOMBRE);
        int nb = data.getAlertCount();
        JLabel compteurAlertes = new JLabel(nb + " produit(s)");
        compteurAlertes.setFont(new Font("Segoe UI", Font.BOLD, 12));
        compteurAlertes.setForeground(nb > 0 ? ROUGE : VERT);
        enteteAlertes.add(titreAlertes, BorderLayout.WEST);
        enteteAlertes.add(compteurAlertes, BorderLayout.EAST);
        carteAlertes.add(enteteAlertes, BorderLayout.NORTH);

        JPanel listeAlertes = new JPanel();
        listeAlertes.setLayout(new BoxLayout(listeAlertes, BoxLayout.Y_AXIS));
        listeAlertes.setOpaque(false);
        listeAlertes.setBorder(new EmptyBorder(4, 16, 14, 16));

        List<AlerteProduit> alertes = data.getAlerteProduits();
        if (alertes.isEmpty()) {
            JLabel ok = new JLabel("Tous les stocks sont suffisants.");
            ok.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            ok.setForeground(VERT);
            listeAlertes.add(ok);
        } else {
            for (AlerteProduit ap : alertes) listeAlertes.add(ligneAlerte(ap));
        }

        JScrollPane scrollAlertes = new JScrollPane(listeAlertes);
        scrollAlertes.setBorder(null);
        scrollAlertes.setOpaque(false);
        scrollAlertes.getViewport().setOpaque(false);
        carteAlertes.add(scrollAlertes, BorderLayout.CENTER);
        ligne.add(carteAlertes, BorderLayout.CENTER);

        return ligne;
    }

    private JPanel ligneAlerte(AlerteProduit ap) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF5F5F5)),
            new EmptyBorder(6, 0, 6, 0)
        ));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel nom = new JLabel(ap.nom);
        nom.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nom.setForeground(TEXTE_SOMBRE);

        JLabel stockInfo = new JLabel("Stock : " + ap.stockActuel + "  |  Seuil : " + ap.seuilAlerte);
        stockInfo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        stockInfo.setForeground(ap.stockActuel == 0 ? ROUGE : ORANGE);

        p.add(nom, BorderLayout.WEST);
        p.add(stockInfo, BorderLayout.EAST);
        return p;
    }

    // ── Ligne 4 : Dernieres commandes + Top produits ───────────────────────────
    private JPanel ligneCommandesEtTop() {
        JPanel ligne = new JPanel(new GridLayout(1, 2, 14, 0));
        ligne.setOpaque(false);
        ligne.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        // Dernieres commandes
        JPanel carteCommandes = new CartePanel();
        carteCommandes.setLayout(new BorderLayout());
        carteCommandes.setBackground(BLANC);

        JPanel enteteCmd = new JPanel(new BorderLayout());
        enteteCmd.setOpaque(false);
        enteteCmd.setBorder(new EmptyBorder(14, 16, 8, 16));
        JLabel titreCmd = new JLabel("Dernieres commandes");
        titreCmd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titreCmd.setForeground(TEXTE_SOMBRE);
        enteteCmd.add(titreCmd, BorderLayout.WEST);
        carteCommandes.add(enteteCmd, BorderLayout.NORTH);

        JPanel listeCmd = new JPanel();
        listeCmd.setLayout(new BoxLayout(listeCmd, BoxLayout.Y_AXIS));
        listeCmd.setOpaque(false);
        listeCmd.setBorder(new EmptyBorder(0, 16, 14, 16));

        List<CommandeRecente> commandes = data.getCommandesRecentes(5);
        if (commandes.isEmpty()) {
            listeCmd.add(labelVide("Aucune commande enregistree."));
        } else {
            for (CommandeRecente c : commandes) listeCmd.add(ligneCommande(c));
        }
        JScrollPane scCmd = new JScrollPane(listeCmd);
        scCmd.setBorder(null); scCmd.setOpaque(false); scCmd.getViewport().setOpaque(false);
        carteCommandes.add(scCmd, BorderLayout.CENTER);
        ligne.add(carteCommandes);

        // Top produits
        JPanel carteTop = new CartePanel();
        carteTop.setLayout(new BorderLayout());
        carteTop.setBackground(BLANC);

        JPanel enteteTop = new JPanel(new BorderLayout());
        enteteTop.setOpaque(false);
        enteteTop.setBorder(new EmptyBorder(14, 16, 8, 16));
        JLabel titreTop = new JLabel("Top produits vendus");
        titreTop.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titreTop.setForeground(TEXTE_SOMBRE);
        enteteTop.add(titreTop, BorderLayout.WEST);
        carteTop.add(enteteTop, BorderLayout.NORTH);

        JPanel listeTop = new JPanel();
        listeTop.setLayout(new BoxLayout(listeTop, BoxLayout.Y_AXIS));
        listeTop.setOpaque(false);
        listeTop.setBorder(new EmptyBorder(0, 16, 14, 16));

        List<TopProduit> tops = data.getTopProduits(5);
        if (tops.isEmpty()) {
            listeTop.add(labelVide("Aucune vente enregistree."));
        } else {
            int rang = 1;
            for (TopProduit tp : tops) listeTop.add(ligneTop(rang++, tp));
        }
        JScrollPane scTop = new JScrollPane(listeTop);
        scTop.setBorder(null); scTop.setOpaque(false); scTop.getViewport().setOpaque(false);
        carteTop.add(scTop, BorderLayout.CENTER);
        ligne.add(carteTop);

        return ligne;
    }

    private JLabel labelVide(String msg) {
        JLabel l = new JLabel(msg);
        l.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        l.setForeground(TEXTE_GRIS);
        return l;
    }

    private JPanel ligneCommande(CommandeRecente c) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF5F5F5)),
            new EmptyBorder(6, 0, 6, 0)
        ));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel numCmd = new JLabel("N°" + String.format("%05d", c.id));
        numCmd.setFont(new Font("Segoe UI", Font.BOLD, 11));
        numCmd.setForeground(ROSE);
        numCmd.setPreferredSize(new Dimension(72, 20));

        JLabel dateCmd = new JLabel(c.date);
        dateCmd.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dateCmd.setForeground(TEXTE_GRIS);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        droite.setOpaque(false);

        JLabel montant = new JLabel(String.format("%,.0f FCFA", c.total));
        montant.setFont(new Font("Segoe UI", Font.BOLD, 12));
        montant.setForeground(TEXTE_SOMBRE);

        Color couleurEtat = switch (c.etat) {
            case "VALIDEE"  -> VERT;
            case "ANNULEE"  -> ROUGE;
            default         -> ORANGE;
        };
        String libelleEtat = switch (c.etat) {
            case "VALIDEE"  -> "Validee";
            case "ANNULEE"  -> "Annulee";
            default         -> "En cours";
        };
        JLabel statutCmd = new JLabel(libelleEtat);
        statutCmd.setFont(new Font("Segoe UI", Font.BOLD, 10));
        statutCmd.setForeground(couleurEtat);
        statutCmd.setOpaque(true);
        statutCmd.setBackground(new Color(couleurEtat.getRed(), couleurEtat.getGreen(), couleurEtat.getBlue(), 28));
        statutCmd.setBorder(new EmptyBorder(2, 8, 2, 8));

        droite.add(montant);
        droite.add(statutCmd);
        p.add(numCmd, BorderLayout.WEST);
        p.add(dateCmd, BorderLayout.CENTER);
        p.add(droite, BorderLayout.EAST);
        return p;
    }

    private JPanel ligneTop(int rang, TopProduit tp) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF5F5F5)),
            new EmptyBorder(6, 0, 6, 0)
        ));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel rangLbl = new JLabel(rang + ".");
        rangLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rangLbl.setForeground(ROSE);
        rangLbl.setPreferredSize(new Dimension(22, 20));

        JLabel nomLbl = new JLabel(tp.nom);
        nomLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nomLbl.setForeground(TEXTE_SOMBRE);

        JLabel qtyLbl = new JLabel(tp.quantite + " vendus");
        qtyLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        qtyLbl.setForeground(TEXTE_GRIS);

        p.add(rangLbl, BorderLayout.WEST);
        p.add(nomLbl, BorderLayout.CENTER);
        p.add(qtyLbl, BorderLayout.EAST);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PAGE STATISTIQUES
    // ══════════════════════════════════════════════════════════════════════════
    private void afficherStatistiques() {
        JScrollPane scroll = new JScrollPane(construireContenuStats());
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        centerArea.add(scroll, BorderLayout.CENTER);
    }

    private JPanel construireContenuStats() {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(FOND);
        c.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titre = new JLabel("Statistiques et rapports");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titre.setForeground(TEXTE_SOMBRE);
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(titre);
        c.add(Box.createVerticalStrut(18));

        // KPI
        JPanel ligneKPI = new JPanel(new GridLayout(1, 3, 14, 0));
        ligneKPI.setOpaque(false);
        ligneKPI.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        ligneKPI.add(carteKPI("CA Aujourd'hui",      data.getCaJourFormate(),                   ROSE));
        ligneKPI.add(carteKPI("CA Ce mois",           data.getCaMoisFormate(),                   VIOLET));
        ligneKPI.add(carteKPI("Commandes validees",  String.valueOf(data.getCommandesValidees()), VERT));
        c.add(ligneKPI);
        c.add(Box.createVerticalStrut(16));

        // Grand graphe CA
        JPanel carteGrandCA = new CartePanel();
        carteGrandCA.setLayout(new BorderLayout());
        carteGrandCA.setBackground(BLANC);
        carteGrandCA.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        JPanel entete = new JPanel(new BorderLayout());
        entete.setOpaque(false);
        entete.setBorder(new EmptyBorder(14, 16, 0, 16));
        JLabel titreGrandCA = new JLabel("Evolution du chiffre d'affaires — 12 mois");
        titreGrandCA.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titreGrandCA.setForeground(TEXTE_SOMBRE);
        entete.add(titreGrandCA, BorderLayout.WEST);
        carteGrandCA.add(entete, BorderLayout.NORTH);
        carteGrandCA.add(new PanneauGrapheCA(data.getCaMensuel()), BorderLayout.CENTER);
        c.add(carteGrandCA);
        c.add(Box.createVerticalStrut(16));

        // Barres horizontales top 10
        JPanel carteBarres = new CartePanel();
        carteBarres.setLayout(new BorderLayout());
        carteBarres.setBackground(BLANC);
        carteBarres.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        JPanel enteteProd = new JPanel(new BorderLayout());
        enteteProd.setOpaque(false);
        enteteProd.setBorder(new EmptyBorder(14, 16, 8, 16));
        JLabel titreProd = new JLabel("Top 10 produits les plus vendus");
        titreProd.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titreProd.setForeground(TEXTE_SOMBRE);
        enteteProd.add(titreProd, BorderLayout.WEST);
        carteBarres.add(enteteProd, BorderLayout.NORTH);
        carteBarres.add(new PanneauBarresHorizontales(data.getTopProduits(10)), BorderLayout.CENTER);
        c.add(carteBarres);
        c.add(Box.createVerticalStrut(20));

        return c;
    }

    private JPanel carteKPI(String label, String valeur, Color accent) {
        JPanel card = new CartePanel();
        card.setBackground(BLANC);
        card.setLayout(new BorderLayout());

        JPanel barre = new JPanel();
        barre.setBackground(accent);
        barre.setPreferredSize(new Dimension(0, 4));
        barre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));

        JPanel corps = new JPanel();
        corps.setLayout(new BoxLayout(corps, BoxLayout.Y_AXIS));
        corps.setOpaque(false);
        corps.setBorder(new EmptyBorder(14, 20, 16, 20));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLabel.setForeground(TEXTE_GRIS);

        JLabel lblValeur = new JLabel(valeur);
        lblValeur.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValeur.setForeground(accent);

        corps.add(lblLabel);
        corps.add(Box.createVerticalStrut(4));
        corps.add(lblValeur);

        card.add(barre, BorderLayout.NORTH);
        card.add(corps, BorderLayout.CENTER);
        return card;
    }

    // ── Placeholder pour les ecrans non encore implementes ─────────────────────
    private void afficherPlaceholder(String titre, String desc, String btnLabel, Runnable action) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(FOND);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(BLANC);
        inner.setBorder(new EmptyBorder(40, 60, 40, 60));

        JLabel t = new JLabel(titre);
        t.setFont(new Font("Segoe UI", Font.BOLD, 22));
        t.setForeground(TEXTE_SOMBRE);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel d = new JLabel("<html><center>" + desc + "</center></html>");
        d.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        d.setForeground(TEXTE_GRIS);
        d.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btn = boutonRose(btnLabel);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setPreferredSize(new Dimension(240, 38));
        btn.addActionListener(e -> action.run());

        inner.add(t);
        inner.add(Box.createVerticalStrut(12));
        inner.add(d);
        inner.add(Box.createVerticalStrut(24));
        inner.add(btn);

        p.add(inner);
        centerArea.add(p, BorderLayout.CENTER);
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // COMPOSANTS GRAPHIQUES CUSTOM
    // ══════════════════════════════════════════════════════════════════════════

    /** Carte avec ombre portee et coins arrondis */
    static class CartePanel extends JPanel {
        CartePanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 12));
            g2.fillRoundRect(3, 3, getWidth() - 2, getHeight() - 2, 16, 16);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Mini sparkline : courbe de tendance sur les dernieres valeurs */
    static class PanneauSparkline extends JPanel {
        private final int[]  donnees;
        private final Color  couleur;

        PanneauSparkline(int[] donnees, Color couleur) {
            this.donnees = (donnees != null && donnees.length > 1) ? donnees : new int[]{0, 0};
            this.couleur = couleur;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth(), h = getHeight(), n = donnees.length;
            if (n < 2) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int max = 1, min = Integer.MAX_VALUE;
            for (int v : donnees) { if (v > max) max = v; if (v < min) min = v; }
            if (max == min) max = min + 1;

            float xStep = (float) w / (n - 1);
            int[] xs = new int[n], ys = new int[n];
            for (int i = 0; i < n; i++) {
                xs[i] = (int)(i * xStep);
                ys[i] = h - 5 - (int)((donnees[i] - min) * (h - 10) / (float)(max - min));
            }

            GeneralPath fill = new GeneralPath();
            fill.moveTo(xs[0], h);
            for (int i = 0; i < n; i++) fill.lineTo(xs[i], ys[i]);
            fill.lineTo(xs[n-1], h);
            fill.closePath();
            g2.setPaint(new GradientPaint(0, 0,
                new Color(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), 65),
                0, h,
                new Color(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), 5)));
            g2.fill(fill);

            g2.setColor(couleur);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < n - 1; i++) g2.drawLine(xs[i], ys[i], xs[i+1], ys[i+1]);
            g2.dispose();
        }
    }

    /** Graphe barres + courbe pour le CA mensuel */
    static class PanneauGrapheCA extends JPanel {
        private final double[] ca;
        private static final String[] MOIS = {
            "Jan","Fev","Mar","Avr","Mai","Jun","Jul","Aou","Sep","Oct","Nov","Dec"
        };
        private static final Color ROSE_G      = new Color(0xE91E8C);
        private static final Color TEXTE_GRIS_G = new Color(0x9E9E9E);

        PanneauGrapheCA(double[] ca) {
            this.ca = (ca != null && ca.length == 12) ? ca : new double[12];
            setOpaque(false);
            setBorder(new EmptyBorder(10, 16, 16, 16));
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int padG = 52, padB = 26, padH = 10;
            int largG = w - padG - 12, hautG = h - padB - padH;
            float xStep = (float) largG / 12;
            int   largB = (int)(xStep * 0.48f);

            double max = 1;
            for (double v : ca) if (v > max) max = v;
            max = Math.ceil(max / 1000.0) * 1000;
            if (max == 0) max = 1000;

            // Lignes de grille
            for (int s = 0; s <= 4; s++) {
                double val = max * s / 4;
                int y = padH + hautG - (int)(val / max * hautG);
                g2.setColor(new Color(0xEEEEEE));
                g2.setStroke(new BasicStroke(0.7f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        1, new float[]{4, 4}, 0));
                g2.drawLine(padG, y, w - 12, y);
                g2.setColor(TEXTE_GRIS_G);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                String et = val >= 1000 ? String.format("%.0fk", val / 1000) : String.format("%.0f", val);
                g2.drawString(et, 2, y + 4);
            }

            // Barres
            for (int i = 0; i < 12; i++) {
                int x  = padG + (int)(i * xStep) + (int)(xStep / 2) - largB / 2;
                int hb = ca[i] > 0 ? (int)(ca[i] / max * hautG) : 2;
                int y  = padH + hautG - hb;
                g2.setPaint(new GradientPaint(x, y, ROSE_G, x, y + hb, new Color(0xF8BBD9)));
                g2.setStroke(new BasicStroke(1));
                g2.fillRoundRect(x, y, largB, hb, 5, 5);
                g2.setColor(TEXTE_GRIS_G);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(MOIS[i], padG + (int)(i * xStep) + (int)(xStep / 2) - 8, h - 8);
            }

            // Courbe de tendance
            int[] lx = new int[12], ly = new int[12];
            for (int i = 0; i < 12; i++) {
                lx[i] = padG + (int)(i * xStep) + (int)(xStep / 2);
                ly[i] = padH + hautG - (int)(ca[i] / max * hautG);
            }
            g2.setColor(ROSE_G);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 11; i++) {
                if (ca[i] > 0 || ca[i+1] > 0) g2.drawLine(lx[i], ly[i], lx[i+1], ly[i+1]);
            }
            for (int i = 0; i < 12; i++) {
                if (ca[i] > 0) {
                    g2.setColor(Color.WHITE);
                    g2.fillOval(lx[i]-4, ly[i]-4, 8, 8);
                    g2.setColor(ROSE_G);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawOval(lx[i]-4, ly[i]-4, 8, 8);
                    g2.setColor(Color.WHITE);
                }
            }
            g2.dispose();
        }
    }

    /** Graphe donut pour les ventes par categorie */
    static class PanneauDonut extends JPanel {
        private final Map<String, Double> categories;
        private static final Color[] PALETTE = {
            new Color(0xE91E8C), new Color(0x8B2FC9), new Color(0x2196F3),
            new Color(0xFF9800), new Color(0x4CAF50), new Color(0xF44336)
        };

        PanneauDonut(Map<String, Double> categories) {
            this.categories = (categories != null) ? categories : new LinkedHashMap<>();
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2, cy = getHeight() / 2 - 28;
            int r  = Math.min(cx, cy) - 18;
            int ep = 26;

            if (categories.isEmpty()) {
                g2.setColor(new Color(0xEEEEEE));
                g2.setStroke(new BasicStroke(ep));
                g2.drawArc(cx-r, cy-r, r*2, r*2, 0, 360);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.setColor(new Color(0x9E9E9E));
                FontMetrics fm = g2.getFontMetrics();
                String msg = "Aucune donnee";
                g2.drawString(msg, cx - fm.stringWidth(msg)/2, cy + 5);
            } else {
                double total = categories.values().stream().mapToDouble(Double::doubleValue).sum();
                if (total == 0) total = 1;
                float debut = -90;
                int idx = 0;
                for (Map.Entry<String, Double> e : categories.entrySet()) {
                    float sweep = (float)(e.getValue() / total * 360);
                    g2.setColor(PALETTE[idx % PALETTE.length]);
                    g2.setStroke(new BasicStroke(ep, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                    g2.drawArc(cx-r, cy-r, r*2, r*2, (int)debut, (int)sweep);
                    debut += sweep;
                    idx++;
                }
                g2.setColor(new Color(0x333333));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                String cnt = String.valueOf(categories.size());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(cnt, cx - fm.stringWidth(cnt)/2, cy + 7);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.setColor(new Color(0x9E9E9E));
                fm = g2.getFontMetrics();
                g2.drawString("categorie(s)", cx - fm.stringWidth("categorie(s)")/2, cy + 22);
            }

            // Legende
            int ly = getHeight() - 52;
            int lx = 8;
            int idx = 0;
            for (Map.Entry<String, Double> e : categories.entrySet()) {
                if (lx + 78 > getWidth()) { lx = 8; ly += 16; }
                g2.setColor(PALETTE[idx % PALETTE.length]);
                g2.fillRect(lx, ly, 9, 9);
                g2.setColor(new Color(0x555555));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                String lab = e.getKey().length() > 10 ? e.getKey().substring(0, 9) + "." : e.getKey();
                g2.drawString(lab, lx + 12, ly + 9);
                lx += 82;
                idx++;
            }
            g2.dispose();
        }
    }

    /** Jauge circulaire pour l'objectif journalier */
    static class PanneauJauge extends JPanel {
        private final float  pct;
        private final String valeur, sous;

        PanneauJauge(float pct, String valeur, String sous) {
            this.pct    = Math.min(1f, Math.max(0f, pct));
            this.valeur = valeur;
            this.sous   = sous;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2, cy = getHeight() / 2 - 12;
            int r  = Math.min(cx, cy) - 20;
            int ep = 18;

            // Fond
            g2.setColor(new Color(0xEEEEEE));
            g2.setStroke(new BasicStroke(ep, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(cx-r, cy-r, r*2, r*2, 225, -270);

            // Valeur (degrade rose -> violet)
            int sweep = -(int)(pct * 270);
            g2.setPaint(new GradientPaint(cx-r, cy, new Color(0xE91E8C), cx+r, cy, new Color(0x8B2FC9)));
            g2.setStroke(new BasicStroke(ep, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(cx-r, cy-r, r*2, r*2, 225, sweep);

            // Pourcentage
            g2.setColor(new Color(0x1A1A2E));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            String pctStr = (int)(pct * 100) + "%";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(pctStr, cx - fm.stringWidth(pctStr)/2, cy + 8);

            // CA et objectif
            int ty = getHeight() - 36;
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            fm = g2.getFontMetrics();
            g2.setColor(new Color(0x1A1A2E));
            g2.drawString(valeur, cx - fm.stringWidth(valeur)/2, ty);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(new Color(0x9E9E9E));
            fm = g2.getFontMetrics();
            g2.drawString(sous, cx - fm.stringWidth(sous)/2, ty + 15);
            g2.dispose();
        }
    }

 
    static class PanneauBarresHorizontales extends JPanel {
        private final List<TopProduit> produits;

        PanneauBarresHorizontales(List<TopProduit> produits) {
            this.produits = (produits != null) ? produits : new ArrayList<>();
            setOpaque(false);
            setBorder(new EmptyBorder(8, 20, 12, 20));
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (produits.isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                g2.setColor(new Color(0x9E9E9E));
                g2.drawString("Aucune donnee disponible.", 20, 30);
                g2.dispose();
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight(), n = produits.size();
            int maxQte = produits.stream().mapToInt(p -> p.quantite).max().orElse(1);
            if (maxQte == 0) maxQte = 1;

            int largLabel  = 148;
            int largBarMax = w - largLabel - 82;
            int hautRow    = h / n;

            for (int i = 0; i < n; i++) {
                TopProduit tp = produits.get(i);
                int yBase = i * hautRow + hautRow / 2;

                // Rang
                g2.setColor(new Color(0xE91E8C));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g2.drawString("#" + (i + 1), 0, yBase + 4);

                // Nom
                g2.setColor(new Color(0x1A1A2E));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                String nom = tp.nom.length() > 18 ? tp.nom.substring(0, 16) + "..." : tp.nom;
                g2.drawString(nom, 24, yBase + 4);

                // Barre proportionnelle
                int largBarre = (int)((double) tp.quantite / maxQte * largBarMax);
                g2.setPaint(new GradientPaint(
                    largLabel, yBase - 6, new Color(0xE91E8C),
                    largLabel + largBarre, yBase - 6, new Color(0xF48DC1)
                ));
                g2.fillRoundRect(largLabel, yBase - 7, Math.max(largBarre, 4), 12, 6, 6);

                // Quantite
                g2.setColor(new Color(0x1A1A2E));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.drawString(tp.quantite + " ventes", largLabel + largBarre + 6, yBase + 4);
            }
            g2.dispose();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // INTERFACE DashboardData — CONTRAT AVEC LES DAO
    // ══════════════════════════════════════════════════════════════════════════
    /**
     * Implementez cette interface dans DashboardDataImpl (connexion MySQL reelle).
     * Pour tester sans BDD, utilisez DashboardDataTest ci-dessous.
     */
    public interface DashboardData {

        /** Recharge toutes les donnees depuis la base de donnees */
        void reload();

        // Utilisateur connecte
        String getUsername();

        // Chiffres globaux
        int    getTotalCommandes();
        int    getTotalProduits();
        int    getAlertCount();
        int    getCommandesValidees();

        // Chiffre d'affaires
        double getCaJour();
        double getCaMois();
        double getObjectifJour();
        String getCaJourFormate();
        String getCaMoisFormate();
        String getObjectifFormate();

        // Donnees pour les graphes
        /** 12 valeurs CA : index 0 = Janvier, 11 = Decembre */
        double[] getCaMensuel();

        /** nom_categorie -> montant total vendu */
        Map<String, Double> getCategoriesVentes();

        /** Sparkline pour une carte stat donnee (8 a 12 valeurs) */
        int[] getSparklineData(String cardLabel);

        // Listes dynamiques
        List<CommandeRecente> getCommandesRecentes(int limit);
        List<TopProduit>      getTopProduits(int limit);
        List<AlerteProduit>   getAlerteProduits();

        // ── Classes de transfert de donnees (DTO) ─────────────────────────────
        class CommandeRecente {
            public final int    id;
            public final String date;
            public final String etat;   // "VALIDEE", "ANNULEE", ou "EN_COURS"
            public final double total;
            public CommandeRecente(int id, String date, String etat, double total) {
                this.id = id; this.date = date; this.etat = etat; this.total = total;
            }
        }

        class TopProduit {
            public final String nom;
            public final int    quantite;
            public final double ca;
            public TopProduit(String nom, int quantite, double ca) {
                this.nom = nom; this.quantite = quantite; this.ca = ca;
            }
        }

        class AlerteProduit {
            public final String nom;
            public final int    stockActuel;
            public final int    seuilAlerte;
            public AlerteProduit(String nom, int stockActuel, int seuilAlerte) {
                this.nom = nom; this.stockActuel = stockActuel; this.seuilAlerte = seuilAlerte;
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // IMPLEMENTATION DE TEST (sans BDD — pour developpement et debogage)
    // ══════════════════════════════════════════════════════════════════════════
    /**
     * Donnees factices pour tester l'interface sans connexion MySQL.
     * Dans App.java, remplacez par :
     *   new DashboardFrame(new DashboardDataImpl(user.getLogin())).setVisible(true);
     */
    public static class DashboardDataTest implements DashboardData {
        public void   reload()               {}
        public String getUsername()           { return "admin"; }
        public int    getTotalCommandes()     { return 248; }
        public int    getTotalProduits()      { return 34; }
        public int    getAlertCount()         { return 3; }
        public int    getCommandesValidees()  { return 195; }
        public double getCaJour()             { return 87500; }
        public double getCaMois()             { return 1_240_000; }
        public double getObjectifJour()       { return 120_000; }
        public String getCaJourFormate()      { return "87 500 FCFA"; }
        public String getCaMoisFormate()      { return "1 240 000 FCFA"; }
        public String getObjectifFormate()    { return "120 000 FCFA"; }

        public double[] getCaMensuel() {
            return new double[]{350000,520000,680000,420000,590000,460000,
                                620000,480000,540000,470000,610000,870000};
        }
        public Map<String, Double> getCategoriesVentes() {
            Map<String, Double> m = new LinkedHashMap<>();
            m.put("Plats",    650000.0);
            m.put("Boissons", 310000.0);
            m.put("Desserts", 180000.0);
            return m;
        }
        public int[] getSparklineData(String l) {
            return new int[]{20,35,28,50,42,65,55,70,60,80,72,87};
        }
        public List<CommandeRecente> getCommandesRecentes(int limit) {
            List<CommandeRecente> list = new ArrayList<>();
            list.add(new CommandeRecente(248, "19/02/2026 14:32", "EN_COURS",  4500));
            list.add(new CommandeRecente(247, "19/02/2026 12:10", "VALIDEE",  12800));
            list.add(new CommandeRecente(246, "18/02/2026 18:45", "VALIDEE",   7200));
            list.add(new CommandeRecente(245, "18/02/2026 09:20", "ANNULEE",   3300));
            list.add(new CommandeRecente(244, "17/02/2026 16:55", "VALIDEE",   9100));
            return list.subList(0, Math.min(limit, list.size()));
        }
        public List<TopProduit> getTopProduits(int limit) {
            List<TopProduit> list = new ArrayList<>();
            list.add(new TopProduit("Poulet Braise",     87, 261000));
            list.add(new TopProduit("Coca-Cola 50cl",     72,  36000));
            list.add(new TopProduit("Riz Sauce Arachide", 65, 195000));
            list.add(new TopProduit("Burger Special",     58, 174000));
            list.add(new TopProduit("Jus Naturel",        50,  50000));
            list.add(new TopProduit("Alloko Oeuf",        44,  88000));
            list.add(new TopProduit("Eau Minerale",       40,  12000));
            list.add(new TopProduit("Pizza Margherita",   33, 132000));
            list.add(new TopProduit("Cafe Express",       29,  14500));
            list.add(new TopProduit("Thieboudienne",      22,  88000));
            return list.subList(0, Math.min(limit, list.size()));
        }
        public List<AlerteProduit> getAlerteProduits() {
            List<AlerteProduit> list = new ArrayList<>();
            list.add(new AlerteProduit("Tomates fraiches",  2, 10));
            list.add(new AlerteProduit("Huile de palme",    1,  5));
            list.add(new AlerteProduit("Farine de ble",     0,  3));
            return list;
        }
    }
}