/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MODEL;

import java.time.LocalDateTime;

/**
 *
 * @author nexa
 */
public class MouvementStock {
    
     private int id;
    private MouvementType type;
    private Produit produit;
    private int quantite;
    private LocalDateTime dateMouvement;
    private String motif;

    public enum MouvementType {ENTREE, SORTIE}

    public MouvementStock() {}

    public MouvementStock(int id, MouvementType type, Produit produit, int quantite, LocalDateTime dateMouvement, String motif) {
        this.id = id;
        this.type = type;
        this.produit = produit;
        this.quantite = quantite;
        this.dateMouvement = dateMouvement;
        this.motif = motif;
    }

   
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public MouvementType getType() { return type; }
    public void setType(MouvementType type) { this.type = type; }
    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public LocalDateTime getDateMouvement() { return dateMouvement; }
    public void setDateMouvement(LocalDateTime dateMouvement) { this.dateMouvement = dateMouvement; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
}
    

