package splendormarvel.ia;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import splendormarvel.Bot;
import splendormarvel.Jeu;
import splendormarvel.Joueur;
import splendormarvel.Lieu;
import splendormarvel.Personnage;
import splendormarvel.utils.Couleur;
import splendormarvel.utils.RandomSingleton;
import java.util.*;

public class WeightCombinationOptimizer {
    private static final int NUM_GAMES = 5000;
    
    
    // Les différentes options de poids à tester
    private static final double[][] WEIGHT_CARD_POINTS_OPTIONS = {
        {2.45, 2.45, 2.45},
        {1.35, 2.81, 1.86}
    };
    
    private static final double[][] WEIGHT_CARD_LEVEL_OPTIONS = {
        {0.5, 0.5, 0.5},
        {0.09, 0.27, 1.12}
    };
    
    private static final double[][] WEIGHT_CARD_AVENGERS_OPTIONS = {
        {1.0, 1.0, 1.0}
    };
    
    private static final double[][] WEIGHT_NEW_COLOR_OPTIONS = {
        {5.0, 5.0, 5.0},
        {4.48, 3.68, 0.52}
    };
    
    private static final double[][] WEIGHT_COST_OPTIONS = {
        {0.75, 0.75, 0.75}
    };
    
    private static final double[][] WEIGHT_LEVEL3_SYNERGY_OPTIONS = {
        {2.5, 2.5, 2.5},
        {3.34, 1.53, 4.99}
    };
    
    private static final double[][] WEIGHT_LEVEL2_SYNERGY_OPTIONS = {
        {2.0, 2.0, 2.0},
        {2.16, 4.40, 4.91}
    };
    
    private static final double[][] WEIGHT_LOCATION_OPTIONS = {
        {3.0, 3.0, 3.0},
        {3.29, 3.80, 3.42}
    };
    
    private static final double[][] WEIGHT_RESERVED_OPTIONS = {
        {5.0, 5.0, 5.0},
        {4.88, 2.40, 0.70},
        {4.36, 2.96, 3.51}
    };
    
    private static final double[][] WEIGHT_ACQUIS_OPTIONS = {
        {1.5, 1.5, 1.5},
        {4.08, 4.97, 3.20}
    };
    
    // Classe pour stocker une combinaison de poids
    public static class WeightCombination {
        double[] WEIGHT_CARD_POINTS;
        double[] WEIGHT_CARD_LEVEL;
        double[] WEIGHT_CARD_AVENGERS;
        double[] WEIGHT_NEW_COLOR;
        double[] WEIGHT_COST;
        double[] WEIGHT_LEVEL3_SYNERGY;
        double[] WEIGHT_LEVEL2_SYNERGY;
        double[] WEIGHT_LOCATION;
        double[] WEIGHT_RESERVED;
        double[] WEIGHT_ACQUIS;
        int victories;
        int draws; // Nouveau compteur pour les parties nulles
        
        public WeightCombination() {
            victories = 0;
            draws = 0; // Initialisation du compteur de parties nulles
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Score: ").append(String.format("%.2f%%", (victories * 100.0 + draws * 50.0) / NUM_GAMES));
            sb.append(" (").append(victories).append(" victoires, ").append(draws).append(" nuls)").append("\n");
            sb.append("WEIGHT_CARD_POINTS: ").append(Arrays.toString(WEIGHT_CARD_POINTS)).append("\n");
            sb.append("WEIGHT_CARD_LEVEL: ").append(Arrays.toString(WEIGHT_CARD_LEVEL)).append("\n");
            sb.append("WEIGHT_CARD_AVENGERS: ").append(Arrays.toString(WEIGHT_CARD_AVENGERS)).append("\n");
            sb.append("WEIGHT_NEW_COLOR: ").append(Arrays.toString(WEIGHT_NEW_COLOR)).append("\n");
            sb.append("WEIGHT_COST: ").append(Arrays.toString(WEIGHT_COST)).append("\n");
            sb.append("WEIGHT_LEVEL3_SYNERGY: ").append(Arrays.toString(WEIGHT_LEVEL3_SYNERGY)).append("\n");
            sb.append("WEIGHT_LEVEL2_SYNERGY: ").append(Arrays.toString(WEIGHT_LEVEL2_SYNERGY)).append("\n");
            sb.append("WEIGHT_LOCATION: ").append(Arrays.toString(WEIGHT_LOCATION)).append("\n");
            sb.append("WEIGHT_RESERVED: ").append(Arrays.toString(WEIGHT_RESERVED)).append("\n");
            sb.append("WEIGHT_ACQUIS: ").append(Arrays.toString(WEIGHT_ACQUIS)).append("\n");
            return sb.toString();
        }
    }
    
