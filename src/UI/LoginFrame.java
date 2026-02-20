/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;
import DAO.UtilisateurDAO;
import MODEL.Utilisateur;
import UI.DashboardDataImpl;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;


/**
 *
 * @author nexa
 */

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

public class LoginFrame extends JFrame {

    // champs exposés pour l'action
    private PlaceholderTextField emailField;
    private PlaceholderPasswordField passwordField;
    private GradientButton signInButton;

    public LoginFrame() {
        setTitle("Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Panneau principal avec le style du conteneur
        JPanel container = new RoundedPanel(40);
        container.setLayout(new GridBagLayout());
        container.setBorder(new EmptyBorder(25, 35, 25, 35));
        container.setBackground(new Color(0xF8F9FD));

        container.setPreferredSize(new Dimension(350, 520));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);

        JLabel heading = new JLabel("Connexion");
        heading.setFont(new Font("SansSerif", Font.BOLD, 30));
        heading.setForeground(new Color(0x1089D3));
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        container.add(heading, gbc);

        emailField = new PlaceholderTextField("   Identifiant");
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        emailField.setPreferredSize(new Dimension(280, 50));
        emailField.setBackground(Color.WHITE);
        emailField.setBorder(new RoundedBorder(20, new Color(0x12B1D1), 2));
        container.add(emailField, gbc);

        passwordField = new PlaceholderPasswordField("  Mot de passe");
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(280, 50));
        passwordField.setBackground(Color.WHITE);
        passwordField.setBorder(new RoundedBorder(20, new Color(0x12B1D1), 2));
        container.add(passwordField, gbc);

        gbc.insets = new Insets(5, 0, 0, 0);

        signInButton = new GradientButton("Se connecter");
        signInButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        signInButton.setForeground(Color.WHITE);
        signInButton.setPreferredSize(new Dimension(280, 50));
        signInButton.setBorder(new RoundedBorder(20, null, 0));
        container.add(signInButton, gbc);

        signInButton.addActionListener(ev -> performLogin());

        add(container);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // AUTHENTIFICATION + REDIRECTION VERS LE DASHBOARD
    // ──────────────────────────────────────────────────────────────────────────
    private void performLogin() {
        final String login    = emailField.getText().trim();
        final String password = new String(passwordField.getPassword());

        if (login.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez remplir tous les champs.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        signInButton.setEnabled(false);

        SwingWorker<Utilisateur, Void> worker = new SwingWorker<>() {
            private Exception error = null;

            @Override
            protected Utilisateur doInBackground() {
                try {
                    UtilisateurDAO dao = new UtilisateurDAO();
                    return dao.authenticate(login, password);
                } catch (Exception ex) {
                    error = ex;
                    return null;
                }
            }

            @Override
            protected void done() {
                signInButton.setEnabled(true);

                if (error != null) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Erreur de connexion :\n" + error.getMessage(),
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    error.printStackTrace();
                    return;
                }

                try {
                    Utilisateur user = get();

                    if (user != null) {
    dispose();

    SwingUtilities.invokeLater(() -> {
        DashboardFrame dashboard = new DashboardFrame(
            new DashboardDataImpl(user.getLogin())
        );
        dashboard.setVisible(true);
    });

} else {
                       JOptionPane.showMessageDialog(LoginFrame.this,
            "Login ou mot de passe incorrect.",
            "Erreur d'authentification",
            JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Erreur inattendue : " + e.getMessage(),
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    // ------------------------------------------------------------
    // Panneau à fond dégradé et coins arrondis (avec ombre simulée)
    // ------------------------------------------------------------
    static class RoundedPanel extends JPanel {
        private final int radius;

        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Paint gradient = new GradientPaint(0, 0, new Color(0xFFFFFF),
                    0, getHeight(), new Color(0xF4F7FB));
            g2.setPaint(gradient);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.setColor(new Color(0x85BDD7, true));
            g2.setStroke(new BasicStroke(5));
            g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, radius, radius);

            g2.dispose();
        }
    }

    // ------------------------------------------------------------
    // Bordure arrondie avec couleur de focus éventuelle
    // ------------------------------------------------------------
    static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color focusColor;
        private final int thickness;

        public RoundedBorder(int radius, Color focusColor, int thickness) {
            this.radius    = radius;
            this.focusColor = focusColor;
            this.thickness  = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (focusColor != null && c.hasFocus()) {
                g2.setColor(focusColor);
                g2.setStroke(new BasicStroke(thickness));
                g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius, radius);
            } else {
                g2.setColor(new Color(0xCF, 0xF0, 0xFF, 80));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(x, y + 5, width - 1, height - 6, radius, radius);
            }
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(15, 20, 15, 20);
        }
    }

    // ------------------------------------------------------------
    // Champ de texte avec placeholder
    // ------------------------------------------------------------
    static class PlaceholderTextField extends JTextField {
        private final String placeholder;

        public PlaceholderTextField(String placeholder) {
            super();
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0xAAAAAA));
                g2.setFont(getFont().deriveFont(Font.PLAIN));
                Insets insets = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int x = insets.left;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(placeholder, x, y);
                g2.dispose();
            }
        }
    }

    // ------------------------------------------------------------
    // Champ de mot de passe avec placeholder
    // ------------------------------------------------------------
    static class PlaceholderPasswordField extends JPasswordField {
        private final String placeholder;

        public PlaceholderPasswordField(String placeholder) {
            super();
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getPassword().length == 0 && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0xAAAAAA));
                g2.setFont(getFont().deriveFont(Font.PLAIN));
                Insets insets = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int x = insets.left;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(placeholder, x, y);
                g2.dispose();
            }
        }
    }

    // ------------------------------------------------------------
    // Bouton avec dégradé et effets de survol / clic
    // ------------------------------------------------------------
    static class GradientButton extends JButton {
        private boolean hover   = false;
        private boolean pressed = false;

        public GradientButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e)  { hover = true;   repaint(); }
                @Override public void mouseExited(MouseEvent e)   { hover = false; pressed = false; repaint(); }
                @Override public void mousePressed(MouseEvent e)  { pressed = true;  repaint(); }
                @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Paint gradient = new GradientPaint(0, 0, new Color(0x1089D3),
                    getWidth(), getHeight(), new Color(0x12B1D1));
            g2.setPaint(gradient);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

            Color shadowColor = new Color(0x85BDD7, true);
            if (pressed) {
                g2.setColor(shadowColor);
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);
            } else if (hover) {
                g2.setColor(shadowColor);
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
            } else {
                g2.setColor(shadowColor);
                g2.fillRoundRect(2, 5, getWidth() - 4, getHeight() - 7, 20, 20);
            }

            FontMetrics fm = g2.getFontMetrics();
            Rectangle textRect = fm.getStringBounds(getText(), g2).getBounds();
            int x = (getWidth()  - textRect.width)  / 2;
            int y = (getHeight() - textRect.height) / 2 + fm.getAscent();
            if (pressed) { x += 1; y += 1; }
            g2.setColor(getForeground());
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }
}