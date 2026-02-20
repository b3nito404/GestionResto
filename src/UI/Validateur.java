/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;
import java.util.regex.Pattern;

/**
 *
 * @author nexa
 */

public class Validateur {

    // Autorise lettres (y compris accentuées), chiffres, espaces, tirets, apostrophes, points, virgules, parenthèses
    private static final Pattern NOM_VALIDE = Pattern.compile(
        "^[\\p{L}0-9 '\\-.,()&éèêëàâùûüîïôœç]{1,100}$",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );

    // Motif : lettres, chiffres, espaces et ponctuation courante — max 255 chars
    private static final Pattern MOTIF_VALIDE = Pattern.compile(
        "^[\\p{L}0-9 '\\-.,;:!?()/&%éèêëàâùûüîïôœç]{0,255}$",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );

    // ── Résultat de validation ─────────────────────────────────────────────────
    public static class Resultat {
        public final boolean ok;
        public final String  message;
        Resultat(boolean ok, String message) { this.ok = ok; this.message = message; }
    }

    public static Resultat ok()                  { return new Resultat(true,  null); }
    public static Resultat erreur(String msg)    { return new Resultat(false, msg);  }

    // ══════════════════════════════════════════════════════════════════════════
    // VALIDATIONS GÉNÉRIQUES
    // ══════════════════════════════════════════════════════════════════════════

    /** Vérifie qu'un nom (produit, catégorie…) est valide. */
    public static Resultat validerNom(String nom) {
        if (nom == null || nom.trim().isEmpty())
            return erreur("Le nom ne peut pas être vide.");
        nom = nom.trim();
        if (nom.length() < 2)
            return erreur("Le nom doit contenir au moins 2 caractères.");
        if (nom.length() > 100)
            return erreur("Le nom ne peut pas dépasser 100 caractères.");
        if (!NOM_VALIDE.matcher(nom).matches())
            return erreur("Le nom contient des caractères non autorisés.\n"
                        + "Seules les lettres, chiffres, espaces et la ponctuation courante sont acceptés.");
        return ok();
    }

    /** Vérifie qu'un prix est valide (nombre positif, max 10 millions). */
    public static Resultat validerPrix(String texte) {
        if (texte == null || texte.trim().isEmpty())
            return erreur("Le prix ne peut pas être vide.");
        try {
            double prix = Double.parseDouble(texte.trim().replace(",", "."));
            if (prix < 0)
                return erreur("Le prix ne peut pas être négatif.");
            if (prix == 0)
                return erreur("Le prix doit être supérieur à 0.");
            if (prix > 10_000_000)
                return erreur("Le prix semble anormalement élevé (max 10 000 000 FCFA).");
            return ok();
        } catch (NumberFormatException e) {
            return erreur("Le prix doit être un nombre valide (ex: 1500 ou 1500.50).");
        }
    }

    /** Vérifie qu'un stock est valide (entier >= 0, max 1 million). */
    public static Resultat validerStock(String texte) {
        if (texte == null || texte.trim().isEmpty())
            return erreur("La valeur du stock ne peut pas être vide.");
        try {
            int stock = Integer.parseInt(texte.trim());
            if (stock < 0)
                return erreur("Le stock ne peut pas être négatif.");
            if (stock > 1_000_000)
                return erreur("La valeur du stock semble anormalement élevée (max 1 000 000).");
            return ok();
        } catch (NumberFormatException e) {
            return erreur("Le stock doit être un nombre entier (ex: 50).");
        }
    }

    /** Vérifie qu'un seuil d'alerte est valide (entier >= 0). */
    public static Resultat validerSeuil(String texte) {
        if (texte == null || texte.trim().isEmpty())
            return erreur("Le seuil d'alerte ne peut pas être vide.");
        try {
            int seuil = Integer.parseInt(texte.trim());
            if (seuil < 0)
                return erreur("Le seuil d'alerte ne peut pas être négatif.");
            if (seuil > 100_000)
                return erreur("Le seuil d'alerte semble anormalement élevé (max 100 000).");
            return ok();
        } catch (NumberFormatException e) {
            return erreur("Le seuil doit être un nombre entier (ex: 10).");
        }
    }

    /** Vérifie qu'une quantité de mouvement est valide (entier strictement positif). */
    public static Resultat validerQuantite(String texte) {
        if (texte == null || texte.trim().isEmpty())
            return erreur("La quantité ne peut pas être vide.");
        try {
            int qty = Integer.parseInt(texte.trim());
            if (qty <= 0)
                return erreur("La quantité doit être un entier strictement positif (≥ 1).");
            if (qty > 100_000)
                return erreur("La quantité semble anormalement élevée (max 100 000).");
            return ok();
        } catch (NumberFormatException e) {
            return erreur("La quantité doit être un nombre entier (ex: 5).");
        }
    }

    /** Vérifie qu'un motif de mouvement de stock est valide (optionnel mais contrôlé). */
    public static Resultat validerMotif(String texte) {
        if (texte == null) return ok(); // optionnel
        texte = texte.trim();
        if (texte.length() > 255)
            return erreur("Le motif ne peut pas dépasser 255 caractères.");
        if (!texte.isEmpty() && !MOTIF_VALIDE.matcher(texte).matches())
            return erreur("Le motif contient des caractères non autorisés.");
        return ok();
    }

    
    public static Resultat validerCoherenceStockSeuil(int stock, int seuil) {
        if (seuil >= stock && stock > 0)
            return erreur("Attention : le seuil d'alerte (" + seuil + ") est supérieur ou égal au stock actuel (" + stock + ").\n"
                        + "Une alerte sera immédiatement déclenchée.\nVoulez-vous continuer ?");
        return ok();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MÉTHODE UTILITAIRE : afficher les erreurs de façon homogène
    // ══════════════════════════════════════════════════════════════════════════
    public static void afficherErreur(java.awt.Component parent, String message) {
        javax.swing.JOptionPane.showMessageDialog(
            parent, message, "Erreur de saisie",
            javax.swing.JOptionPane.ERROR_MESSAGE
        );
    }

    public static boolean confirmerAvertissement(java.awt.Component parent, String message) {
        int rep = javax.swing.JOptionPane.showConfirmDialog(
            parent, message, "Avertissement",
            javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.WARNING_MESSAGE
        );
        return rep == javax.swing.JOptionPane.YES_OPTION;
    }
}

