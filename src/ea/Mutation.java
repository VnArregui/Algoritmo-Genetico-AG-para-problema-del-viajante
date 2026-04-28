package ea;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import model.City;
import model.Route;

public class Mutation {
    private Mutation() {}
    private static final Random random = new Random();

    // Mutacion por intercambio, selecciona dos ciudades al azar y las intercambia
    public static void swapMutation(Route route) {
        List<City> cities = route.getCities();
        int index1 = random.nextInt(cities.size());
        int index2 = random.nextInt(cities.size());
        Collections.swap(cities, index1, index2);
        route.invalidateFitness();
    }

    // Mutacion por inversión, selecciona un segmento de la ruta y lo invierte
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

    // Mutacion por desplazamiento, selecciona una ciudad y la mueve a otra posición
    public static void shiftMutation(Route route) {
        List<City> cities = route.getCities();
        int fromIndex = random.nextInt(cities.size());
        int toIndex = random.nextInt(cities.size());
        while (toIndex == fromIndex) {
            toIndex = random.nextInt(cities.size());
        }
        City city = cities.remove(fromIndex);
        cities.add(toIndex, city);
        route.invalidateFitness();
    }
}
