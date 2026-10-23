# Assignment 3 — City Network & Job Scheduler

**Student:** Zhumabay Aikyn  
**Group:** SE-2523  
**Course:** Design and Analysis of Algorithms  
**Language:** Java 17  
**Build Tool:** Maven

## Project Description

This project implements graph algorithms for a city road network and a job scheduling system.

The main goal is to find minimum spanning trees, strongly connected components, topological ordering, and shortest paths in directed acyclic graphs (DAGs).

The project also compares algorithm performance using operation counters and tests correctness.

## Implemented Algorithms

1. **DSU** — Union by size and path compression.
2. **Kruskal** — Finds a minimum spanning tree using DSU.
3. **Prim** — Finds a minimum spanning tree using a priority queue.
4. **Kosaraju** — Finds strongly connected components using iterative DFS.
5. **Kahn** — Performs topological sorting and detects cycles.
6. **DAG Shortest Paths** — Finds shortest paths in a DAG, including negative edge weights.

### Bonus Tasks

- **Tarjan's Algorithm (+10%)** — Finds strongly connected components in one DFS pass.
- **Condensation & Critical Path (+5%)** — Builds a condensation DAG and finds its longest path.

## Project Structure

- `src/main/java/citygraph/` — Graph, DSU, metrics, and graph generator.
- `algorithms/` — Main graph algorithms.
- `bonus/` — Tarjan and Condensation algorithms.
- `benchmark/` — Performance measurements and reference checks.
- `src/test/java/` — JUnit 5 tests.
- `data/` — Example input graphs.
- `results/` — Benchmark CSV files.
- `results/plots/` — Performance charts.
- `REPORT.md` — Algorithm analysis and conclusions.

## How to Run

**Requirements:**
- Java JDK 17 or newer
- Apache Maven

**1. Run tests:**

`mvn clean test`

**2. Compile the project:**

`mvn compile`

**3. Run the benchmark:**

`mvn exec:java`

**4. Generate charts (requires Python and matplotlib):**

`python tools/plot_results.py`

## Input Format

Graphs are stored in text files.

The first line contains:

`n m directed|undirected`

Each following line contains:

`u v w`

Example:

`4 3 undirected`  
`0 1 5`  
`1 2 3`  
`2 3 7`

## Testing and Benchmark

JUnit 5 tests are used to check algorithm correctness.

The tests compare:
- Kruskal and Prim results.
- SCC results against brute force and Tarjan.
- DAG shortest paths against Bellman-Ford.
- Topological order validity and cycle detection.
- Stack safety on 100,000 vertices.

The benchmark uses 5 graph sizes and 2 densities. Each algorithm is executed 5 times, and the median execution time is recorded.

Performance is evaluated mainly through operation counters rather than execution time.

Results are saved in `results/results.csv`.

## Git Workflow

The project uses the `main` branch and five feature branches:

- `feature/dsu`
- `feature/mst`
- `feature/scc`
- `feature/paths`
- `feature/metrics`

The final working version is marked with the tag `v1.0`.

## AI Usage

ChatGPT was used to assist with code development, debugging, testing, and documentation. The implementation and results were reviewed as part of the assignment.

## Conclusion

This project demonstrates how different graph algorithms solve network and scheduling problems. It also shows how algorithm correctness and complexity can be tested and compared using reproducible operation counts.
