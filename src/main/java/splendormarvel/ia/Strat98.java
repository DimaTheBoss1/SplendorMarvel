package splendormarvel.ia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import splendormarvel.Bot;
import splendormarvel.Jeu;
import splendormarvel.Joueur;
import splendormarvel.Lieu;
import splendormarvel.Personnage;
import splendormarvel.actions.ActionManager;
import splendormarvel.utils.Couleur;

/**
 * @author Gabriel DIMA NKOA
 * 
 * Stratégie avancée pour Splendor Marvel qui:
 *  - S'adapte en trois phases de jeu (début, milieu, fin) avec des priorités différentes; les 2 premières consistant à
 *  	accumuler les cartes de niveau 1 et 2 si possible. La dernière servant à acquérir une carte de niveau 3
 *  -Priorise les cartes réservées pour optimiser la prise de jetons
 *	-Utilise un système défensif pour bloquer les actions critiques des adversaires .Anticipe et bloque les mouvements gagnants de l'adversaire
 *	-Optimise l'acquisition des lieux et du rassemblement des Avengers
 *	-Évalue dynamiquement les cartes selon leur coût, points, Avengers et synergies.
 *		Utilise un système de scoring sophistiqué pour attribuer des valeurs aux cartes du plateau 
 *	-Maximise les réductions de coût pour accélérer l'acquisition des cartes de haut niveau
 *	-Utilise les valeurs des cartes du plateau pour prendre les jetons(aussi en fonction de leur rareté)
 *	-Adapte la stratégie de prise de jetons selon les besoins immédiats et futurs
 *	-Combine les actions JOUER avec une action LIEU et les actions JETONS avec une action REMETTRE lorsque cela est nécessaire
 *	-Si aucune action n'est trouvée, jouer une fallBackAction
 * 
 */

public class Strat98 extends Strat {
	private static final double[] WEIGHT_CARD_POINTS = {2.45, 2.45, 2.45};
	private static final double[] WEIGHT_CARD_LEVEL = {0.09, 0.27, 1.12};
	private static final double[] WEIGHT_CARD_AVENGERS = {1.0, 1.0, 1.0};
	private static final double[] WEIGHT_NEW_COLOR = {5.0, 5.0, 5.0};
	private static final double[] WEIGHT_COST = {0.75, 0.75, 0.75};
	private static final double[] WEIGHT_LEVEL3_SYNERGY = {2.5, 2.5, 2.5};
	private static final double[] WEIGHT_LOCATION = {3.0, 3.0, 3.0};
	private static final double[] WEIGHT_RESERVED = {4.36, 2.96, 3.51};
	private static final double[] WEIGHT_ACQUIS = {1.5, 1.5, 1.5};

	private int nbJ;
	private int place;
	private int phase;
	private int tour;
	private int[] nbreCartesRestantesDansPioche;
	private Lieu lieu1;
	private int idLieu1;
	private Lieu lieu2;
	private int idLieu2;
	private String nom;
	private Joueur joueurEnCours;
	private Joueur joueurSuivant;
	private Joueur[] joueurs;
	private Jeu jeu;
	private Personnage bestLevel3;
	private boolean hasLevel3Target;
	

	public Strat98() {
		this.nom = "DIMA NKOA Gabriel";
	}

	@Override
	public String nomJoueur() {
		return nom;
	}

	@Override
	public void init(String etatDuJeu) {
		String[] lines = etatDuJeu.split("\\r?\\n");
		hasLevel3Target = false;
		
		this.place = Integer.parseInt(lines[1].split(" ")[0]); 
		this.nbJ = 0;
		int[][] tableau = new int[5][5];
		String[] tableauString = {"lieu1", "lieu2"};
		this.lieu1 = new Lieu(tableauString, tableau);
		this.lieu2 = new Lieu(tableauString, tableau);
		this.joueurEnCours = new Bot(this, "joueurEnCours");
		this.joueurSuivant = new Bot(this, "joueurSuivant");
		this.joueurs = new Joueur[2];
		this.joueurs[place] = this.joueurEnCours;
		this.joueurs[1-place] = this.joueurSuivant;
		this.jeu = new Jeu(joueurs, false);
		tour = 0;
		this.nbreCartesRestantesDansPioche = new int[3];
		bestLevel3 = null;
		parserEtatDuJeu(lines);
		updateGamePhase();
	}

	@Override
	public String jouer(String etatDuJeu) {
		String[] lines = etatDuJeu.split("\\r?\\n");
		parserEtatDuJeu(lines);
		tour++;
		updateGamePhase();
		
		String actionWin = winCheck();
		String actionDefensive = defensiveReservation();
		
		if(actionWin != null) {
			return actionWin;
		}
		else if(actionDefensive != null) {
			return actionDefensive;
		}
		
		switch (phase) {
			case 0: return playStartGame();
			case 1: return playMidGame();
			case 2: return playEndGame();
			default: return playStartGame();
		}
	}
	
	

	    private String combinerAvecPriseLieu(String actionJouer, Joueur joueur) {
	        if (actionJouer == null) {
	            return null;
	        }
	        // On simule le jeu de la carte
	        String[] actionJouerArray = actionJouer.split(" ");
	        Joueur joueurSimule = simulerJouerCarte(actionJouerArray, joueur);

	        // On vérifie si on peut prendre un lieu avec l'état simulé
	        String[] actionLieu = prendreLieu(joueurSimule);

	        // Si pas de lieu à prendre
	        if (actionLieu == null) {
	            return actionJouer;
	        }

	        // On combine les deux actions
	        StringBuilder actionComplete = new StringBuilder();
	        String[] parts = actionJouer.split(" ");
	        for (String part : parts) {
	            actionComplete.append(part).append(" ");
	        }

	        actionComplete.append(";");

	        for (String part : actionLieu) {
	            actionComplete.append(" ").append(part);
	        }

	        return actionComplete.toString().trim();
	    }

	    private Joueur simulerJouerCarte(String[] actionJouerArray, Joueur joueurInput) {
	    	if(verifierJouerCarte(joueurInput, actionJouerArray) == false) return null;
	        if (joueurInput == null || jeu == null) return null; // Basic validation
	        Joueur joueurSimule = safeDeepCopyJoueur(joueurInput);
	        if (joueurSimule == null) return null; // Copy failed

	        Personnage carteJouee = null;
	        int nbParams = actionJouerArray.length -1;

	        if (nbParams == 1) {
	            try {
	                int mainIndex = Integer.parseInt(actionJouerArray[1]);
	                if (mainIndex < 0 || mainIndex >= joueurSimule.main.size()) return null; // Invalid index
	                carteJouee = joueurSimule.main.get(mainIndex);
	                joueurSimule.main.remove(mainIndex);
	            } catch (NumberFormatException | IndexOutOfBoundsException e) {
	                return null; // Error in action, return original
	            }
	        } else if (nbParams == 2) {
	            try {
	                int niv = Integer.parseInt(actionJouerArray[1]);
	                int pos = Integer.parseInt(actionJouerArray[2]);
	                if (niv < 0 || niv > 2 || pos < 0 || pos > 3  || jeu.persos[niv][pos] == null) return null; // Invalid index
	                carteJouee = jeu.persos[niv][pos];
	            } catch (NumberFormatException e) {
	                return null; // Error in action, return original
	            }
	        } else {
	            return null; // Invalid action format
	        }

	        if (carteJouee == null) return null; // Card not found

	        int jokerAUtiliser = 0;
	        for (int i = 0; i < 5; i++) {
	            int coutReel = Math.max(0, carteJouee.cout[i] - joueurSimule.reduc[i]);
	            if (joueurSimule.jetons[i] >= coutReel) {
	                joueurSimule.jetons[i] -= coutReel;
	            } else {
	                jokerAUtiliser += coutReel - joueurSimule.jetons[i];
	                joueurSimule.jetons[i] = 0;
	            }
	        }
	        if (joueurSimule.jetons[5] < jokerAUtiliser) {
	            // Not enough jokers, should have been caught by verifierJouerCarte
	            return null; // simulation leads to invalid state
	        }
	        joueurSimule.jetons[5] -= jokerAUtiliser;

	        joueurSimule.reduc[carteJouee.couleur]++;
	        joueurSimule.points += carteJouee.points;
	        joueurSimule.avenger += carteJouee.avenger;

	        
	        if ( carteJouee.niveau == 2 && joueurSimule.jetons[6] == 0) {
	            joueurSimule.jetons[6] = 1; // Thanos tile
	        }
	        
	     // Check if player can take a location
	        String[] actionLieu = prendreLieu(joueurSimule);
	        if (actionLieu != null) {
	            // Player can take a location, update state and add points
	            int lieuId = Integer.parseInt(actionLieu[1]);
	            Lieu lieuPris = (lieuId == idLieu1) ? lieu1 : lieu2;
	            joueurSimule.lieuxConquis.add(lieuPris);
	            joueurSimule.points += 3; // Locations are worth 3 points
	        }
	        
	        //  Check for Avengers assembly
	        Joueur autreJoueur = (joueurInput == joueurEnCours) ? joueurSuivant : joueurEnCours;
	        if (joueurSimule.avenger >= 3 && 
	            (joueurSimule.avenger > autreJoueur.avenger || 
	            (joueurSimule.avenger == autreJoueur.avenger && !autreJoueur.rassemblement))) {
	            // Set rassemblement flag and add points if not already taken
	            if (!joueurSimule.rassemblement) {
	                joueurSimule.rassemblement = true;
	                joueurSimule.points += 3; // Avengers assembly is worth 3 points
	            }
	        }
	        return joueurSimule;
	    }
	    private String tryAllTokenCombinations() {
	        
	        // Try all possible triple token combinations
	        for (int i = 0; i < 5; i++) {
	            for (int j = i + 1; j < 5; j++) {
	                for (int k = j + 1; k < 5; k++) {
	                    String[] action = {"JETONS", String.valueOf(i), String.valueOf(j), String.valueOf(k)};
	                    if (verifierPrendreJeton(action, joueurEnCours)) {
	                        return String.join(" ", action);
	                    }
	                }
	            }
	        }
	        
	     // Try all possible double different token combinations
	        for (int i = 0; i < 5; i++) {
				for (int j = i + 1; j < 5; j++) {
						String[] action = {"JETONS", String.valueOf(i), String.valueOf(j)};
						if (verifierPrendreJeton(action, joueurEnCours)) {
							return String.join(" ", action);
					}
				}
			}
	        
	     // Try all possible double token combinations
	        for (int i = 0; i < 5; i++) {
	            if (jeu.jetons[i] >= 4) {
	                String[] action = {"JETONS", String.valueOf(i), String.valueOf(i)};
	                if (verifierPrendreJeton(action, joueurEnCours)) {
	                    return String.join(" ", action);
	                }
	            }
	        }
	        
	        
	     // Try all possible single token combinations
	        for (int i = 0; i < 5; i++) {
	            if (jeu.jetons[i] > 0) {
	                String[] action = {"JETONS", String.valueOf(i)};
	                if (verifierPrendreJeton(action, joueurEnCours)) {
	                    return String.join(" ", action);
	                }
	            }
	        }

	        return "JETONS 0"; // Fallback action
	    }

	    

	   

