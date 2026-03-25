package ea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import model.City;
import model.Route;

public class EvolutionaryAlgorithm {

    private final int populationSize;
    private final double crossoverRate;
    private final double mutationRate;
    private final int tournamentSize;
    private final int maxGenerations;
    private final List<City> cities;
    private final double[][] costMatrix;
    private final Random random;

    public EvolutionaryAlgorithm(List<City> cities, double[][] costMatrix, int populationSize,
                                  double crossoverRate, double mutationRate, int tournamentSize,
                                  int maxGenerations) {
        this.cities = cities;
        this.costMatrix = costMatrix;
        this.populationSize = populationSize;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.tournamentSize = tournamentSize;
        this.maxGenerations = maxGenerations;
        this.random = new Random();
    }

    /**
     * Initializes a random population of routes.
     */
    public List<Route> initializePopulation() {
        List<Route> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
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
        Route globalBest = getBestRoute(population);

        for (int generation = 0; generation < maxGenerations; generation++) {
            List<Route> newPopulation = new ArrayList<>();

            // Elitism: carry the best individual to the next generation
            newPopulation.add(getBestRoute(population));

            while (newPopulation.size() < populationSize) {
                // Selection
                Route parent1 = Selection.tournamentSelection(population, tournamentSize);
                Route parent2 = Selection.tournamentSelection(population, tournamentSize);

                // Crossover
                Route offspring;
                if (random.nextDouble() < crossoverRate) {
                    offspring = Crossover.orderCrossover(parent1, parent2);
                } else {
                    offspring = new Route(parent1.getCities(), costMatrix);
                }

                // Mutation
                if (random.nextDouble() < mutationRate) {
                    Mutation.swapMutation(offspring);
                }

                newPopulation.add(offspring);
            }

            population = newPopulation;

            Route currentBest = getBestRoute(population);
            if (currentBest.getFitness() > globalBest.getFitness()) {
                globalBest = currentBest;
            }

            // Print progress every 100 generations
            if (generation % 100 == 0 || generation == maxGenerations - 1) {
                System.out.println("Generacion " + generation
                        + " | Mejor coste: " + String.format("%.2f", currentBest.getTotalCost()));
            }
        }

        return globalBest;
    }
}
