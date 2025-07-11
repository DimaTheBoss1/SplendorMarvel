package splendormarvel.ia;

public class WeightOptimizerMain {
    public static void main(String[] args) {
        System.out.println("Démarrage de l'optimisation des poids...");
        
        WeightOptimizer optimizer = new WeightOptimizer();
        WeightOptimizer.Weights bestWeights = optimizer.optimize();
        
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
} 