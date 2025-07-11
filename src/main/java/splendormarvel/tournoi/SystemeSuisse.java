package splendormarvel.tournoi;
import splendormarvel.*;
import splendormarvel.utils.*;
import java.util.concurrent.*;

public class SystemeSuisse extends Tournoi{

    public int nbPartie;
    public int[][] scores;
    public int[] nbVictoire;
    public Ronde[] rondes;
    public boolean[] dispo; 
    public int[] index;
    public int rencontreParRonde;
    
    public SystemeSuisse(Joueur[] joueurs, int nbPartieParRencontre){
        super(joueurs, nbPartieParRencontre);
        init();
    }
    
    public SystemeSuisse(int[] strats, int nbPartieParRencontre) throws NoSuchMethodException{
        super(strats, nbPartieParRencontre);
        init();
    }
    
    public void init(){
        scores = new int[joueurs.length][joueurs.length+1];
        index = new int[joueurs.length];
        nbVictoire = new int[joueurs.length];
        for(int i=0; i< joueurs.length; i++){
            index[i]=i;
            for(int j=0; j<joueurs.length; j++)
                scores[i][j]=-1;
            scores[i][joueurs.length]=0;
            nbVictoire[i]=0;
        }
        
        rencontreParRonde=joueurs.length/2;

        nbPartie = (int)Math.ceil(Math.log(joueurs.length)/Math.log(2));
        rondes=new Ronde[nbPartie];
        for(int i=0; i<nbPartie; i++){
            rondes[i]=new Ronde(i+1, rencontreParRonde);
            for(int j=0; j<rencontreParRonde; j++)
                rondes[i].rencontres[j]=new Rencontre(j, joueurs, nbPartieParRencontre);
        }
        dispo = new boolean[joueurs.length];
    }
    
    
    public void resetDispo(){
        for(int i=0; i<dispo.length; i++)
            dispo[i]=true;
    }
    
    public void printTab(){
        for(int i=0; i<joueurs.length; i++){
            System.out.println(index[i] + " : " + scores[index[i]][joueurs.length] + ", " + nbVictoire[index[i]]);
        }
    }
    
    public void printScore(){
        for(int i=0; i< joueurs.length; i++){
            System.out.print("\t" + i);
        }
        System.out.println();
        for(int i=0; i< joueurs.length; i++){
            System.out.print(i);
            for(int j=0; j<joueurs.length; j++){
                System.out.print("\t" + scores[i][j]);
            }
            System.out.println();
        }
    }
    
    public String scoreToJson(){
        StringBuilder json = new StringBuilder("{\n");

        // Boucle pour ajouter chaque participant
        for (int i = 0; i < joueurs.length; i++) {
            json.append("    \"").append(i + 1).append("\": {\n")
                .append("        \"Nom\": \"").append(joueurs[index[i]].nom).append("\",\n")
                .append("        \"Points\": ").append(scores[index[i]][joueurs.length]).append(",\n")
                .append("        \"nbVictoire\": ").append(nbVictoire[index[i]]).append("\n")
                .append("    }");
            // Ajouter une virgule entre les participants, sauf pour le dernier
            if (i < joueurs.length - 1) {
                json.append(",\n");
            }
        }

        // Fin du JSON
        json.append("\n}");
        return json.toString();
    }
    
    public void trierJoueur(){
        int tmp;
        for(int i=0; i<joueurs.length-1; i++){
            for(int j=0; j<joueurs.length-i-1; j++){
                if(scores[index[j]][joueurs.length]<scores[index[j+1]][joueurs.length] || (scores[index[j]][joueurs.length] == scores[index[j+1]][joueurs.length] && nbVictoire[index[j]] < nbVictoire[index[j+1]])){
                    tmp=index[j];
                    index[j]=index[j+1];
                    index[j+1]=tmp;
                }
            }
        }             
    }
    
