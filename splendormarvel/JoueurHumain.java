package splendormarvel;

import splendormarvel.utils.ClavierSingleton;
import splendormarvel.utils.Couleur;

/**
 * Classe représentant un joueur Humain qui intéragit via une saisie au clavier. Hérite de la classe Joueur.
 * @author jeremie.humeau
 */
public class JoueurHumain extends Joueur{
    
    /**
     * Constructeur pour JoueurHumain
     * @param nom
     */
    public JoueurHumain(String nom) {
        super(nom);
        this.humain=true;
    }
    
    /**
     * Permet la saisie au clavier des actions par le joueur Humain.
     * @param etatDuJeu l'état du jeu sous forme de chaine de caractères.
     * @return la chaine de caractères saisie au clavier par le joueur Humain qui sera interprétée par le manager d'action
     */
    @Override
    public String jouer(String etatDuJeu){
        SplendorMarvel.afficher(etatDuJeu, this);
        System.out.println(Couleur.ROUGE + nom + " -> " + Couleur.CYAN +"ENTREZ VOS ACTIONS!" + Couleur.RESET + "(Pour plusieurs actions, mettez un \";\" entre chaque, ex: JETONS 1 2 3; REMETTRE 0)");
        String res=ClavierSingleton.getInstance().nextAction();
        return res;
    }

    /**
     * Permet un traitement potentiel au début de la partie. TODO!!! Afficher l'état initial du jeu
     * @param etatDuJeu l'état du jeu avant le début de la partie, sous forme de chaine de caractères.
     */
    @Override
    public void init(String etatDuJeu) {}
    
}
