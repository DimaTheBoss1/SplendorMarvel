package splendormarvel.ia;

import java.util.*;
import java.util.concurrent.*;
import splendormarvel.Bot;
import splendormarvel.Jeu;
import splendormarvel.Joueur;

public class WeightOptimizer {
    private static final int POPULATION_SIZE = 50;
    private static final int GENERATIONS = 100;
    private static final double MUTATION_RATE = 0.1;
    private static final double CROSSOVER_RATE = 0.7;
    private static final int TOURNAMENT_SIZE = 5;
    private static final int NUM_GAMES_PER_EVALUATION = 100;
    private static final double MIN_WEIGHT = 0.0;
    private static final double MAX_WEIGHT = 50.0;

    // Structure pour stocker les poids
    public static class Weights {
        public double[] WEIGHT_CARD_POINTS;
        public double[] WEIGHT_CARD_LEVEL;
        public double[] WEIGHT_CARD_AVENGERS;
        public double[] WEIGHT_NEW_COLOR;
        public double[] WEIGHT_COST;
        public double[] WEIGHT_LEVEL3_SYNERGY;
        public double[] WEIGHT_LEVEL2_SYNERGY;
        public double[] WEIGHT_LOCATION;
        public double[] WEIGHT_RESERVED;
        public double[] WEIGHT_ACQUIS;

        public Weights() {
            WEIGHT_CARD_POINTS = new double[3];
            WEIGHT_CARD_LEVEL = new double[3];
            WEIGHT_CARD_AVENGERS = new double[3];
            WEIGHT_NEW_COLOR = new double[3];
            WEIGHT_COST = new double[3];
            WEIGHT_LEVEL3_SYNERGY = new double[3];
            WEIGHT_LEVEL2_SYNERGY = new double[3];
            WEIGHT_LOCATION = new double[3];
            WEIGHT_RESERVED = new double[3];
            WEIGHT_ACQUIS = new double[3];
        }

        public Weights(Weights other) {
            this.WEIGHT_CARD_POINTS = Arrays.copyOf(other.WEIGHT_CARD_POINTS, 3);
            this.WEIGHT_CARD_LEVEL = Arrays.copyOf(other.WEIGHT_CARD_LEVEL, 3);
            this.WEIGHT_CARD_AVENGERS = Arrays.copyOf(other.WEIGHT_CARD_AVENGERS, 3);
            this.WEIGHT_NEW_COLOR = Arrays.copyOf(other.WEIGHT_NEW_COLOR, 3);
            this.WEIGHT_COST = Arrays.copyOf(other.WEIGHT_COST, 3);
            this.WEIGHT_LEVEL3_SYNERGY = Arrays.copyOf(other.WEIGHT_LEVEL3_SYNERGY, 3);
            this.WEIGHT_LEVEL2_SYNERGY = Arrays.copyOf(other.WEIGHT_LEVEL2_SYNERGY, 3);
            this.WEIGHT_LOCATION = Arrays.copyOf(other.WEIGHT_LOCATION, 3);
            this.WEIGHT_RESERVED = Arrays.copyOf(other.WEIGHT_RESERVED, 3);
            this.WEIGHT_ACQUIS = Arrays.copyOf(other.WEIGHT_ACQUIS, 3);
        }

