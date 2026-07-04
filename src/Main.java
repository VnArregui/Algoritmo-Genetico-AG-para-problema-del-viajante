import ea.EvolutionaryAlgorithm;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import model.City;
import model.Route;

public class Main {
    public static void main(String[] args) throws IOException {
        // Parametros del algoritmo

        // Tamaño de la poblacion
        int populationSize = 100;
        // Para generar más individuos inicialmente, se puede usar un multiplicador
        int initialGenerationsMul = 1; // multiplicador para generar más individuos inicialmente
        // Probabilidades de cruce y mutacion
        double crossoverRate = 1;
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
        int costRange = 149;
        // Elegir metodo de cruce y mutacion
        EvolutionaryAlgorithm.CrossoverMethod crossoverMethod = EvolutionaryAlgorithm.CrossoverMethod.PMX; // Cambiar a OX, PMX o DPX si se desea
        EvolutionaryAlgorithm.MutationMethod mutationMethod = EvolutionaryAlgorithm.MutationMethod.INVERSION; // Cambiar a SWAP, INVERSION o SHIFT si se desea



        // Modo benchmark: usar la instancia real kroA100 (con optimo conocido) en lugar del grafo aleatorio.
        // Poner en false para volver al grafo aleatorio de 'numberOfCities' ciudades.
        boolean useBenchmark = true;
        String benchmarkFile = "data/kroA100.tsp";
        double knownOptimum = 21282; // optimo probado de kroA100

        List<City> cities;
        double[][] costMatrix;
        if (useBenchmark) {
            costMatrix = loadTsplibEuc2d(benchmarkFile);
            numberOfCities = costMatrix.length; // la dimension la fija la instancia
            cities = generateCities(numberOfCities);
        } else {
            cities = generateCities(numberOfCities);
            costMatrix = generateCostMatrix(numberOfCities, costRange);
        }

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
        if (useBenchmark) {
            double gap = (bestRoute.getTotalCost() - knownOptimum) / knownOptimum * 100;
            System.out.println(String.format(Locale.US,
                    "Optimo conocido (%s): %.0f | Gap respecto al optimo: %.2f%%",
                    benchmarkFile, knownOptimum, gap));
        }
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
                double cost = 1 + random.nextInt(costRange); // coste entero entre 1 y 100
                matrix[i][j] = cost;
                matrix[j][i] = cost;
            }
        }
        return matrix;
    }

    /**
     * Carga una instancia TSPLIB con EDGE_WEIGHT_TYPE = EUC_2D y construye la matriz de costes.
     * La distancia es d(i,j) = nint(sqrt(dx^2 + dy^2)) (redondeo al entero mas cercano), igual que
     * usa TSPLIB para calcular los optimos publicados. Las coordenadas solo se usan aqui para
     * construir la matriz; las ciudades siguen siendo ids 0..n-1.
     */
    private static double[][] loadTsplibEuc2d(String path) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(path));
        int n = 0;
        int coordStart = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith("DIMENSION")) {
                n = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
            } else if (line.equals("NODE_COORD_SECTION")) {
                coordStart = i + 1;
                break;
            }
        }
        if (n <= 0 || coordStart < 0) {
            throw new IOException("Formato TSPLIB no valido en " + path);
        }

        double[] x = new double[n];
        double[] y = new double[n];
        for (int k = 0; k < n; k++) {
            String[] parts = lines.get(coordStart + k).trim().split("\\s+");
            int idx = Integer.parseInt(parts[0]) - 1; // TSPLIB es 1-based
            x[idx] = Double.parseDouble(parts[1]);
            y[idx] = Double.parseDouble(parts[2]);
        }

        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double dx = x[i] - x[j];
                double dy = y[i] - y[j];
                double d = Math.round(Math.sqrt(dx * dx + dy * dy)); // nint
                matrix[i][j] = d;
                matrix[j][i] = d;
            }
        }
        return matrix;
    }
}
