package splendormarvel.tournoi;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import splendormarvel.*;
import splendormarvel.ia.*;

/**
 *
 * @author jeremie.humeau
 */
public abstract class Tournoi {
    
    public Joueur[] joueurs;
    public int nbPartieParRencontre;
    
    public Tournoi(Joueur[] joueurs, int nbPartieParRencontre){
        this.joueurs=joueurs;
        this.nbPartieParRencontre=nbPartieParRencontre;
    }
    
    public Tournoi(int[] _strats, int nbPartieParRencontre) throws NoSuchMethodException{
        this.nbPartieParRencontre=nbPartieParRencontre;
        this.joueurs=new Joueur[_strats.length];
        for(int i=0; i<_strats.length; i++){
            try{
                Class classe = Class.forName("splendormarvel.ia.Strat" + _strats[i]);
                Strat s = (Strat)classe.getDeclaredConstructor().newInstance();
                joueurs[i]=new Bot(s, s.nomJoueur());
                
            }
            catch(ClassNotFoundException e){
                e.printStackTrace();
            } catch (InstantiationException ex) {
                Logger.getLogger(Tournoi.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Tournoi.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Tournoi.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Tournoi.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        checkStrat();
    }
    
    //TODO checker et virer les strats qui plantent tournent en boucle etc.
    private void checkStrat(){};
    
    public abstract void lancer();
    
}
