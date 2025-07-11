package splendormarvel.ia;

import java.io.*;
import java.util.Properties;

public class WeightOptimizerMain {
    private static final String WEIGHTS_FILE = "best_weights.properties";
    
    public static void main(String[] args) {
        System.out.println("Démarrage de l'optimisation des poids...");
        
        // Créer et lancer l'optimiseur
        WeightOptimizer optimizer = new WeightOptimizer();
        WeightOptimizer.Weights bestWeights = optimizer.optimize();
        
        // Sauvegarder les poids dans un fichier
        saveWeights(bestWeights);
        
        // Afficher les résultats
        System.out.println("\nMeilleurs poids trouvés :");
        System.out.println("WEIGHT_CARD_POINTS: " + arrayToString(bestWeights.WEIGHT_CARD_POINTS));
        System.out.println("WEIGHT_CARD_LEVEL: " + arrayToString(bestWeights.WEIGHT_CARD_LEVEL));
        System.out.println("WEIGHT_CARD_AVENGERS: " + arrayToString(bestWeights.WEIGHT_CARD_AVENGERS));
        System.out.println("WEIGHT_NEW_COLOR: " + arrayToString(bestWeights.WEIGHT_NEW_COLOR));
        System.out.println("WEIGHT_COST: " + arrayToString(bestWeights.WEIGHT_COST));
        System.out.println("WEIGHT_LEVEL3_SYNERGY: " + arrayToString(bestWeights.WEIGHT_LEVEL3_SYNERGY));
        System.out.println("WEIGHT_LEVEL2_SYNERGY: " + arrayToString(bestWeights.WEIGHT_LEVEL2_SYNERGY));
        System.out.println("WEIGHT_LOCATION: " + arrayToString(bestWeights.WEIGHT_LOCATION));
        System.out.println("WEIGHT_RESERVED: " + arrayToString(bestWeights.WEIGHT_RESERVED));
        System.out.println("WEIGHT_ACQUIS: " + arrayToString(bestWeights.WEIGHT_ACQUIS));
        
        System.out.println("\nLes poids ont été sauvegardés dans " + WEIGHTS_FILE);
    }
    
    private static String arrayToString(double[] array) {
        StringBuilder sb = new StringBuilder("{ ");
        for (int i = 0; i < array.length; i++) {
            sb.append(String.format("%.2f", array[i]));
            if (i < array.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(" }");
        return sb.toString();
    }
    
    public static void saveWeights(WeightOptimizer.Weights weights) {
        Properties props = new Properties();
        
        // Sauvegarder chaque tableau de poids
        saveWeightArray(props, "WEIGHT_CARD_POINTS", weights.WEIGHT_CARD_POINTS);
        saveWeightArray(props, "WEIGHT_CARD_LEVEL", weights.WEIGHT_CARD_LEVEL);
        saveWeightArray(props, "WEIGHT_CARD_AVENGERS", weights.WEIGHT_CARD_AVENGERS);
        saveWeightArray(props, "WEIGHT_NEW_COLOR", weights.WEIGHT_NEW_COLOR);
        saveWeightArray(props, "WEIGHT_COST", weights.WEIGHT_COST);
        saveWeightArray(props, "WEIGHT_LEVEL3_SYNERGY", weights.WEIGHT_LEVEL3_SYNERGY);
        saveWeightArray(props, "WEIGHT_LEVEL2_SYNERGY", weights.WEIGHT_LEVEL2_SYNERGY);
        saveWeightArray(props, "WEIGHT_LOCATION", weights.WEIGHT_LOCATION);
        saveWeightArray(props, "WEIGHT_RESERVED", weights.WEIGHT_RESERVED);
        saveWeightArray(props, "WEIGHT_ACQUIS", weights.WEIGHT_ACQUIS);

        try (FileOutputStream out = new FileOutputStream(WEIGHTS_FILE)) {
            props.store(out, "Meilleurs poids trouvés par l'optimisation");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void saveWeightArray(Properties props, String name, double[] weights) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < weights.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format("%.6f", weights[i]));
        }
        props.setProperty(name, sb.toString());
    }
    
    public static WeightOptimizer.Weights loadWeights() {
        Properties props = new Properties();
        WeightOptimizer.Weights weights = new WeightOptimizer.Weights();

        try (FileInputStream in = new FileInputStream(WEIGHTS_FILE)) {
            props.load(in);
            
            // Charger chaque tableau de poids
            loadWeightArray(props, "WEIGHT_CARD_POINTS", weights.WEIGHT_CARD_POINTS);
            loadWeightArray(props, "WEIGHT_CARD_LEVEL", weights.WEIGHT_CARD_LEVEL);
            loadWeightArray(props, "WEIGHT_CARD_AVENGERS", weights.WEIGHT_CARD_AVENGERS);
            loadWeightArray(props, "WEIGHT_NEW_COLOR", weights.WEIGHT_NEW_COLOR);
            loadWeightArray(props, "WEIGHT_COST", weights.WEIGHT_COST);
            loadWeightArray(props, "WEIGHT_LEVEL3_SYNERGY", weights.WEIGHT_LEVEL3_SYNERGY);
            loadWeightArray(props, "WEIGHT_LEVEL2_SYNERGY", weights.WEIGHT_LEVEL2_SYNERGY);
            loadWeightArray(props, "WEIGHT_LOCATION", weights.WEIGHT_LOCATION);
            loadWeightArray(props, "WEIGHT_RESERVED", weights.WEIGHT_RESERVED);
            loadWeightArray(props, "WEIGHT_ACQUIS", weights.WEIGHT_ACQUIS);
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        return weights;
    }
    
    private static void loadWeightArray(Properties props, String name, double[] weights) {
        String[] values = props.getProperty(name, "0,0,0").split(",");
        for (int i = 0; i < Math.min(values.length, weights.length); i++) {
            weights[i] = Double.parseDouble(values[i]);
        }
    }
} 