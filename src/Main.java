import ea.EvolutionaryAlgorithm;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.City;
import model.Route;

public class Main {
    public static void main(String[] args) {
        // Parametros del algoritmo

        // Tamaño de la poblacion
        int populationSize = 100;
        // Para generar más individuos inicialmente, se puede usar un multiplicador
        int initialGenerationsMul = 5; // multiplicador para generar más individuos inicialmente
        // Probabilidades de cruce y mutacion
        double crossoverRate = 0.9;
        double mutationRate = 0.1;
        // Tamaño del torneo para la seleccion de padres
        int tournamentSize = 5;
        // Cantidad de generaciones a ejecutar
        int maxGenerations = 2000;
        // Metodo de seleccion de padres: true para torneo, false para ranking de mapeo lineal
        boolean parentSelectionMethod = true;
        // Metodo de seleccion de sobrevivientes: true para round robin, false para fitness-based
        boolean survivalSelectionMethod = true;
        // Generar ciudades y matriz de costes
        int numberOfCities = 100;
        // Rango de costes entre ciudades (1 a 100) (Se le suma 1 para evitar costes de 0)
        int costRange = 99;
        // Elegir metodo de cruce y mutacion
        EvolutionaryAlgorithm.CrossoverMethod crossoverMethod = EvolutionaryAlgorithm.CrossoverMethod.PMX; // Cambiar a OX, PMX o DPX si se desea
        EvolutionaryAlgorithm.MutationMethod mutationMethod = EvolutionaryAlgorithm.MutationMethod.SWAP; // Cambiar a SWAP, INVERSION o SHIFT si se desea



        List<City> cities = generateCities(numberOfCities);
        double[][] costMatrix = generateCostMatrix(numberOfCities, costRange);

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
                cities, costMatrix, populationSize, initialGenerationsMul, 
                crossoverRate, mutationRate, tournamentSize, maxGenerations, parentSelectionMethod, 
                survivalSelectionMethod, crossoverMethod, mutationMethod
            );

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

    private static double[][] generateCostMatrix(int size, int costRange) {
        Random random = new Random(42);
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                double cost = 1 + random.nextDouble() * costRange; // coste entre 1 y 100
                matrix[i][j] = cost;
                matrix[j][i] = cost;
            }
        }
        return matrix;
    }
}
