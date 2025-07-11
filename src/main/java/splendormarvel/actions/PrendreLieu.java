package splendormarvel.actions;

import splendormarvel.Jeu;
import splendormarvel.Joueur;

/**
 * Action permettant de conquérir un lieu
 * @author jeremie.humeau
 */
public class PrendreLieu implements Action{
    
    /**
     * Le lieu voulu
     */
    int id;
    
    /**
     * Le joueur actif
     */
    Joueur joueur;
    
    /**
     * Constructeur
     */
    public PrendreLieu(){}
    
   /**
     * Parse l'action LIEU
     * @param j le jeu dans son état actuel
     * @param place la place du joueur actif
     * @param action l'action LIEU ainsi que ses paramètres
     * @return vrai si l'action est valide, faux sinon
     */
    @Override
    public boolean parse(Jeu j, int place, String[] action) {
        boolean res=false;
        if(action.length!=2)// Vérifie que l'action a bien 1 seul paramètre (LIEU + l'id du lieu voulu)
            System.err.println("Action LIEU: nombre de paramètres incorrect");
        else{
            try{
                id=Integer.parseInt(action[1]); // fixe l'id du lieu voulu
            }        
            catch(NumberFormatException e){
                e.printStackTrace();
            }
            joueur=j.joueurs[j.ordre[place]]; // fixe le joueur actif
            res=verifier(j, place); // vérifie que l'action est valide
        }
        return res;
    }

     /**
     * Vérifie si l'action LIEU est valide en fonction de l'état du jeu actuel
     * @param j l'état du jeu actuel
     * @param place la place du joueur actif
     * @return vrai si l'action est valide, faux sinon
     */
    @Override
    public boolean verifier(Jeu j, int place) {
        boolean res=false;
        //Checke que le lieu est bien disponible, qu'il n'est pas déjà conquis et qu'il peut l'être par le joueur actif
        if(j.lieux[id].disponible && j.lieux[id].conquis<0 && j.lieux[id].peutEtreConquis(joueur)){
            res=true;
            appliquer(j, place); // Si c'est le cas on applique l'action
        }
        return res;
    }

    /**
     * Applique l'action LIEU dans le cas ou elle est valide
     * @param j le jeu dans son état actuel
     * @param place la place du joueur actif
     */
    @Override
    public void appliquer(Jeu j, int place) {
        j.lieux[id].conquis=j.ordre[place]; // On affecte le joueur comme possesseur du lieu
        joueur.points+=j.lieux[id].points; // On ajoute les points au joueur (généralement 3)
        joueur.lieuxConquis.add(j.lieux[id]); // On ajoute le lieu au joueur
    }
    
}
