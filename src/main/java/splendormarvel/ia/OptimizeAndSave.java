package splendormarvel.ia;

import java.io.*;
import java.util.Properties;

public class OptimizeAndSave {
    private static final String WEIGHTS_FILE = "best_weights.properties";

    public static void main(String[] args) {
        // Lancer l'optimisation
        System.out.println("Démarrage de l'optimisation des poids...");
        WeightOptimizer optimizer = new WeightOptimizer();
        WeightOptimizer.Weights bestWeights = optimizer.optimize();

        // Sauvegarder les meilleurs poids
        saveWeights(bestWeights);
        
        System.out.println("Optimisation terminée. Les meilleurs poids ont été sauvegardés dans " + WEIGHTS_FILE);
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

    private static void saveWeightArray(Properties props, String name, double[] weights) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < weights.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format("%.6f", weights[i]));
        }
        props.setProperty(name, sb.toString());
    }

    private static void loadWeightArray(Properties props, String name, double[] weights) {
        String[] values = props.getProperty(name, "0,0,0").split(",");
        for (int i = 0; i < Math.min(values.length, weights.length); i++) {
            weights[i] = Double.parseDouble(values[i]);
        }
    }
} 