	    private boolean isColorNeededForLocation(int couleur) {
	        if (lieu1 != null && lieu1.disponible) {
	            if (lieu1.couleurs[lieu1.face][couleur] > joueurEnCours.reduc[couleur]) {
	                return true;
	            }
	        }
	        if (lieu2 != null && lieu2.disponible) {
	            if (lieu2.couleurs[lieu2.face][couleur] > joueurEnCours.reduc[couleur]) {
	                return true;
	            }
	        }
	        return false;
	    }

	    private Personnage findBestLevel3Card(int seuil) {
	        Personnage bestCard = null;
	        int minResourcesNeeded = Integer.MAX_VALUE;

	        // Parcourir toutes les cartes de niveau 3
	        for (int pos = 0; pos < 4; pos++) {
	            if (jeu.persos[2][pos] != null) {
	                Personnage card = jeu.persos[2][pos];
	                int resourcesNeeded = 0;
	                
	                // Calculer les ressources manquantes pour chaque couleur
	                for (int i = 0; i < 5; i++) {
	                    int cout = card.cout[i];
	                    int reduc = joueurEnCours.reduc[i];
	                    int jetons = joueurEnCours.jetons[i];
	                    
	                    // Ressources manquantes = coût - (réductions + jetons)
	                    int manquant = Math.max(0, cout - (reduc + jetons));
	                    resourcesNeeded += manquant;
	                }
	                
	                // Prendre en compte les jokers disponibles
	                resourcesNeeded = Math.max(0, resourcesNeeded - joueurEnCours.jetons[5]);
	                
	                // Si cette carte nécessite moins de ressources, elle devient la meilleure
	                if (resourcesNeeded < minResourcesNeeded) {
	                    minResourcesNeeded = resourcesNeeded;
	                    bestCard = card;
	                }
	                // En cas d'égalité, préférer la carte avec le plus de points ou avengers
	                else if (resourcesNeeded == minResourcesNeeded && bestCard != null) {
	                    if (card.points > bestCard.points || 
	                        (card.points == bestCard.points && card.avenger > bestCard.avenger)) {
	                        bestCard = card;
	                    }
	                }
	            }
	        }
	        
	        return minResourcesNeeded <= seuil ? bestCard : null ;
	    }
	    
	    private int findCardPosition(Personnage card) {
	        for (int i = 0; i < 4; i++) {
	            if (jeu.persos[2][i] == card) {
	                return i;
	            }
	        }
	        for (int i = 0; i < 4; i++) {
	            if (jeu.persos[1][i] == card) {
	                return i;
	            }
	        }
	        for (int i = 0; i < 4; i++) {
	            if (jeu.persos[0][i] == card) {
	                return i;
	            }
	        }
	        return -1;
	    }

	    private String trouverMeilleureCarteAJouer(int limitation) {
	    	
	    	// Check if we can win by playing any card from the board
	        for (int winNiv = 2; winNiv >= 0; winNiv--) {
	            for (int winPos = 0; winPos < 4; winPos++) {
	                Personnage winCard = jeu.persos[winNiv][winPos];
	                if (winCard != null) {
	                    String[] winAction = {"JOUER", String.valueOf(winNiv), String.valueOf(winPos)};
	                    if (verifierJouerCarte(joueurEnCours, winAction)) {
	                        Joueur winSimulated = simulerJouerCarte(winAction, joueurEnCours);
	                        
	                        if (winSimulated != null) {
	                            Joueur[] joueursOriginal = jeu.joueurs;
	                            boolean[] gagnantsOriginal = jeu.gagnants;
	                            this.jeu.joueurs[place] = winSimulated;
	                            this.jeu.joueurs[1-place] = joueurSuivant;
	                            
	                            if (jeu.end()) {
	                                jeu.quiGagne();
	                                if (jeu.gagnants[place] == true) {
//	                                    System.out.println(Couleur.VERT_BOLD + "Playing board card for immediate win! 1" + Couleur.RESET);
	                                    
	                                    this.jeu.joueurs = joueursOriginal;
	                                    this.jeu.gagnants = gagnantsOriginal;
	                                    return String.join(" ",  winAction);
	                                }
	                            }
	                            this.jeu.joueurs = joueursOriginal;
	                            this.jeu.gagnants = gagnantsOriginal;
	                        }
	                    }
	                }
	            }
	        }

	        // Check cards in hand for winning moves
	        for (int i = 0; i < joueurEnCours.main.size(); i++) {
	            String[] winHandAction = {"JOUER", String.valueOf(i)};
	            if (verifierJouerCarte(joueurEnCours, winHandAction)) {
	                Joueur winSimulated = simulerJouerCarte(winHandAction, joueurEnCours);
	                
	                if (winSimulated != null) {
	                    Joueur[] joueursOriginal = jeu.joueurs;
	                    boolean[] gagnantsOriginal = jeu.gagnants;
	                    this.jeu.joueurs[place] = winSimulated;
	                    this.jeu.joueurs[1-place] = joueurSuivant;
	                    
	                    if (jeu.end()) {
	                        jeu.quiGagne();
	                        if (jeu.gagnants[place] == true) {
//	                            System.out.println(Couleur.VERT_BOLD + "Playing reserved card for immediate win! 1" + Couleur.RESET);
	                            this.jeu.joueurs = joueursOriginal;
	                            this.jeu.gagnants = gagnantsOriginal;
	                            return String.join(" ",  winHandAction);
	                        }
	                    }
	                    this.jeu.joueurs = joueursOriginal;
	                    this.jeu.gagnants = gagnantsOriginal;
	                }
	            }
	        }
	        
	        //Sinon, on cherche la meilleure carte à jouer en priorisant les cartes réservées
	        List<Personnage> cartesPossibles = new ArrayList<>();
	        Map<Personnage, Double> cartesPrioritaires = cartesAPrioriserPourCartesReservees(joueurEnCours,phase);
	        List<Personnage> topCartes = cartesPrioritaires.keySet().stream()
	                .limit(limitation)
	                .collect(Collectors.toList());
	            
	        for (int i = 0; i < joueurEnCours.main.size(); i++) {
	            Personnage carte = joueurEnCours.main.get(i);
	            if (carte != null ) {
	                cartesPossibles.add(carte);
	            }
	        }

	        for(Personnage carte : topCartes){
	            cartesPossibles.add(carte);
	            
	        }

	        

	        if (!cartesPossibles.isEmpty()) {
	            for(int i=0; i<cartesPossibles.size(); i++){
	                Personnage meilleureCarte = cartesPossibles.get(i);
	                int position = findCardPosition(meilleureCarte);
	                
	                if (position == -1 ) {
	                    String[] action1 = new String[2]; // Change the size according to your needs
	                    action1[0] = "JOUER";
	                    action1[1] = String.valueOf(i);
	                    if(verifierJouerCarte(joueurEnCours, action1)){
	                        return "JOUER " + String.valueOf(i) ;
	                    }
	                    
	                } else {
	                    String[] action1 = new String[3]; // Change the size according to your needs
	                    action1[0] = "JOUER";
	                    action1[1] = String.valueOf(meilleureCarte.niveau);
	                    action1[2] = String.valueOf(position);
	                    if(verifierJouerCarte(joueurEnCours, action1)){
	                        return "JOUER " + String.valueOf(meilleureCarte.niveau) + " " + String.valueOf(position);
	                    }
	                    
	                }
	            }
	        }

	        return null;
	    }


	    private List<String[]> generateTokenCombinations() {
	        List<String[]> combinations = new ArrayList<>();
	     // Triple different tokens
	        for (int i = 0; i < 5; i++) {
	            for (int j = i + 1; j < 5; j++) {
	                for (int k = j + 1; k < 5; k++) {
	                    combinations.add(new String[]{"JETONS", String.valueOf(i), String.valueOf(j), String.valueOf(k)});
	                }
	            }
	        }
	        
	      //Double different tokens
	        for (int i = 0; i < 5; i++) {
				for (int j = i + 1; j < 5; j++) {
					//if (jeu.jetons[i] >= 2 && jeu.jetons[j] >= 2) {
						combinations.add(new String[]{"JETONS", String.valueOf(i), String.valueOf(j)});
					//}
				}
			}
	        
	     // Double same tokens
	        for (int i = 0; i < 5; i++) {
	            //if (jeu.jetons[i] >= 4) {
	                combinations.add(new String[]{"JETONS", String.valueOf(i), String.valueOf(i)});
	            //}
	        }
	        
	        // Single tokens
	        for (int i = 0; i < 5; i++) {
	            combinations.add(new String[]{"JETONS", String.valueOf(i)});
	        }
	        return combinations;
	    }

	    

	    private double evaluerCombinaisonJetons(String[] combination, double[] jetonsNecessaires, Map<Personnage, Double> cartesPrioritaires) {
	        double[] score = new double[combination.length - 1];
	        Map<Personnage, Double> cartesPrioritairesAdverses = cartesAPrioriserPourCartesReservees(joueurSuivant,phase)
	        		.entrySet()
	                .stream()
	                .filter(entry -> entry.getValue() > 0)
	                .limit(2)
	                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	        for (int i = 1; i < combination.length; i++) {
	            int couleur = Integer.parseInt(combination[i]);
	            score[i-1] += jetonsNecessaires[couleur];
	            
	            if (jeu.jetons[couleur] == 2) {
	                score[i-1] += 4;
	            }
	            if (jeu.jetons[couleur] == 1){
	                score[i-1] += 5;
	            }
	            
	            for(Personnage carte: joueurEnCours.main){
		            if(carte.cout[couleur] - joueurEnCours.reduc[couleur] - joueurEnCours.jetons[couleur] > 0){
			            score[i-1] += 15;
		            }
	            }
	            //On augmente le score si le joueur adverse a besoin de ce jeton
	            for (Map.Entry<Personnage, Double> entry : cartesPrioritairesAdverses.entrySet()) {
	                if (entry.getKey().cout[couleur] - joueurSuivant.reduc[couleur] - joueurSuivant.jetons[couleur]> 0) {
	                        score[i-1] += entry.getValue()/3;///2.0;     
	                }
	            }
	            for (Map.Entry<Personnage, Double> entry : cartesPrioritaires.entrySet()) {
	                if (entry.getKey().cout[couleur] - joueurEnCours.reduc[couleur] - joueurEnCours.jetons[couleur] > 0) {
	                    score[i-1] += entry.getValue();
	                }
	            }
	            
	            
	        }
	        return score.length > 0 ? Arrays.stream(score).sum() : 0;
	    }

	    

