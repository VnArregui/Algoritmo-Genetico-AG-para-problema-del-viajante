package ea;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import model.City;
import model.Route;

public class Mutation {
    private Mutation() {
        /* This utility class should not be instantiated */
    }


    private static final Random random = new Random();

    /**
     * Swap mutation: randomly swaps two cities in the route.
     */
    public static void swapMutation(Route route) {
        List<City> cities = route.getCities();
        int index1 = random.nextInt(cities.size());
        int index2 = random.nextInt(cities.size());
        Collections.swap(cities, index1, index2);
        route.invalidateFitness();
    }

    /**
     * Inversion mutation: reverses a random sub-section of the route.
     */
    public static void inversionMutation(Route route) {
        List<City> cities = route.getCities();
        int start = random.nextInt(cities.size());
        int end = random.nextInt(cities.size());
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }
        while (start < end) {
            Collections.swap(cities, start, end);
            start++;
            end--;
        }
        route.invalidateFitness();
    }
}
