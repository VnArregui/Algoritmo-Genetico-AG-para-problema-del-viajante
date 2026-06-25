package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Route {

    private final List<City> cities;
    private final double[][] costMatrix;
    private double fitness;

    public Route(List<City> cities, double[][] costMatrix) {
        this.cities = new ArrayList<>(cities);
        this.costMatrix = costMatrix;
        this.fitness = 0;
    }

    public List<City> getCities() {
        return cities;
    }

    public double[][] getCostMatrix() {
        return costMatrix;
    }

    public double getFitness() {
        if (fitness == 0) {
            fitness = calculateFitness();
        }
        return fitness;
    }

    public double getTotalCost() {
        double totalCost = 0;
        for (int i = 0; i < cities.size() - 1; i++) {
            int from = cities.get(i).getId();
            int to = cities.get(i + 1).getId();
            totalCost += costMatrix[from][to];
        }
        // Return to starting city
        int last = cities.get(cities.size() - 1).getId();
        int first = cities.get(0).getId();
        totalCost += costMatrix[last][first];
        return totalCost;
    }

    private double calculateFitness() {
        return 1.0 / getTotalCost();
    }

    public void invalidateFitness() {
        this.fitness = 0;
    }

    /**
     * Comprueba que la ruta sea una permutacion valida: visita cada id de ciudad
     * exactamente una vez. Util como red de seguridad al probar operadores.
     */
    public boolean isValidPermutation() {
        Set<Integer> seen = new HashSet<>();
        for (City city : cities) {
            if (!seen.add(city.getId())) {
                return false; // id repetido
            }
        }
        return seen.size() == cities.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (City city : cities) {
            sb.append(city.getId()).append(" -> ");
        }
        sb.append(cities.get(0).getId());
        sb.append(" | Coste: ").append(String.format("%.2f", getTotalCost()));
        return sb.toString();
    }
}
