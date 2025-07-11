package splendormarvel.actions;

import splendormarvel.Jeu;
import splendormarvel.Joueur;

/**
 * Action permettant de remettre des jetons (généralement utilisé quand un joueur dépasse les 10 jetons)
 * @author jeremie.humeau
 */
public class RemettreJeton implements Action{
    
    /**
     * Les jetons que le joueur actif souhaite remettre en jeu
     */
    int[] jetonsRemis;
    
    /**
     * le décompte des jetons voulant être remis pour chaque couleur
     */
    int[] cpt;
    
    /**
     * le nombre de jetons que le joueur actif veut remettre
     */
    int nb;
    
    /**
     * le joueur actif
     */
    Joueur joueur;
    
    /**
     * Constructeur
     */
    public RemettreJeton(){
        jetonsRemis= new int[3];
        cpt=new int[6];
    }
    
   /**
     * Parse l'action REMETTRE
     * @param j le jeu dans son état actuel
     * @param place la place du joueur actif
     * @param action l'action REMETTRE ainsi que ses paramètres
     * @return vrai si l'action est valide, faux sinon
     */
    @Override
    public boolean parse(Jeu j, int place, String[] action) {
        boolean res=false;
        int tmp;
        if(action.length<2 || action.length>4){ //On checke que le nombre de param est correct (REMETTRE + 1 à 3 valeurs pour les jetons)
            System.err.println("Action REMETTRE: nombre de paramètres incorrect");
        }
        else{
            nb=action.length-1; // on fixe le nombre de jetons à remettre
            joueur=j.joueurs[j.ordre[place]]; // on fixe le joueur actif
            try{
                for(int i=0; i<nb; i++){
                    tmp=Integer.parseInt(action[i+1]); // On fixe les jetons à remettre
                    jetonsRemis[i]=tmp;
                }
            }
            catch(NumberFormatException e){
                e.printStackTrace();
            }
            res=verifier(j, place); // On vérifie si l'action est valide
        }
        return res;
    }

     /**
     * Vérifie si l'action REMETTRE est valide en fonction de l'état du jeu actuel
     * @param j l'état du jeu actuel
     * @param place la place du joueur actif
     * @return vrai si l'action est valide, faux sinon
     */
    @Override
    public boolean verifier(Jeu j, int place) {
        boolean res=true;
        for(int i=0; i<6; i++)
            cpt[i]=0; // On réinitialise le tableau pour compter les jetons
        for(int i=0; i<nb; i++){
            //Pour chaque paramètre on vérifie que c'est une couleur valide (compris dans [0,5], on ne peut pas remettre un jeton vert)
            if(jetonsRemis[i]<0 || jetonsRemis[i]>5)
                res=false;
            else
                cpt[jetonsRemis[i]]++; // si c'est le cas on enregistre l'info
        }
        if(res){ // Pour chaque couleur on vérifie que le joueur a bien les jetons voulus
            for(int i=0; i<6; i++){
                if(joueur.jetons[i]<cpt[i])
                    res=false;
            }
        }
        if(res)
            appliquer(j, place); // Si l'action est valide, on l'applique
        return res;
    }

    /**
     * Applique l'action REMETTRE dans le cas ou elle est valide
     * @param j le jeu dans son état actuel
     * @param place la place du joueur actif
     */
    @Override
    public void appliquer(Jeu j, int place) {
        for(int i=0; i<6; i++){ // Pour chaque couleur
            j.jetons[i]+=cpt[i]; // On remet les jetons nécéssaires disponibles
            joueur.jetons[i]-=cpt[i]; // Et on les enlève au joueur actif
        }
    }
    
}
