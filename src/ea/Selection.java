package ea;

import java.util.ArrayList;
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
}
