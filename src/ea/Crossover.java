package ea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import model.City;
import model.Route;

public class Crossover {
    private Crossover() {}

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

    // Partially Mapped Crossover (PMX).
    public static Route[] partiallyMappedCrossover(Route parent1, Route parent2) {
        List<City> p1 = parent1.getCities();
        List<City> p2 = parent2.getCities();
        int size = p1.size();
        Map<Integer, Integer> crossMap = new HashMap<>();

        City[] child1 = new City[size];
        City[] child2 = new City[size];

        int start = random.nextInt(size);
        int end = random.nextInt(size);
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        for (int i = start; i <= end; i++) {
            child1[i] = p1.get(i);
            child2[i] = p2.get(i);
            crossMap.put(p1.get(i).getId(), p2.get(i).getId());
        }
        
        for (int i = 0; i < start; i++) {
            child1[i] = resolveMapping(p2.get(i), crossMap);
            child2[i] = resolveMapping(p1.get(i), crossMap);
        }

        for (int i = end + 1; i < size; i++) {
            child1[i] = resolveMapping(p2.get(i), crossMap);
            child2[i] = resolveMapping(p1.get(i), crossMap);
        }

        List<City> child1List = new ArrayList<>();
        List<City> child2List = new ArrayList<>();
        
        child1List.addAll(Arrays.asList(child1));
        child2List.addAll(Arrays.asList(child2));

        return new Route[] {
            new Route(child1List, parent1.getCostMatrix()),
            new Route(child2List, parent1.getCostMatrix())
        };
    }

    private static City resolveMapping(City city, Map<Integer, Integer> crossMap) {
        if (!crossMap.containsKey(city.getId())) {
            return city;
        } 
        return new City(crossMap.get(city.getId()));
    }
}
