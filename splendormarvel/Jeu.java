package splendormarvel;
import java.util.HashMap;
import splendormarvel.utils.*;
import splendormarvel.actions.ActionManager;

/**
 * Classe principale représentant l'ensemble du jeu
 * @author jeremie.humeau
 */
public class Jeu {
    
    /**
     * Tableau stockant les différents joueurs
     */
    public Joueur[] joueurs;
    /**
     * Tableau stockant les gagnants
     */
    public boolean[] gagnants;
    /**
     * Tableau des 3 pioches de personnages
     */
    public Pioche[] pioches;
    /**
     * Les 12 (3*4) personnages disponibles
     */
    public Personnage[][] persos;
    /**
     * Les piles de jetons restants
     */
    public int[] jetons;
    /**
     * Les lieux
     */
    public Lieu[] lieux;
    /**
     * L'id du joueur possédant le rassemblement des avengers
     */
    public int posseseurAvenger;

    /**
     * Tableau contenant l'id des joueurs dans un certain ordre (aléatoire ou non)
     */
    public int[] ordre;

    /**
     * Représente le fait qu'on tire au hasard ou non  l'ordre du tour de jeu
     */
    public boolean positionAleatoire;

    /**
     * Le manager d'action qui route les actions des joueurs (sous forme de chaine de caractères) vers les actions correpondantes.
     */
    public ActionManager am;
    
    /**
     * Mode verbose
     */
    boolean verbose=false;
    
    /**
     * 
     */
    int tour;
	public int nbActionsInvalides;//Que j'ai ajouté
	
    
    
    
    private void checkCartes(){
        for(int i=0; i<3; i++){
            for(int j=0; j<3; j++){
                for(int k=j+1; k<4; k++){
                    if(persos[i][j]!=null && persos[i][k]!=null && persos[i][j].nom.equals(persos[i][k].nom)){
                        System.out.println(Couleur.VIOLET + "PROBLEME SUR LES PERSOS DE NIVEAU " + i +  Couleur.RESET);
                        System.out.println(persos[i][j].nom);
                        System.out.println(persos[i][k].nom);
                    }
                }
            }
        }
    }
    
    /**
     * Nombre de couleurs disponibles
     * @return le nombre de couleurs ou il y a au moins 1 jeton disponible
     */
    public int nbCouleurDispo(){
        int res=0;
        for(int i=0; i<5; i++)
            if(jetons[i]>0)
                res++;
        return res;
    }
    
    /**
     * Checke si un des joueurs peut encore reserver une carte
     * @return vrai si une reservation est possible par un des joueurs
     */
    public boolean reservationPossible(){
        boolean res=false;
        int tmp=0;
        while (!res && tmp<joueurs.length){
            res=(joueurs[tmp].main.size() < 3);
            tmp++;
        }
        return res;
    }
    
    
    
    /**
     * Checke si un des joueurs peut jouer une carte
     * @return vrai si un des joueurs peut jouer au moins une carte de sa main ou des cartes disponibles
     */
    public boolean carteJouable(){
        boolean res=false;
        int x=0, y, nbJ;
        while(!res && x<persos.length){
            y=0;
            while(!res && y < persos[x].length){
                nbJ=0;
                while(!res && nbJ<joueurs.length){
                    if(persos[x][y]!=null){
                        res=persos[x][y].peutEtreJouePar(joueurs[nbJ]);
                    }
                    nbJ++;
                }
                y++;
            }
            x++;
        }
        
        nbJ=0;
        while(!res && nbJ<joueurs.length){
            x=0;
            while(!res && x<joueurs[nbJ].main.size()){
                res=joueurs[nbJ].main.get(x).peutEtreJouePar(joueurs[nbJ]);
                x++;
            }
            nbJ++;
        }
        return res;
    }
    
    /**
     * Teste si le jeu est arrivé dans une situation de blocage
     * @return vrai si le jeu est bloqué, faux sinon
     */
    public boolean blocage(){
        return (nbCouleurDispo()==0 && !reservationPossible() && !carteJouable());
    }
    
