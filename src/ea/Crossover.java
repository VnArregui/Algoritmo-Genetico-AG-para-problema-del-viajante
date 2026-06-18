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

    // Order Crossover (OX)
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

        List<City> childList = Arrays.asList(child);
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

    // Distance Preserving Crossover (DPX).
    // Fragment-building adapted from Epsilon2/Memetic-Algorithm-for-TSP (MIT license).
    // Reconnection follows Freisleben & Merz (1996): prefers P1/P2 successor edges.
    public static Route distancePreservingCrossover(Route parent1, Route parent2) {
        List<City> p1 = parent1.getCities();
        List<City> p2 = parent2.getCities();
        int n = p1.size();

        // Build neighbor array for parent2 (O(1) common-edge lookup)
        int[][] nb2 = new int[n][2];
        for (int i = 0; i < n; i++) {
            int id = p2.get(i).getId();
            nb2[id][0] = p2.get((i - 1 + n) % n).getId();
            nb2[id][1] = p2.get((i + 1) % n).getId();
        }

        // Form fragments by scanning P1:
        // each fragment is a maximal sequence where consecutive edges are common
        List<List<Integer>> fragments = new ArrayList<>();
        int k = 0;
        while (k < n) {
            List<Integer> frag = new ArrayList<>();
            frag.add(p1.get(k).getId());
            k++;
            while (k < n) {
                int prev = p1.get(k - 1).getId();
                int curr = p1.get(k).getId();
                if (curr == nb2[prev][0] || curr == nb2[prev][1]) {
                    frag.add(curr);
                    k++;
                } else break;
            }
            fragments.add(frag);
        }

        // Build city-by-ID lookup and successor maps
        City[] cityById = new City[n];
        int[] succ1 = new int[n];
        int[] succ2 = new int[n];
        for (int i = 0; i < n; i++) {
            City c = p1.get(i);
            cityById[c.getId()] = c;
            succ1[c.getId()] = p1.get((i + 1) % n).getId();
            succ2[p2.get(i).getId()] = p2.get((i + 1) % n).getId();
        }

        // Reconnect fragments greedily into a complete tour
        boolean[] used = new boolean[fragments.size()];
        List<City> child = new ArrayList<>();
        int current = 0;

        for (int step = 0; step < fragments.size(); step++) {
            used[current] = true;
            List<Integer> frag = fragments.get(current);
            for (int id : frag) child.add(cityById[id]);

            if (step == fragments.size() - 1) break;

            int tail = frag.get(frag.size() - 1);

            // Prefer fragment whose head matches P1/P2 successor of tail
            current = -1;
            for (int j = 0; j < fragments.size(); j++) {
                if (used[j]) continue;
                int head = fragments.get(j).get(0);
                if (head == succ1[tail] || head == succ2[tail]) {
                    current = j;
                    break;
                }
            }

            // If not found, prefer fragment whose tail matches (reverse it)
            if (current == -1) {
                for (int j = 0; j < fragments.size(); j++) {
                    if (used[j]) continue;
                    List<Integer> f = fragments.get(j);
                    int ft = f.get(f.size() - 1);
                    if (ft == succ1[tail] || ft == succ2[tail]) {
                        List<Integer> rev = new ArrayList<>(f.size());
                        for (int idx = f.size() - 1; idx >= 0; idx--)
                            rev.add(f.get(idx));
                        fragments.set(j, rev);
                        current = j;
                        break;
                    }
                }
            }

            // Fallback: any unused fragment
            if (current == -1) {
                for (int j = 0; j < fragments.size(); j++) {
                    if (!used[j]) { current = j; break; }
                }
            }
        }

        return new Route(child, parent1.getCostMatrix());
    }

    private static City resolveMapping(City city, Map<Integer, Integer> crossMap) {
        if (!crossMap.containsKey(city.getId())) {
            return city;
        } 
        return new City(crossMap.get(city.getId()));
    }
}
