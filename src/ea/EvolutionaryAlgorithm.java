package ea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import model.City;
import model.Route;

public class EvolutionaryAlgorithm {

    private final int populationSize;
    private final int initialPopulationMul;
    private final double crossoverRate;
    private final double mutationRate;
    private final int tournamentSize;
    private final int maxGenerations;
    private final List<City> cities;
    private final double[][] costMatrix;
    private boolean parentSelectionMethod; // true: torneo, false: ranking de mapeo lineal
    private final Random random;

    public EvolutionaryAlgorithm(List<City> cities, double[][] costMatrix, int populationSize, int initialPopulationMul,
                                  double crossoverRate, double mutationRate, int tournamentSize,
                                  int maxGenerations, boolean parentSelectionMethod) {
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

            // Elitismo: siempre mantenemos el mejor individuo de la generación anterior
            newPopulation.add(getBestRoute(population));

            while (newPopulation.size() < populationSize) {
                // Seleccion de padres
                if (parentSelectionMethod) {
                    Route parent1 = Selection.tournamentSelection(population, tournamentSize);
                    Route parent2 = Selection.tournamentSelection(population, tournamentSize);
                    newPopulation.add(getOffSpring(parent1, parent2));
                }

                else {
                    Route parent1 = Selection.linearRankingSelection(population);
                    Route parent2 = Selection.linearRankingSelection(population);
                    newPopulation.add(getOffSpring(parent1, parent2));
                }
            }

            population = newPopulation;

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

    // Crossover y mutación para generar un nuevo individuo a partir de dos padres
    public Route getOffSpring(Route parent1, Route parent2) {
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
        return offspring;
    }
}
