package splendormarvel.ia;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import splendormarvel.Bot;
import splendormarvel.Jeu;
import splendormarvel.Joueur;
import splendormarvel.ia.Strat9819;
import splendormarvel.ia.Strat9812;
import splendormarvel.ia.Strat;

/**
 * Optimiseur génétique amélioré pour les poids de la stratégie Splendor Marvel
 */
public class WeightOptimizer {
	private static final int POPULATION_SIZE = 100;
    private static final int GENERATIONS = 50;
    private static final double MUTATION_RATE = 0.2;
    private static final double CROSSOVER_RATE = 0.8;
    private static final int TOURNAMENT_SIZE = 5;
    private static final int NUM_GAMES_PER_EVALUATION = 30;
    private static final double MIN_WEIGHT = 0.01;
    private static final double MAX_WEIGHT = 5.0;
    private static final double MUTATION_STRENGTH = 0.5;
    
    // PARAMÈTRES DE CONVERGENCE
    private static final double CONVERGENCE_THRESHOLD = 0.01;
    private static final int MAX_STAGNATION_GENERATIONS = 3;
    private static final long MAX_EXECUTION_TIME_MINUTES = 30;
    private static final long MAX_GENERATION_TIME_MINUTES = 5;
    
    // NOUVEAU : Seuil d'arrêt pour fitness élevée
    private static final double TARGET_FITNESS_THRESHOLD = 0.75; // 60% de victoires

    // Champs de la classe principale
    private List<Weights> population;
    private Map<Weights, Double> fitnessCache;
    private ExecutorService executor;
    private Random random;
    private Strat opponent;
    
    // Champs pour la convergence
    private long startTime;
    private double bestFitnessOverall;
    private int stagnationCounter;
    private List<Double> fitnessHistory;

    public WeightOptimizer() {
        this.population = new ArrayList<>();
        this.fitnessCache = new ConcurrentHashMap<>();
        this.executor = Executors.newFixedThreadPool(
            Math.min(Runtime.getRuntime().availableProcessors(), POPULATION_SIZE)
        );
        this.random = new Random();
        this.opponent = new Strat9819();
        
        this.bestFitnessOverall = -1.0;
        this.stagnationCounter = 0;
        this.fitnessHistory = new ArrayList<>();

        initializePopulation();
    }

