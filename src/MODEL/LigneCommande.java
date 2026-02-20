/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MODEL;

/**
 *
 * @author nexa
 */
public class LigneCommande {
    private int id;
    private Produit produit;
    private int quantite;
    private double prixUnitaire;
    private double montantLigne;

    public LigneCommande() {}

    public LigneCommande(int id, Produit produit, int quantite, double prixUnitaire) {
        this.id = id;
        this.produit = produit;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.montantLigne = prixUnitaire * quantite;
    }

    // getters / setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { 
        this.quantite = quantite; 
        this.montantLigne = this.prixUnitaire * this.quantite;
    }
    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { 
        this.prixUnitaire = prixUnitaire; 
        this.montantLigne = this.prixUnitaire * this.quantite;
    }
    public double getMontantLigne() { return montantLigne; }
}