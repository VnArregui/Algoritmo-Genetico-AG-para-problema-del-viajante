package ea;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import model.Route;

public class Selection {
    private Selection() {}
    private static final Random random = new Random();

    // Seleccion por torneo, elige al azar un grupo de individuos y selecciona el mejor entre ellos
    public static Route tournamentSelection(List<Route> population, int tournamentSize) {
        List<Route> tournament = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++) {
            int index = random.nextInt(population.size());
            tournament.add(population.get(index));
        }
        Route best = tournament.get(0);
        for (Route route : tournament) {
            if (route.getFitness() > best.getFitness()) {
                best = route;
            }
        }
        return best;
    }

    // Seleccion por torneo Round Robin, se elige la solucion con mas puntajes en enfrentamientos directos
    // Como se usa para seleccionar sobrevivientes, devuelve la lista de los seleccionados, no solo uno.
    public static List<Route> roundRobinSelection(List<Route> population, int tournamentSize, int survivors) {
        int n = population.size();
        int[] wins = new int[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < tournamentSize; j++) {
                int opponent = random.nextInt(n);
                while (opponent == i) {
                    opponent = random.nextInt(n);
                }
                if (population.get(i).getFitness() > population.get(opponent).getFitness()) {
                    wins[i]++;
                }
            }
        }
        List<Route> selected = new ArrayList<>();
        List<Route> sorted = new ArrayList<>(population);
        Map<Route, Integer> indexMap = new HashMap<>();

        for (int i = 0; i < n; i++) indexMap.put(population.get(i), i);
        sorted.sort(Comparator.comparingInt((Route r) -> wins[indexMap.get(r)]).reversed());

        for (int i = 0; i < survivors && i < sorted.size(); i++) {
            selected.add(sorted.get(i));
        }
        return selected;
    }



    // Seleccion por ranking de mapeo lineal: asigna probabilidades según el orden de fitness
    // SELECTION_PRESSURE controla cuánto más probable es elegir al mejor respecto al peor (1.0 = uniforme, 2.0 = el mejor 2 veces más probable que el peor)
    private static final double SELECTION_PRESSURE = 2.0;

    public static Route linearRankingSelection(List<Route> population) {
        List<Route> sorted = new ArrayList<>(population);
        sorted.sort(Comparator.comparingDouble(Route::getFitness)); // 1 es el peor, n el mejor
        int n = sorted.size();
        double sMin = 2.0 - SELECTION_PRESSURE;
        double sMax = SELECTION_PRESSURE;
        double spin = random.nextDouble();
        double cumulative = 0.0;
        for (int i = 0; i < n; i++) {
            double rank = i + 1.0;
            cumulative += (1.0 / n) * (sMin + (sMax - sMin) * ((rank - 1) / (n - 1)));
            if (spin <= cumulative) {
                return sorted.get(i);
            }
        }
        return sorted.get(n - 1); // retorna el mejor si falla
    }

    // Seleecion de sobrevivientes por fitness, se ordena la poblacion por fitness y se elige los mejores
    public static final List<Route> fitnessBasedSelection(List<Route> population, int survivors) {
        List<Route> sorted = new ArrayList<>(population);
        sorted.sort(Comparator.comparingDouble(Route::getFitness).reversed());
        List<Route> selected = new ArrayList<>();
        for (int i = 0; i < survivors && i < sorted.size(); i++) {
            selected.add(sorted.get(i));
        }
        return selected;
    }
}