    /**
     * MÉTHODE PRINCIPALE AVEC ARRÊT À SEUIL DE FITNESS
     */
    public Weights optimize() {
        startTime = System.currentTimeMillis();
        
        System.out.println("Démarrage de l'optimisation génétique...");
        System.out.println("Population: " + POPULATION_SIZE + ", Générations max: " + GENERATIONS);
        System.out.println("Nombre de parties par évaluation: " + NUM_GAMES_PER_EVALUATION);
        System.out.println("Fitness cible: " + String.format("%.1f", TARGET_FITNESS_THRESHOLD * 100) + "% de victoires");
        System.out.println("Timeout global: " + MAX_EXECUTION_TIME_MINUTES + " minutes");
        
        Weights bestOverall = null;

        for (int generation = 0; generation < GENERATIONS; generation++) {
            // VÉRIFICATION DU TIMEOUT GLOBAL
            if (hasExceededMaxTime()) {
                System.out.println("\n*** ARRÊT: Timeout global atteint (" + MAX_EXECUTION_TIME_MINUTES + " min) ***");
                break;
            }

            System.out.println("\n=== Génération " + (generation + 1) + "/" + GENERATIONS + " ===");
            long generationStartTime = System.currentTimeMillis();

            // Évaluation de la population avec timeout
            if (!evaluatePopulationWithTimeout()) {
                System.out.println("*** ARRÊT: Timeout de génération atteint ***");
                break;
            }

            // Trouve le meilleur individu de cette génération
            Weights bestCurrent = getBestIndividual();
            double bestFitnessCurrent = fitnessCache.get(bestCurrent);

            // Mise à jour du meilleur individu global
            boolean hasImproved = false;
            if (bestFitnessCurrent > bestFitnessOverall) {
                bestOverall = bestCurrent.clone();
                bestFitnessOverall = bestFitnessCurrent;
                stagnationCounter = 0;
                hasImproved = true;
            } else {
                stagnationCounter++;
            }

            // VÉRIFICATION DU SEUIL DE FITNESS CIBLE
            if (bestFitnessOverall >= TARGET_FITNESS_THRESHOLD) {
                System.out.println("\n*** ARRÊT: Fitness cible atteinte (" 
                                  + String.format("%.4f", bestFitnessOverall) 
                                  + " >= " + TARGET_FITNESS_THRESHOLD + " - " 
                                  + String.format("%.1f", bestFitnessOverall * 100) + "% de victoires) ***");
                break;
            }

            // Ajouter à l'historique
            fitnessHistory.add(bestFitnessCurrent);

            // Affichage des statistiques
            double avgFitness = getAverageFitness();
            long generationTime = (System.currentTimeMillis() - generationStartTime) / 1000;
            
            System.out.println("Meilleure fitness: " + String.format("%.4f", bestFitnessCurrent) + 
                             " (" + String.format("%.1f", bestFitnessCurrent * 100) + "% victoires)" +
                             (hasImproved ? " *** NOUVEAU RECORD ***" : ""));
            System.out.println("Fitness moyenne: " + String.format("%.4f", avgFitness) + 
                             " (" + String.format("%.1f", avgFitness * 100) + "% victoires)");
            System.out.println("Temps de génération: " + generationTime + "s");
            System.out.println("Stagnation: " + stagnationCounter + "/" + MAX_STAGNATION_GENERATIONS);
            System.out.println("Objectif: " + String.format("%.1f", TARGET_FITNESS_THRESHOLD * 100) + "% victoires");

            // VÉRIFICATION DE LA CONVERGENCE
            if (hasConverged()) {
                System.out.println("\n*** CONVERGENCE ATTEINTE ***");
                break;
            }

            // VÉRIFICATION DE LA STAGNATION
            if (stagnationCounter >= MAX_STAGNATION_GENERATIONS) {
                System.out.println("\n*** ARRÊT: Stagnation détectée (" + MAX_STAGNATION_GENERATIONS + " générations sans amélioration) ***");
                break;
            }

            // Création de la nouvelle population
            if (generation < GENERATIONS - 1) {
                createNewGeneration();
            }
        }

        shutdown();
        
        long totalTime = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("\n=== Optimisation terminée ===");
        System.out.println("Temps total: " + totalTime + "s");
        System.out.println("Meilleure fitness globale: " + String.format("%.4f", bestFitnessOverall) + 
                          " (" + String.format("%.1f", bestFitnessOverall * 100) + "% de victoires)");
        
        if (bestFitnessOverall >= TARGET_FITNESS_THRESHOLD) {
            System.out.println("🎯 OBJECTIF ATTEINT ! Fitness >= " + String.format("%.1f", TARGET_FITNESS_THRESHOLD * 100) + "%");
        } else {
            System.out.println("⚠️  Objectif non atteint. Cible: " + String.format("%.1f", TARGET_FITNESS_THRESHOLD * 100) + 
                             "%, Atteint: " + String.format("%.1f", bestFitnessOverall * 100) + "%");
        }
        
        System.out.println("Évolution de la fitness: " + fitnessHistory.stream()
            .map(f -> String.format("%.3f", f))
            .collect(Collectors.joining(" -> ")));
        System.out.println("\nPoids optimaux trouvés:");
        System.out.println(bestOverall);
        
        return bestOverall;
    }

    // Vérification du timeout global
    private boolean hasExceededMaxTime() {
        long elapsedMinutes = (System.currentTimeMillis() - startTime) / (1000 * 60);
        return elapsedMinutes >= MAX_EXECUTION_TIME_MINUTES;
    }

    // Vérification de la convergence
    private boolean hasConverged() {
        if (fitnessHistory.size() < 3) return false;
        
        int size = fitnessHistory.size();
        double recent1 = fitnessHistory.get(size - 1);
        double recent2 = fitnessHistory.get(size - 2);
        double recent3 = fitnessHistory.get(size - 3);
        
        return Math.abs(recent1 - recent2) < CONVERGENCE_THRESHOLD && 
               Math.abs(recent2 - recent3) < CONVERGENCE_THRESHOLD;
    }
    // Structure pour stocker les poids (identique)
    public static class Weights implements Cloneable {
        private static final double[] WEIGHT_CARD_POINTS = {2.45, 2.45, 2.45};
        private static final double[] WEIGHT_CARD_LEVEL = {0.5, 0.5, 0.5};
        private static final double[] WEIGHT_CARD_AVENGERS = {1.0, 1.0, 1.0};
        private static final double[] WEIGHT_NEW_COLOR = {5.0, 5.0, 5.0};
        private static final double[] WEIGHT_COST = {0.75, 0.75, 0.75};
        private static final double[] WEIGHT_LEVEL3_SYNERGY = {2.5, 2.5, 2.5};
        static final double[] WEIGHT_LEVEL2_SYNERGY = {2.0, 2.0, 2.0};
        private static final double[] WEIGHT_LOCATION = {3.0, 3.0, 3.0};
        private static final double[] WEIGHT_RESERVED = {5.0, 5.0, 5.0};
        public double[] WEIGHT_ACQUIS;

