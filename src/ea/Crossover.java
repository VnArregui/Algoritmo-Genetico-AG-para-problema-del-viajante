package ea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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

        Map<Integer, Integer> map1 = new HashMap<>();
        Map<Integer, Integer> map2 = new HashMap<>();
        Set<Integer> segment1 = new HashSet<>(); // ids copiados en child1 (segmento de p1)
        Set<Integer> segment2 = new HashSet<>(); // ids copiados en child2 (segmento de p2)

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
            map1.put(p1.get(i).getId(), p2.get(i).getId());
            map2.put(p2.get(i).getId(), p1.get(i).getId());
            segment1.add(p1.get(i).getId());
            segment2.add(p2.get(i).getId());
        }

        for (int i = 0; i < size; i++) {
            if (i >= start && i <= end) continue;
            child1[i] = resolveMapping(p2.get(i), map1, segment1);
            child2[i] = resolveMapping(p1.get(i), map2, segment2);
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


    // Implementado con asistencia de Claude Opus 4.8
    // Distance Preserving Crossover (DPX) - version de libro de texto (Freisleben & Merz).
    // Fragment-building y reconexion adaptados de Epsilon2/Memetic-Algorithm-for-TSP (MIT license):
    // se preservan las aristas comunes a ambos padres y los fragmentos se reconectan por vecino
    // mas cercano (getNearestCity), introduciendo aristas nuevas en lugar de reutilizar las de los
    // padres. Nota: esta version practica no prohibe estrictamente reintroducir una arista de un
    // padre (la variante teorica si; Merz indica que el backtracking para forzarlo "no vale la pena").
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

        // Build city-by-ID lookup
        City[] cityById = new City[n];
        for (int i = 0; i < n; i++) {
            City c = p1.get(i);
            cityById[c.getId()] = c;
        }

        // Reconnect fragments by nearest neighbour (vecino mas cercano), como en Epsilon2.
        // Desde el extremo actual del recorrido se elige el fragmento no usado cuyo extremo
        // (cabeza o cola) este mas cerca segun la matriz de costes, introduciendo aristas nuevas.
        double[][] costMatrix = parent1.getCostMatrix();
        boolean[] used = new boolean[fragments.size()];
        List<City> child = new ArrayList<>();

        // Empezamos con el primer fragmento
        used[0] = true;
        for (int id : fragments.get(0)) child.add(cityById[id]);
        int end = fragments.get(0).get(fragments.get(0).size() - 1);

        for (int placed = 1; placed < fragments.size(); placed++) {
            int bestFrag = -1;
            boolean connectByTail = false;
            double bestDist = Double.MAX_VALUE;

            for (int j = 0; j < fragments.size(); j++) {
                if (used[j]) continue;
                List<Integer> f = fragments.get(j);
                int head = f.get(0);
                int tail = f.get(f.size() - 1);
                if (costMatrix[end][head] < bestDist) {
                    bestDist = costMatrix[end][head];
                    bestFrag = j;
                    connectByTail = false;
                }
                if (costMatrix[end][tail] < bestDist) {
                    bestDist = costMatrix[end][tail];
                    bestFrag = j;
                    connectByTail = true;
                }
            }

            used[bestFrag] = true;
            List<Integer> f = fragments.get(bestFrag);
            // Si el extremo mas cercano era la cola, invertimos el fragmento para que su
            // cabeza (ahora el antiguo extremo cercano) conecte con el recorrido actual.
            if (connectByTail) {
                Collections.reverse(f);
            }
            for (int id : f) child.add(cityById[id]);
            end = f.get(f.size() - 1);
        }

        return new Route(child, costMatrix);
    }

    private static City resolveMapping(City city, Map<Integer, Integer> map, Set<Integer> segment) {
        int id = city.getId();
        while (segment.contains(id)) {
            id = map.get(id);
        }
        return new City(id);
    }
}