	    private String[] remettreJetons(Joueur joueur) {
	    	
	    		int[] jExcess = Arrays.copyOf(joueur.jetons, 7);
	            int total = Arrays.stream(jExcess).sum();

	            if (total <= 10) {
	                return null;
	            }

	            // Calculate scores for each token color
	            double[] tokenScores = new double[6];
	            Map<Personnage, Double> cartesPrioritaires = cartesAPrioriserPourCartesReservees(joueur,phase);
	            Map<Personnage, Double> cartesPrioritairesAdverses = cartesAPrioriserPourCartesReservees(joueurSuivant,phase)
						.entrySet()
						.stream()
						.filter(entry -> entry.getValue() > 0)
						.limit(2)
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

	            for (int i = 0; i < 5; i++) {
	                if (jeu.jetons[i] == 2) {
	                    tokenScores[i] += 4;
	                }
	                if (jeu.jetons[i] == 1) {
	                    tokenScores[i] += 5;
	                }
	                if(bestLevel3 != null && bestLevel3.cout[i] - joueur.reduc[i] - joueur.jetons[i] > 0){
		                tokenScores[i] += 15;
	                }
	               
	                // Score based on priority cards
	                for (Map.Entry<Personnage, Double> entry : cartesPrioritaires.entrySet()) {
	                    if (entry.getKey().cout[i] - joueur.reduc[i] - joueur.jetons[i] > 0) {
	                        tokenScores[i] += entry.getValue();
	                    }
	                }
	                
	                // Score based on opponent's needs
	                for (Map.Entry<Personnage, Double> entry : cartesPrioritairesAdverses.entrySet()) {
	                    if (entry.getKey().cout[i] - joueurSuivant.reduc[i] - joueurSuivant.jetons[i] > 0) {
	                        tokenScores[i] += entry.getValue() ;/// 2;
	                    }
	                }
	            }

	            List<Integer> tokensToRemove = new ArrayList<>();
	            while (total > 10) {
	                // Find token with lowest score
	                int minScoreIndex = 0;
	                for (int i = 1; i < 5; i++) {
	                    if (jExcess[i] > 0 && (tokenScores[i] < tokenScores[minScoreIndex] || jExcess[minScoreIndex] == 0)) {
	                        minScoreIndex = i;
	                    }
	                }
	                jExcess[minScoreIndex]--;
	                tokensToRemove.add(minScoreIndex);
	                total--;
	            }
	            if(tokensToRemove.size()>=1) {
		            String[] result = new String[tokensToRemove.size() + 1];
		            result[0] = "REMETTRE";
		            for (int i = 0; i < tokensToRemove.size(); i++) {
		                result[i + 1] = String.valueOf(tokensToRemove.get(i));
		            }
		
		            return verifierRemettreJeton(result,joueur)?result:null;
	            }
				return null;
	       
	    }

	   

	        private boolean verifierLieu(int idLieu, Joueur joueur) {
	            Lieu lieu = (idLieu == idLieu1) ? lieu1 : lieu2;
	            if (lieu.disponible == false || lieu.conquis >= 0) {
	                return false;
	            }
	            
	            for (int i = 0; i < 5; i++) {
	                if (lieu.couleurs[lieu.face][i] > joueur.reduc[i]) {
	                    return false;
	                }
	            }
	            return true;
	        }

	        private String[] prendreLieu(Joueur joueur) {
	                if (joueur == null) return null;
	                Lieu bestLieuToTake = null;
	                int bestLieuId = -1;
	                int highestPoints = -1; // Keep track of points of the best lieu found so far
	                boolean currentBestLieuWinsGame = false;

	                // Check Lieu 1
	                if (lieu1 != null && lieu1.disponible && lieu1.conquis == -1 && verifierLieu(idLieu1, joueur)) {
	                    boolean winsWithLieu1 = (joueur.points + lieu1.points >= 15);
	                    if (bestLieuToTake == null || // First one we can take
	                        (winsWithLieu1 && !currentBestLieuWinsGame) || // New one wins, old one didn't
	                        (winsWithLieu1 == currentBestLieuWinsGame && lieu1.points > highestPoints) || // Both win (or both don't), take higher points
	                        (!winsWithLieu1 && !currentBestLieuWinsGame && lieu1.points > highestPoints)) { // Neither wins, take higher points
	                        bestLieuToTake = lieu1;
	                        bestLieuId = idLieu1;
	                        highestPoints = lieu1.points;
	                        currentBestLieuWinsGame = winsWithLieu1;
	                    }
	                }

	                // Check Lieu 2
	                if (lieu2 != null && lieu2.disponible && lieu2.conquis == -1 && verifierLieu(idLieu2, joueur)) {
	                    boolean winsWithLieu2 = (joueur.points + lieu2.points >= 15);
	                     if (bestLieuToTake == null ||
	                        (winsWithLieu2 && !currentBestLieuWinsGame) ||
	                        (winsWithLieu2 == currentBestLieuWinsGame && lieu2.points > highestPoints) ||
	                        (!winsWithLieu2 && !currentBestLieuWinsGame && lieu2.points > highestPoints)) {
	                        bestLieuToTake = lieu2; // This will overwrite lieu1 if lieu2 is better by the criteria
	                        bestLieuId = idLieu2;
	                        highestPoints = lieu2.points; // Update highestPoints and winning status
	                        currentBestLieuWinsGame = winsWithLieu2;
	                    }
	                }

	                if (bestLieuToTake != null) {
	                    return new String[]{"LIEU", String.valueOf(bestLieuId)};
	                }
	                return null;
	            }
	        

	    private boolean verifierReserverCarte(String[] action, Joueur joueur) {
	        if (joueur.main.size() >= 3) {
	            return false;
	        }

	        int niv = Integer.parseInt(action[1]);
	        if (niv < 0 || niv > 2) {
	            return false;
	        }

	        if (action.length == 3) {
	            int pos = Integer.parseInt(action[2]);
	            if (pos < 0 || pos > 3) {
	                return false;
	            }
	            return jeu.persos[niv][pos] != null;
	        } else if (action.length == 4 && action[3].equals("4")) {
	            return nbreCartesRestantesDansPioche[niv] > 0;
	        }

	        return false;
	    }

	    private boolean jetonsDispoPourJouer(Joueur joueur, String[] action) {
	        boolean res = true;
	        int joker = joueur.jetons[5]; // le nombre de jokers du joueur
	        int cpt = 0;
	        int nb = action.length - 1;
	        Personnage perso;
	        int[] jetonsRequis = new int[5]; // le nombre de jetons requis pour la carte
	        // On fixe la carte voulue
	        if (nb == 1)
	            perso = joueur.main.get(Integer.parseInt(action[1])); // carte en main
	        else if (nb == 2 && jeu.persos[Integer.parseInt(action[1])][Integer.parseInt(action[2])] == null) {
	            return false;
	        } else
	            perso = jeu.persos[Integer.parseInt(action[1])][Integer.parseInt(action[2])]; // carte en jeu
	        for (int i = 0; i < 5; i++) // on définit le nombre de jetons requis en fonction des réductions du joueur
	            jetonsRequis[i] = Math.max(0, perso.cout[i] - joueur.reduc[i]);
	        while (cpt < 5 && res && joker >= 0) { // On compte le nombre de jokers nécessaires (quand le joueur n'a pas
	                                               // assez de jetons d'une couleur)
	            if (jetonsRequis[cpt] > (joueur.jetons[cpt] + joker))
	                res = false; // Si le joueur n'as pas assez de jokers, on fixe la valeur de retour à faux
	            else
	                joker -= (Math.max(0, jetonsRequis[cpt] - joueur.jetons[cpt])); // Sinon on décrémente le nombre de
	                                                                                // jokers
	            cpt++; // Et on passe à la couleur suivante
	        }
	        return res;
	    }

	    private boolean couleurValideEtDispo(String[] action) {
	        int nb = action.length - 1;
	        if (nb > 3 || nb < 1) {
	            return false;
	        }
	        for (int i = 1; i <= nb; i++) {
	            int couleur = Integer.parseInt(action[i]);
	            if (couleur < 0 || couleur > 5 || jeu.jetons[couleur] <= 0) {
	                return false;
	            }
	            if (nb == 2 && action[1].equals(action[2]) && jeu.jetons[couleur] < 4) {
	                return false;
	            }
	        }
	        return true;
	    }

