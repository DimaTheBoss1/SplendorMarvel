package splendormarvel;

import java.lang.reflect.InvocationTargetException;


import java.util.Arrays;
import java.util.HashMap;

import splendormarvel.ia.*;
import splendormarvel.utils.Couleur;
import splendormarvel.utils.Jeton;
import splendormarvel.tournoi.*;
//Mes modifications jusqu'ici
//Ajout de appliquer dans verifier de la classe PrendreJeton
/**
 * Classe contenant la méthode main (point d'entrée du programme)
 * @author jeremie.humeau
 */
public class SplendorMarvel{

    /**
     * Le point d'entrée du programme.
     * C'est ici que vous pouvez tester tout ce que vous voulez
     * @param args the command line arguments
     */
//    public static void main(String[] args) throws NoSuchMethodException {
//        Joueur j1=new JoueurHumain("Humain 1"); // VOUS POUVEZ METTRE VOTRE PRENOM SI JAMAIS
//        Joueur j2=new JoueurHumain("Humain 2"); //IDEM
//        Joueur j3=new Bot(new Strat9814(), "Bot1");
//        Joueur j4=new Bot(new Strat9816(), "Bot2");

//        Joueur[] joueurs=new Joueur[2]; //METTRE LE NOMBRE DE JOUEURS ICI
//        joueurs[0]=j3;
//        joueurs[1]=j4;
        //joueurs[2]=j3; //DECOMMENTER POUR AJOUTER LE JOUEUR SI 3 joueurs OU PLUS
        //joueurs[3]=j4; //DECOMMENTER POUR AJOUTER LE JOUEUR SI 4 joueurs
        
//        Jeu jeu = new Jeu(joueurs, true);
//        jeu.newgame();

//        SystemeSuisse tournoi= new SystemeSuisse(new int[]{983,984},50);     
//        tournoi.testmultiThread();
//        tournoi.lancer();
//        System.out.println(tournoi.scoreToJson());
//
//    }
	
