package splendormarvel;

import splendormarvel.ia.Strat;
/**
 * Classe représentant un joueur automatisé (Bot)
 * @author jeremie.humeau
 */
public class Bot extends Joueur{

    /**
     * la stratégie associée au Bot
     */
    public Strat strat;
    
    /**
     * Constructeur
     * @param strat: la stratégie associée au Bot
     * @param nom: le nom du Bot
    */
    public Bot(Strat strat, String nom) {
        super(nom); //appel du constructeur de la classe mère (Joueur)
        this.strat=strat; //affectation de la strat
        this.humain=false;
    }
      
    /**
     * Permet de jouer au jeu en fonction de l'état actuel de la partie
     * @param etatDuJeu: l'état actuel de la partie encodée dans une chaine de caractères
     * @return la ou les actions à effectuer
    */
    @Override
    public String jouer(String etatDuJeu){
        //SplendorMarvel.afficher(etatDuJeu, this);
        return strat.jouer(etatDuJeu);
    }

    /**
     * Permet d'iniatilser les variables du jeu (nombre de joueurs, de jetons, etc.)
     * @param etatDuJeu l'état initial de la partie encodée dans une chaine de caractères
     */
    @Override
    public void init(String etatDuJeu) {
        strat.init(etatDuJeu);
    }
}
