package splendormarvel;

import splendormarvel.utils.RandomSingleton;
import java.util.ArrayList;

/**
 * Classe modélisant une pioche de Carte
 * @author jeremie.humeau
 */
public class Pioche implements Cloneable{
    private ArrayList<Carte> cartes; //un paquet de cartes
    
    /**
     * Clone de l'objet Pioche
     * @return le clone de l'objet
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {      
        Pioche pioche= (Pioche)super.clone();
        pioche.cartes=new ArrayList<>();
        pioche.cartes.addAll(this.cartes);
        return pioche;
    }

    /**
     * Constructeur
     */
    public Pioche(){
        cartes=new ArrayList();
    }

    /**
     * Ajout d'une carte au dessus de la pioche (dernier élément de la liste)
     * @param c la carte à ajouter
     */
    public void ajouter(Carte c){
        cartes.add(c);
    }

    /**
     * Permet d'obtenir la taille de la pioche (nombre de cartes)
     * @return la taille de la pioche
     */
    public int getTaille(){
        return cartes.size();
    }

    /**
     * Vide la pioche
     */
    public void vider(){
        cartes.clear();
    }

    /**
     * Melange de la pioche selon le melange de Fisher-Yates
     */
    public void melanger(){
        Carte tmp;
        for(int i=cartes.size()-1; i>0 ; i--){
            int j=RandomSingleton.getInstance().nextInt(i);
            tmp= cartes.get(i);
            cartes.set(i, cartes.get(j));
            cartes.set(j, tmp);
        }
    }
    
    /**
     * Piocher la carte du haut de la pioche (la carte n'y sera plus), cette dernière sera donc enlevée de l'ArrayList.
     * @return la carte piochée
     */
    public Carte piocher(){
        Carte res=null;
        if(!cartes.isEmpty()){
            res=cartes.get(cartes.size()-1);
            cartes.remove(cartes.get(cartes.size()-1));
        }
        return res;
    }

    /**
     * Consulte la carte du haut de la pioche (la carte y reste)
     * @return la carte du haut de la pioche
     */
    public Carte top(){
        Carte res=null;
        if(!cartes.isEmpty()){
            res=cartes.get(cartes.size()-1);
        }
        return res;
    }

    /**
     * Utile à l'affichage de l'ensemble des cartes d'une pioche
     * @return une chaine de caractères représentant l'état actuel de la pioche
     */
    @Override
    public String toString(){
        String res="";
        for(Carte elem: cartes)
            res+=elem.toString()+"\n";
        return res;
    }
}