	    private void parserEtatDuJeu(String[] lines) {
	        nbJ = Integer.parseInt(lines[0]); 
	        place = Integer.parseInt(lines[1].split(" ")[0]); 
	        
	        
	        
	        lieu1 = new Lieu(new String[2], new int[5][5]);
	        lieu2 = new Lieu(new String[2], new int[5][5]);
	        lieu1.disponible = true;
	        lieu1.points = 3;
	        lieu2.disponible = true;
	        lieu2.points = 3;
	        
	        for (int i = 0; i < 2; i++) {
	            String[] lieuInfo = lines[18 + i].split(" ");

	            int[] couleurs1 = {
	                    Integer.parseInt(lieuInfo[1]), Integer.parseInt(lieuInfo[2]),
	                    Integer.parseInt(lieuInfo[3]), Integer.parseInt(lieuInfo[4]),
	                    Integer.parseInt(lieuInfo[5])
	            };
	            int[][] couleurs = { couleurs1, couleurs1 };

	            if (i == 0) {
	                lieu1.conquis = Integer.parseInt(lieuInfo[6]);
	                lieu1.couleurs = couleurs;
	                idLieu1 = Integer.parseInt(lieuInfo[0]);
	                lieu1.face = 1;
	            } 
	            else {
	                lieu2.conquis = Integer.parseInt(lieuInfo[6]);
	                lieu2.couleurs = couleurs;
	                idLieu2 = Integer.parseInt(lieuInfo[0]);
	                lieu2.face = 1;
	            } 

	        }
	        

	        for (int i = 0; i < nbJ; i++) {
	            if (i == place) {
	                joueurEnCours = new Bot(this, "joueurEnCours");
	                String[] joueurInfo = lines[1].split(" ");
	                

	                joueurEnCours.points = Integer.parseInt(joueurInfo[14]);
	                joueurEnCours.avenger = Integer.parseInt(joueurInfo[15]);

	                for (int j = 0; j < 7; j++) {
	                    joueurEnCours.jetons[j] = Integer.parseInt(joueurInfo[6 + j]);
	                }
	                for (int j = 0; j < 5; j++) {
	                    joueurEnCours.reduc[j] = Integer.parseInt(joueurInfo[1 + j]);
	                }
	                int nbCartesReservees = Integer.parseInt(joueurInfo[13]);
	                for (int j = 0; j < nbCartesReservees; j++) {
	                    String[] carteResInfo = lines[21 + j].split(" ");
	                    int cout[] = { Integer.parseInt(carteResInfo[2]), Integer.parseInt(carteResInfo[3]),
	                            Integer.parseInt(carteResInfo[4]), Integer.parseInt(carteResInfo[5]),
	                            Integer.parseInt(carteResInfo[6]) };

	                    joueurEnCours.main.add(new Personnage(Integer.parseInt(carteResInfo[0]), "carte" + j, cout,
	                            Integer.parseInt(carteResInfo[1]), Integer.parseInt(carteResInfo[8]),
	                            Integer.parseInt(carteResInfo[7])));
	                }
	                joueurEnCours.rassemblement = joueurInfo[16].equals("1");

	            }

	            if (i != place) {
	                joueurSuivant = new Bot(this, "joueurSuivant");
	                String[] joueurInfo = lines[2].split(" ");
	               

	                joueurSuivant.points = Integer.parseInt(joueurInfo[14]);
	                joueurSuivant.avenger = Integer.parseInt(joueurInfo[15]);

	                for (int j = 0; j < 7; j++) {
	                    joueurSuivant.jetons[j] = Integer.parseInt(joueurInfo[6 + j]);
	                }

	                for (int j = 0; j < 5; j++) {
	                    joueurSuivant.reduc[j] = Integer.parseInt(joueurInfo[1 + j]);
	                }
	                int nbCartesReservees = Integer.parseInt(joueurInfo[13]);
	                for (int j = 0; j < nbCartesReservees; j++) {
	                    String[] carteResInfo = lines[21 + j + Integer.parseInt(lines[1].split(" ")[13])].split(" ");
	                    int cout[] = { Integer.parseInt(carteResInfo[2]), Integer.parseInt(carteResInfo[3]),
	                            Integer.parseInt(carteResInfo[4]), Integer.parseInt(carteResInfo[5]),
	                            Integer.parseInt(carteResInfo[6]) };

	                    joueurSuivant.main.add(new Personnage(Integer.parseInt(carteResInfo[0]), "carte" + j, cout,
	                            Integer.parseInt(carteResInfo[1]), Integer.parseInt(carteResInfo[8]),
	                            Integer.parseInt(carteResInfo[7])));
	                }
	                joueurSuivant.rassemblement = joueurInfo[16].equals("1");

	            }
	        }
	        
	        jeu.lieux[idLieu1] = lieu1;
	        jeu.lieux[idLieu2] = lieu2;
	        jeu.ordre[place] = place;
	        jeu.ordre[1 - place] = 1 - place;
	        jeu.joueurs[jeu.ordre[place]] = joueurEnCours;	
	        jeu.joueurs[jeu.ordre[1 - place]] = joueurSuivant;

	        for (int niveau = 0; niveau < 3; niveau++) {
	            String[] piocheInfo = lines[3 + niveau * 5].split(" ");
	            nbreCartesRestantesDansPioche[niveau] = Integer.parseInt(piocheInfo[0]);
	            for (int i = 0; i < 4; i++) {
	                String[] carteInfo = lines[4 + niveau * 5 + i].split(" ");
	                if (carteInfo.length == 3) {
	                    jeu.persos[niveau][Integer.parseInt(carteInfo[1])] = null;
	                } else {
	                    int[] cout = {
	                            Integer.parseInt(carteInfo[3]), Integer.parseInt(carteInfo[4]),
	                            Integer.parseInt(carteInfo[5]), Integer.parseInt(carteInfo[6]),
	                            Integer.parseInt(carteInfo[7])
	                    };
	                    jeu.persos[niveau][Integer.parseInt(carteInfo[1])] = new Personnage(niveau, "carte 1", cout,
	                            Integer.parseInt(carteInfo[2]), Integer.parseInt(carteInfo[9]),
	                            Integer.parseInt(carteInfo[8]));
	                }

	            }
	        }

	        for (int i = 0; i < 7; i++) {
	            jeu.jetons[i] = Integer.parseInt(lines[20].split(" ")[i]);
	        }


	    }

	    private boolean verifierJouerCarte(Joueur joueur, String[] action) {
	        boolean res = true;
	        int nb = action.length - 1;
	        
	        if ((nb == 1 && joueur.main.size() < Integer.parseInt(action[1]) + 1) ||
	                (nb == 2 && (Integer.parseInt(action[1]) < 0 || Integer.parseInt(action[1]) > 3 ||
	                        Integer.parseInt(action[2]) < 0 || Integer.parseInt(action[2]) > 3) &&
	                        jeu.persos[Integer.parseInt(action[1])][Integer.parseInt(action[2])] == null)
	                || jetonsDispoPourJouer(joueur, action) == false) {
	            res = false;
	        }
	        return res;
	    }

	    private boolean verifierPrendreJeton(String[] action, Joueur joueur) {
	        int nb = action.length - 1;
	        int[] jetonsVoulus = new int[nb];
	        boolean res;
	        for (int i = 0; i < nb; i++) {
	            jetonsVoulus[i] = Integer.parseInt(action[i + 1]);
	        }
	        if (res = couleurValideEtDispo(action)) { 
	            if (nb == 3 && (jetonsVoulus[0] == jetonsVoulus[1] || jetonsVoulus[0] == jetonsVoulus[2]
	                    || jetonsVoulus[1] == jetonsVoulus[2]))
	                res = false;
	            else if (nb == 2 && jetonsVoulus[0] == jetonsVoulus[1] && jeu.jetons[jetonsVoulus[0]] < 4)
	                res = false;
	        }

	        return res;
	    }

	    private boolean verifierRemettreJeton(String[] action, Joueur joueur) {
	        if (Arrays.stream(joueur.jetons).sum()  <= 10) {
	            return false;
	        }
	        
	        int[] jetonsRemis = new int[3];
	        int[] cpt = new int[6];
	        int nb = action.length - 1; 
	        for (int i = 0; i < nb; i++) {
	            jetonsRemis[i] = Integer.parseInt(action[i + 1]);
	        }
	        boolean res = true;
	        for (int i = 0; i < 6; i++)
	            cpt[i] = 0; 
	        for (int i = 0; i < nb; i++) {
	            if (jetonsRemis[i] < 0 || jetonsRemis[i] > 5)
	                res = false;
	            else
	                cpt[jetonsRemis[i]]++; 
	        }
	        if (res) { 
	            for (int i = 0; i < 6; i++) {
	                if (joueur.jetons[i] < cpt[i])
	                    res = false;
	            }
	        }
	        return res;
	    }

	   

	            private String combinerAvecRemise(Joueur joueur, String actionJetons) {
	                if (actionJetons == null) {
	                    return null;
	                }

	                // On simule la prise de jetons
	                Joueur joueurSimule = new Bot(this, joueur.nom);
	                joueurSimule.jetons = Arrays.copyOf(joueur.jetons, joueur.jetons.length);

	                // On ajoute les jetons de l'action
	                String[] parts = actionJetons.split(" ");
	                for (int i = 1; i < parts.length; i++) {
	                    int couleur = Integer.parseInt(parts[i]);
	                    joueurSimule.jetons[couleur]++;
	                }

	                // On vérifie si on doit remettre des jetons
	                String[] actionRemise = remettreJetons(joueurSimule);

	                // Si pas de remise nécessaire
	                if (actionRemise == null) {
	                    return actionJetons;
	                }

	                // On combine les deux actions
	                StringBuilder actionComplete = new StringBuilder(actionJetons);
	                actionComplete.append("; ");

	                for (String part : actionRemise) {
	                    actionComplete.append(part).append(" ");
	                }

	                return actionComplete.toString().trim();
	            }
	          
	            
	            private Joueur safeDeepCopyJoueur(Joueur original) {
	                if (original == null) return null;
	                // Assuming 'this' (the Strat instance) is passed to Bot constructor for 'stratRef'
	                // And that Bot constructor (Strat, String) exists.
	                Bot copy = new Bot(this, "copy_" + original.nom); // Create a Bot, as Joueur is abstract or base
	                copy.jetons = Arrays.copyOf(original.jetons, original.jetons.length);
	                copy.reduc = Arrays.copyOf(original.reduc, original.reduc.length);
	                copy.points = original.points;
	                copy.avenger = original.avenger;
	                copy.main = new ArrayList<>();
	                for(Personnage p : original.main) {
	                    if (p != null) { // Personnage objects are assumed immutable enough for game state copy
	                        copy.main.add(p);
	                    }
	                }
	                copy.lieuxConquis = new ArrayList<>();
	                for(Lieu l : original.lieuxConquis) {
	                    if (l != null) { // Lieu references are copied; if Lieu state changes, deep copy Lieu too
	                        copy.lieuxConquis.add(l);
	                    }
	                }
	                copy.rassemblement = original.rassemblement;
	                
	                return copy;
	            }
	            
