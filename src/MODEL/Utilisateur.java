/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MODEL;

/**
 *
 * @author nexa
 */
public class Utilisateur {
    private int id;
    private String login;
    private String motDePasse; 

    public Utilisateur() {}
    public Utilisateur(int id, String login, String motDePasse) {
        this.id = id;
        this.login = login;
        this.motDePasse = motDePasse;
    }

    // getters/setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
}