package splendormarvel.actions;

import splendormarvel.Jeu;
import splendormarvel.Joueur;
import splendormarvel.Personnage;

/**
 * Action permettant de jouer une carte Personnage
 * @author jeremie.humeau
 */
public class JouerCarte implements Action {
    
    /**
     * Le joueur réalisant l'action
     */
    Joueur joueur;
    
    /**
     * le nombre de paramètres
     */
    int nb;
    
    /**
     * L'emplacement de la carte visée
     */
    int[] id;
    
    /**
     * le nombre de jetons requis pour la carte visée
     */
    int[] jetonsRequis;
    
    /**
     * Le Personnage visé
     */
    Personnage perso;
    
    /**
     * Constructeur
     */
    public JouerCarte(){
        jetonsRequis=new int[5];
        id = new int[2];
    }
    
    /**
     * Parse l'action JOUER
     * @param j le jeu dans son état actuel
     * @param place la place du joueur actif
     * @param action l'action JOUER ainsi que ses paramètres
     * @return vrai si l'action est valide, faux sinon
     */
    @Override
    public boolean parse(Jeu j, int place, String[] action) {
        boolean res=false;
        int tmp;
        // Checke que le nombre de paramètres est cohérent "JOUER" + 1 ou 2 valeurs (1 si carte de la main, 2 sinon)
        if(action.length<2 || action.length>3)
            System.err.println("Actions JOUER: nombre de paramètre incorrect");
        else{
            nb=action.length-1; //fixe le nombre de param
            joueur=j.joueurs[j.ordre[place]]; // fixe le joueur
            try{ // Parse les param et les transforme en entier
                for(int i=0; i<nb; i++){
                    tmp=Integer.parseInt(action[i+1]);
                    id[i]=tmp;
                }
            }
            catch(NumberFormatException e){
                e.printStackTrace();
            }
            //Vérification de la validité de l'action
            res=verifier(j, place);
        }
        return res;
    }

    /**
     * Vérifie si l'action JOUER est valide en fonction de l'état du jeu actuel
     * @param j l'état du jeu actuel
     * @param place la place du joueur actif
     * @return vrai si l'action est valide, faux sinon
     */
    @Override
    public boolean verifier(Jeu j, int place) {
        boolean res=true;
        //Si un seul paramètre, checke si le joueur a bien la carte ciblée en main
        //Si 2 paramètres checke que la carte est bien disponible
        if((nb==1 && joueur.main.size()< id[0]+1) || (nb==2 && (id[0]<0 || id[0]>3 || id[1]<0 || id[1]>3) && j.persos[id[0]][id[1]]==null))
            res=false;
        //Si les paramètres sont corrects, checke si le joueur a les jetons nécessaires pour jouer la carte
        else if(jetonsDispo(j))
            appliquer(j, place); // Si c'est le cas on applique effectivement l'action
        else
            res=false;
        return res;
    }
    
    /**
     * Vérifie si le joueur a les jetons nécéssaires pour jouer la carte voulue
     * @param j le jeu dans son état actuel
     * @return vrai si le joueur a bien les jetons nécessaires, faux sinon
     */
    public boolean jetonsDispo(Jeu j){
        boolean res=true;
        int joker=joueur.jetons[5]; // le nombre de jokers du joueur
        int cpt=0;
        // On fixe la carte voulue
        if(nb==1)
            perso=joueur.main.get(id[0]); // carte en main
        else
            perso=j.persos[id[0]][id[1]]; // carte en jeu
        for(int i=0; i<5; i++) // on définit le nombre de jetons requis en fonction des réductions du joueur
            jetonsRequis[i]= Math.max(0, perso.cout[i]-joueur.reduc[i]);
        while(cpt<5 && res && joker>=0){ //On compte le nombre de jokers nécessaires (quand le joueur n'a pas assez de jetons d'une couleur)
            if(jetonsRequis[cpt] > (joueur.jetons[cpt] + joker))
                res=false; //Si le joueur n'as pas assez de jokers, on fixe la valeur de retour à faux
            else
                joker-=(Math.max(0, jetonsRequis[cpt]-joueur.jetons[cpt])); // Sinon on décrémente le nombre de jokers
            cpt++; // Et on passe à la couleur suivante
        }
        return res;
    }

