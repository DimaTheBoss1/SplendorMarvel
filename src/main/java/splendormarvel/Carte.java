package splendormarvel;

/**
 * Classe abstraite pour gérer des Cartes
 * @author jeremie.humeau
 */
public abstract class Carte {
    
    /**
     * Méthode abstraite à implémenter dans les sous classes afin d'afficher les informations d'une carte
     * @return la chaine de caractères représentant les instances de carte
     */
    @Override
    abstract public String toString();  
}