    /**
     * Lance une nouvelle partie
     * @return une Map contenant les scores (négatif pour les perdants, positif pour le ou les gagnants)
     */
    public HashMap<String,Integer> newgame(){
        tour=0;
        boolean finDePartie=false;
        HashMap<String,Integer> score= new HashMap();
        init();        
        for(int i=0; i<joueurs.length; i++)
            joueurs[ordre[i]].init(encoderEtatDuJeu(i));
        
        //Boucle de jeu
        while(!finDePartie){
            if(blocage()){
                System.out.println(Couleur.ROUGE + "BLOCAGE DETECTE!!!" + Couleur.RESET + " redemarrage de la partie...");
                SplendorMarvel.afficher(encoderEtatDuJeu(0), joueurs[0]);
                
                ClavierSingleton.getInstance().nextAction();
                init();
                for(int i=0; i<joueurs.length; i++)
                    joueurs[ordre[i]].init(encoderEtatDuJeu(i));
            }
            else{
                jouerUnNouveauTour();
                tour++;
            }
            finDePartie=end();
        }
        System.out.println("Fini en " + tour + " tours");
        
        //Une fois la partie finie, on cherche qui a gagné (dans de rares cas, il peut y avoir plusieurs gagnants)
        quiGagne();
        
        //On affecte les scores
        for(int i=0; i<joueurs.length; i++){
            if(gagnants[i])
                score.put(joueurs[i].nom, joueurs[i].points);
            else
                score.put(joueurs[i].nom, joueurs[i].points * -1);
            
            if(!botUniquement() || verbose){
                System.out.println(joueurs[i].nom + " a fini avec " + joueurs[i].points + " points!");
                System.out.println(joueurs[i].reduc[0] +  " " + joueurs[i].reduc[1] + " " + joueurs[i].reduc[2] + " " + joueurs[i].reduc[3] + " " + joueurs[i].reduc[4]);
            }
        }
//        // Affichage des lieux
//        System.out.println(Couleur.JAUNE + "Lieux disponibles :" + Couleur.RESET);
//        for (int i = 0; i < 4; i++) {
//            Lieu lieu = lieux[i];
//            System.out.print("  - " + lieu.noms[lieu.face] + " (" + (lieu.disponible ? "disponible" : "non disponible") + ")");
//            System.out.print("\n    - Coût : ");
//            for (int j = 0; j < 5; j++) {
//                if (lieu.couleurs[lieu.face][j] > 0) {
//                    System.out.print(lieu.couleurs[lieu.face][j] + " ");
//                    switch (j) {
//                        case 0: System.out.print("jaune "); break;
//                        case 1: System.out.print("bleu "); break;
//                        case 2: System.out.print("orange "); break;
//                        case 3: System.out.print("violet "); break;
//                        case 4: System.out.print("rouge "); break;
//                    }
//                }
//            }
//            System.out.println("\n    - Conquis par : " + (lieu.conquis == -1 ? "personne" : joueurs[lieu.conquis].nom));
//        }
//
//        // Affichage du possesseur de l'Avenger
//        System.out.println(Couleur.JAUNE + "Possesseur de l'Avenger : " + Couleur.RESET + 
//            (posseseurAvenger == -1 ? "personne" : joueurs[posseseurAvenger].nom));
        System.out.println(score);
        return score;
    }
    
    /**
     * Affecte les gagnants et les perdants
     */
    public void quiGagne(){
        int max=0;
        int acc=0;
        int min=100;
        boolean avenger=false;
        for(int i=0; i<gagnants.length; i++)
            gagnants[i]=false;
        for(int i=0; i<joueurs.length; i++){
            if(joueurs[i].points> max && joueurs[i].points>=16 && joueurs[i].jetons[6]>0 && cinqCouleurs(joueurs[i]))
                max=joueurs[i].points;
        }
        for(int i=0; i<joueurs.length; i++){
            if(joueurs[i].points==max && joueurs[i].points>=16 && joueurs[i].jetons[6]>0 && cinqCouleurs(joueurs[i])){
                if(joueurs[i].rassemblement){
                    avenger=true;
                    if(nbReductions(joueurs[i])<min)
                        min=nbReductions(joueurs[i]);
                }
                gagnants[i]=true;
                acc++;
            }
        }
        if(acc>1){
            if(avenger){
                for(int i=0; i<joueurs.length; i++){
                    if(gagnants[i] && !joueurs[i].rassemblement)
                        gagnants[i]=false;
                }
            }
            else{
                for(int i=0; i<joueurs.length; i++){
                    if(gagnants[i] && nbReductions(joueurs[i])>min)
                        gagnants[i]=false;
                }
            }
        }      
    }
    
    /**
     * Compte le nombre de Personnages recrutés
     * @param _joueur, le joueur concérné
     * @return le nombre de Personnages recrutés par un joueur
     */
    public int nbReductions(Joueur _joueur){
        int res=0;
        for (int i=0; i<5; i++)
            res+=_joueur.reduc[i];
        return res;
    }
    