    private List<WeightCombination> generateAllCombinations() {
        List<WeightCombination> combinations = new ArrayList<>();
        
        // Générer toutes les combinaisons possibles
        for (double[] cardPoints : WEIGHT_CARD_POINTS_OPTIONS) {
            for (double[] cardLevel : WEIGHT_CARD_LEVEL_OPTIONS) {
                for (double[] cardAvengers : WEIGHT_CARD_AVENGERS_OPTIONS) {
                    for (double[] newColor : WEIGHT_NEW_COLOR_OPTIONS) {
                        for (double[] cost : WEIGHT_COST_OPTIONS) {
                            for (double[] level3Synergy : WEIGHT_LEVEL3_SYNERGY_OPTIONS) {
                                for (double[] level2Synergy : WEIGHT_LEVEL2_SYNERGY_OPTIONS) {
                                    for (double[] location : WEIGHT_LOCATION_OPTIONS) {
                                        for (double[] reserved : WEIGHT_RESERVED_OPTIONS) {
                                            for (double[] acquis : WEIGHT_ACQUIS_OPTIONS) {
                                                WeightCombination combo = new WeightCombination();
                                                combo.WEIGHT_CARD_POINTS = cardPoints;
                                                combo.WEIGHT_CARD_LEVEL = cardLevel;
                                                combo.WEIGHT_CARD_AVENGERS = cardAvengers;
                                                combo.WEIGHT_NEW_COLOR = newColor;
                                                combo.WEIGHT_COST = cost;
                                                combo.WEIGHT_LEVEL3_SYNERGY = level3Synergy;
                                                combo.WEIGHT_LEVEL2_SYNERGY = level2Synergy;
                                                combo.WEIGHT_LOCATION = location;
                                                combo.WEIGHT_RESERVED = reserved;
                                                combo.WEIGHT_ACQUIS = acquis;
                                                combinations.add(combo);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return combinations;
    }
    
    private boolean simulateGame(WeightCombination weights1, WeightCombination weights2) {
        try {
            Joueur[] joueurs = new Joueur[2];
            
            // Créer les stratégies avec leurs poids respectifs
            Strat9813 strat1 = new Strat9813();
            strat1.setWeights(weights1);
            
            Strat9813 strat2 = new Strat9813();
            strat2.setWeights(weights2);
            
            joueurs[0] = new Bot(strat1, "Player 1");
            joueurs[1] = new Bot(strat2, "Player 2");
            
            Jeu jeu = new Jeu(joueurs, true);
            jeu.init();
            
            for (int i = 0; i < joueurs.length; i++) {
                joueurs[jeu.ordre[i]].init(jeu.encoderEtatDuJeu(i));
            }

            int maxTurns = 50;
            int turnCount = 0;
            
            while (!jeu.end() && turnCount < maxTurns) {
                jeu.jouerUnNouveauTour();
                turnCount++;
            }
            
            // Si le nombre maximum de tours est atteint, c'est une partie nulle
            if (turnCount >= maxTurns) {
                weights1.draws++;
                weights2.draws++;
                return false;
            }
            
            // Déterminer le gagnant
            jeu.quiGagne();
            if (jeu.gagnants[0]  == false && jeu.gagnants[1] == false ) { // S'il y a plusieurs gagnants, c'est une partie nulle
                weights1.draws++;
                weights2.draws++;
                return false;
            }
            
            if (jeu.gagnants[0]) {
                weights1.victories++;
                return true;
            } else if(jeu.gagnants[1]) {
                weights2.victories++;
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la simulation: " + e.getMessage());
            return false;
        }
        return false;
    }
    
    public WeightCombination findBestCombination() {
        List<WeightCombination> combinations = generateAllCombinations();
        System.out.println("Nombre total de combinaisons : " + combinations.size());
        
        // Faire s'affronter chaque combinaison contre toutes les autres
        for (int i = 0; i < combinations.size(); i++) {
            WeightCombination weights1 = combinations.get(i);
            
            for (int j = i + 1; j < combinations.size(); j++) {
                WeightCombination weights2 = combinations.get(j);
                
                // Simuler plusieurs parties entre les deux combinaisons
                for (int k = 0; k < NUM_GAMES / combinations.size(); k++) {
                    simulateGame(weights1, weights2);
                }
            }
            
            // Afficher la progression
            if ((i + 1) % 10 == 0) {
                System.out.println("Progression : " + (i + 1) + "/" + combinations.size() + " combinaisons testées");
            }
        }
        
        // Trouver la meilleure combinaison en tenant compte des parties nulles
        return combinations.stream()
                .max(Comparator.comparingDouble(w -> (w.victories * 100.0 + w.draws * 50.0) / NUM_GAMES))
                .orElse(combinations.get(0));
    }
    
    public static void main(String[] args) {
        WeightCombinationOptimizer optimizer = new WeightCombinationOptimizer();
        WeightCombination bestCombo = optimizer.findBestCombination();
        
        System.out.println("\nMeilleure combinaison trouvée:");
        System.out.println(bestCombo);
    }
} 