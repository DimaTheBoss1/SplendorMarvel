package splendormarvel.tournoi;
import java.util.HashMap;
import splendormarvel.*;
import splendormarvel.utils.*;
import java.util.concurrent.*;

public class Rencontre implements Callable<Integer>{
    
    int taskId;
    Joueur[] joueurs;
    public int id1;
    public int id2;
    public int nbConfrontation;
    public int[][] scores;
    
    public Rencontre(int taskId, Joueur[] joueurs, int nbConfrontation){
        this.taskId=taskId;
        this.joueurs=joueurs;
        this.nbConfrontation=nbConfrontation;
        this.scores=new int[2][nbConfrontation+1];
        scores[0][nbConfrontation]=0;
        scores[1][nbConfrontation]=0;
    }
    
    public int jouer(){
        int res=0;
        HashMap<String,Integer> tmp;
        joueurs[id1].clear();
        joueurs[id2].clear();
        Jeu jeu= new Jeu(new Joueur[]{joueurs[id1],joueurs[id2]}, true);
        
        for(int i=0; i<nbConfrontation; i++){
            tmp = jeu.newgame();
            scores[0][i]=tmp.get(joueurs[id1].nom);
            scores[1][i]=tmp.get(joueurs[id2].nom);
            if(scores[0][i]>scores[1][i]){
                scores[0][nbConfrontation]++;
            }
            else if(scores[0][i]<scores[1][i]){
                scores[1][nbConfrontation]++;             
            }
            
        }
        if(scores[0][nbConfrontation] < scores[1][nbConfrontation])
            res=2;
        else if(scores[0][nbConfrontation] > scores[1][nbConfrontation])
            res=1;
        return res;
    }
    
    public void setId(int id1, int id2){
        this.id1=id1;
        this.id2=id2;
    }
    
    @Override
    public String toString(){
        String res="";
        res+= id1 + "(" + joueurs[id1].nom +"): ";
        for(int i=0; i<nbConfrontation; i++)
            res+=" " + scores[0][i];
        res+="\n";
        res+= id2 + "(" + joueurs[id2].nom +"): ";
        for(int i=0; i<nbConfrontation; i++)
            res+=" " + scores[1][i];
        res+="\n";
        if(scores[0][nbConfrontation] == scores[1][nbConfrontation])
            res+="Egalité!\n";
        else if(scores[0][nbConfrontation] > scores[1][nbConfrontation])
            res+="Victoire de " + id1 + "\n";
        else
            res+="Victoire de " + id2 + "\n";
        return res;
    }

    @Override
    public Integer call() throws Exception {
        int res=0;
        HashMap<String,Integer> tmp;
        joueurs[id1].clear();
        joueurs[id2].clear();
        Jeu jeu= new Jeu(new Joueur[]{joueurs[id1],joueurs[id2]}, true);
        
        for(int i=0; i<nbConfrontation; i++){
            tmp = jeu.newgame();
            scores[0][i]=tmp.get(joueurs[id1].nom);
            scores[1][i]=tmp.get(joueurs[id2].nom);
            if(scores[0][i]>scores[1][i]){
                scores[0][nbConfrontation]++;
            }
            else if(scores[0][i]<scores[1][i]){
                scores[1][nbConfrontation]++;               
            }
            
        }
        if(scores[0][nbConfrontation] < scores[1][nbConfrontation])
            res=2;
        else if(scores[0][nbConfrontation] > scores[1][nbConfrontation])
            res=1;
        System.out.println("Task " + taskId + " is being executed by " + Thread.currentThread().getName());
        return res;
    }
    
}
