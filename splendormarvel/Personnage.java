package splendormarvel;
import splendormarvel.utils.Couleur;

/**
 * Classe modélisant les cartes Personnage. Hérite de Carte.
 * @author jeremie.humeau
 */
public class Personnage extends Carte {

    /**
     * Le niveau du Personnage (1, 2 ou 3)
     */
    public int niveau;

    /**
     * Le nom du Personnage
     */
    public String nom;

    /**
     * Le cout en jetons du Personnage (tableau de taille 5)
     */
    public int[] cout;

    /**
     * La couleur du Personnage (la couleur de la réduction qu'il apporte)
     */
    public int couleur;

    /**
     * Le nombre d'avengers du Personnage
     */
    public int avenger;

    /**
     * Le nombre de points apportés par le Personnage
     */
    public int points;
    
    /**
     * Constructeur
     * @param niveau le niveau du Personnage (1, 2 ou 3)
     * @param nom le nom du Personnage
     * @param cout le cout du Personnage (tableau de 5 entiers correspondant aux 5 couleurs)
     * @param couleur la couleur (réduction) du Personnage
     * @param avenger le nombre d'avengers du Personnage
     * @param points le nombre de points du Personnage
     */
    public Personnage(int niveau, String nom, int[] cout, int couleur, int avenger, int points){
        this.niveau=niveau;
        this.nom=nom;
        this.cout=cout.clone(); // Clone du tableau pour éviter la référence commune.
        this.couleur=couleur;
        this.avenger=avenger;
        this.points=points;
    }
    
    /**
     * Vérifie si une carte peut être jouée par un joueur
     * @param j le joueur en question
     * @return vrai si a les reductions et jetons nécessaires pour jouer le Personnage, faux sinon.
     */
    public boolean peutEtreJouePar(Joueur j){
        int jokerRequis=0;
        for(int i=0; i<5; i++){
            if(j.jetons[i] + j.reduc[i] <cout[i])
                jokerRequis+= (cout[i]-(j.reduc[i]+j.jetons[i]));
        }
        return j.jetons[5] >= jokerRequis;
    }
    
    /**
     * Utile à l'affichage d'un Personnage
     * @return la chaine de caractères représentant une carte Personnage
     */
    @Override
    public String toString() {
        String res= Couleur.couleurs[couleur] + nom + Couleur.RESET + ": niveau " + niveau + ", coût:";
        for(int i=0; i<5; i++){
            if(cout[i]>0)
                res+= " " + Couleur.couleurs[i]+cout[i]+Couleur.RESET;
        }
        res+= " (points: " + points + ", avenger: " + avenger + ")";
        return res;
    }
    
}