        public Weights crossover(Weights other) {
            Weights child = new Weights();
            Random rand = new Random();
            
            for (int i = 0; i < 3; i++) {
                if (rand.nextDouble() < CROSSOVER_RATE) {
                    child.WEIGHT_CARD_POINTS[i] = this.WEIGHT_CARD_POINTS[i];
                    child.WEIGHT_CARD_LEVEL[i] = this.WEIGHT_CARD_LEVEL[i];
                    child.WEIGHT_CARD_AVENGERS[i] = this.WEIGHT_CARD_AVENGERS[i];
                } else {
                    child.WEIGHT_CARD_POINTS[i] = other.WEIGHT_CARD_POINTS[i];
                    child.WEIGHT_CARD_LEVEL[i] = other.WEIGHT_CARD_LEVEL[i];
                    child.WEIGHT_CARD_AVENGERS[i] = other.WEIGHT_CARD_AVENGERS[i];
                }
                
                if (rand.nextDouble() < CROSSOVER_RATE) {
                    child.WEIGHT_NEW_COLOR[i] = this.WEIGHT_NEW_COLOR[i];
                    child.WEIGHT_COST[i] = this.WEIGHT_COST[i];
                    child.WEIGHT_LEVEL3_SYNERGY[i] = this.WEIGHT_LEVEL3_SYNERGY[i];
                } else {
                    child.WEIGHT_NEW_COLOR[i] = other.WEIGHT_NEW_COLOR[i];
                    child.WEIGHT_COST[i] = other.WEIGHT_COST[i];
                    child.WEIGHT_LEVEL3_SYNERGY[i] = other.WEIGHT_LEVEL3_SYNERGY[i];
                }
                
                if (rand.nextDouble() < CROSSOVER_RATE) {
                    child.WEIGHT_LEVEL2_SYNERGY[i] = this.WEIGHT_LEVEL2_SYNERGY[i];
                    child.WEIGHT_LOCATION[i] = this.WEIGHT_LOCATION[i];
                    child.WEIGHT_RESERVED[i] = this.WEIGHT_RESERVED[i];
                    child.WEIGHT_ACQUIS[i] = this.WEIGHT_ACQUIS[i];
                } else {
                    child.WEIGHT_LEVEL2_SYNERGY[i] = other.WEIGHT_LEVEL2_SYNERGY[i];
                    child.WEIGHT_LOCATION[i] = other.WEIGHT_LOCATION[i];
                    child.WEIGHT_RESERVED[i] = other.WEIGHT_RESERVED[i];
                    child.WEIGHT_ACQUIS[i] = other.WEIGHT_ACQUIS[i];
                }
            }
            
            return child;
        }