	            private String winCheck() {
	                if(1 == place) {//Si le joueurEnCours clôture le tour(le joueurSuivant a déjà joué)
	                    // Check if we can win by playing any card from the board
	                    for (int winNiv = 2; winNiv >= 0; winNiv--) {
	                        for (int winPos = 0; winPos < 4; winPos++) {
	                            Personnage winCard = jeu.persos[winNiv][winPos];
	                            if (winCard != null) {
	                                String[] winAction = {"JOUER", String.valueOf(winNiv), String.valueOf(winPos)};
	                                if (verifierJouerCarte(joueurEnCours, winAction)) {
	                                    Joueur winSimulated = simulerJouerCarte(winAction, joueurEnCours);
	                                    
	                                    if (winSimulated != null) {
	                                        Jeu simJeu = deepCopyJeu();
	                                        if (simJeu != null && simJeu.am != null) {
	                                            simJeu.joueurs[place] = winSimulated;
	                                            simJeu.joueurs[1-place] = joueurSuivant;
	                                            
	                                            if (simJeu.end()) {
	                                                simJeu.quiGagne();
	                                                if (simJeu.gagnants != null && simJeu.gagnants[place] == true) {
//	                                                    System.out.println(Couleur.VERT_BOLD + "Action gagnante trouvée (winCheck) : " + String.join(" ", winAction) + Couleur.RESET);
	                                                    return combinerAvecPriseLieu(String.join(" ", winAction), joueurEnCours);
	                                                }
	                                            }
	                                        }
	                                    }
	                                }
	                            }
	                        }
	                    }

	                    // Check cards in hand for winning moves
	                    for (int i = 0; i < joueurEnCours.main.size(); i++) {
	                        String[] winHandAction = {"JOUER", String.valueOf(i)};
	                        if (verifierJouerCarte(joueurEnCours, winHandAction)) {
	                            Joueur winSimulated = simulerJouerCarte(winHandAction, joueurEnCours);
	                            
	                            if (winSimulated != null) {
	                                Jeu simJeu = deepCopyJeu();
	                                if (simJeu != null && simJeu.am != null) {
	                                    simJeu.joueurs[place] = winSimulated;
	                                    simJeu.joueurs[1-place] = joueurSuivant;
	                                    
	                                    if (simJeu.end()) {
	                                        simJeu.quiGagne();
	                                        if (simJeu.gagnants != null && simJeu.gagnants[place] == true) {
//	                                            System.out.println(Couleur.VERT_BOLD + "Action gagnante trouvée avec carte réservée (winCheck) : " + String.join(" ", winHandAction) + Couleur.RESET);
	                                            return combinerAvecPriseLieu(String.join(" ", winHandAction), joueurEnCours);
	                                        }
	                                    }
	                                }
	                            }
	                        }
	                    }
	                    
	                    //On regarde le tour suivant en essayant d'empêcher le joueurSuivant de gagner
	                    if (joueurEnCours.main.size() < 3) {
	                        for (int niv = 2; niv >= 0; niv--) {
	                            for (int pos = 0; pos < 4; pos++) {
	                                Personnage boardCard = jeu.persos[niv][pos];
	                                if (boardCard != null) {
	                                    String[] opponentPlayAction = {"JOUER", String.valueOf(niv), String.valueOf(pos)};
	                                    Joueur opponentSimulated = simulerJouerCarte(opponentPlayAction, joueurSuivant);
	                                    if (opponentSimulated != null) {
	                                        Jeu simJeu = deepCopyJeu();
	                                        if (simJeu != null && simJeu.am != null) {
	                                            simJeu.joueurs[place] = joueurEnCours;
	                                            simJeu.joueurs[1-place] = opponentSimulated;
	                                            
	                                            if (simJeu.end()) {
	                                                String[] actionJouer = {"JOUER", String.valueOf(niv), String.valueOf(pos)};
	                                                String[] actionReserver = {"RESERVER", String.valueOf(niv), String.valueOf(pos)};
	                                                if(verifierJouerCarte(joueurEnCours, actionJouer)) {
//	                                                    System.out.println(Couleur.JAUNE_BOLD + "Action défensive - jouer carte : " + String.join(" ", actionJouer) + Couleur.RESET);
	                                                    return combinerAvecPriseLieu(String.join(" ", actionJouer), joueurEnCours);
	                                                }
	                                                
	                                                if (verifierReserverCarte(actionReserver, joueurEnCours)) {
//	                                                    System.out.println(Couleur.JAUNE_BOLD + "Action défensive - réserver carte : " + String.join(" ", actionReserver) + Couleur.RESET);
	                                                    return String.join(" ", actionReserver);
	                                                }
	                                            }
	                                        }
	                                    }
	                                }
	                            }
	                        }
	                    }
	                }
	                
	                // We're starting the turn, check if opponent can win with their move
	                if (0 == place) {
	                    //On regarde la meilleure carte à joueur pour le joueurEnCours
	                    String meAction = trouverMeilleureCarteAJouer(12);
	                    Personnage meCard = meAction != null ? getCardFromAction(meAction.split(" "), joueurEnCours) : null;  
	                    Joueur meSimulated = meAction != null ? simulerJouerCarte(meAction.split(" "), joueurEnCours) : joueurEnCours;
	                    
	                    //On anticipe sur le joueurSuivant
	                    for (int oppNiv = 2; oppNiv >= 0; oppNiv--) {
	                        for (int oppPos = 0; oppPos < 4; oppPos++) {
	                            Personnage oppCard = jeu.persos[oppNiv][oppPos];
	                            if (oppCard != null && oppCard != meCard) {
	                                String[] oppAction = {"JOUER", String.valueOf(oppNiv), String.valueOf(oppPos)};
	                                Joueur opponentSimulated = simulerJouerCarte(oppAction, joueurSuivant);
	                                
	                                if (opponentSimulated != null) {
	                                    Jeu simJeu = deepCopyJeu();
	                                    if (simJeu != null && simJeu.am != null) {
	                                        simJeu.joueurs[place] = meSimulated;
	                                        simJeu.joueurs[1-place] = opponentSimulated;
	                                        
	                                        if (simJeu.end()) {
	                                            simJeu.quiGagne();
	                                            if (simJeu.gagnants != null && simJeu.gagnants[1-place] == true) {
	                                                // Try to play it ourselves first
	                                                String[] actionJouer = {"JOUER", String.valueOf(oppNiv), String.valueOf(oppPos)};
	                                                if (verifierJouerCarte(joueurEnCours, actionJouer)) {
//	                                                    System.out.println(Couleur.JAUNE_BOLD + "Action défensive - bloquer adversaire : " + String.join(" ", actionJouer) + Couleur.RESET);
	                                                    return combinerAvecPriseLieu(String.join(" ", actionJouer), joueurEnCours);
	                                                }
	                                                
	                                                // Otherwise reserve it
	                                                String[] actionReserver = {"RESERVER", String.valueOf(oppNiv), String.valueOf(oppPos)};
	                                                if (verifierReserverCarte(actionReserver, joueurEnCours)) {
//	                                                    System.out.println(Couleur.JAUNE_BOLD + "Action défensive - réserver carte : " + String.join(" ", actionReserver) + Couleur.RESET);
	                                                    return String.join(" ", actionReserver);
	                                                }
	                                            }
	                                        }
	                                    }
	                                }
	                            }
	                        }
	                    }
	                    
	                    // Check if we can win with our best move
	                    if (meAction != null) {
	                        Jeu simJeu = deepCopyJeu();
	                        if (simJeu != null && simJeu.am != null) {
	                            simJeu.joueurs[place] = meSimulated;
	                            simJeu.joueurs[1-place] = joueurSuivant;
	                            if (simJeu.end()) {
	                                return combinerAvecPriseLieu(meAction, joueurEnCours);
	                            }
	                        }
	                    }
	                }
	                
	                return null;
	            }
	            
	            private String defensiveReservation() {
	                // Don't reserve if hand is full
	                if (joueurEnCours.main.size() >= 3) {
	                    return null;
	                }
	                
	                //On regarde la meilleure carte à joueur pour le joueurEnCours
	                String meAction = trouverMeilleureCarteAJouer(12);
	                Personnage meCard = meAction != null ? getCardFromAction(meAction.split(" "), joueurEnCours) : null;  
	                Joueur meSimulated = meAction != null ? simulerJouerCarte(meAction.split(" "), joueurEnCours) : joueurEnCours;
	                
	                for (int niv = 2; niv >= 0; niv--) {
	                    for (int pos = 0; pos < 4; pos++) {
	                        Personnage boardCard = jeu.persos[niv][pos];
	                        if (boardCard != null && boardCard != meCard) {
	                            // Try to play the card as the opponent
	                            String[] opponentPlayAction = {"JOUER", String.valueOf(niv), String.valueOf(pos)};
	                            Joueur opponentSimulated = simulerJouerCarte(opponentPlayAction, joueurSuivant);
	                            
	                            if (opponentSimulated != null) {
	                                // Check if opponent can take a location with this card
	                                String[] opponentLocationAction = prendreLieu(opponentSimulated);
	                                
	                                Jeu simJeu = deepCopyJeu();
	                                if (simJeu != null && simJeu.am != null) {
	                                    simJeu.joueurs[place] = meSimulated;
	                                    simJeu.joueurs[1-place] = opponentSimulated;
	                                    
	                                    boolean opponentWinsOrTakesCriticalLocation = false;
	                                    
	                                    // Check if opponent can take a critical location
	                                    if (opponentLocationAction != null) {
	                                        int idLieuPrisAdv = Integer.parseInt(opponentLocationAction[1]);
	                                        Lieu lieuAdv = (idLieuPrisAdv == idLieu1) ? lieu1 : lieu2;
	                                        
	                                        boolean newLoc = false;
	                                        if (lieuAdv != null && !meSimulated.lieuxConquis.contains(lieuAdv)) {
	                                            if (idLieuPrisAdv == idLieu1 || idLieuPrisAdv == idLieu2) {
	                                                newLoc = true;
	                                            }
	                                            
	                                            // Block if opponent would have high points with this location
	                                            if (newLoc && opponentSimulated.points >= 12 && meSimulated.points < opponentSimulated.points || 
	                                                opponentSimulated.rassemblement && !joueurSuivant.rassemblement && opponentSimulated.points >= 12 && meSimulated.points < opponentSimulated.points) {
	                                                opponentWinsOrTakesCriticalLocation = true;
	                                            }
	                                        }
	                                    }
	                                    
	                                    if (opponentWinsOrTakesCriticalLocation) {
	                                        // Try to play it ourselves
	                                        String[] actionJouer = {"JOUER", String.valueOf(niv), String.valueOf(pos)};
	                                        if (verifierJouerCarte(joueurEnCours, actionJouer)) {
	                                            return combinerAvecPriseLieu(String.join(" ", actionJouer), joueurEnCours);
	                                        }
	                                        
	                                        return "RESERVER " + niv + " " + pos;
	                                    }
	                                }
	                            }
	                        }
	                    }
	                }
	                
	                return null;
	            }
	            
