package splendormarvel.actions;

import splendormarvel.Jeu;

/**
 * Classe abstraite pour les actions réalisables par les joueurs
 * @author jeremie.humeau
 */
public interface Action {
    
    /**
     * Parse la ou les actions renvoyées par le joueur sous forme de chaine de caractères
     * @param j le jeu dans son état actuel
     * @param place la place du joueur dont c'est le tour
     * @param action la ou les actions à analyser
     * @return vrai si l'action est valide, faux sinon
     */
    public boolean parse(Jeu j, int place, String[] action);
    
    /**
     * Permet de vérifier si l'action en cours de traitement est valide ou non
     * @param j le jeu dans son état actuel
     * @param place la place du joueur dont c'est le tour
     * @return vrai si l'action du joueur actif est valide, faux sinon
     */
    public boolean verifier(Jeu j, int place);
    
    /**
     * Permet d'appliquer l'action si cette dernière est valide
     * @param j le jeu dans son état actuel
     * @param place la place du joueur dont c'est le tour
     */
    public void appliquer(Jeu j, int place);
    
}
