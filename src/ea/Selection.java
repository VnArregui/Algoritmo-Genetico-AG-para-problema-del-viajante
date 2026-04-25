package ea;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import model.Route;

public class Selection {
    private Selection() {
        /* This utility class should not be instantiated */
    }


    private static final Random random = new Random();

    /**
     * Tournament selection: picks 'tournamentSize' random individuals
     * and returns the one with the best fitness.
     */
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
}