    /**
     * Applique l'action JOUER dans le cas ou elle est valide
     * @param jeu le jeu dans son état actuel
     * @param place la place du joueur actif
     */
    @Override
    public void appliquer(Jeu jeu, int place) {
        int besoinJoker=0;
        for(int i=0; i<5; i++){ // Pour chaque couleur on enleve les jetons nécessaires et on compte le nombre de jokers nécessaires en cas de manque
            if(joueur.jetons[i]<jetonsRequis[i]){ // Cas ou joker nécéssaire
                jeu.jetons[i]+=joueur.jetons[i]; //On remet les jetons du joueur en jeu
                besoinJoker+=jetonsRequis[i]-joueur.jetons[i]; // On compte le nombre de jokers nécessaires
                joueur.jetons[i]=0; // On fixe le nombre de jetons du joueur à 0
            }
            else{ // Cas ou le joueur a assez de jetons de la couleur en cours de traitement
                jeu.jetons[i]+=jetonsRequis[i]; // On remet les jetons du joueur en jeu 
                joueur.jetons[i]-=jetonsRequis[i]; // On enlève le même nombre de jetons au joueur
            }     
        }
        jeu.jetons[5]+=besoinJoker; // On remet le nombre de jokers en jeu
        joueur.jetons[5]-=besoinJoker; // On enlève le même nombre de jokers au joueur
        joueur.persos.add(perso); // On ajoute le perso au joueur
        joueur.points+=perso.points; // On ajoute les points du perso au joueur
        joueur.avenger+=perso.avenger; // On ajoute le nombre d'avengers au joueur
        if(perso.niveau==3) //Si le perso est de niveau 3, on fixe le nombre de jetons verts du joueur à 1
            joueur.jetons[6]=1;
        gainAvenger(jeu, place); //On checke si le joueur gagne le rassemblement des avengers
        joueur.reduc[perso.couleur]++; // On augmente le nombre de reduc du joueur en fonction de la couleur du perso joué
        if(nb==1) //Si le perso était dans la main du joueur, on l'enlève
            joueur.main.remove(id[0]);
        else if(jeu.pioches[id[0]].getTaille()>0) // Sinon on pioche une nouvelle carte pour l'emplacement du perso joué
            jeu.persos[id[0]][id[1]]=(Personnage)jeu.pioches[id[0]].piocher();
        else
            jeu.persos[id[0]][id[1]]=null; // Si la pioche est vide, on rend l'emplacement vide
    }
    
    /**
     * Vérifie si le joueur gagne le rassemblement des avengers et l'applique si c'est le cas
     * @param j le jeu dans son état actuel (après traitement de la carte jouée)
     * @param place la place du joueur actif dans le tour
     */
    public void gainAvenger(Jeu j, int place){
        //Si le perso a au moins 1 avenger  que le joueur a au moins 3 avengers et qu'il ne possède pas déjà le rassemblement
        if(perso.avenger>0 && !joueur.rassemblement && joueur.avenger>=3){
            if(j.posseseurAvenger<0){ //On lui affecte si le rassemblement n'appartient pas encore à quelqu'un
                j.posseseurAvenger=j.ordre[place];
                joueur.rassemblement=true;
                joueur.points+=3;
            }
            //Ou on lui affecte s'il dépasse le nombre d'avengers du joueur le possédant
            else if(j.joueurs[j.posseseurAvenger].avenger<joueur.avenger){ 
                j.joueurs[j.posseseurAvenger].rassemblement=false; // l'ancien possesseur perd le rassemblement
                j.joueurs[j.posseseurAvenger].points-=3; // l'ancien possesseur perd 3 points
                j.posseseurAvenger=j.ordre[place];
                joueur.rassemblement=true;
                joueur.points+=3;                
            }
        }
    }
    
}