	            private Personnage getCardFromAction(String[] action, Joueur joueur) {
	                if (action == null || action.length < 2) {
	                    return null;
	                }
	                
	                try {
	                    if (action.length == 2) {
	                        // Card from hand: ["JOUER", handIndex]
	                        int handIndex = Integer.parseInt(action[1]);
	                        if (handIndex >= 0 && handIndex < joueur.main.size()) {
	                            return joueur.main.get(handIndex);
	                        }
	                    } else if (action.length >= 3) {
	                        // Card from board: ["JOUER"|"RESERVER", level, position]
	                        int level = Integer.parseInt(action[1]);
	                        int position = Integer.parseInt(action[2]);
	                        
	                        if (level >= 0 && level < 3 && position >= 0 && position < 4) {
	                            return jeu.persos[level][position];
	                        }
	                    }
	                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
	                    // Handle parsing errors or out of bounds accesses
	                    return null;
	                }
	                
	                return null; // Action format not recognized or invalid indices
	            }
	            
	            
	            private String getFallbackAction(Joueur joueur) {
	                
//	            	System.out.println(" Playing fallback action");

	                // Try to take any 3 different tokens if possible
	                for (int i = 0; i < 5; i++) {
	                    for (int j = i + 1; j < 5; j++) {
	                        for (int k = j + 1; k < 5; k++) {
	                            if (jeu.jetons[i] > 0 && jeu.jetons[j] > 0 && jeu.jetons[k] > 0) {
	                                String[] fallbackTokens = {"JETONS", String.valueOf(i), String.valueOf(j), String.valueOf(k)};
	                                if (verifierPrendreJeton(fallbackTokens, joueur)) {
	                                     return combinerAvecRemise(joueur,String.join(" ", fallbackTokens));
	                                }
	                            }
	                        }
	                    }
	                }
	                
	                //Try to take 2 differents tokens if possible
	                for (int i = 0; i < 5; i++) {
						for (int j = i + 1; j < 5; j++) {
							if (jeu.jetons[i] > 0 && jeu.jetons[j] > 0) {
								String[] fallbackTokens = {"JETONS", String.valueOf(i), String.valueOf(j)};
								if (verifierPrendreJeton(fallbackTokens, joueur)) {
									 return combinerAvecRemise(joueur,String.join(" ", fallbackTokens));
								}
							}
						}
					}
	                
	                // If not, take any 2 same
	                 for (int i = 0; i < 5; i++) {
	                    if (jeu.jetons[i] >= 4) {
	                         String[] fallbackTokens = {"JETONS", String.valueOf(i), String.valueOf(i)};
	                         if (verifierPrendreJeton(fallbackTokens, joueur)) {
	                            return combinerAvecRemise(joueur,String.join(" ", fallbackTokens));
	                         }
	                    }
	                }
	                 
	                 
	                // If not, take any 1 available token (non-joker first)
	                for (int i = 0; i < 5; i++) {
	                     if (jeu.jetons[i] > 0) {
	                         String[] fallbackTokens = {"JETONS", String.valueOf(i)};
	                          if (verifierPrendreJeton(fallbackTokens, joueur)) {
	                            return combinerAvecRemise(joueur,String.join(" ", fallbackTokens));
	                         }
	                     }
	                }
	                

	                // Try to reserve from pioche 0 if hand is not full
	                if (joueur.main.size() < 3 && nbreCartesRestantesDansPioche != null && nbreCartesRestantesDansPioche[0] > 0) {
	                    return "RESERVER 0 4";
	                }
	                // Absolute last resort
	                return "JETONS 0"; // This might be an invalid move.
	            }
	            
	            
	           


	            private void updateGamePhase() {
	                
	                
	                if (tour >= 23) {//25
	                    phase = 2; // end game
	                } else if (tour >=12 ) {//15
	                    phase = 1; // mid game
	                } else {
	                    phase = 0; // early game
	                }
	            }
	            

	            private String playStartGame() {
	            
	                // Try to play a level 1 card that helps with level 3 or locations
	                String actionJouerCarte = trouverMeilleureCarteAJouer(4);
	                if (actionJouerCarte != null) {
//	                    System.out.println(Couleur.CYAN_BOLD + "Jouer meilleure carte : " + actionJouerCarte + Couleur.RESET);
	                    return combinerAvecPriseLieu(actionJouerCarte, joueurEnCours);
	                }
	                // Try to take optimal tokens for early game
		             String actionJetons = optimiserPriseJetons(0);
		             if (actionJetons != null) {
//		                 System.out.println(Couleur.CYAN_BOLD + "Optimisation prise de jetons : " + actionJetons + Couleur.RESET);
		                 return combinerAvecRemise(joueurEnCours, actionJetons);
		             }   
	              
	                return getFallbackAction(joueurEnCours);
	            }

	            
	            private String playMidGame() {
	            	
	                // Essayer d'abord de jouer une carte qui permet de prendre un lieu
	                String actionCartePourLieu = jouerCartePourLieu(joueurEnCours);
	                if (actionCartePourLieu != null) {
//	                    System.out.println(Couleur.CYAN_BOLD + "Jouer carte pour prendre lieu : " + actionCartePourLieu + Couleur.RESET);
	                    return combinerAvecPriseLieu(actionCartePourLieu, joueurEnCours);
	                }
	                
	                // Essayer de jouer une carte pour obtenir le rassemblement
	                String actionCartePourRassemblement = jouerCartePourRassemblement(joueurEnCours);
	                if (actionCartePourRassemblement != null) {
//	                    System.out.println(Couleur.CYAN_BOLD + "Jouer carte pour rassemblement : " + actionCartePourRassemblement + Couleur.RESET);
	                    return combinerAvecPriseLieu(actionCartePourRassemblement, joueurEnCours);
	                }
	               
	                // Try to play a card (prioritizing level 1 then level 2)
	                String actionJouerCarte = trouverMeilleureCarteAJouer(8);
	                if (actionJouerCarte != null) {
//	                    System.out.println(Couleur.CYAN_BOLD + "Jouer meilleure carte : " + actionJouerCarte + Couleur.RESET);
	                    return combinerAvecPriseLieu(actionJouerCarte, joueurEnCours);
	                }
	              
		                // Try to take optimal tokens for early game
		                String actionJetons = optimiserPriseJetons(1);
		                if (actionJetons != null) {
//		                    System.out.println(Couleur.CYAN_BOLD + "Optimisation prise de jetons : " + actionJetons + Couleur.RESET);
		                    return combinerAvecRemise(joueurEnCours, actionJetons);
		                }
	                

	                return getFallbackAction(joueurEnCours);
	            }

	            private String playEndGame() {

	                // If no level 3 target, try to get one
	                if (!hasLevel3Target ) {
	                    bestLevel3 = findBestLevel3Card(5);
	                    if(bestLevel3 != null && bestLevel3.peutEtreJouePar(joueurEnCours)) {
//							System.out.println(Couleur.BLEU_BOLD + "Jouer carte niveau 3 ciblée : JOUER 2 " + String.valueOf(findCardPosition(bestLevel3)) + Couleur.RESET);
//							hasLevel3Target = true;
							return combinerAvecPriseLieu("JOUER "+ "2 "+ String.valueOf(findCardPosition(bestLevel3)), joueurEnCours);
	                	}
	                    if (bestLevel3 != null && joueurEnCours.main.size() < 3) {
	                        String[] actionReserver = {"RESERVER", "2", String.valueOf(findCardPosition(bestLevel3))};
	                        if (verifierReserverCarte(actionReserver, joueurEnCours)) {
//	                            System.out.println(Couleur.BLEU_BOLD + "Réserver carte niveau 3 ciblée : " + String.join(" ", actionReserver) + Couleur.RESET);
	                            hasLevel3Target = true;
	                            return String.join(" ", actionReserver);
	                        }
	                    }
	                }
	                
	                
	            	// Essayer d'abord de jouer une carte qui permet de prendre un lieu
	                String actionCartePourLieu = jouerCartePourLieu(joueurEnCours);
	                if (actionCartePourLieu != null) {
//	                    System.out.println(Couleur.CYAN_BOLD + "Jouer carte pour prendre lieu : " + actionCartePourLieu + Couleur.RESET);
	                    return combinerAvecPriseLieu(actionCartePourLieu, joueurEnCours);
	                }
	                
	                // Essayer de jouer une carte pour obtenir le rassemblement
	                String actionCartePourRassemblement = jouerCartePourRassemblement(joueurEnCours);
	                if (actionCartePourRassemblement != null) {
//	                    System.out.println(Couleur.CYAN_BOLD + "Jouer carte pour rassemblement : " + actionCartePourRassemblement + Couleur.RESET);
	                    return combinerAvecPriseLieu(actionCartePourRassemblement, joueurEnCours);
	                }
	                
	                // Try to play a level 3 or level 2 card
	                String actionJouerCarte = trouverMeilleureCarteAJouer(12);
	                if (actionJouerCarte != null) {
//	                    System.out.println(Couleur.CYAN_BOLD + "Jouer meilleure carte : " + actionJouerCarte + Couleur.RESET);
	                    return combinerAvecPriseLieu(actionJouerCarte, joueurEnCours);
	                }
	                
	                //On voit si on peut prendre des jetons pour recruter immédiatement les cartes reservées
	                if(joueurEnCours.main.size() >= 1) {
		                for(Personnage p : joueurEnCours.main) {
		                	Map<Integer, Integer> ressourcesManquantes = calculerRessourcesManquantes(p, joueurEnCours);
		                	if(ressourcesManquantes.values().stream()
		                               .mapToInt(Integer::intValue)
		                               .sum()<=5) {
			                    String action = genererActionJetonsOptimale(ressourcesManquantes);
			                    if (action != null) {
			                        // Utiliser l'action
//			                    	System.out.println(Couleur.BLEU_BOLD + "Prise de jetons optimale : " + action + Couleur.RESET);
			                    	return action;
				                    	
			                    }
		                	}
		                }
	                }
	                
		                // Try to take optimal tokens for early game
		                String actionJetons = optimiserPriseJetons(2);
		                if (actionJetons != null) {
//		                    System.out.println(Couleur.CYAN_BOLD + "Optimisation prise de jetons : " + actionJetons + Couleur.RESET);
		                    return combinerAvecRemise(joueurEnCours, actionJetons);
		                }
	                
	               

	                // If we can't play a high-level card, fall back to mid game strategy
	                return playMidGame();
	            }

