package model;

import ea.EvolutionaryAlgorithm;
import ea.EvolutionaryAlgorithm.CrossoverMethod;
import ea.EvolutionaryAlgorithm.MutationMethod;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

// ############# Test implementado con Claude Opus 4.8 de Anthropic ############# 
/**
 * Banco de pruebas para el estudio: ejecuta cada configuracion de parametros
 * varias veces (la busqueda es estocastica) y reporta el mejor / promedio / peor
 * coste, la desviacion estandar y el tiempo medio. Imprime una tabla por consola
 * y vuelca los resultados a 'test-results.csv'.
 */
public class AlgorithmTest {

    // Numero de ejecuciones por configuracion (la busqueda es estocastica)
    private static final int RUNS_PER_CONFIG = 20;
    private static final String CSV_FILE = "test-results.csv";

    // Grafo unico y compartido: TODAS las configuraciones se prueban sobre el mismo grafo.
    // Cambiar estos valores cambia el grafo para todos los casos.
    private static final int GRAPH_SIZE = 100;
    private static final int GRAPH_COST_RANGE = 149;

    // Modo benchmark: usar una instancia real de TSPLIB con optimo conocido en lugar del grafo
    // aleatorio. Permite medir la distancia al optimo (gap %) del algoritmo.
    private static final boolean USE_BENCHMARK = true;
    private static final String BENCHMARK_FILE = "data/kroA100.tsp";
    private static final String BENCHMARK_OPT_TOUR = "data/kroA100.opt.tour";
    private static final double KNOWN_OPTIMUM = 21282; // optimo probado de kroA100

    /** Configuracion completa de parametros para una ejecucion del AE. */
    static class Config {
        final String label;
        final int populationSize;
        final int initialPopulationMul;
        final double crossoverRate;
        final double mutationRate;
        final int tournamentSize;
        final int maxGenerations;
        final boolean parentSelectionMethod;   // true: torneo, false: ranking lineal
        final boolean survivalSelectionMethod; // true: round robin, false: fitness-based
        final CrossoverMethod crossoverMethod;
        final MutationMethod mutationMethod;

        Config(String label, int populationSize, int initialPopulationMul, double crossoverRate,
               double mutationRate, int tournamentSize, int maxGenerations, boolean parentSelectionMethod,
               boolean survivalSelectionMethod, CrossoverMethod crossoverMethod, MutationMethod mutationMethod) {
            this.label = label;
            this.populationSize = populationSize;
            this.initialPopulationMul = initialPopulationMul;
            this.crossoverRate = crossoverRate;
            this.mutationRate = mutationRate;
            this.tournamentSize = tournamentSize;
            this.maxGenerations = maxGenerations;
            this.parentSelectionMethod = parentSelectionMethod;
            this.survivalSelectionMethod = survivalSelectionMethod;
            this.crossoverMethod = crossoverMethod;
            this.mutationMethod = mutationMethod;
        }
    }

    /** Estadisticas agregadas de las ejecuciones de una configuracion. */
    static class Result {
        double best;
        double average;
        double worst;
        double stdDev;
        double avgTimeMs;
        boolean allValid;
    }

    // Comparacion contra el optimo conocido (kroA100): mejor PMX vs mejor DPX.
    // Base: pob=100, mul=1, torneo=5, 2000 generaciones, cruce=1.0, supervivientes=round robin.
    private static final List<Config> CONFIGS = List.of(
        new Config("PMX+INV", 100, 1, 1.0, 0.30, 5, 2000, true, true, CrossoverMethod.PMX, MutationMethod.INVERSION),
        new Config("DPX+INV", 100, 1, 1.0, 0.10, 5, 2000, true, true, CrossoverMethod.DPX, MutationMethod.INVERSION)
    );

