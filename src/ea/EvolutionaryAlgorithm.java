package ea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import model.City;
import model.Route;

public class EvolutionaryAlgorithm {

    public enum CrossoverMethod { ORDER, PMX, DPX }
    public enum MutationMethod  { SWAP, INVERSION, SHIFT }
    
    private final int populationSize;
    private final int initialPopulationMul;
    private final double crossoverRate;
    private final double mutationRate;
    private final int tournamentSize;
    private final int maxGenerations;
    private final List<City> cities;
    private final double[][] costMatrix;
    private final boolean parentSelectionMethod; // true: torneo, false: ranking de mapeo lineal
    private final boolean survivalSelectionMethod; // true: round robin, false: fitness-based
    private final Random random;
    private final CrossoverMethod crossoverMethod;
    private final MutationMethod mutationMethod;

    public EvolutionaryAlgorithm(List<City> cities, double[][] costMatrix, int populationSize, int initialPopulationMul,
                                  double crossoverRate, double mutationRate, int tournamentSize,
                                  int maxGenerations, boolean parentSelectionMethod, boolean survivalSelectionMethod, 
                                  CrossoverMethod crossoverMethod, MutationMethod mutationMethod) {
        this.cities = cities;
        this.costMatrix = costMatrix;
        this.populationSize = populationSize;
        this.initialPopulationMul = initialPopulationMul;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.tournamentSize = tournamentSize;
        this.maxGenerations = maxGenerations;
        this.parentSelectionMethod = parentSelectionMethod;
        this.random = new Random();
        this.survivalSelectionMethod = survivalSelectionMethod; // Por defecto, usamos round robin para selección de sobrevivientes
        this.crossoverMethod = crossoverMethod;
        this.mutationMethod = mutationMethod;
    }

    /**
     * Initializes a random population of routes.
     */
    public List<Route> initializePopulation() {
        List<Route> population = new ArrayList<>();

        // Si queremos generar más individuos inicialmente, usamos el multiplicador
        int totalInitial = populationSize * initialPopulationMul;

        for (int i = 0; i < totalInitial; i++) {
            List<City> shuffled = new ArrayList<>(cities);
            Collections.shuffle(shuffled);
            population.add(new Route(shuffled, costMatrix));
        }
        return population;
    }

    /**
     * Returns the best route (highest fitness / shortest distance) in the population.
     */
    public Route getBestRoute(List<Route> population) {
        Route best = population.get(0);
        for (Route route : population) {
            if (route.getFitness() > best.getFitness()) {
                best = route;
            }
        }
        return best;
    }

    /**
     * Runs the evolutionary algorithm and returns the best route found.
     */
    public Route run() {
        List<Route> population = initializePopulation();

        // Si generamos mas individuos inicialmente, hacemos una selección previa para reducir a 'populationSize'
        if (initialPopulationMul > 1) {
            List<Route> reduced = new ArrayList<>();
            for (int i = 0; i < populationSize; i++) {
                reduced.add(Selection.tournamentSelection(population, tournamentSize));
            }
            population = reduced;
        }

        Route globalBest = getBestRoute(population);

        for (int generation = 0; generation < maxGenerations; generation++) {
            List<Route> newPopulation = new ArrayList<>();

            while (newPopulation.size() < populationSize) {
                // Seleccion de padres
                Route parent1, parent2;
                if (parentSelectionMethod) {
                    parent1 = Selection.tournamentSelection(population, tournamentSize);
                    parent2 = Selection.tournamentSelection(population, tournamentSize);
                } else {
                    parent1 = Selection.linearRankingSelection(population);
                    parent2 = Selection.linearRankingSelection(population);
                }

                for (Route offspring : getOffSpring(parent1, parent2)) {
                    if (newPopulation.size() < populationSize) {
                        newPopulation.add(offspring);
                    }
                }
            }

            // Seleccion de sobrevivientes, si tenemos en cuenta la población vieja, mezclamos ambas y seleccionamos los mejores
            if (survivalSelectionMethod) {
                List<Route> combined = new ArrayList<>(population);
                combined.addAll(newPopulation);
                population = new ArrayList<>(Selection.roundRobinSelection(combined, tournamentSize, populationSize)); 
            } else {
                List<Route> combined = new ArrayList<>(population);
                combined.addAll(newPopulation);
                population = Selection.fitnessBasedSelection(combined, populationSize);
            }

            Route currentBest = getBestRoute(population);
            if (currentBest.getFitness() > globalBest.getFitness()) {
                globalBest = currentBest;
            }

            // Imprimimos el mejor coste cada X generaciones para ver la evolución
            if (generation % 10 == 0 || generation == maxGenerations - 1) {
                System.out.println("Generacion " + generation
                        + " | Mejor coste: " + String.format("%.2f", currentBest.getTotalCost()));
            }
        }

        return globalBest;
    }

    // Crossover y mutación para generar nuevos individuos a partir de dos padres
    public List<Route> getOffSpring(Route parent1, Route parent2) {
        Route[] children;
        if (random.nextDouble() < crossoverRate) {
            children = getCrossover(parent1, parent2);
        } else {
            children = new Route[] {
                new Route(parent1.getCities(), costMatrix)
            };
        }

        // Mutacion
        List<Route> result = new ArrayList<>();
        for (Route child : children) {
            if (random.nextDouble() < mutationRate) {
                getMutation(child);
            }
            result.add(child);
        }
        return result;
    }

    private Route[] getCrossover(Route parent1, Route parent2) {
        return switch (crossoverMethod) {
            case ORDER -> new Route[] { Crossover.orderCrossover(parent1, parent2) };
            case PMX   -> Crossover.partiallyMappedCrossover(parent1, parent2);
            case DPX   -> new Route[] { Crossover.distancePreservingCrossover(parent1, parent2) };
        };
    }

    private void getMutation(Route route) {
        switch (mutationMethod) {
            case SWAP -> Mutation.swapMutation(route);
            case INVERSION -> Mutation.inversionMutation(route);
            case SHIFT -> Mutation.shiftMutation(route);
            default -> Mutation.swapMutation(route);
        }
    }
}
