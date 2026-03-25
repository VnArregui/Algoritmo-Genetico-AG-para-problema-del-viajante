# AE-1: Travelling Salesman Problem — Evolutionary Algorithm

Solving the **Travelling Salesman Problem (TSP)** using an **Evolutionary Algorithm (EA)** implemented in Java.

## Project Structure

```
src/
├── Main.java                          Entry point
├── model/
│   ├── City.java                      City with (x, y) coordinates
│   └── Route.java                     Candidate solution (ordered list of cities)
└── ea/
    ├── EvolutionaryAlgorithm.java     EA engine (population loop, elitism)
    ├── Selection.java                 Tournament selection
    ├── Crossover.java                 Order Crossover (OX)
    └── Mutation.java                  Swap & inversion mutation
```

## How to Compile & Run

```bash
cd src
javac -d ../out model/*.java ea/*.java Main.java
cd ../out
java Main
```

## EA Parameters (configurable in Main.java)

| Parameter        | Default |
|------------------|---------|
| Population size  | 100     |
| Crossover rate   | 0.9     |
| Mutation rate    | 0.1     |
| Tournament size  | 5       |
| Max generations  | 1000    |