    /**
     * Permet de savoir si il n'y a que des Bots ou non dans la partie
     * @return vrai si il n'y a que des Bots, faux sinon
     */
    public boolean botUniquement(){
        boolean res=true;
        for(int i=0; i<joueurs.length; i++){
            if(joueurs[i].humain)
                res=false;
        }
        return res;
    }
    
    /**
     * Permet de faire jouer un tour à chaque joueur suivant l'ordre défini
     */
    public void jouerUnNouveauTour(){
        checkCartes();
		System.out.println(Couleur.JAUNE + Couleur.FOND_VERT + "Tour " + tour + Couleur.RESET);
        for(int i=0; i<joueurs.length; i++){
            faireJouer(joueurs[ordre[i]], i);
        }
    }
    
    /**
     * Fait jouer un joueur et vérifie s'il n'a pas un surplus de jetons après son tour
     * @param joueur le joueur en question
     * @param place sa place dans l'ordre du tour
     */
    public void faireJouer(Joueur joueur, int place){
        if(joueur.humain){
            while(!this.am.redirige(this, place, joueur.jouer(encoderEtatDuJeu(place))))
                System.out.println("Veuillez entrer une action valide");//analyse la réponse du joueur
        }
        else {
        	System.out.println(Couleur.BLEU + "Tour de " + joueur.nom + Couleur.RESET);
        	String reponse;
            if( !this.am.redirige(this, place, reponse =joueur.jouer(encoderEtatDuJeu(place)))) nbActionsInvalides++;//J'ai modifié ceci
            System.out.println(Couleur.JAUNE + Couleur.FOND_NOIR + reponse + Couleur.RESET);
            System.out.println("Points: "+ joueur.points);
            
        }
        verifierSurplusJeton(joueur); //Vérifie si surplus de jetons
    }
   
    /**
     * Retire des jetons aléatoirement à un joueur s'il en a plus que 10
     * @param joueur le joueur en question
     */
    public void verifierSurplusJeton(Joueur joueur){
        //Somme les jetons du joueur
        int somme=0;
        for(int i=0; i<7; i++){
            somme+=joueur.jetons[i];
        }
        //Tant qu'il en a plus que 10, on lui en enlève un aléatoirement parmi les 5 couleurs de base
        while(somme>10){
            enleverUnJetonAleatoire(joueur);
            somme--;
        }
    }
    
    /**
     * Retire un jeton alétoire parmi les 5 couleurs de base
     * @param joueur le joueur en question
     */
    public void enleverUnJetonAleatoire(Joueur joueur){
        int r;
        do
            r=RandomSingleton.getInstance().nextInt(5);
        while(joueur.jetons[r]==0); // On tire une valeur au hasard jusqu'à trouver une couleur de jeton que le joueur possède
        joueur.jetons[r]--; // on lui retire
        this.jetons[r]++; // on l'ajoute au stock de jetons disponibles
    }
        
