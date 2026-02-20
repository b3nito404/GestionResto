/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MODEL;

/**
 *
 * @author nexa
 */
public class Produit {
    
     private int id;
    private String nom;
    private double prixVente;
    private int stockActuel;
    private int seuilAlerte;
    private Categorie categorie;

 
    public Produit() {
    }

    public Produit(int id, String nom, double prixVente,
                   int stockActuel, int seuilAlerte, Categorie categorie) {
        this.id = id;
        this.nom = nom;
        this.prixVente = prixVente;
        this.stockActuel = stockActuel;
        this.seuilAlerte = seuilAlerte;
        this.categorie = categorie;
    }
   
    public Produit(String nom, double prixVente,
                   int stockActuel, int seuilAlerte, Categorie categorie) {
        this.nom = nom;
        this.prixVente = prixVente;
        this.stockActuel = stockActuel;
        this.seuilAlerte = seuilAlerte;
        this.categorie = categorie;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getPrixVente() {
        return prixVente;
    }

    public void setPrixVente(double prixVente) {
        this.prixVente = prixVente;
    }

    public int getStockActuel() {
        return stockActuel;
    }

    public void setStockActuel(int stockActuel) {
        this.stockActuel = stockActuel;
    }

    public int getSeuilAlerte() {
        return seuilAlerte;
    }

    public void setSeuilAlerte(int seuilAlerte) {
        this.seuilAlerte = seuilAlerte;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    @Override
    public String toString() {
        return nom;
    }
    
}
