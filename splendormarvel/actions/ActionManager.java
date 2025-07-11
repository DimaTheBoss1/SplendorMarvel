package splendormarvel.actions;

import splendormarvel.utils.Couleur;
import splendormarvel.Jeu;

/**
 * Classe permettant de gérer les actions voulant être réalisées par les joueurs
 * @author jeremie.humeau
 */
public class ActionManager {  
    /**
     * Compteur d'actions
     */
    int[] nbAction;
    
    /**
     * Action pour prendre des jetons de la réserve
     */
    PrendreJeton actionJeton;
    
    /**
     * Action pour jouer des cartes Personnage
     */
    JouerCarte actionJouerCarte;
    
    /**
     * Action pour conquérir des Lieux
     */
    PrendreLieu actionLieu;
    
    /**
     * Action pour remettre des jetons dans la réserve
     */
    RemettreJeton actionRemettre;
    
    /**
     * Action pour reserver une carte
     */
    ReserverCarte actionReserver;

    /**
     * Constructeur qui va instancier les 5 actions possibles
     */
    public ActionManager(){
        nbAction=new int[5];
        actionJeton=new PrendreJeton();
        actionJouerCarte=new JouerCarte();
        actionLieu=new PrendreLieu();
        actionRemettre=new RemettreJeton();
        actionReserver=new ReserverCarte();
    }
    
    /**
     * Analyse la réponse des joueurs afin d'appliquer l'action correspondante
     * @param j le jeu dans son état actuel
     * @param place la place du joueur actif dans le tour
     * @param reponse la ou les actions que le joueur actif veut faire (sous forme de chaîne de caractères)
     * @return vrai si l'action est valide, faux sinon
     */
    public boolean redirige(Jeu j, int place, String reponse){
        boolean res=false;
        String erreur="";
        for(int i=0; i<5; i++) // On réinitialise le nombre d'action à 0
            nbAction[i]=0;
        String[] actions=reponse.split(";"); // On splitte les différentes actions
        for(int i=0; i<actions.length; i++){
            if(actions[i].charAt(0)==' '){
                actions[i]=actions[i].substring(1);
            }
            String[] tokens=actions[i].split(" "); //On sépare l'action et ses paramètres
            switch (tokens[0]) { //On compte les actions envisagées
                case "JETONS" -> nbAction[0]++;
                case "JOUER" -> nbAction[1]++;
                case "LIEU" -> nbAction[2]++;
                case "RESERVER" -> nbAction[3]++;
                case "REMETTRE" -> nbAction[4]++;
                default -> erreur=tokens[0];
            }
        }
        if(check()){ //Si les actions envisagées sont compatibles, on les parse une à une en fonction du type d'action.
            for(int i=0; i<actions.length; i++){
                if(actions[i].charAt(0)==' '){
                    actions[i]=actions[i].substring(1);
                }
                String[] tokens=actions[i].split(" ");
                switch (tokens[0]) {
                    case "JETONS" -> res=actionJeton.parse(j, place, tokens);
                    case "JOUER" -> res=actionJouerCarte.parse(j, place, tokens);
                    case "LIEU" -> res=actionLieu.parse(j, place, tokens);
                    case "RESERVER" -> res=actionReserver.parse(j, place, tokens);
                    case "REMETTRE" -> res=actionRemettre.parse(j, place, tokens);
                    default -> erreur=tokens[0];
                }
            }
        }
        if(!res)
            System.out.println(Couleur.ROUGE + "L'action " + erreur + " n'est pas valide!" + Couleur.RESET);
        return res;
    }
    
    /**
     * Vérifie si la série d'actions envisagées est possible ou non
     * @return vrai si les actions sont compatibles, faux sinon
     */
    public boolean check(){
        //On ne peut pas faire plus d'une action parmi JETONS, JOUER et RESERVER
        //On ne peut pas prendre 2 LIEUX en mm temps
        return !(((nbAction[0]+nbAction[1]+nbAction[3])>1) || (nbAction[2]>1));
    }
}