    public boolean appareiller(int ronde){
        boolean res=true;
        int premier;
        int second;
        for(int i=0; i<rencontreParRonde; i++){
            premier=premierDispo();
            second=premierDispoNonRencontrePar(premier);
            if(second>=0){
                rondes[ronde].rencontres[i].setId(premier, second);
                //System.out.println("Rencontre " + (i+1) + ": " + premier + " vs " + second);
            }
            else{
                //System.out.println("BLOCAGE!!!");
                res=false;
            }
        }
        if(res && joueurs.length%2==1){
            premier=premierDispo();
            if(scores[premier][premier]>0){
                //System.out.println("Bye déjà fait!!!");
                res=false;
            }
            else{
                //System.out.println("Bye du joueur " + premier);
                scores[premier][premier]=3;
                scores[premier][joueurs.length]+=3;
                rondes[ronde].bye=premier;
            }
        }
        return res;
    }
    
    //RENVOI L'ID DU JOUEUR
    public int premierDispo(){
        int i=0;
        while(!dispo[index[i]])
            i++;
        dispo[index[i]]=false;
        return index[i];
    }
    
    public int premierDispoNonRencontrePar(int premier){
        int res=-1;
        int i=0;
        while(i<dispo.length && (!dispo[index[i]] || scores[premier][index[i]]>=0))
            i++;
        if(i<dispo.length){
            dispo[index[i]]=false;
            res=index[i];
        }
        return res;    
    }
    
    public void melanger(){
        int tmp;
        for(int i=joueurs.length-1; i>0 ; i--){
            int j=RandomSingleton.getInstance().nextInt(i);
            tmp= index[i];
            index[i]=index[j];
            index[j]=tmp;
        }
    }
    
    public void echange(int v){
        if(v<joueurs.length/2){
            int tmp=index[joueurs.length-(2*v+1)];
            index[joueurs.length-(2*v+1)]=index[joueurs.length-(2*v+2)];
            index[joueurs.length-(2*v+2)]=tmp;
        }
        else if(v<joueurs.length*2/3){
            v-=joueurs.length/2;
            int tmp=index[joueurs.length-(2*v+1)];
            index[joueurs.length-(2*v+1)]=index[joueurs.length-(2*v+3)];
            index[joueurs.length-(2*v+3)]=tmp;
        }
        else{
            //System.out.println("MELANGE");
            melanger();
        }
    }
    
