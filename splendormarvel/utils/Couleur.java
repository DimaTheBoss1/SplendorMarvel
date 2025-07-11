package splendormarvel.utils;
/**
 * Classe facilitant la gestion des couleurs pour l'affichage
 * @author jeremie.humeau
 */
public class Couleur {
    
    /**
     * Chaine de caractères pour le reset de la Couleur
     */
    public static final String RESET = "\u001B[0m";

    /**
     * Chaine de caractères pour la couleur NOIR
     */
    public static final String NOIR = "\u001B[30m";

    /**
     * Chaine de caractères pour la couleur ROUGE
     */
    public static final String ROUGE = "\u001B[31m";

    /**
     * Chaine de caractères pour la couleur VERT
     */
    public static final String VERT = "\u001B[32m";

    /**
     * Chaine de caractères pour la couleur JAUNE
     */
    public static final String JAUNE = "\u001B[33m";

    /**
     * Chaine de caractères pour la couleur BLEU
     */
    public static final String BLEU = "\u001B[34m";

    /**
     * Chaine de caractères pour la couleur VIOLET
     */
    public static final String VIOLET = "\u001B[35m";

    /**
     * Chaine de caractères pour la couleur CYAN
     */
    public static final String CYAN = "\u001B[36m";

    /**
     * Chaine de caractères pour la couleur BLANC
     */
    public static final String BLANC = "\u001B[37m";

    /**
     * Chaine de caractères pour activer le BOLD
     */
    public static final String BOLD_ON = "\033[0;1m";

    /**
     * Chaine de caractères pour desactiver le BOLD
     */
    public static final String BOLD_OFF = "\033[0;0m";
    
    /**
     * Chaine de caractères pour le fond de couleur NOIR
     */
    public static final String FOND_NOIR = "\u001B[40m";

    /**
     * Chaine de caractères pour le fond de couleur ROUGE
     */
    public static final String FOND_ROUGE = "\u001B[41m";

    /**
     * Chaine de caractères pour le fond de couleur VERT
     */
    public static final String FOND_VERT = "\u001B[42m";

    /**
     * Chaine de caractères pour le fond de couleur JAUNE
     */
    public static final String FOND_JAUNE = "\u001B[43m";

    /**
     * Chaine de caractères pour le fond de couleur BLEU
     */
    public static final String FOND_BLEU = "\u001B[44m";

    /**
     * Chaine de caractères pour le fond de couleur VIOLET
     */
    public static final String FOND_VIOLET = "\u001B[45m";

    /**
     * Chaine de caractères pour le fond de couleur CYAN
     */
    public static final String FOND_CYAN = "\u001B[46m";

    /**
     * Chaine de caractères pour le fond de couleur BLANC
     */
    public static final String FOND_BLANC = "\u001B[47m";
    
    /**
     * Chaine de caractères pour la couleur BOLD NOIR
     */
    public static final String NOIR_BOLD = "\033[1;30m";  // BLACK

    /**
     * Chaine de caractères pour la couleur BOLD ROUGE
     */
    public static final String ROUGE_BOLD = "\033[1;31m";    // RED

    /**
     * Chaine de caractères pour la couleur BOLD VERT
     */
    public static final String VERT_BOLD = "\033[1;32m";  // GREEN

    /**
     * Chaine de caractères pour la couleur BOLD JAUNE
     */
    public static final String JAUNE_BOLD = "\033[1;33m"; // YELLOW

    /**
     * Chaine de caractères pour la couleur BOLD BLEU
     */
    public static final String BLEU_BOLD = "\033[1;34m";   // BLUE

    /**
     * Chaine de caractères pour la couleur BOLD VIOLET
     */
    public static final String VIOLET_BOLD = "\033[1;35m"; // PURPLE

    /**
     * Chaine de caractères pour la couleur BOLD CYAN
     */
    public static final String CYAN_BOLD = "\033[1;36m";   // CYAN

    /**
     * Chaine de caractères pour la couleur BOLD BLANC
     */
    public static final String BLANC_BOLD = "\033[1;37m";  // WHITE
    
    /**
     * Tableau des couleurs pour l'affichage des jetons
     */
    public static final String[] couleurs=new String[]{Couleur.JAUNE, Couleur.BLEU, Couleur.FOND_JAUNE + Couleur.ROUGE, Couleur.VIOLET, Couleur.ROUGE, Couleur.NOIR, Couleur.VERT};
    
}
