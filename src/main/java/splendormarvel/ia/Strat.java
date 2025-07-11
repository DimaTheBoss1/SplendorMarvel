package splendormarvel.ia;

/**
 * Classe abstraite implémentant l'interface Strategie.
 * Elle sera généralement associée à un Bot afin de jouer de manière automatique au jeu
 * @author jeremie.humeau
 */
public abstract class Strat implements Strategie{
    
    /**
     * @return le nom du Joueur ayant codé la Stratégie
     */
    public abstract String nomJoueur();
    
    /**
     * Permet au joueur actif de jouer son tour
     * @param etatDuJeu l'état actuel du jeu
     * @return la ou les actions que le joueur souhaite effectuer
     */
    @Override
    public abstract String jouer(String etatDuJeu);
    
    
     /**
     * Permet au joueur actif d'initialiser les données du jeu
     * @param etatDuJeu l'état initial du jeu
     */
    @Override
    public abstract void init(String etatDuJeu);
}
