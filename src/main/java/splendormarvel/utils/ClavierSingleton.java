package splendormarvel.utils;

import java.util.Scanner;

/**
 * Classe ClavierSingleton,
 * Singleton est un patron de conception de création(design pattern)
 * qui garantit que l'instance d'une classe n'existe qu'en un seul exemplaire
 * tout en fournissant un point d'accès global à cette instance
 * @author jeremie.humeau
 */
public class ClavierSingleton {
   
    /**
     * L'instance static de la classe qui sera unique
     */
    private static ClavierSingleton instance;
    
    /**
     * Le scanner pour la saisie utile pour la saisie au clavier
     */
    private final Scanner s;

    /**
     * Constructeur privé
     */
    private ClavierSingleton() {
        s = new Scanner(System.in);
    }

    /**
     * Permet de récupérer l'instance de la classe (et de la créer si c'est le 1er appel)
     * @return l'instance unique de la classe
     */
    public static ClavierSingleton getInstance() {
        if(instance == null) {
            instance = new ClavierSingleton();
        }
        return instance;
    }

    /**
     * Permet de récupérer un entier entre 2 bornes en gérant la levée d'exception en cas de valeur eronnée
     * @param borneInf la borne inférieure (incluse)
     * @param borneSup la borne supérieure (incluse)
     * @return la valeur saisie par l'utilisateur qui doit être comprise entre les 2 bornes
     */
    public int nextIntBetween(int borneInf, int borneSup) {
        int res=borneInf-1;
        boolean firstTime=true;
        System.out.println("Veuillez entrer un entier dans l'intervalle " + Couleur.ROUGE_BOLD + "[" + borneInf + ", " + borneSup + "]." + Couleur.RESET);     
        while(res<borneInf || res > borneSup){
            try{
                if(!firstTime)
                    System.out.println("L'entier doit être dans l'intervalle " + Couleur.ROUGE_BOLD + "[" + borneInf + ", " + borneSup + "]" + Couleur.RESET + ". Merci de fournir une valeur valide.");
                firstTime=false;
                res=s.nextInt();
            }
            catch(Exception e){
                System.out.println("L'exception suivante a été levée:\n"  + e);
                System.out.println("Nouvelle tentative...");
                s.nextLine();
            }
        }
        return res;
    }

    /**
     * Permet de récupérer un booléen sous le format 0, 1 (plus pratique à taper que true ou false)
     * Utilise la méthode nextIntBetween
     * @return vrai si la valeur 1 est saisie et faux si la valeur 0 est saisie
     */
    public boolean nextBoolean() {
        System.out.println("0: false, 1: true");
        int r= nextIntBetween(0,1);
        return (r==1);
    }
    
    /**
     * Permet de demander et de récupérer une chaine de caractères à l'utilisateur
     * @return la chaine saisie par l'utilisateur (qui devrait être une action valide)
     */
    public String nextAction(){
        return s.nextLine();
    }
}
