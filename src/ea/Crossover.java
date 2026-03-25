package ea;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.City;
import model.Route;

public class Crossover {
    private Crossover() {
        /* This utility class should not be instantiated */
    }

    private static final Random random = new Random();

    /**
     * Order Crossover (OX): a common crossover operator for permutation-based
     * representations like TSP.
     */
    public static Route orderCrossover(Route parent1, Route parent2) {
        List<City> p1 = parent1.getCities();
        List<City> p2 = parent2.getCities();
        int size = p1.size();

        City[] child = new City[size];

        // Select a random sub-route from parent1
        int start = random.nextInt(size);
        int end = random.nextInt(size);
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        // Copy the sub-route from parent1
        for (int i = start; i <= end; i++) {
            child[i] = p1.get(i);
        }

        // Fill the remaining positions with cities from parent2 (in order)
        int currentIndex = (end + 1) % size;
        for (int i = 0; i < size; i++) {
            int p2Index = (end + 1 + i) % size;
            City candidate = p2.get(p2Index);

            boolean alreadyInChild = false;
            for (City c : child) {
                if (c != null && c.getId() == candidate.getId()) {
                    alreadyInChild = true;
                    break;
                }
            }

            if (!alreadyInChild) {
                child[currentIndex] = candidate;
                currentIndex = (currentIndex + 1) % size;
            }
        }

        List<City> childList = new ArrayList<>();
        for (City c : child) {
            childList.add(c);
        }
        return new Route(childList, parent1.getCostMatrix());
    }
}
