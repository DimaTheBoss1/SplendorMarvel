package splendormarvel.actions;

import splendormarvel.Jeu;
import splendormarvel.Joueur;
import splendormarvel.Personnage;

/**
 * Action permettant de réserver une carte
 * @author jeremie.humeau
 */
public class ReserverCarte implements Action{
    
    /**
     * l'emplacement de la carte voulue
     */
    int[] id;
    
    /**
     * le joueur actif
     */
    Joueur joueur;

    /**
     * Constructeur
     */
    public ReserverCarte(){
        id= new int[2];
    }
    
   /**
     * Parse l'action RESERVER
     * @param j le jeu dans son état actuel
     * @param place la place du joueur actif
     * @param action l'action RESERVER ainsi que ses paramètres
     * @return vrai si l'action est valide, faux sinon
     */
    @Override
    public boolean parse(Jeu j, int place, String[] action) {
        boolean res=false;
        if(action.length!=3){ // Checke que le nombre de param est correct (RESERVER + niveau + position de la carte)
            System.err.println("Action RESERVER: nombre de paramètres incorrect");
        }
        else{
            try{
                id[0]=Integer.parseInt(action[1]); // on fixe le niveau de la carte
                id[1]=Integer.parseInt(action[2]); // on fixe la position
            }        
            catch(NumberFormatException e){
                e.printStackTrace();
            }
            joueur=j.joueurs[j.ordre[place]]; // on fixe le joueur actif
            res=verifier(j, place); // on vérifie si l'action est valide
        }
        return res;
    }
    
     /**
     * Vérifie si l'action RESERVER est valide en fonction de l'état du jeu actuel
     * @param j l'état du jeu actuel
     * @param place la place du joueur actif
     * @return vrai si l'action est valide, faux sinon
     */
    @Override
    public boolean verifier(Jeu j, int place) {
        boolean res=true;
        // L'action n'est pas valide si
        // Le joueur a déjà 3 cartes en main
        // OU si le niveau n'est pas correct
        // OU si la position ciblée ne contient plus de carte
        if(joueur.main.size()==3 || id[0]<0 || id[0]>2 || ((id[1]>=0 && id[1]<=3) && j.persos[id[0]][id[1]]==null) || ((id[1]<0 || id[1]>3) && j.pioches[id[0]].getTaille()==0)){
            res=false;
        }
        if(res)
            appliquer(j, place); // Si l'action est valide on l'applique
        return res;
    }

    /**
     * Applique l'action RESERVER dans le cas ou elle est valide
     * @param j le jeu dans son état actuel
     * @param place la place du joueur actif
     */
    @Override
    public void appliquer(Jeu j, int place) {
        if(id[1]<0 || id[1]>3){ // Si la position de la carte n'est pas compris dans [0, 3], on pioche la carte de la pioche du niveau voulu
            joueur.main.add((Personnage)j.pioches[id[0]].piocher());
        }
        else{
            // Sinon on ajoute le perso visible voulu à la main du joueur
            joueur.main.add(j.persos[id[0]][id[1]]);
            // ET on repioche un nouveau personnage pour remplacer l'emplacemement vide si la pioche du niveau correspondant n'est pas vide
            if(j.pioches[id[0]].getTaille()>0)
                j.persos[id[0]][id[1]]=((Personnage)j.pioches[id[0]].piocher());
            else
                j.persos[id[0]][id[1]]=null;
        }
        //On ajoute un joker au joueur actif s'il reste des jokers dispos
        if(j.jetons[5]>0){
            joueur.jetons[5]++;
            j.jetons[5]--;
        }
    }
    
}