	            private String optimiserPriseJetons(int gamePhase) {
	            	
	                    Map<Personnage, Double> cartesPrioritaires = cartesAPrioriserPourCartesReservees(joueurEnCours, gamePhase)
	                        .entrySet()
	                        .stream()
	                        .filter(entry -> entry.getValue() > 0)
//	                        .limit(2)
	                        .collect(Collectors.toMap(
	                            Map.Entry::getKey,
	                            Map.Entry::getValue,
	                            (e1, e2) -> e1,
	                            LinkedHashMap::new
	                        ));

	                    double[] jetonsNecessaires = new double[5];
	                    for (Map.Entry<Personnage, Double> entry : cartesPrioritaires.entrySet()) {
	                        for (int i = 0; i < 5; i++) {
	                            if (entry.getKey().cout[i] > joueurEnCours.reduc[i]) {
	                                jetonsNecessaires[i] += entry.getKey().cout[i] - joueurEnCours.reduc[i];
	                            }
	                        }
	                    }

	                    List<String[]> combinations = generateTokenCombinations();
	                    double bestScore = Integer.MIN_VALUE;
	                    String[] bestCombination = null;

	                    for (String[] combination : combinations) {
	                        if (verifierPrendreJeton(combination, joueurEnCours) && combination.length >= 4) {
	                            double score = evaluerCombinaisonJetons(combination, jetonsNecessaires, cartesPrioritaires);
	                            if (score > bestScore) {
	                                bestScore = score;
	                                bestCombination = combination;
	                            }
	                        }
	                    }

	                    return bestCombination != null ? String.join(" ", bestCombination) : tryAllTokenCombinations();
	               
	            }

	            private Map<Personnage, Double> cartesAPrioriserPourCartesReservees(Joueur joueur, int gamePhase) {
	                Map<Personnage, Double> classement = new HashMap<>();
	                double[] sumCoutAcquisition = new double[5];
	                
	               
	                
	                // Calculer le coût total des couleurs nécessaires pour les cartes réservées
	                for (Personnage reservedCard : joueur.main) {
	                    for (int i = 0; i < 5; i++) {
	                        sumCoutAcquisition[i] += Math.max(0, reservedCard.cout[i] - joueur.reduc[i] - joueur.jetons[i]);
	                    }
	                }

	                // Parcourir les cartes selon la phase de jeu
	                int startLevel = 0;
	                int endLevel = 2;
	                
	                switch(gamePhase) {
	                    case 0: // Early game - focus on level 1
	                        endLevel = 0;
	                        break;
	                    case 1: // Mid game - level 1 and 2
	                        endLevel = 1;
	                        break;
	                    case 2: // End game - all levels, prioritize high levels
	                        startLevel = 2;
	                        break;
	                }

	                for (int niveau = startLevel; niveau <= endLevel; niveau++) {
	                    for (Personnage carte : jeu.persos[niveau]) {
	                        if (carte != null) {
	                            double priorityValue = 0.0;
	                            
	                            // Bonus pour les cartes utiles pour le niveau 3
	                            if ( bestLevel3 != null) {
	                                for(int i = 0; i < 5; i++) {
	                                    if(bestLevel3.cout[i] - joueur.reduc[i] - joueur.jetons[i] > 0 && i == carte.couleur) {
	                                        priorityValue += WEIGHT_LEVEL3_SYNERGY[gamePhase];
	                                    }
	                                }
	                            }
	                            
	                            

	                            // Bonus pour les cartes utiles pour les cartes réservées
	                            for(int i=0; i<joueur.main.size(); i++) {
	                                for(int j=0; j<5; j++) {
	                                    if(joueur.main.get(i).cout[j] - joueur.reduc[j] > 0 && j == carte.couleur) {
	                                        priorityValue += WEIGHT_RESERVED[gamePhase];
	                                    }
	                                }
	                            }

	                            // Valeur de base avec poids adaptés
	                            priorityValue += carte.points * WEIGHT_CARD_POINTS[gamePhase] + 
	                                           carte.avenger * WEIGHT_CARD_AVENGERS[gamePhase] + 
	                                           carte.niveau * WEIGHT_CARD_LEVEL[gamePhase];
	                            priorityValue += sumCoutAcquisition[carte.couleur] * WEIGHT_ACQUIS[gamePhase];
	                            
	                            // Pénalité pour le coût avec poids adaptés
	                            for (int i = 0; i < 5; i++) {
	                                priorityValue -= Math.max(0,(carte.cout[i] - joueur.reduc[i]) * WEIGHT_COST[gamePhase]);
	                            }
	                            
	                            // Bonus pour nouvelle couleur avec poids adaptés
	                            if (joueur.reduc[carte.couleur] == 0) {
	                                priorityValue += WEIGHT_NEW_COLOR[gamePhase];
	                            }
	                            
	                            // Bonus pour les lieux avec poids adaptés
	                            if (isColorNeededForLocation(carte.couleur)) {
	                                priorityValue += WEIGHT_LOCATION[niveau];
	                            }

	                            if(priorityValue > 0) {
	                                classement.put(carte, priorityValue);
	                            }
	                        }
	                    }
	                }

	                return classement.entrySet().stream()
	                        .sorted(Map.Entry.<Personnage, Double>comparingByValue().reversed())
	                        .collect(Collectors.toMap(
	                            Map.Entry::getKey,
	                            Map.Entry::getValue,
	                            (e1, e2) -> e1,
	                            LinkedHashMap::new));
	            }

	    private Jeu deepCopyJeu() {
	        if (this.jeu == null) return null;
	        
	        try {
	            // Créer une nouvelle instance de Jeu avec des copies des joueurs
	            Joueur[] copieJoueurs = new Joueur[2];
	            copieJoueurs[place] = safeDeepCopyJoueur(joueurEnCours);
	            copieJoueurs[1-place] = safeDeepCopyJoueur(joueurSuivant);
	            
	            Jeu copyJeu = new Jeu(copieJoueurs, this.jeu.positionAleatoire);
	            
	            // Copier les attributs simples
	            
	            copyJeu.posseseurAvenger = jeu.posseseurAvenger;
	            
	            // Copier les tableaux
	            if (jeu.ordre != null) {
	                copyJeu.ordre = Arrays.copyOf(jeu.ordre, jeu.ordre.length);
	            }
	            
	            if (jeu.gagnants != null) {
	                copyJeu.gagnants = Arrays.copyOf(jeu.gagnants, jeu.gagnants.length);
	            }
	            
	            if (jeu.jetons != null) {
	                copyJeu.jetons = Arrays.copyOf(jeu.jetons, jeu.jetons.length);
	            }
	            
	            // Copie profonde du tableau persos
	            if (jeu.persos != null) {
	                copyJeu.persos = new Personnage[jeu.persos.length][];
	                for (int i = 0; i < jeu.persos.length; i++) {
	                    if (jeu.persos[i] != null) {
	                        copyJeu.persos[i] = new Personnage[jeu.persos[i].length];
	                        for (int j = 0; j < jeu.persos[i].length; j++) {
	                            if (jeu.persos[i][j] != null) {
	                                // Copie profonde de chaque Personnage
	                                Personnage original = jeu.persos[i][j];
	                                copyJeu.persos[i][j] = new Personnage(
	                                    original.niveau,
	                                    original.nom,
	                                    Arrays.copyOf(original.cout, original.cout.length),
	                                    original.couleur,
	                                    original.avenger,
	                                    original.points
	                                );
	                            }
	                        }
	                    }
	                }
	            }
	            
	            // Copier l'ActionManager
	            if (jeu.am != null) {
	                copyJeu.am = new ActionManager();
	            }
	            
	            return copyJeu;
	        } catch (Exception e) {
//	            System.out.println("Exception dans deepCopyJeu: " + e.getMessage());
	            return null;
	        }
	    }

