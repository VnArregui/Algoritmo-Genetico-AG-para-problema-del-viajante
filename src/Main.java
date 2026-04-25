import ea.EvolutionaryAlgorithm;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.City;
import model.Route;

public class Main {

    public static void main(String[] args) {
        // Parametros del algoritmo
        int populationSize = 100;
        int initialGenerationsMul = 5; // multiplicador para generar más individuos inicialmente
        double crossoverRate = 0.9;
        double mutationRate = 0.1;
        int tournamentSize = 5;
        int maxGenerations = 2000;
        boolean parentSelectionMethod = true; // true: torneo, false: ranking de mapeo lineal

        // Generar ciudades y matriz de costes
        int numberOfCities = 100;
        List<City> cities = generateCities(numberOfCities);
        double[][] costMatrix = generateCostMatrix(numberOfCities);

        System.out.println("=== Problema del viajante - Algoritmos Evolutivos ===");
        System.out.println("Ciudades: " + numberOfCities);
        System.out.println("Tamaño poblacion: " + populationSize);
        System.out.println("Probabilidad de cruze: " + crossoverRate);
        System.out.println("Probabilidad de mutacion: " + mutationRate);
        System.out.println("Tamaño de torneo: " + tournamentSize);
        System.out.println("Cantidad de generaciones: " + maxGenerations);
        System.out.println("===========================================================\n");

        // Ejecutar el algoritmo evolutivo
        EvolutionaryAlgorithm ea = new EvolutionaryAlgorithm(
                cities, costMatrix, populationSize, initialGenerationsMul, crossoverRate, mutationRate, tournamentSize, maxGenerations, parentSelectionMethod);

        Route bestRoute = ea.run();

        System.out.println("\n=== Mejor ruta encontrada ===");
        System.out.println(bestRoute);
    }

    private static List<City> generateCities(int count) {
        List<City> cities = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cities.add(new City(i));
        }
        return cities;
    }

    private static double[][] generateCostMatrix(int size) {
        Random random = new Random(42);
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                double cost = 1 + random.nextDouble() * 99; // coste entre 1 y 100
                matrix[i][j] = cost;
                matrix[j][i] = cost; // simetrico
            }
        }
        return matrix;
    }
}
