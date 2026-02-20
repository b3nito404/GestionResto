/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MODEL;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nexa
 */
public class Commande {
    
    public enum Etat {EN_COURS, VALIDE, ANNULEE}

    private int id;
    private LocalDateTime dateCommande;
    private Etat etat;
    private List<LigneCommande> lignes = new ArrayList<>();
    private double total;

    public Commande() {
        this.dateCommande = LocalDateTime.now();
        this.etat = Etat.EN_COURS;
    }

    public Commande(int id) {
        this.id = id;
        this.dateCommande = LocalDateTime.now();
        this.etat = Etat.EN_COURS;
    }

    // getters/setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDateTime dateCommande) { this.dateCommande = dateCommande; }
    public Etat getEtat() { return etat; }
    public void setEtat(Etat etat) { this.etat = etat; }
    public List<LigneCommande> getLignes() { return lignes; }
    public void setLignes(List<LigneCommande> lignes) { this.lignes = lignes; recalcTotal(); }
    public double getTotal() { return total; }

    public void addLigne(LigneCommande ligne) {
        lignes.add(ligne);
        recalcTotal();
    }

    public void removeLigne(LigneCommande ligne) {
        lignes.remove(ligne);
        recalcTotal();
    }

    public void recalcTotal() {
        total = 0;
        for (LigneCommande l : lignes) total += l.getMontantLigne();
    }
}
    