	    private String jouerCartePourLieu(Joueur joueur) {
	        // Vérifier d'abord si un lieu est disponible et peut être pris
	        if (lieu1 == null && lieu2 == null) return null;
	        
	        // Parcourir toutes les cartes disponibles
	        for (int niveau = 0; niveau < 3; niveau++) {
	            for (int position = 0; position < 4; position++) {
	                Personnage carte = jeu.persos[niveau][position];
	                if (carte != null && carte.peutEtreJouePar(joueur)) {
	                    // Simuler l'acquisition de la carte
	                    Joueur joueurSimule = safeDeepCopyJoueur(joueur);
	                    joueurSimule.reduc[carte.couleur]++;
	                    joueurSimule.points += carte.points;
	                    
	                    // Vérifier si cette carte permet de prendre un lieu
	                    String[] actionLieu = prendreLieu(joueurSimule);
	                    if (actionLieu != null) {
	                        // Si oui, retourner l'action pour jouer cette carte
	                        return "JOUER " + niveau + " " + position;
	                    }
	                }
	            }
	        }
	        
	        // Vérifier aussi les cartes en main
	        for (int i = 0; i < joueur.main.size(); i++) {
	            Personnage carte = joueur.main.get(i);
	            if (carte.peutEtreJouePar(joueur)) {
	                // Simuler l'acquisition de la carte
	                Joueur joueurSimule = safeDeepCopyJoueur(joueur);
	                joueurSimule.reduc[carte.couleur]++;
	                joueurSimule.points += carte.points;
	                
	                // Vérifier si cette carte permet de prendre un lieu
	                String[] actionLieu = prendreLieu(joueurSimule);
	                if (actionLieu != null) {
	                    // Si oui, retourner l'action pour jouer cette carte
	                    return "JOUER " + i;
	                }
	            }
	        }
	        
	        return null;
	    }
	    
	    
	    private String[] remettreJetons1(Joueur joueur, List<Integer> jetonsPrioritaires) {
	        
	        int[] jExcess = Arrays.copyOf(joueur.jetons, 7);
	        int total = Arrays.stream(jExcess).sum();

	        if (total <= 10) {
	            return null;
	        }

	        // Calculer les scores pour chaque couleur de jeton
	        double[] tokenScores = new double[6];
	        Map<Personnage, Double> cartesPrioritaires = cartesAPrioriserPourCartesReservees(joueur, phase);
	        Map<Personnage, Double> cartesPrioritairesAdverses = cartesAPrioriserPourCartesReservees(joueurSuivant, phase);

	        for (int i = 0; i < 5; i++) {
	            // Ne pas considérer les jetons prioritaires pour la remise
	            if (jetonsPrioritaires.contains(i)) {
	                continue;
	            }

	            if (jeu.jetons[i] == 2) {
	                tokenScores[i] += 4;
	            }
	            if (jeu.jetons[i] == 1) {
	                tokenScores[i] += 5;
	            }
	            if(bestLevel3 != null && bestLevel3.cout[i] - joueur.reduc[i] - joueur.jetons[i] > 0){
	                tokenScores[i] += 150;
	            }
	            
	            // Score basé sur les cartes prioritaires
	            for (Map.Entry<Personnage, Double> entry : cartesPrioritaires.entrySet()) {
	                if (entry.getKey().cout[i] - joueur.reduc[i] - joueur.jetons[i] > 0) {
	                    tokenScores[i] += entry.getValue();
	                }
	            }
	            
	            // Score basé sur les besoins de l'adversaire
	            for (Map.Entry<Personnage, Double> entry : cartesPrioritairesAdverses.entrySet()) {
	                if (entry.getKey().cout[i] - joueurSuivant.reduc[i] - joueurSuivant.jetons[i] > 0) {
	                    tokenScores[i] += entry.getValue();
	                }
	            }
	        }

	        List<Integer> tokensToRemove = new ArrayList<>();
	        while (total > 10) {
	            // Trouver le jeton avec le score le plus bas (qui n'est pas prioritaire)
	            int minScoreIndex = -1;
	            double minScore = Double.MAX_VALUE;
	            
	            for (int i = 0; i < 5; i++) {
	                if (jExcess[i] > 0 && !jetonsPrioritaires.contains(i) && tokenScores[i] < minScore) {
	                    minScore = tokenScores[i];
	                    minScoreIndex = i;
	                }
	            }
	            
	            // Si on ne trouve pas de jeton non prioritaire à remettre, on doit prendre dans les prioritaires
	            if (minScoreIndex == -1) {
	                for (int i = 0; i < 5; i++) {
	                    if (jExcess[i] > 0 && tokenScores[i] < minScore) {
	                        minScore = tokenScores[i];
	                        minScoreIndex = i;
	                    }
	                }
	            }
	            
	            if (minScoreIndex != -1) {
	                jExcess[minScoreIndex]--;
	                tokensToRemove.add(minScoreIndex);
	                total--;
	            } else {
	                // Si on ne peut plus remettre de jetons, on sort de la boucle
	                break;
	            }
	        }

	        if(tokensToRemove.size() >= 1) {
	            String[] result = new String[tokensToRemove.size() + 1];
	            result[0] = "REMETTRE";
	            for (int i = 0; i < tokensToRemove.size(); i++) {
	                result[i + 1] = String.valueOf(tokensToRemove.get(i));
	            }
	            return verifierRemettreJeton(result,joueur)? result:null;
	        }
	        return null;
	    }

	    private String combinerAvecRemise1(Joueur joueur, String actionJetons, List<Integer> jetonsPrioritaires) {
	        if (actionJetons == null) {
	            return null;
	        }

	        // On simule la prise de jetons
	        Joueur joueurSimule = new Bot(this, joueur.nom);
	        joueurSimule.jetons = Arrays.copyOf(joueur.jetons, joueur.jetons.length);

	        // On ajoute les jetons de l'action
	        String[] parts = actionJetons.split(" ");
	        for (int i = 1; i < parts.length; i++) {
	            int couleur = Integer.parseInt(parts[i]);
	            joueurSimule.jetons[couleur]++;
	        }

	        // On vérifie si on doit remettre des jetons en préservant les jetons prioritaires
	        String[] actionRemise = remettreJetons1(joueurSimule, jetonsPrioritaires);

	        // Si pas de remise nécessaire
	        if (actionRemise == null) {
	            return actionJetons;
	        }

	        // On combine les deux actions
	        StringBuilder actionComplete = new StringBuilder(actionJetons);
	        actionComplete.append("; ");

	        for (String part : actionRemise) {
	            actionComplete.append(part).append(" ");
	        }

	        return actionComplete.toString().trim();
	    }

	    /**
	     * Calcule le nombre de ressources manquantes pour chaque couleur pour une carte donnée
	     * @param carte La carte pour laquelle on veut calculer les ressources manquantes
	     * @param joueur Le joueur dont on veut évaluer les ressources
	     * @return Une Map où la clé est la couleur (0-4) et la valeur est le nombre de ressources manquantes
	     *         Si la valeur est 0 ou négative, le joueur a suffisamment de ressources pour cette couleur
	     */
	    private Map<Integer, Integer> calculerRessourcesManquantes(Personnage carte, Joueur joueur) {
	        Map<Integer, Integer> ressourcesManquantes = new HashMap<>();
	        if(carte == null || joueur == null) {
		        return null;
	        }
	        // Pour chaque couleur
	        for (int couleur = 0; couleur < 5; couleur++) {
	            // Calculer les ressources nécessaires
	            int coutReel = Math.max(0, carte.cout[couleur] - joueur.reduc[couleur]);
	            int manquant = Math.max(0, coutReel - joueur.jetons[couleur]);
	            
	            // N'ajouter à la map que si des ressources sont manquantes
	            if (manquant > 0) {
	                ressourcesManquantes.put(couleur, manquant);
	            }
	        }
	        
	        return ressourcesManquantes;
	    }

	    /**
	     * Génère une action JETONS optimale basée sur les ressources manquantes
	     * @param ressourcesManquantes Map des ressources manquantes par couleur
	     * @return Une action JETONS combinée avec REMETTRE, ou null si aucune action valide n'est possible
	     */
	    private String genererActionJetonsOptimale(Map<Integer, Integer> ressourcesManquantes) {
	        if (ressourcesManquantes == null || ressourcesManquantes.isEmpty()) {
	            return null;
	        }

	        // Trier les ressources manquantes par quantité décroissante
	        List<Map.Entry<Integer, Integer>> ressourcesTriees = ressourcesManquantes.entrySet()
	                .stream()
	                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
	                .collect(Collectors.toList());

	        List<Integer> jetonsAPrelevers = new ArrayList<>();
	        List<Integer> jetonsPrioritaires = new ArrayList<>(ressourcesManquantes.keySet());

	        // Cas spécial : une seule couleur manquante avec besoin de 2 ou plus
	        if (ressourcesTriees.size() == 1 && ressourcesTriees.get(0).getValue() >= 2) {
	            int couleur = ressourcesTriees.get(0).getKey();
	            if (jeu.jetons[couleur] >= 4) {
	                // Prendre 2 jetons de la même couleur
	                String[] action = {"JETONS", String.valueOf(couleur), String.valueOf(couleur)};
	                if (verifierPrendreJeton(action, joueurEnCours)) {
	                    return combinerAvecRemise1(joueurEnCours, String.join(" ", action), jetonsPrioritaires);
	                }
	            }
	        }

	        // Cas général : essayer de prendre 3 jetons différents
	        for (Map.Entry<Integer, Integer> entry : ressourcesTriees) {
	            int couleur = entry.getKey();
	            if (jeu.jetons[couleur] > 0 && jetonsAPrelevers.size() < 3) {
	                jetonsAPrelevers.add(couleur);
	            }
	        }

	        // Si on n'a pas assez de jetons prioritaires, compléter avec optimiserPriseJetons
	        if (jetonsAPrelevers.size() < 3) {
	            String actionComplementaire = optimiserPriseJetons(phase);
	            if (actionComplementaire != null) {
	                String[] partsComplementaires = actionComplementaire.split(" ");
	                // Ajouter les jetons complémentaires en évitant les doublons
	                for (int i = 1; i < partsComplementaires.length && jetonsAPrelevers.size() < 3; i++) {
	                    int couleurComplementaire = Integer.parseInt(partsComplementaires[i]);
	                    if (!jetonsAPrelevers.contains(couleurComplementaire)) {
	                        jetonsAPrelevers.add(couleurComplementaire);
	                    }
	                }
	            }
	        }

	        // Construire l'action finale
	        if (!jetonsAPrelevers.isEmpty()) {
	            List<String> actionParts = new ArrayList<>();
	            actionParts.add("JETONS");
	            for (Integer couleur : jetonsAPrelevers) {
	                actionParts.add(String.valueOf(couleur));
	            }

	            String[] action = actionParts.toArray(new String[0]);
	            if (verifierPrendreJeton(action, joueurEnCours)) {
	                return combinerAvecRemise1(joueurEnCours, String.join(" ", action), jetonsPrioritaires);
	            }
	        }

	        return null;
	    }
	    
	    private String jouerCartePourRassemblement(Joueur joueur) {
	        // Vérifier d'abord si le joueur a déjà le rassemblement
	        if (joueur.rassemblement || joueur.avenger <= 2) {
	            return null;
	        }
	        
	        // Parcourir toutes les cartes disponibles
	        for (int niveau = 0; niveau < 3; niveau++) {
	            for (int position = 0; position < 4; position++) {
	                Personnage carte = jeu.persos[niveau][position];
	                if (carte != null && carte.peutEtreJouePar(joueur)) {
	                    // Simuler l'acquisition de la carte
	                    Joueur joueurSimule = safeDeepCopyJoueur(joueur);
	                    joueurSimule.avenger += carte.avenger;
	                    
	                    // Vérifier si cette carte permet d'obtenir le rassemblement
	                    if (joueurSimule.avenger >= 3 && 
	                        (joueurSimule.avenger > joueurSuivant.avenger || 
	                        (joueurSimule.avenger == joueurSuivant.avenger && !joueurSuivant.rassemblement))) {
	                        return "JOUER " + niveau + " " + position;
	                    }
	                }
	            }
	        }
	        
	        // Vérifier aussi les cartes en main
	        for (int i = 0; i < joueur.main.size(); i++) {
	            Personnage carte = joueur.main.get(i);
	            if (carte.peutEtreJouePar(joueur)) {
	                // Simuler l'acquisition de la carte
	                Joueur joueurSimule = safeDeepCopyJoueur(joueur);
	                joueurSimule.avenger += carte.avenger;
	                
	                // Vérifier si cette carte permet d'obtenir le rassemblement
	                if (joueurSimule.avenger >= 3 && 
	                    (joueurSimule.avenger > joueurSuivant.avenger || 
	                    (joueurSimule.avenger == joueurSuivant.avenger && !joueurSuivant.rassemblement))) {
	                    return "JOUER " + i;
	                }
	            }
	        }
	        
	        return null;
	    }
	}

