package splendormarvel;

import splendormarvel.utils.Couleur;

/**
 * Classe modélisant l'objet Lieu
 * @author jeremie.humeau
 */
public class Lieu {
    
    /**
     * Les couleurs nécéssaires pour les 2 faces du Lieu
     */
    public int[][] couleurs;

    /**
     * Les points gagnés par le Lieu (généralement 3)
     */
    public int points;

    /**
     * Le joueur ayant conquis le lieu (-1 si personne)
     */
    public int conquis;

    /**
     * La disponibilité du lieu pour la partie en cours
     */
    public boolean disponible;

    /**
     * La face du lieu active
     */
    public int face;

    /**
     * Les noms des 2 faces du Lieu
     */
    public String[] noms;
    
    /**
     * Constructeur
     * @param noms les noms des 2 faces du lieu
     * @param couleurs les couleurs des cartes nécessaires pour les 2 faces du lieu
     */
    public Lieu(String[] noms, int[][] couleurs){
        this.noms=noms; // les noms
        this.couleurs=couleurs; // les couleurs des cartes nécessaires
        points=3; // les 3 points
        conquis=-1; // Par défaut le lieu est non conquis
        disponible=false; // Par défaut le lieu n'est pas disponible
        face=-1; // Par défaut la face du lieu n'est pas affectée
    }

    /**
     * Est-ce que le lieu peut être conquis par un joueur
     * @param joueur le joueur en question
     * @return vrai si le joueur peut conquérir le lieu, faux sinon
     */
    public boolean peutEtreConquis(Joueur joueur){
        boolean res=true;
        for(int i=0; i<5; i++)
            if(joueur.reduc[i]<couleurs[face][i])
                res=false;
        return res;
    }
    
    /**
     * Utile à l'affichage d'un lieu
     * @return la chaine de caractères correspondant à l'état actuel du lieu
     */
    @Override
    public String toString(){
        String res=noms[face] + ":";
        for(int i=0; i<5; i++){
            if(couleurs[face][i]>0)
                res+= " " + Couleur.couleurs[i]+couleurs[face][i]+Couleur.RESET;
        }
        return res;
    }
    
}