    public static void main(String[] args) throws IOException {
        List<City> cities;
        double[][] costMatrix;

        if (USE_BENCHMARK) {
            costMatrix = loadTsplibEuc2d(BENCHMARK_FILE);
            cities = generateCities(costMatrix.length); // ciudades 0..n-1, la geometria esta en la matriz
            // Validacion del cargador: el coste del tour optimo bajo NUESTRA matriz debe dar el optimo publicado
            Route optimalTour = loadOptimalTour(BENCHMARK_OPT_TOUR, costMatrix);
            System.out.println("Instancia benchmark: " + BENCHMARK_FILE + " (" + costMatrix.length + " ciudades)");
            System.out.println("Optimo publicado: " + KNOWN_OPTIMUM
                    + " | Coste del tour optimo con nuestra matriz: " + String.format(Locale.US, "%.0f", optimalTour.getTotalCost())
                    + (optimalTour.getTotalCost() == KNOWN_OPTIMUM ? "  [OK]" : "  [ERROR: no coincide]") + "\n");
        } else {
            costMatrix = generateCostMatrix(GRAPH_SIZE, GRAPH_COST_RANGE);
            cities = generateCities(GRAPH_SIZE);
            System.out.println("Grafo compartido: " + GRAPH_SIZE + " ciudades, rango de coste " + GRAPH_COST_RANGE + "\n");
        }

        List<Result> results = new ArrayList<>();
        for (Config config : CONFIGS) {
            System.out.println("Ejecutando " + config.label + " (" + RUNS_PER_CONFIG + " ejecuciones)...");
            results.add(runConfig(config, cities, costMatrix));
        }

        printTable(results);
        writeCsv(results);
    }

    /** Ejecuta una configuracion RUNS_PER_CONFIG veces sobre el grafo compartido y agrega las estadisticas. */
    private static Result runConfig(Config config, List<City> cities, double[][] costMatrix) {
        double[] costs = new double[RUNS_PER_CONFIG];
        double totalTimeMs = 0;
        boolean allValid = true;

        for (int run = 0; run < RUNS_PER_CONFIG; run++) {
            EvolutionaryAlgorithm ea = new EvolutionaryAlgorithm(
                cities, costMatrix, config.populationSize, config.initialPopulationMul,
                config.crossoverRate, config.mutationRate, config.tournamentSize, config.maxGenerations,
                config.parentSelectionMethod, config.survivalSelectionMethod,
                config.crossoverMethod, config.mutationMethod
            );

            long start = System.nanoTime();
            Route best = runQuiet(ea);
            long elapsed = System.nanoTime() - start;

            costs[run] = best.getTotalCost();
            totalTimeMs += elapsed / 1_000_000.0;
            if (!best.isValidPermutation()) {
                allValid = false;
            }
        }

        return summarize(costs, totalTimeMs / RUNS_PER_CONFIG, allValid);
    }

    /** Ejecuta el AE silenciando su salida por consola (imprime cada 10 generaciones). */
    private static Route runQuiet(EvolutionaryAlgorithm ea) {
        PrintStream original = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        try {
            return ea.run();
        } finally {
            System.setOut(original);
        }
    }

    private static Result summarize(double[] costs, double avgTimeMs, boolean allValid) {
        Result r = new Result();
        r.best = costs[0];
        r.worst = costs[0];
        double sum = 0;
        for (double c : costs) {
            if (c < r.best) r.best = c;
            if (c > r.worst) r.worst = c;
            sum += c;
        }
        r.average = sum / costs.length;

        double sqDiff = 0;
        for (double c : costs) {
            sqDiff += (c - r.average) * (c - r.average);
        }
        r.stdDev = Math.sqrt(sqDiff / costs.length);
        r.avgTimeMs = avgTimeMs;
        r.allValid = allValid;
        return r;
    }