    /**
     * Encode l'état actuel du jeu
     * @param place la place du joueur dont c'est le tour
     * @return l'état du jeu encodé dans une chaine de caractères
     */
    public String encoderEtatDuJeu(int place){
        int nb=joueurs.length;
        int p;
        String res=nb+"\n"; //une ligne avec le nombre de joueurs
        for(int i=place; i<(place+joueurs.length); i++){ //Pour chaque joueur une ligne avec:
            p=ordre[i%nb];
            res+=(i%nb); //sa place dans le tour
            for (int k=0; k<5; k++) //le nombre de reductions pour chaque couleur
                res+= " " + joueurs[p].reduc[k];
            for (int k=0; k<7; k++)
                res+= " " + joueurs[p].jetons[k]; // le nombre de jetons
            res+= " " + joueurs[p].main.size() + " " + joueurs[p].points + " " + joueurs[p].avenger; // le nombre de carte en main, ses points et son nombre d'avenger
            if(joueurs[p].rassemblement) //Et s'il possède le rassemblement des avengers ou non
                res += " 1\n";
            else
                res+= " 0\n";
        }
        for(int i=0; i<3; i++){//Pour chaque niveau de cartes
            res+=pioches[i].getTaille() + "\n"; //Une ligne pour la taille de la pioche
            for(int j=0; j<4; j++){ //Pour chaque carte, une ligne avec:
                res+=i + " " + j; // les indices de la carte (niveau position)
                if(persos[i][j] != null){ //Si l'emplacement n'est pas vide
                    res+= " " + persos[i][j].couleur; // la couleur de la carte
                    for (int k=0; k<5; k++)
                        res+= " " + persos[i][j].cout[k]; // le cout pour chaque couleur
                    res+=" " + persos[i][j].points; // les points qu'elle rapporte
                    res+=" " + persos[i][j].avenger + "\n"; // son nombre d'avengers
                }
                else{
                    res+=" -1\n"; // -1 si l'emplacement est vide
                }
            }
        }
        for(int i=0; i<4; i++){ 
            if(lieux[i].disponible){ // Pour chaque lieu disponible
                res+=i; // une ligne avec: son id
                for(int k=0; k<5; k++) // les cartes nécessaires pour chacune des couleurs
                    res+= " " + lieux[i].couleurs[lieux[i].face][k];
                res+= " " + lieux[i].conquis + "\n"; //l'id du joueur qui a conquis le lieu
            }
        }
        res+=jetons[0]; // une ligne pour les jetons disponibles
        for(int i=1; i<7; i++)
            res+= " " + jetons[i];
        res+="\n";
        for(int i=place; i<(place+joueurs.length); i++){ // Pour chaque joueur
            p=ordre[i%nb];
            for(int j=0; j<joueurs[p].main.size(); j++){ // Une ligne pour chaque carte dans sa main avec:
                res+=joueurs[p].main.get(j).niveau + " " + joueurs[p].main.get(j).couleur; //le niveau et la couleur de la carte
                for(int k=0; k<5; k++) // le cout des 5 couleurs
                   res+=" " + joueurs[p].main.get(j).cout[k];
                res+= " " + joueurs[p].main.get(j).points + " " + joueurs[p].main.get(j).avenger + "\n"; //le nombre d'avengers et le nombre de points
            }
        }
        return res;
    }
    
    /**
     * Teste si on est à la fin de la partie
     * @return vrai si la partie est terminée, faux sinon
     */
    public boolean end(){
        boolean res=false;
        for(int i=0; i<joueurs.length; i++){
            if(joueurs[i].points>=16 && joueurs[i].jetons[6]>0 && cinqCouleurs(joueurs[i]))
                res=true; // Partie finie si un joueur a au moins 16 points, au moins un jeton vert et au moins 1 carte de chaque couleur.
        }
        if(tour>70) res =true;//J'ai ajouté ceci
        return res;
    }
    
    /**
     * Teste si un joueur a au moins 1 carte de chaque couleur
     * @param joueur le joueur en question
     * @return vrai si ce joueur a au moins une carte de chaque couleur
     */
    public boolean cinqCouleurs(Joueur joueur){
        boolean res=true;
        for(int i=0; i<5; i++)
            if(joueur.reduc[i]==0)
                res=false; // faux si une couleur est manquante
        return res;
    }
    
    /**
     * Constructeur 
     * @param joueurs le tableau des joueurs
     * @param positionAleatoire si vrai, les positions des joueurs sont tirées au hasard, sinon ordre identique à celui du tableau des joueurs
     */
    public Jeu(Joueur[] joueurs, boolean positionAleatoire){
        this.joueurs=joueurs; // On affecte le tableau des joueurs
        this.gagnants=new boolean[joueurs.length];
        this.positionAleatoire=positionAleatoire; // la variable position aléatoire
        this.ordre=new int[joueurs.length]; //on instancie le tableau pour l'ordre des joueurs (dans l'ordre par défaut)
        for(int i=0; i<ordre.length; i++)
            ordre[i]=i;
        pioches=new Pioche[3]; // On instancie les 3 pioches
        pioches[0]=new Pioche();
        pioches[1]=new Pioche();
        pioches[2]=new Pioche();
        jetons=new int[7]; // les jetons disponibles
        persos=new Personnage[3][4]; // Les personnages disponibles
        lieux=new Lieu[4]; // On instancie les 4 lieux
        posseseurAvenger=-1; // on affecte à -1 (pas de possesseur) la variable indiquant qui a le rassemblement des avengers
        creerPersos(); // On crée l'ensemble des personnages
        creerLieu(); // On crée les 4 lieux
        am = new ActionManager(); // On instancie le manager d'action
    }
    
