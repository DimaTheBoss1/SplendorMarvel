package splendormarvel.utils;

import java.util.Random;

/**
 * Classe RandomSingleton,
 * Singleton est un patron de conception de création(design pattern)
 * qui garantit que l'instance d'une classe n'existe qu'en un seul exemplaire
 * tout en fournissant un point d'accès global à cette instance
 * @author jeremie.humeau
 */

public class RandomSingleton {
    /**
     * L'instance static de la classe qui sera unique
     */
    private static RandomSingleton instance;
    
    /**
     * L'objet Random qui sera utile pour les tirages aléatoires
     */
    private final Random rnd;

    /**
     * Constructeur privé
     */
    private RandomSingleton() {
        rnd = new Random();
    }
    
    /**
     * Permet de récupérer l'instance de la classe (et de la créer si c'est le 1er appel)
     * @return l'instance unique de la classe
     */
    public static RandomSingleton getInstance() {
        if(instance == null) {
            instance = new RandomSingleton();
        }
        return instance;
    }

    /**
     * Retourne un entier aléatoire dans l'intervalle [0, a[
     * @param a
     * @return
     */
    public int nextInt(int a) {
         return rnd.nextInt(a);
    } 
}