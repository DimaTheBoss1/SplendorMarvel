package splendormarvel;

import java.util.ArrayList;
import splendormarvel.ia.Strategie;

/**
 * Classe abstraite représentant un joueur
 * @author jeremie.humeau
 */
public abstract class Joueur implements Strategie{
    
    /**
     * Le nom du joueur
     */
    public String nom;

    /**
     * Les jetons du joueur
     */
    public int[] jetons;

    /**
     * Le nombre de reductions (cartes) de chaque couleur que le joueur possède
     */
    public int[] reduc;

    /**
     * La liste des Personnages que le joueur a recrutés
     */
    public ArrayList<Personnage> persos;

    /**
     * La liste des Peronnage que le joueur a dans sa main
     */
    public ArrayList<Personnage> main;

    /**
     * La liste des Lieux conquis par le joueur
     */
    public ArrayList<Lieu> lieuxConquis;

    /**
     * Le nombre d'avengers du joueur
     */
    public int avenger;

    /**
     * Possède ou non le rassemblement des avengers
     */
    public boolean rassemblement;

    /**
     * Le nombre de points du joueur
     */
    public int points;
    
    /**
     * Est un joueur humain
     */   
    public boolean humain;
    
    /**
     * Constructeur
     * @param nom le nom du joueur
     */
    public Joueur(String nom){
        // On initialise toutes les valeurs
        this.nom=nom;
        jetons=new int[]{0,0,0,0,0,0,0};
        reduc=new int[]{0,0,0,0,0};
        persos=new ArrayList();
        main=new ArrayList();
        lieuxConquis= new ArrayList();
        avenger=0;
        rassemblement=false;
        points=0;
    }
    
    /**
     * Initialise tous les attributs du joueur
     */
    public void clear(){
        jetons=new int[]{0,0,0,0,0,0,0};
        reduc=new int[]{0,0,0,0,0};
        lieuxConquis.clear();
        avenger=0;
        rassemblement=false;
        points=0;
        persos=new ArrayList();
        main=new ArrayList();
    }  
}