        public Weights() {
            initializeArrays();
        }

        public Weights(Weights other) {
            this.WEIGHT_ACQUIS = Arrays.copyOf(other.WEIGHT_ACQUIS, 3);
        }

        private void initializeArrays() {
            WEIGHT_ACQUIS = new double[3];
        }

        public Weights crossover(Weights other) {
            Weights child = new Weights();
            Random rand = new Random();
            crossoverArray(this.WEIGHT_ACQUIS, other.WEIGHT_ACQUIS, child.WEIGHT_ACQUIS, rand);
            return child;
        }

        private void crossoverArray(double[] parent1, double[] parent2, double[] child, Random rand) {
            for (int i = 0; i < parent1.length; i++) {
                if (rand.nextDouble() < CROSSOVER_RATE) {
                    child[i] = parent1[i];
                } else {
                    child[i] = parent2[i];
                }
            }
        }

        public void mutate() {
            Random rand = new Random();
            mutateArray(WEIGHT_ACQUIS, rand);
        }

        private void mutateArray(double[] array, Random rand) {
            for (int i = 0; i < array.length; i++) {
                if (rand.nextDouble() < MUTATION_RATE) {
                    double delta = rand.nextGaussian() * MUTATION_STRENGTH;
                    array[i] = Math.min(MAX_WEIGHT, Math.max(MIN_WEIGHT, array[i] + delta));
                }
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Weights weights = (Weights) obj;
            return Arrays.equals(WEIGHT_ACQUIS, weights.WEIGHT_ACQUIS);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(WEIGHT_ACQUIS);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("private static final double[] WEIGHT_ACQUIS = ")
              .append(formatArrayAsInitializer(WEIGHT_ACQUIS)).append(";\n");
            return sb.toString();
        }

        private String formatArrayAsInitializer(double[] array) {
            return "{ " + Arrays.stream(array)
                    .mapToObj(d -> String.format("%.2f", d))
                    .collect(Collectors.joining(", ")) + " }";
        }

        public String toFormattedString() {
            StringBuilder sb = new StringBuilder();
            sb.append("private static final double[] ***WEIGHT_ACQUIS*** = ")
              .append(formatArrayAsInitializer(WEIGHT_ACQUIS)).append(";\n");
            return sb.toString();
        }

        @Override
        protected Weights clone() {
            return new Weights(this);
        }
    }

   

    public WeightOptimizer(Strat opponent) {
        this();
        this.opponent = opponent;
    }

    private void initializePopulation() {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Weights weights = new Weights();
            randomizeWeights(weights);
            population.add(weights);
        }
    }

    private void randomizeWeights(Weights weights) {
        randomizeArray(weights.WEIGHT_ACQUIS);
    }