    private static void printTable(List<Result> results) {
        System.out.println("\n=== Resultados (" + RUNS_PER_CONFIG + " ejecuciones por caso) ===");
        String header = String.format("%-8s %-6s %-6s %-12s %-6s %-7s %-6s %-6s %-10s %-9s %-10s %-10s %-10s %-9s %-10s %-9s %-9s %-7s",
                "Caso", "Cruce", "%X", "Mutacion", "%M", "Pob", "Torn", "Gen", "Sel.Padres", "Superv.",
                "Mejor", "Promedio", "Peor", "DesvEst", "Tiempo(ms)", "GapMej%", "GapProm%", "Valido");
        System.out.println(header);
        System.out.println("-".repeat(header.length()));

        for (int i = 0; i < CONFIGS.size(); i++) {
            Config c = CONFIGS.get(i);
            Result r = results.get(i);
            System.out.println(String.format(Locale.US,
                    "%-8s %-6s %-6.2f %-12s %-6.2f %-7d %-6d %-6d %-10s %-9s %-10.2f %-10.2f %-10.2f %-9.2f %-10.1f %-9s %-9s %-7s",
                    c.label, c.crossoverMethod, c.crossoverRate, c.mutationMethod, c.mutationRate,
                    c.populationSize, c.tournamentSize, c.maxGenerations,
                    c.parentSelectionMethod ? "torneo" : "ranking",
                    c.survivalSelectionMethod ? "roundRob" : "fitness",
                    r.best, r.average, r.worst, r.stdDev, r.avgTimeMs,
                    gapString(r.best), gapString(r.average), r.allValid ? "si" : "NO"));
        }
    }

    /** Gap porcentual respecto al optimo conocido, o "-" si no estamos en modo benchmark. */
    private static String gapString(double value) {
        if (!USE_BENCHMARK) {
            return "-";
        }
        return String.format(Locale.US, "%.2f", (value - KNOWN_OPTIMUM) / KNOWN_OPTIMUM * 100);
    }

    private static void writeCsv(List<Result> results) {
        try (FileWriter w = new FileWriter(CSV_FILE)) {
            w.write("caso,crossover,crossoverRate,mutation,mutationRate,populationSize,initialPopulationMul,"
                    + "tournamentSize,maxGenerations,parentSelection,survivalSelection,numberOfCities,costRange,"
                    + "runs,best,average,worst,stdDev,avgTimeMs,optimum,gapBestPct,gapAvgPct,allValid\n");
            String optimum = USE_BENCHMARK ? String.format(Locale.US, "%.0f", KNOWN_OPTIMUM) : "-";
            for (int i = 0; i < CONFIGS.size(); i++) {
                Config c = CONFIGS.get(i);
                Result r = results.get(i);
                w.write(String.format(Locale.US,
                        "%s,%s,%.2f,%s,%.2f,%d,%d,%d,%d,%s,%s,%d,%d,%d,%.4f,%.4f,%.4f,%.4f,%.2f,%s,%s,%s,%s\n",
                        c.label, c.crossoverMethod, c.crossoverRate, c.mutationMethod, c.mutationRate,
                        c.populationSize, c.initialPopulationMul, c.tournamentSize, c.maxGenerations,
                        c.parentSelectionMethod ? "torneo" : "ranking",
                        c.survivalSelectionMethod ? "roundRobin" : "fitness",
                        GRAPH_SIZE, GRAPH_COST_RANGE, RUNS_PER_CONFIG,
                        r.best, r.average, r.worst, r.stdDev, r.avgTimeMs,
                        optimum, gapString(r.best), gapString(r.average), r.allValid ? "si" : "no"));
            }
            System.out.println("\nResultados guardados en " + CSV_FILE);
        } catch (IOException e) {
            System.out.println("\nNo se pudo escribir el CSV: " + e.getMessage());
        }
    }

    // Generacion del problema (mismo metodo y semilla que Main para reproducibilidad)
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
                double cost = 1 + random.nextInt(costRange); // coste entero entre 1 y costRange
                matrix[i][j] = cost;
                matrix[j][i] = cost; // simetrico
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

    /**
     * Carga el tour optimo de un fichero TSPLIB .opt.tour (TOUR_SECTION, ids 1-based terminados
     * en -1) y devuelve la ruta correspondiente construida con la matriz dada.
     */
    private static Route loadOptimalTour(String path, double[][] costMatrix) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(path));
        List<City> tour = new ArrayList<>();
        boolean inTour = false;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.equals("TOUR_SECTION")) {
                inTour = true;
            } else if (inTour) {
                if (line.equals("-1") || line.equals("EOF") || line.isEmpty()) {
                    break;
                }
                tour.add(new City(Integer.parseInt(line) - 1)); // 1-based -> 0-based
            }
        }
        return new Route(tour, costMatrix);
    }
}
