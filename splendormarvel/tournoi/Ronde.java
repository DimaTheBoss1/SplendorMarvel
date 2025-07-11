package splendormarvel.tournoi;
import splendormarvel.utils.*;

/**
 *
 * @author jeremie.humeau
 */
public class Ronde {
    public int id;
    public Rencontre[] rencontres;
    public int bye;
    
    public Ronde(int id, int nbRencontre){
        this.id=id;
        rencontres=new Rencontre[nbRencontre];
        bye=-1;
    }
    
    @Override
    public String toString(){
        String res="";
        res+= Couleur.ROUGE + "Ronde " + id + ":\n" + Couleur.RESET;
        if(bye>=0){
            res+= Couleur.VERT + "Bye du joueur " + bye + "\n" + Couleur.RESET;
        }
        for(int i=0; i<rencontres.length; i++)
            res+=rencontres[i].toString();
        return res;
    }
    
    
}