        public void mutate() {
            Random rand = new Random();
            for (int i = 0; i < 3; i++) {
                if (rand.nextDouble() < MUTATION_RATE) {
                    WEIGHT_CARD_POINTS[i] *= (0.8 + rand.nextDouble() * 0.4);
                }
                if (rand.nextDouble() < MUTATION_RATE) {
                    WEIGHT_CARD_LEVEL[i] *= (0.8 + rand.nextDouble() * 0.4);
                }
                if (rand.nextDouble() < MUTATION_RATE) {
                    WEIGHT_CARD_AVENGERS[i] *= (0.8 + rand.nextDouble() * 0.4);
                }
                if (rand.nextDouble() < MUTATION_RATE) {
                    WEIGHT_NEW_COLOR[i] *= (0.8 + rand.nextDouble() * 0.4);
                }
                if (rand.nextDouble() < MUTATION_RATE) {
                    WEIGHT_COST[i] *= (0.8 + rand.nextDouble() * 0.4);
                }
                if (rand.nextDouble() < MUTATION_RATE) {
                    WEIGHT_LEVEL3_SYNERGY[i] *= (0.8 + rand.nextDouble() * 0.4);
                }
                if (rand.nextDouble() < MUTATION_RATE) {
                    WEIGHT_LEVEL2_SYNERGY[i] *= (0.8 + rand.nextDouble() * 0.4);
                }
                if (rand.nextDouble() < MUTATION_RATE) {
                    WEIGHT_LOCATION[i] *= (0.8 + rand.nextDouble() * 0.4);
                }
                if (rand.nextDouble() < MUTATION_RATE) {
                    WEIGHT_RESERVED[i] *= (0.8 + rand.nextDouble() * 0.4);
                }
                if (rand.nextDouble() < MUTATION_RATE) {
                    WEIGHT_ACQUIS[i] *= (0.8 + rand.nextDouble() * 0.4);
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("WEIGHT_CARD_POINTS = { ");
            for (double w : WEIGHT_CARD_POINTS) sb.append(String.format("%.2f, ", w));
            sb.append("}\n");
            
            sb.append("WEIGHT_CARD_LEVEL = { ");
            for (double w : WEIGHT_CARD_LEVEL) sb.append(String.format("%.2f, ", w));
            sb.append("}\n");
            
            sb.append("WEIGHT_CARD_AVENGERS = { ");
            for (double w : WEIGHT_CARD_AVENGERS) sb.append(String.format("%.2f, ", w));
            sb.append("}\n");
            
            sb.append("WEIGHT_NEW_COLOR = { ");
            for (double w : WEIGHT_NEW_COLOR) sb.append(String.format("%.2f, ", w));
            sb.append("}\n");
            
            sb.append("WEIGHT_COST = { ");
            for (double w : WEIGHT_COST) sb.append(String.format("%.2f, ", w));
            sb.append("}\n");
            
            sb.append("WEIGHT_LEVEL3_SYNERGY = { ");
            for (double w : WEIGHT_LEVEL3_SYNERGY) sb.append(String.format("%.2f, ", w));
            sb.append("}\n");
            
            sb.append("WEIGHT_LEVEL2_SYNERGY = { ");
            for (double w : WEIGHT_LEVEL2_SYNERGY) sb.append(String.format("%.2f, ", w));
            sb.append("}\n");
            
            sb.append("WEIGHT_LOCATION = { ");
            for (double w : WEIGHT_LOCATION) sb.append(String.format("%.2f, ", w));
            sb.append("}\n");
            
            sb.append("WEIGHT_RESERVED = { ");
            for (double w : WEIGHT_RESERVED) sb.append(String.format("%.2f, ", w));
            sb.append("}\n");
            
            sb.append("WEIGHT_ACQUIS = { ");
            for (double w : WEIGHT_ACQUIS) sb.append(String.format("%.2f, ", w));
            sb.append("}\n");
            
            return sb.toString();
        }
    }

    private List<Weights> population;
    private Map<Weights, Double> fitnessCache;
    private ExecutorService executor;
    private Random random;
    private Strat9811 opponent;

    public WeightOptimizer() {
        this.population = new ArrayList<>();
        this.fitnessCache = new ConcurrentHashMap<>();
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.random = new Random();
        this.opponent = new Strat9811();
        
        // Initialize population
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Weights weights = new Weights();
            randomizeWeights(weights);
            population.add(weights);
        }
    }