    public void testmultiThread(){
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Future<Integer>[] future = new Future[rencontreParRonde];
        Integer[] result = new Integer[rencontreParRonde];
        int tmp;
        try {
            for(int i=0; i<nbPartie; i++){  
                resetDispo();
                if(i>0){
                    trierJoueur();
                }
                tmp=0;
                while(!appareiller(i)){
                    resetDispo();
                    echange(tmp);
                    tmp++;
                }
                for(int k=0; k<rencontreParRonde; k++){
                    future[k] = executor.submit(rondes[i].rencontres[k]);
                    System.out.println("Submit OK");
                }          
                // Attendre la fin des tâches et récupérer les résultats
                for(int j=0; j<rencontreParRonde; j++){
                    result[j] = future[j].get();  // La méthode `get()` attend la fin du calcul
                    System.out.println("Résultat de la tâche " + j + " : " + result[j]);
                    nbVictoire[rondes[i].rencontres[j].id1]+=rondes[i].rencontres[j].scores[0][nbPartieParRencontre];
                    nbVictoire[rondes[i].rencontres[j].id2]+=rondes[i].rencontres[j].scores[1][nbPartieParRencontre];
                    if(result[j] == 0){
                        //System.out.println(rondes[i].rencontres[j].id1 + " vs " + rondes[i].rencontres[j].id2 + " -> Egalité");
                        scores[rondes[i].rencontres[j].id1][rondes[i].rencontres[j].id2]=1;
                        scores[rondes[i].rencontres[j].id1][joueurs.length]+=1;
                        scores[rondes[i].rencontres[j].id2][rondes[i].rencontres[j].id1]=1;
                        scores[rondes[i].rencontres[j].id2][joueurs.length]+=1;
                    }
                    else if (result[j]==1){
                        //System.out.println(Couleur.ROUGE + "[" + rondes[i].rencontres[j].id1 + "]" +Couleur.RESET + " vs " + rondes[i].rencontres[j].id2);
                        scores[rondes[i].rencontres[j].id1][rondes[i].rencontres[j].id2]=3;
                        scores[rondes[i].rencontres[j].id1][joueurs.length]+=3;
                        scores[rondes[i].rencontres[j].id2][rondes[i].rencontres[j].id1]=0;
                    }
                    else{
                        //System.out.println( rondes[i].rencontres[j].id1  + " vs " + Couleur.ROUGE + "[" + rondes[i].rencontres[j].id2 + "]" +Couleur.RESET);
                        scores[rondes[i].rencontres[j].id2][rondes[i].rencontres[j].id1]=3;
                        scores[rondes[i].rencontres[j].id2][joueurs.length]+=3;
                        scores[rondes[i].rencontres[j].id1][rondes[i].rencontres[j].id2]=0;
                    }
                }     
            }
            trierJoueur();
            for (int i=0; i<rondes.length; i++)
                System.out.println(rondes[i]);
            printTab();
            printScore();
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        finally {
            // Arrêter proprement l'executor
            executor.shutdown();
        }
    }
    
    @Override
    public void lancer() {       
        int tmp;
        //System.out.println("NB PARTIE: " + nbPartie);
        
        for(int i=0; i<nbPartie; i++){
            resetDispo();
            if(i>0){
                trierJoueur();
            }
            //System.out.println();

            //printTab();
            tmp=0;
            while(!appareiller(i)){
                resetDispo();
                echange(tmp);
                tmp++;
            }
            for(int j=0; j<rencontreParRonde; j++){
                tmp=rondes[i].rencontres[j].jouer();
                nbVictoire[rondes[i].rencontres[j].id1]+=rondes[i].rencontres[j].scores[0][nbPartieParRencontre];
                nbVictoire[rondes[i].rencontres[j].id2]+=rondes[i].rencontres[j].scores[1][nbPartieParRencontre];
                if(tmp == 0){
                    //System.out.println(rondes[i].rencontres[j].id1 + " vs " + rondes[i].rencontres[j].id2 + " -> Egalité");
                    scores[rondes[i].rencontres[j].id1][rondes[i].rencontres[j].id2]=1;
                    scores[rondes[i].rencontres[j].id1][joueurs.length]+=1;
                    scores[rondes[i].rencontres[j].id2][rondes[i].rencontres[j].id1]=1;
                    scores[rondes[i].rencontres[j].id2][joueurs.length]+=1;
                }
                else if (tmp==1){
                    //System.out.println(Couleur.ROUGE + "[" + rondes[i].rencontres[j].id1 + "]" +Couleur.RESET + " vs " + rondes[i].rencontres[j].id2);
                    scores[rondes[i].rencontres[j].id1][rondes[i].rencontres[j].id2]=3;
                    scores[rondes[i].rencontres[j].id1][joueurs.length]+=3;
                    scores[rondes[i].rencontres[j].id2][rondes[i].rencontres[j].id1]=0;
                }
                else{
                    //System.out.println( rondes[i].rencontres[j].id1  + " vs " + Couleur.ROUGE + "[" + rondes[i].rencontres[j].id2 + "]" +Couleur.RESET);
                    scores[rondes[i].rencontres[j].id2][rondes[i].rencontres[j].id1]=3;
                    scores[rondes[i].rencontres[j].id2][joueurs.length]+=3;
                    scores[rondes[i].rencontres[j].id1][rondes[i].rencontres[j].id2]=0;
                }

            }
                
            
        }
        trierJoueur();
        for (int i=0; i<rondes.length; i++)
            System.out.println(rondes[i]);
        printTab();
        printScore();
        
    }
    
}
