package splendormarvel.actions;

import splendormarvel.Jeu;
import splendormarvel.Joueur;

/**
 * Action permettant de prendre des jetons
 * @author jeremie.humeau
 */
public class PrendreJeton implements Action {

    /**
     * les jetons que le joueur veut prendre
     */
    int[] jetonsVoulus;
    
    /**
     * le nombre de jetons que le joueur veut prendre
     */
    int nb;
    
    /**
     * le joueur actif
     */
    Joueur joueur;
    
    /**
     * Constructeur
     */
    public PrendreJeton(){
        jetonsVoulus= new int[3];
    }
    
    /**
     * Parse l'action JETONS
     * @param j le jeu dans son état actuel
     * @param place la place du joueur actif
     * @param action l'action JETONS ainsi que ses paramètres
     * @return vrai si l'action est valide, faux sinon
     */
    @Override
    public boolean parse(Jeu j, int place, String[] action) {
        boolean res=false;
        int tmp;
        if(action.length<2 || action.length>4){ // Vérifie que le nombre de paramètres est correct JETONS + 1, 2 ou 3 valeurs pour les jetons
            System.err.println("Action JETONS: nombre de paramètres incorrect");
        }
        else{
            nb=action.length-1; // fixe le nombre de jetons voulus
            joueur=j.joueurs[j.ordre[place]]; // fixe le joueur actif
            try{
                //fixe les jetons voulus
                for(int i=0; i<nb; i++){
                    tmp=Integer.parseInt(action[i+1]);
                    jetonsVoulus[i]=tmp;
                }
            }
            catch(NumberFormatException e){
                e.printStackTrace();
            }
            //Vérifie que l'action est valide
            res=verifier(j, place);
        }
        return res;
    }

    /**
     * Vérifie si l'action JETONS est valide en fonction de l'état du jeu actuel
     * @param j l'état du jeu actuel
     * @param place la place du joueur actif
     * @return vrai si l'action est valide, faux sinon
     */
    @Override
    public boolean verifier(Jeu j, int place) {
        boolean res;
        if(res=couleurValideEtDispo(j)){ //checke si les paramètres sont valides et que les couleurs correspondantes sont disponibles
            //Si c'est le cas on checke:
            // Dans le cas de 3 jetons qu'ils sont bien tous différents
            if(nb==3 && (jetonsVoulus[0]==jetonsVoulus[1] || jetonsVoulus[0]==jetonsVoulus[2] || jetonsVoulus[1]==jetonsVoulus[2]))
                res=false;
            // Dans le cas de 2 jetons identiques que le nombre de jetons dispos est bien d'au moins 4
            else if(nb==2 && jetonsVoulus[0]==jetonsVoulus[1] && j.jetons[jetonsVoulus[0]]<4)
                res=false;         
        }
        //Si ces conditions sont vérifiées on applique l'action
        if(res)
            appliquer(j, place);
        return res;
    }
    
    /**
     * Vérifie que les paramètres de l'action sont bien des couleurs valides et disponibles
     * @param j le jeu dans son état actuel
     * @return vrai si les couleurs sont valides et disponibles, faux sinon
     */
    public boolean couleurValideEtDispo(Jeu j){
        //le cas pour la disponibilité de 2 jetons identiques est traité dans vérifier
        boolean res=true;
        for(int i=0; i<nb; i++)
            if(jetonsVoulus[i]<0 || jetonsVoulus[i]>4 || j.jetons[jetonsVoulus[i]]<1)
                res=false;
        return res;
    }

    /**
     * Applique l'action JETONS dans le cas ou elle est valide
     * @param j le jeu dans son état actuel
     * @param place la place du joueur actif
     */
    @Override
    public void appliquer(Jeu j, int place) {
        for(int i=0; i<nb; i++){ // Pour chaque jeton voulu
            j.jetons[jetonsVoulus[i]]--; // On les enleve de la reserve du jeu
            joueur.jetons[jetonsVoulus[i]]++; // Pour les ajouter à celle du joueur
        }
    }
    
}