    private void randomizeArray(double[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = MIN_WEIGHT + random.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT);
        }
    }

    

    
    /**
     * ÉVALUATION AVEC TIMEOUT PAR GÉNÉRATION
     */
    private boolean evaluatePopulationWithTimeout() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        long generationStartTime = System.currentTimeMillis();

        for (Weights weights : population) {
            if (!fitnessCache.containsKey(weights)) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    double fitness = evaluateFitness(weights);
                    fitnessCache.put(weights, fitness);
                }, executor);
                futures.add(future);
            }
        }

        try {
            // Attendre avec timeout
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(MAX_GENERATION_TIME_MINUTES, TimeUnit.MINUTES);
            return true;
        } catch (TimeoutException e) {
            System.out.println("*** Timeout de génération, annulation des tâches en cours ***");
            futures.forEach(f -> f.cancel(true));
            return false;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Erreur lors de l'évaluation: " + e.getMessage());
            return false;
        }
    }

    /**
     * ÉVALUATION PLUS RAPIDE AVEC ARRÊT PRÉCOCE
     */
    private double evaluateFitness(Weights weights) {
        if (opponent == null) {
            return random.nextDouble();
        }

        double totalWinRate = 0.0;
        int totalGames = 0;
        int consecutiveLosses = 0;

        try {
            for (int i = 0; i < NUM_GAMES_PER_EVALUATION; i++) {
                // VÉRIFICATION DE TIMEOUT PENDANT L'ÉVALUATION
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }

                Object player = new Strat9812(weights);
                boolean won1 = simulateGame(player, opponent);
                boolean won2 = !simulateGame(opponent, player);

                if (won1) {
                    totalWinRate += 1.0;
                    consecutiveLosses = 0;
                } else {
                    consecutiveLosses++;
                }
                
                if (won2) {
                    totalWinRate += 1.0;
                    consecutiveLosses = 0;
                } else {
                    consecutiveLosses++;
                }
                
                totalGames += 2;

                // ARRÊT PRÉCOCE si les performances sont très mauvaises
                if (consecutiveLosses >= 10 && totalGames >= 20) {
                    System.out.print(".");  // Indicateur visuel d'arrêt précoce
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'évaluation: " + e.getMessage());
            return 0.0;
        }

        return totalGames > 0 ? totalWinRate / totalGames : 0.0;
    }

    /**
     * SIMULATION AVEC TIMEOUT
     */
    private boolean simulateGame(Object player1, Object player2) {
        try {
            Joueur[] joueurs = new Joueur[2];
            joueurs[0] = new Bot((Strat) player1, "Player 1");
            joueurs[1] = new Bot((Strat) player2, "Player 2");
            
            Jeu jeu = new Jeu(joueurs, false);
            jeu.init();
            
            for (int i = 0; i < joueurs.length; i++) {
                joueurs[jeu.ordre[i]].init(jeu.encoderEtatDuJeu(i));
            }

            // LIMITATION DU NOMBRE DE TOURS POUR ÉVITER LES PARTIES INFINIES
            int maxTurns = 200;
            int turnCount = 0;
            
            while (!jeu.end() && turnCount < maxTurns) {
                jeu.jouerUnNouveauTour();
                turnCount++;
                
                // Vérification d'interruption
                if (Thread.currentThread().isInterrupted()) {
                    return false;
                }
            }

            if (turnCount >= maxTurns) {
                // Partie trop longue, considérée comme nulle
                return false;
            }

            jeu.quiGagne();
            return jeu.gagnants[0];
            
        } catch (Exception e) {
            return false;
        }
    }

    private void createNewGeneration() {
        List<Weights> newPopulation = new ArrayList<>();

        // Élitisme: conserver le meilleur individu
        Weights bestIndividual = getBestIndividual();
        newPopulation.add(bestIndividual.clone());

        // Générer le reste de la population
        while (newPopulation.size() < POPULATION_SIZE) {
            Weights parent1 = tournamentSelection();
            Weights parent2 = tournamentSelection();
            Weights child = parent1.crossover(parent2);
            child.mutate();
            newPopulation.add(child);
        }

        population = newPopulation;
        cleanupFitnessCache();
    }

    private void cleanupFitnessCache() {
        Set<Weights> currentPopulation = new HashSet<>(population);
        fitnessCache.keySet().retainAll(currentPopulation);
    }

    private Weights getBestIndividual() {
        return population.stream()
                .max(Comparator.comparingDouble(w -> fitnessCache.getOrDefault(w, 0.0)))
                .orElse(population.get(0));
    }

    private double getAverageFitness() {
        return population.stream()
                .mapToDouble(w -> fitnessCache.getOrDefault(w, 0.0))
                .average()
                .orElse(0.0);
    }

    private Weights tournamentSelection() {
        List<Weights> tournament = new ArrayList<>();
        
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            int randomIndex = random.nextInt(population.size());
            tournament.add(population.get(randomIndex));
        }

        return tournament.stream()
                .max(Comparator.comparingDouble(w -> fitnessCache.getOrDefault(w, 0.0)))
                .orElse(tournament.get(0));
    }

    private void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        
        WeightOptimizer optimizer = new WeightOptimizer();
        Weights bestWeights = optimizer.optimize();
        
        System.out.println("\nFormat avec étoiles:");
        System.out.println(bestWeights.toFormattedString());
    }
}