	/**
	 * Main method to run multiple matches between two bots and track statistics
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
	    int numMatches = 100; // Nombre de matchs à jouer
	    int[] botIds = {9815, 98}; // Test de la nouvelle stratégie contre Strat9819
	    runBotTournament(botIds, numMatches);
	}

	/**
	 * Runs a tournament between specified bots for a given number of matches
	 * @param botIds array containing the IDs of the bots to compete
	 * @param numMatches number of matches to run
	 */
	public static void runBotTournament(int[] botIds, int numMatches) {
	    int[] wins = new int[botIds.length];
	    int draws = 0;
	    int totalActionsInvalides = 0;
        int[] totalTours = new int[numMatches];
        int[] totalPointsJoueur1 = new int[numMatches];
        int[] totalPointsJoueur2 = new int[numMatches];
	    try {
	        // Create bot players
	        Joueur[] joueurs = new Joueur[botIds.length];
	        for (int i = 0; i < botIds.length; i++) {
	            Class<?> classe = Class.forName("splendormarvel.ia.Strat" + botIds[i]);
	            Strat s = (Strat) classe.getDeclaredConstructor().newInstance();
	            joueurs[i] = new Bot(s, s.nomJoueur());
	        }
	        
	        // Run matches
	        for (int match = 0; match < numMatches; match++) {
	            System.out.println(Couleur.FOND_BLEU + Couleur.BLANC_BOLD + "MATCH " + (match + 1) + " / " + numMatches + Couleur.RESET);
	            
	            // Create new game with random positions
	            Jeu jeu = new Jeu(joueurs, true);//mettre true tjrs
	            jeu.newgame();
	            
	            totalActionsInvalides += jeu.nbActionsInvalides;
	            totalTours[match] = jeu.tour;
	            totalPointsJoueur1[match] = jeu.joueurs[0].points;
	            totalPointsJoueur2[match] = jeu.joueurs[1].points;
	            
	            // Determine winner
	            boolean isDraw = true;
	            for (int i = 0; i < joueurs.length; i++) {
	                if (jeu.gagnants[i]) {
	                    wins[i]++;
	                    isDraw = false;
	                }
	            }
	            
	            if (isDraw) {
	                draws++;
	            }
	        }
	        
	        // Print results
	        System.out.println(Couleur.FOND_VERT + Couleur.NOIR_BOLD + "TOURNAMENT RESULTS" + Couleur.RESET);
	        System.out.println("Nombre total d'actions invalides : " + totalActionsInvalides);
	        System.out.println("Nombre de tours minimum : " + Arrays.stream(totalTours).min().getAsInt());
	        System.out.println("Nombre total de parties à tour minimum : " + Arrays.stream(totalTours).filter(tour -> tour == Arrays.stream(totalTours).min().getAsInt()).count());
	        System.out.println("Nombre de tours maximum : " + Arrays.stream(totalTours).max().getAsInt());
	        System.out.println("Nombre total de parties à tour maximum : " + Arrays.stream(totalTours).filter(tour -> tour == Arrays.stream(totalTours).max().getAsInt()).count());
	        System.out.println("Nombre de parties à tour <= 28 : " + Arrays.stream(totalTours).filter(tour -> tour <= 28).count());
	        System.out.println("Nombre médian de points de  " + joueurs[0].nom + ":"+ calculateMedian(totalPointsJoueur1));
	        System.out.println("Nombre médian de points de  " + joueurs[1].nom + ":"+ calculateMedian(totalPointsJoueur2));
	        System.out.println("Nombre médian de tours  : " + calculateMedian(totalTours));
	        for (int i = 0; i < botIds.length; i++) {
	            System.out.println("Bot " + botIds[i] + " (" + joueurs[i].nom + "): " + wins[i] + " wins (" + 
	                    String.format("%.2f%%", (wins[i] * 100.0 / numMatches)) + ")");
	        }
	        System.out.println("Draws: " + draws + " (" + String.format("%.2f%%", (draws * 100.0 / numMatches)) + ")");
	        
	    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | 
	             IllegalAccessException | InvocationTargetException e) {
	        ((Throwable) e).printStackTrace();
	    }
	}
	
	
	 public static double calculateMedian(int[] numbers) {
	        // Trier le tableau
	        Arrays.sort(numbers);

	        int n = numbers.length;
	        // Si le nombre d'éléments est impair, retourner l'élément du milieu
	        if (n % 2 != 0) {
	            return numbers[n / 2];
	        }
	        // Si le nombre d'éléments est pair, retourner la moyenne des deux éléments centraux
	        return (numbers[(n - 1) / 2] + numbers[n / 2]) / 2.0;
	    }
    