    /**
     * Crée l'ensemble des cartes Personnages du jeu et les ajoute aux pioches correspondant à leur niveau
     */
    public final void creerPersos(){
        //Niveau 1 JAUNE
        pioches[0].ajouter(new Personnage(1,"Baron Zemo", new int[]{0,0,0,0,3}, Jeton.JAUNE, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Spider-Woman", new int[]{1,1,0,0,3}, Jeton.JAUNE, 1, 0));
        pioches[0].ajouter(new Personnage(1,"Grandmaster", new int[]{1,1,0,1,1}, Jeton.JAUNE, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Prowler", new int[]{0,1,1,1,2}, Jeton.JAUNE, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Vulture", new int[]{0,0,0,0,4}, Jeton.JAUNE, 0, 1));
        pioches[0].ajouter(new Personnage(1,"Squirrel Girl", new int[]{0,0,1,2,2}, Jeton.JAUNE, 1, 0));
        pioches[0].ajouter(new Personnage(1,"Rocket", new int[]{1,0,0,0,2}, Jeton.JAUNE, 0, 0));
        pioches[0].ajouter(new Personnage(1,"M.O.D.O.K.", new int[]{0,0,2,0,2}, Jeton.JAUNE, 0, 0));
        
        //Niveau 1 Bleu
        pioches[0].ajouter(new Personnage(1,"Valkyrie", new int[]{3,0,1,1,0}, Jeton.BLEU, 1, 0));
        pioches[0].ajouter(new Personnage(1,"Taskmaster", new int[]{4,0,0,0,0}, Jeton.BLEU, 0, 1));
        pioches[0].ajouter(new Personnage(1,"Spider-man 2099", new int[]{2,0,0,0,1}, Jeton.BLEU, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Moon Knight", new int[]{3,0,0,0,0}, Jeton.BLEU, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Lockjaw", new int[]{1,1,1,1,0}, Jeton.BLEU, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Bullseye", new int[]{0,1,1,1,2}, Jeton.BLEU, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Elektra", new int[]{2,0,0,2,0}, Jeton.BLEU, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Wasp", new int[]{2,1,2,0,0}, Jeton.BLEU, 1, 0));
        
        //Niveau 1 Orange
        pioches[0].ajouter(new Personnage(1,"Silversable", new int[]{0,0,0,3,0}, Jeton.ORANGE, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Scarletspider", new int[]{0,1,1,2,1}, Jeton.ORANGE, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Spider-Girl", new int[]{1,1,1,1,0}, Jeton.ORANGE, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Yondu", new int[]{0,0,0,4,0}, Jeton.ORANGE, 0, 1));
        pioches[0].ajouter(new Personnage(1,"Spider-Ham", new int[]{1,0,0,2,0}, Jeton.ORANGE, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Kingpin", new int[]{1,0,0,2,0}, Jeton.ORANGE, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Hawkeye", new int[]{1,2,0,2,0}, Jeton.ORANGE, 1, 0));
        pioches[0].ajouter(new Personnage(1,"Kate Bishop", new int[]{0,1,0,3,1}, Jeton.ORANGE, 1, 0));
        
        //Niveau 1 Violet
        pioches[0].ajouter(new Personnage(1,"Gorgon", new int[]{2,2,0,0,0}, Jeton.VIOLET, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Abomination", new int[]{0,2,1,1,1}, Jeton.VIOLET, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Kraven", new int[]{0,3,0,0,0}, Jeton.VIOLET, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Lizard", new int[]{0,2,0,1,0}, Jeton.VIOLET, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Rhino", new int[]{1,1,1,1,0}, Jeton.VIOLET, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Winter Soldier", new int[]{0,2,1,0,2}, Jeton.VIOLET, 1, 0));
        pioches[0].ajouter(new Personnage(1,"America Chavez", new int[]{1,3,0,0,1}, Jeton.VIOLET, 1, 0));
        pioches[0].ajouter(new Personnage(1,"Scorpion", new int[]{0,4,0,0,0}, Jeton.VIOLET, 0, 1));
        
        //Niveau 1 Rouge
        pioches[0].ajouter(new Personnage(1,"Ms.Marvel", new int[]{2,0,2,0,1}, Jeton.ROUGE, 1, 0));
        pioches[0].ajouter(new Personnage(1,"Mysterio", new int[]{0,0,3,0,0}, Jeton.ROUGE, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Crystal", new int[]{0,0,2,2,0}, Jeton.ROUGE, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Triton", new int[]{1,1,1,0,1}, Jeton.ROUGE, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Wong", new int[]{1,1,2,0,1}, Jeton.ROUGE, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Electro", new int[]{0,0,2,0,1}, Jeton.ROUGE, 0, 0));
        pioches[0].ajouter(new Personnage(1,"Sandman", new int[]{0,0,4,0,0}, Jeton.ROUGE, 0, 1));
        pioches[0].ajouter(new Personnage(1,"Quake", new int[]{0,1,3,1,0}, Jeton.ROUGE, 1, 0));
        
        //Niveau 2 JAUNE
        pioches[1].ajouter(new Personnage(2,"The Collector", new int[]{0,0,5,0,0}, Jeton.JAUNE, 0, 2));
        pioches[1].ajouter(new Personnage(2,"Red Skull", new int[]{0,0,6,0,0}, Jeton.JAUNE, 0, 3));
        pioches[1].ajouter(new Personnage(2,"Maria Hill", new int[]{0,2,3,2,0}, Jeton.JAUNE, 1, 1));
        pioches[1].ajouter(new Personnage(2,"Karnak", new int[]{3,0,5,0,0}, Jeton.JAUNE, 0, 2));
        pioches[1].ajouter(new Personnage(2,"Maximus", new int[]{0,2,4,0,1}, Jeton.JAUNE, 0, 2));
        pioches[1].ajouter(new Personnage(2,"Shuri", new int[]{3,2,3,0,0}, Jeton.JAUNE, 1, 1));
        
        //Niveau 2 BLEU
        pioches[1].ajouter(new Personnage(2,"Black Cat", new int[]{0,0,0,5,3}, Jeton.BLEU, 0, 2));
        pioches[1].ajouter(new Personnage(2,"Quicksilver", new int[]{2,0,0,3,3}, Jeton.BLEU, 1, 1));
        pioches[1].ajouter(new Personnage(2,"Ghost-Spider", new int[]{0,2,1,4,0}, Jeton.BLEU, 0, 2));
        pioches[1].ajouter(new Personnage(2,"War Machine", new int[]{0,2,2,3,0}, Jeton.BLEU, 1, 1));
        pioches[1].ajouter(new Personnage(2,"Miles Morales", new int[]{0,0,0,5,0}, Jeton.BLEU, 0, 2));
        pioches[1].ajouter(new Personnage(2,"Nebula", new int[]{0,0,0,6,0}, Jeton.BLEU, 0, 3));
        
        //Niveau 2 ORANGE
        pioches[1].ajouter(new Personnage(2,"Daredevil", new int[]{0,0,0,0,6}, Jeton.ORANGE, 0, 3));
        pioches[1].ajouter(new Personnage(2,"Falcon", new int[]{3,0,0,2,3}, Jeton.ORANGE, 1, 1));
        pioches[1].ajouter(new Personnage(2,"Nick Fury", new int[]{0,0,2,2,3}, Jeton.ORANGE, 1, 1));
        pioches[1].ajouter(new Personnage(2,"Punisher", new int[]{0,0,0,0,5}, Jeton.ORANGE, 0, 2));
        pioches[1].ajouter(new Personnage(2,"Groot", new int[]{0,0,0,3,5}, Jeton.ORANGE, 0, 2));
        pioches[1].ajouter(new Personnage(2,"Star-Lord", new int[]{0,1,2,0,4}, Jeton.ORANGE, 0, 2));
        
        //Niveau 2 VIOLET
        pioches[1].ajouter(new Personnage(2,"Jessica Jones", new int[]{6,0,0,0,0}, Jeton.VIOLET, 0, 3));
        pioches[1].ajouter(new Personnage(2,"She-Hulk", new int[]{3,0,2,2,0}, Jeton.VIOLET, 1, 1));
        pioches[1].ajouter(new Personnage(2,"Carnage", new int[]{5,0,0,0,3}, Jeton.VIOLET, 0, 2));
        pioches[1].ajouter(new Personnage(2,"Ronan", new int[]{5,0,0,0,0}, Jeton.VIOLET, 0, 2));
        pioches[1].ajouter(new Personnage(2,"Crossbones", new int[]{4,2,1,0,0}, Jeton.VIOLET, 0, 2));
        pioches[1].ajouter(new Personnage(2,"Okoye", new int[]{3,2,0,3,0}, Jeton.VIOLET, 1, 1));
        
        //Niveau 2 ROUGE
        pioches[1].ajouter(new Personnage(2,"Ghost Rider", new int[]{2,4,0,1,0}, Jeton.ROUGE, 0, 2));
        pioches[1].ajouter(new Personnage(2,"Medusa", new int[]{0,5,3,0,0}, Jeton.ROUGE, 0, 2));
        pioches[1].ajouter(new Personnage(2,"Scarlet Witch", new int[]{2,3,0,0,2}, Jeton.ROUGE, 1, 1));
        pioches[1].ajouter(new Personnage(2,"Beta Ray Bill", new int[]{0,3,2,0,3}, Jeton.ROUGE, 1, 1));
        pioches[1].ajouter(new Personnage(2,"Venom", new int[]{0,5,0,0,0}, Jeton.ROUGE, 0, 2));
        pioches[1].ajouter(new Personnage(2,"Hela", new int[]{0,6,0,0,0}, Jeton.ROUGE, 0, 3));
        
        //Niveau 3 JAUNE
        pioches[2].ajouter(new Personnage(3,"Iron Man", new int[]{3,5,3,0,3}, Jeton.JAUNE, 2, 3));
        pioches[2].ajouter(new Personnage(3,"Vision", new int[]{3,6,0,0,3}, Jeton.JAUNE, 1, 4));
        pioches[2].ajouter(new Personnage(3,"Green Goblin", new int[]{0,7,0,3,0}, Jeton.JAUNE, 0, 5));
        pioches[2].ajouter(new Personnage(3,"Doctor Octopus", new int[]{0,7,0,0,0}, Jeton.JAUNE, 0, 4));
        
        //Niveau 3 BLEU
        pioches[2].ajouter(new Personnage(3,"Ant-Man", new int[]{3,0,3,0,6}, Jeton.BLEU, 1, 4));
        pioches[2].ajouter(new Personnage(3,"Iron Fist", new int[]{0,0,0,0,7}, Jeton.BLEU, 0, 4));
        pioches[2].ajouter(new Personnage(3,"Gamora", new int[]{0,3,0,0,7}, Jeton.BLEU, 0, 5));
        pioches[2].ajouter(new Personnage(3,"Black Widow", new int[]{3,3,0,3,5}, Jeton.BLEU, 2, 3));
                
        //Niveau 3 ORANGE
        pioches[2].ajouter(new Personnage(3,"Black Bolt", new int[]{7,0,0,0,0}, Jeton.ORANGE, 0, 4));
        pioches[2].ajouter(new Personnage(3,"Captain America", new int[]{5,3,3,3,0}, Jeton.ORANGE, 2, 3));
        pioches[2].ajouter(new Personnage(3,"Spider-Man", new int[]{7,0,0,0,3}, Jeton.ORANGE, 0, 5));
        pioches[2].ajouter(new Personnage(3,"Black Panther", new int[]{6,0,3,3,0}, Jeton.ORANGE, 1, 4));
        
        //Niveau 3 VIOLET
        pioches[2].ajouter(new Personnage(3,"Hulk", new int[]{0,3,5,3,3}, Jeton.VIOLET, 2, 3));
        pioches[2].ajouter(new Personnage(3,"Drax", new int[]{0,0,7,0,0}, Jeton.VIOLET, 0, 4));
        pioches[2].ajouter(new Personnage(3,"Captain Marvel", new int[]{0,3,6,3,0}, Jeton.VIOLET, 1, 4));
        pioches[2].ajouter(new Personnage(3,"Luke Cage", new int[]{3,0,7,0,0}, Jeton.VIOLET, 0, 5));
        
        //Niveau 3 ROUGE
        pioches[2].ajouter(new Personnage(3,"Loki", new int[]{0,3,0,7,0}, Jeton.ROUGE, 0, 5));
        pioches[2].ajouter(new Personnage(3,"Thor", new int[]{3,3,0,5,3}, Jeton.ROUGE, 2, 3));
        pioches[2].ajouter(new Personnage(3,"Doctor Strange", new int[]{0,0,0,7,0}, Jeton.ROUGE, 1, 4));
        pioches[2].ajouter(new Personnage(3,"Nova", new int[]{0,0,3,6,3}, Jeton.ROUGE, 0, 4));
        
    }
    
    /**
     * Crée les 4 lieux
     */
    public final void creerLieu(){
        lieux[0]= new Lieu(new String[]{"Asgard","Wakanda"}, new int[][]{{3,0,3,3,0},{0,4,0,0,4}});
        lieux[1]= new Lieu(new String[]{"Hell's Kitchen, NYC","Triskelion"}, new int[][]{{4,0,4,0,0},{0,0,4,4,0}});
        lieux[2]= new Lieu(new String[]{"Knowhere","Atlantis"}, new int[][]{{3,3,0,0,3},{0,3,0,3,3}});
        lieux[3]= new Lieu(new String[]{"Attilan","Avengers Tower, NYC"}, new int[][]{{0,0,0,4,4},{4,4,0,0,0}});
    }
    
    /**
     * Initialise toutes les valeurs nécessaires pour le commencement d'une nouvelle partie
     */
    public void init(){
        /*
        System.out.println("################# AVANT ###################");
        System.out.println("pioche 0: " + pioches[0].getTaille());
        System.out.println("pioche 1: " + pioches[1].getTaille());
        System.out.println("pioche 2: " + pioches[2].getTaille());
        for(int i=0; i< joueurs.length; i++){
            System.out.println("Taille main de " + i + ": " + joueurs[i].main.size());
            System.out.println("Persos de " + i + ": " + joueurs[i].persos.size());
        }
        */
        Personnage tmp;
        int a, b;
        
        if(positionAleatoire){ //Si les positions sont aléatoires on mélange le tableau ordre
            for(int i=ordre.length-1; i>0 ; i--){
                int j=RandomSingleton.getInstance().nextInt(i+1);
                a = ordre[i];
                ordre[i]= ordre[j];
                ordre[j]= a;
            }
        }
        
        posseseurAvenger=-1; // On réinitialise le possesseur du rassemblement des avengers à -1
        //Pour chaque joueur
        for(int i=0; i< joueurs.length; i++){
            //On remet dans les pioches les persos qu'il a joués
            while(!joueurs[i].persos.isEmpty()){
                tmp =joueurs[i].persos.remove(joueurs[i].persos.size()-1);
                pioches[tmp.niveau - 1].ajouter(tmp);
                
            }
            //On remet dans les pioches les persos de sa main
            while(!joueurs[i].main.isEmpty()){
                tmp=joueurs[i].main.remove(joueurs[i].main.size()-1);
                pioches[tmp.niveau - 1].ajouter(tmp);
            }
            //On initialise tous ses attributs
            joueurs[i].clear();
        }
        //On remet les persos disponibles dans les pioches
        for(int i=0; i<persos.length; i++){
            for (int j=0; j<persos[i].length; j++){
                if(persos[i][j]!=null){
                    tmp=persos[i][j];
                    pioches[tmp.niveau - 1].ajouter(tmp);
                    persos[i][j]=null;
                }
            }
        }
        //On mélange les pioches
        pioches[0].melanger();
        pioches[1].melanger();
        pioches[2].melanger();
        
        /*
        System.out.println("------------- APRES ------------");
        System.out.println("pioche 0: " + pioches[0].getTaille());
        System.out.println("pioche 1: " + pioches[1].getTaille());
        System.out.println("pioche 2: " + pioches[2].getTaille());
        for(int i=0; i< joueurs.length; i++){
            System.out.println("Taille main de " + i + ": " + joueurs[i].main.size());
            System.out.println("Persos de " + i + ": " + joueurs[i].persos.size());
        }
*/
        
        
        //On tire les 4 premières cartes de chaque pioche
        for(int i=0; i<persos.length; i++){
            for (int j=0; j<persos[i].length; j++){
                persos[i][j]=(Personnage)pioches[i].piocher();
            }
        }
        //Pour chaque lieu on remet l'attribut conquis à -1 (non conquis) et on fixe le lieu comme étant indisponible
        for(int i=0; i<lieux.length; i++){
            lieux[i].disponible=false;
            lieux[i].conquis=-1;
        }
        
        //On ajuste les jetons et les lieux dispos en fonction du nombre de joueurs
        switch (joueurs.length) {
            case 2 -> {
                jetons=new int[]{4,4,4,4,4,5,2}; //jetons pour 2 joueurs
                //On tire au hasard les 2 lieux dispos ainsi que leur face
                a=RandomSingleton.getInstance().nextInt(4);
                b=a;
                while(b==a)
                    b=RandomSingleton.getInstance().nextInt(4);
                lieux[a].disponible=true;
                lieux[b].disponible=true;
                lieux[a].face=RandomSingleton.getInstance().nextInt(2);
                lieux[b].face=RandomSingleton.getInstance().nextInt(2);
            }
            case 3 -> {
                jetons=new int[]{5,5,5,5,5,5,3}; //jetons pour 3 joueurs
                // On tire au hasard le lieu non dispo et les faces des autres.
                a=RandomSingleton.getInstance().nextInt(4);
                for(int i=0; i<lieux.length; i++){
                    if(i!=a){
                        lieux[i].disponible=true;
                        lieux[i].face=RandomSingleton.getInstance().nextInt(2);
                    }
                }
            }
            default -> {
                jetons=new int[]{7,7,7,7,7,5,4}; // jetons pour 4 joueurs et on tire au hasard les faces des lieux
                for(int i=0; i<lieux.length; i++){
                    lieux[i].disponible=true;
                    lieux[i].face=RandomSingleton.getInstance().nextInt(2);
                }
            }
        }
    }
    
}