    private void randomizeWeights(Weights weights) {
        for (int i = 0; i < 3; i++) {
            weights.WEIGHT_CARD_POINTS[i] = MIN_WEIGHT + random.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT);
            weights.WEIGHT_CARD_LEVEL[i] = MIN_WEIGHT + random.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT);
            weights.WEIGHT_CARD_AVENGERS[i] = MIN_WEIGHT + random.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT);
            weights.WEIGHT_NEW_COLOR[i] = MIN_WEIGHT + random.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT);
            weights.WEIGHT_COST[i] = MIN_WEIGHT + random.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT);
            weights.WEIGHT_LEVEL3_SYNERGY[i] = MIN_WEIGHT + random.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT);
            weights.WEIGHT_LEVEL2_SYNERGY[i] = MIN_WEIGHT + random.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT);
            weights.WEIGHT_LOCATION[i] = MIN_WEIGHT + random.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT);
            weights.WEIGHT_RESERVED[i] = MIN_WEIGHT + random.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT);
            weights.WEIGHT_ACQUIS[i] = MIN_WEIGHT + random.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT);
        }
    }

    private void mutateArray(double[] array) {
        for (int i = 0; i < array.length; i++) {
            if (random.nextDouble() < MUTATION_RATE) {
                // Mutation par ajout/soustraction d'une valeur aléatoire
                double delta = (random.nextDouble() * 10.0) - 5.0; // Change de -5 à +5
                array[i] = Math.min(MAX_WEIGHT, Math.max(MIN_WEIGHT, array[i] + delta));
            }
        }
    }

    private void mutate(Weights weights) {
        mutateArray(weights.WEIGHT_CARD_POINTS);
        mutateArray(weights.WEIGHT_CARD_LEVEL);
        mutateArray(weights.WEIGHT_CARD_AVENGERS);
        mutateArray(weights.WEIGHT_NEW_COLOR);
        mutateArray(weights.WEIGHT_COST);
        mutateArray(weights.WEIGHT_LEVEL3_SYNERGY);
        mutateArray(weights.WEIGHT_LEVEL2_SYNERGY);
        mutateArray(weights.WEIGHT_LOCATION);
        mutateArray(weights.WEIGHT_RESERVED);
        mutateArray(weights.WEIGHT_ACQUIS);
    }

    public void optimize() {
        for (int generation = 0; generation < GENERATIONS; generation++) {
            System.out.println("Generation " + (generation + 1) + "/" + GENERATIONS);
            
            // Evaluate fitness for all individuals
            List<Future<Double>> futures = new ArrayList<>();
            for (Weights weights : population) {
                if (!fitnessCache.containsKey(weights)) {
                    futures.add(executor.submit(() -> evaluateFitness(weights)));
                }
            }
            
            // Wait for all evaluations to complete
            for (int i = 0; i < futures.size(); i++) {
                try {
                    double fitness = futures.get(i).get();
                    fitnessCache.put(population.get(i), fitness);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // Create new population
            List<Weights> newPopulation = new ArrayList<>();
            
            // Elitism: keep best individual
            Weights bestIndividual = getBestIndividual();
            newPopulation.add(bestIndividual);
            
            // Generate rest of new population
            while (newPopulation.size() < POPULATION_SIZE) {
                Weights parent1 = tournamentSelection();
                Weights parent2 = tournamentSelection();
                Weights child = parent1.crossover(parent2);
                child.mutate();
                newPopulation.add(child);
            }
            
            // Replace old population
            population = newPopulation;
            
            // Print best fitness
            System.out.println("Best fitness: " + fitnessCache.get(bestIndividual));
            System.out.println("Best weights:\n" + bestIndividual);
        }
        
        executor.shutdown();
    }

    private double evaluateFitness(Weights weights) {
        double totalWinRate = 0.0;
        
        for (int i = 0; i < NUM_GAMES_PER_EVALUATION; i++) {
            Strat9812 player = new Strat9812(weights);
            
            // Simulate game and get result
            boolean won = simulateGame(player, opponent);
            if (won) totalWinRate += 1.0;
            
            // Play as second player too
            won = simulateGame(opponent, player);
            if (!won) totalWinRate += 1.0;
        }
        
        return totalWinRate / (NUM_GAMES_PER_EVALUATION * 2);
    }

    private boolean simulateGame(Strat player1, Strat player2) {
        // Create game instance
        Joueur[] joueurs = new Joueur[2];
        joueurs[0] = new Bot(player1, "Player 1");
        joueurs[1] = new Bot(player2, "Player 2");
        Jeu jeu = new Jeu(joueurs, false);
        
        // Initialize game state
        jeu.init();
        for (int i = 0; i < joueurs.length; i++) {
            joueurs[jeu.ordre[i]].init(jeu.encoderEtatDuJeu(i));
        }
        
        // Play game until end
        while (!jeu.end()) {
            jeu.jouerUnNouveauTour();
        }
        
        // Determine winner
        jeu.quiGagne();
        return jeu.gagnants[0];
    }

    private Weights getBestIndividual() {
        return population.stream()
                .max(Comparator.comparingDouble(w -> fitnessCache.getOrDefault(w, 0.0)))
                .orElse(population.get(0));
    }

    private Weights tournamentSelection() {
        List<Weights> tournament = new ArrayList<>();
        Random rand = new Random();
        
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            tournament.add(population.get(rand.nextInt(population.size())));
        }
        
        return tournament.stream()
                .max(Comparator.comparingDouble(w -> fitnessCache.getOrDefault(w, 0.0)))
                .orElse(tournament.get(0));
    }
} 