    /**
     * Une méthode d'affichage de l'état du jeu
     * @param input
     */
    public static void afficher(String input, Joueur joueur){
        int tmp, current;
        int[] mains= new int[4];
        int[] positions = new int[4];
        String[] lines = input.split("\\r?\\n");
        int nbJ=Integer.parseInt(lines[0]);
        System.out.println (Couleur.JAUNE + "################################# NOUVEAU TOUR!   (Nb joueurs: " + nbJ + ") #################################");
        current = 16+nbJ*2+1;
        for(int i=1; i <1+nbJ; i++){
            String[] infoJoueurs = lines[i].split(" ");
            positions[i-1]=Integer.parseInt(infoJoueurs[0]);
            if(i==1)
                System.out.println(Couleur.VERT + "Tour de " + joueur.nom + Couleur.RESET +"\tPlace du joueur: " + Integer.parseInt(infoJoueurs[0]));
            else
                System.out.println("Place du joueur: " + Integer.parseInt(infoJoueurs[0]));
            System.out.print("Reduc:\t");
            for(int j=1; j<6; j++){
                System.out.print(" " + Couleur.couleurs[j-1] + Integer.parseInt(infoJoueurs[j]) + Couleur.RESET);
            }
            System.out.print("\nJetons:\t");
            for(int j=6; j<13; j++)
                System.out.print(" " + Couleur.couleurs[j-6] + Integer.parseInt(infoJoueurs[j]) + Couleur.RESET);
            mains[i-1]=Integer.parseInt(infoJoueurs[13]);
            System.out.print("\tCartes(s) en Main: " + mains[i-1]);
            System.out.print("\tNb points: " + Integer.parseInt(infoJoueurs[14]));
            System.out.print("\tNb Avengers: " + Integer.parseInt(infoJoueurs[15]));
            System.out.println("\tRassemblement? " + Integer.parseInt(infoJoueurs[16]));
            if(i==1){
                System.out.print("Total:\t");
                for(int j=1; j<6; j++){
                    System.out.print(" " + Couleur.couleurs[j-1] + (Integer.parseInt(infoJoueurs[j]) + Integer.parseInt(infoJoueurs[j+5])) + Couleur.RESET);
            }
                System.out.println("\n" + Couleur.JAUNE + "-----------------------" + Couleur.RESET);
            }
            
            for(int j= current; j<current+mains[i-1]; j++){
                if(i==1 && j==current && mains[0]>0)
                    System.out.println(Couleur.FOND_CYAN + "Carte(s) en main:" + Couleur.RESET);
                else if(j==current && i>1 && mains[i-1]>0){
                    System.out.println(Couleur.FOND_CYAN + "Carte(s) en main du joueur en position " + positions[i-1]  + ":" + Couleur.RESET);
                }
                String[] cartes= lines[j].split(" ");
                Personnage p=new Personnage(0, "", new int[]{Integer.parseInt(cartes[2]), Integer.parseInt(cartes[3]), Integer.parseInt(cartes[4]), Integer.parseInt(cartes[5]), Integer.parseInt(cartes[6])},0,0,0);
                
                tmp=Integer.parseInt(cartes[1]);
                    System.out.print(Couleur.couleurs[tmp] + Jeton.couleur[tmp] + Couleur.RESET);
                    System.out.print(",\t Cout ->");
                for(int k=0; k<5; k++)
                    System.out.print(" " + Couleur.couleurs[k] + cartes[k+2] + Couleur.RESET);
                System.out.print("\t" + Couleur.FOND_VIOLET + Couleur.JAUNE_BOLD + " " + cartes[7] + " PV " + Couleur.RESET + "\t" + Couleur.FOND_CYAN + Couleur.NOIR + " " + cartes[8] + " Av " + Couleur.RESET);
                if(i==1){
                    if(p.peutEtreJouePar(joueur))
                        System.out.println(Couleur.CYAN + "\t\tJOUER " + (j-current) + Couleur.RESET);
                    else
                        System.out.println(Couleur.ROUGE + "\t\tPAS ASSEZ DE JETONS"+ Couleur.RESET);
                }
                
            }
            if(mains[i-1]>0)
                System.out.println();
            current=mains[i-1] + current;
        }
        System.out.println(Couleur.JAUNE + "###################################################################################################\n" + Couleur.RESET);
        for(int i=0; i<3; i++){
            System.out.print(Couleur.FOND_CYAN + "Niveau " + (i+1) + Couleur.RESET + " cartes restantes: " + lines[nbJ+i*5+1]);
            if(Integer.parseInt(lines[nbJ+i*5+1]) > 0 && joueur.main.size()<3)
                System.out.print("\t\t\t\tReserver au hasard:    " + Couleur.CYAN + "RESERVER " + i + " 4" + Couleur.RESET);
            System.out.println();
            for(int j=nbJ+i*5+2; j < nbJ+i*5+6 ; j++){
                String[] cartes= lines[j].split(" ");
                if(cartes[2].equals("-1")){
                    System.out.println("Pas de carte de niveau " + (Integer.parseInt(cartes[0])+1) + "en position " + cartes[1]);
                }
                else{
                    tmp=Integer.parseInt(cartes[2]);
                    System.out.print(Couleur.couleurs[tmp] + Jeton.couleur[tmp] + Couleur.RESET);
                    System.out.print(",\t Cout ->");
                    for(int k=0; k<5; k++)
                        System.out.print(" " + Couleur.couleurs[k] + cartes[k+3] + Couleur.RESET);
                    System.out.print("\t" + Couleur.FOND_VIOLET + Couleur.JAUNE_BOLD + " " + cartes[8] + " PV " + Couleur.RESET + "\t" + Couleur.FOND_CYAN + Couleur.NOIR + " " + cartes[9] + " Av " + Couleur.RESET);
                    Personnage p=new Personnage(0, "", new int[]{Integer.parseInt(cartes[3]), Integer.parseInt(cartes[4]), Integer.parseInt(cartes[5]), Integer.parseInt(cartes[6]), Integer.parseInt(cartes[7])},0,0,0);
                    if(p.peutEtreJouePar(joueur))
                        System.out.print(Couleur.CYAN + "\t\tJOUER " + cartes[0] + " " + cartes[1] + Couleur.RESET);
                    else
                        System.out.print(Couleur.ROUGE + "\t\tPAS ASSEZ DE JETONS"+ Couleur.RESET);
                    if(joueur.main.size()<3)
                        System.out.println(" | " + Couleur.CYAN + " RESERVER " + cartes[0] + " " + cartes[1] + Couleur.RESET);
                    else
                        System.out.println();
                }
            }
            System.out.println();
        }
        for(int i=16+nbJ ; i<16+nbJ*2; i++){
            String[] lieux=lines[i].split(" ");
            System.out.print(Couleur.FOND_CYAN + "Lieu " + (Integer.parseInt(lieux[0])) +":" + Couleur.RESET);
            
            if(lieux[6].equals("-1")){   
                System.out.print("\tNon conquis");
                if(joueur.reduc[0]>=Integer.parseInt(lieux[1])
                        && joueur.reduc[1]>=Integer.parseInt(lieux[2])
                        && joueur.reduc[2]>=Integer.parseInt(lieux[3])
                        && joueur.reduc[3]>=Integer.parseInt(lieux[4])
                        && joueur.reduc[4]>=Integer.parseInt(lieux[5]))
                    System.out.print(Couleur.CYAN + "\tLIEU " + lieux[0] + Couleur.RESET);
                System.out.print("\t");
                for(int j=0; j<5; j++){
                    if(!lieux[j+1].equals("0")){
                        System.out.print(" " + Couleur.couleurs[j] +  lieux[j+1] + " " + Jeton.couleur[j] + Couleur.RESET);
                    }
                }
                System.out.println();
            }
            else{
                System.out.println("\t Conquis par le joueur "+ lieux[6]);
            }

        }
        System.out.println();
        String[] jetons= lines[16+nbJ*2].split(" ");
        System.out.print("Jetons dispos:");
        for(int i=0; i<6; i++){
            System.out.print(" " + Couleur.couleurs[i] + "[" + Jeton.couleur[i] + " " + jetons[i] + "]" + Couleur.RESET);
        }
        System.out.println("\t" + Couleur.CYAN + "JETONS X [Y] [Z]" + Couleur.RESET + " dans l'intervalle " + Couleur.FOND_BLEU + Couleur.BLANC  + "[0,4]" + Couleur.RESET);
        System.out.print("\t\t  " +Couleur.FOND_BLEU + Couleur.BLANC + " 0 " + Couleur.RESET);
        System.out.print("\t    " +Couleur.FOND_BLEU + Couleur.BLANC + " 1 " + Couleur.RESET);
        System.out.print("\t     " +Couleur.FOND_BLEU + Couleur.BLANC + " 2 " + Couleur.RESET);
        System.out.print("      " +Couleur.FOND_BLEU + Couleur.BLANC + " 3 " + Couleur.RESET);
        System.out.println("\t " +Couleur.FOND_BLEU + Couleur.BLANC + " 4 " + Couleur.RESET + Couleur.CYAN + "\t\t\t" + "REMETTRE X [Y] [Z]" + Couleur.RESET + " dans l'intervalle " + Couleur.FOND_BLEU + Couleur.BLANC  + "[0,4]" + Couleur.RESET + "\n");    
    }   
}
