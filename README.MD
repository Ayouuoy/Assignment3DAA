
Assignment 3 — City Network & Job Scheduler
Course: Design and Analysis of Algorithms (DAA)
Student: Zhumabay Aikyn | Group: SE-2523
Language: Java 17 | Build: Maven | Tests: JUnit 5

A reproducible implementation of graph algorithms for city road planning and scheduling. Algorithms use one directed/undirected Graph, stored as an efficient adjacency list (forward-star arrays). We do not use a third-party graph/algorithm library.

Requirements
JDK 17 or newer
Apache Maven 3.9+ (or IntelliJ IDEA with Maven support)
Python 3 with matplotlib (only for regenerating PNG plots, not required for Java)
Memory: recommend MAVEN_OPTS=-Xmx3g for the largest dense benchmark. The benchmark generates a graph at a time.
Run from the project root
mvn clean test
mvn -q -DskipTests compile
mvn -q -DskipTests exec:java
python -m pip install matplotlib
python tools/plot_results.py
The benchmark prints 10 exact reference triples and fails with an AssertionError if any mismatch occurs. It executes every primary algorithm five times per graph and reports the median runtime in results/results.csv (50 rows). Results are always compared using reproducible counters, not timings. Extra results/bonus_metrics.csv has 10 bonus comparisons between Tarjan and Kosaraju.

You can also run citygraph.benchmark.Benchmark.main() in IntelliJ with Working directory = project root and VM options = -Xmx3g. Run the JUnit tests from the src/test/java folder or Maven tool window.

Source layout
CityNetworkScheduler/
├── pom.xml
├── src/
│   ├── main/java/citygraph/
│   │   ├── Graph.java                    directed/undirected adjacency lists
│   │   ├── GraphIO.java                  data file parser
│   │   ├── GraphGenerator.java           deterministic Random(42) generator
│   │   ├── DSU.java                      union by size + path compression
│   │   ├── Metrics.java                  counters
│   │   ├── algorithms/
│   │   │   ├── MST.java                  Kruskal + Prim + forest
│   │   │   ├── Kosaraju.java             stack-safe iterative SCC
│   │   │   ├── Kahn.java                 topological order + cycle detection
│   │   │   └── DagShortestPath.java      virtual source, parent, negatives
│   │   ├── bonus/
│   │   │   ├── Tarjan.java               iterative Tarjan SCC (+10%)
│   │   │   └── Condensation.java         SCC condensation + critical path (+5%)
│   │   └── benchmark/Benchmark.java     self-checking 5-run benchmark
│   └── test/java/citygraph/GraphAlgorithmsTest.java
├── data/
│   ├── connected.txt
│   ├── disconnected.txt
│   ├── cyclic.txt
│   └── dag_negative.txt
├── results/
│   ├── results.csv                    50 main rows
│   ├── bonus_metrics.csv              10 Tarjan/Kosaraju comparisons
│   └── plots/                        PNG graphics
├── tools/plot_results.py
├── REPORT.md
├── STEPS_RU.md
└── README.md
File format
First line: n m directed or n m undirected. Then m lines, each u v weight (zero-based vertices). For example:

4 3 directed
0 1 2
1 2 -5
2 3 1
Isolated vertices, disconnected graphs, parallel edges and self-loops are supported. MST ignores self-loops; Kahn correctly reports cycles induced by self-loops.

Metrics definition
dsu_ops = findCalls + successfulUnions, instrumented inside DSU. Failed unions do not add a successful-union counter.
edge_scans = adjacency arcs examined (Kruskal scans original sorted edges).
comparisons = actual comparator calls for MST and numeric relaxation checks for DAG paths.
max_depth = largest explicit DFS stack size (Kosaraju and Tarjan); zero for algorithms without an explicit DFS stack.
time_ms = middle value of five independent System.nanoTime() durations in milliseconds. Times depend on the machine, but metrics and result values do not.
Counting boundary: The DagShortestPath stage counts the relaxation pass only, because the topological sort is measured in the separate Kahn row. Therefore both Kahn and DAG relaxation each count exactly m scans on DAGs. The first and second Kosaraju passes together count 2m. For Prim, an undirected edge can be viewed from both adjacency lists, so edge_scans=2m.

Validation
The tests cover:

Random DSU operations against a naive reference.
100 random connected MST examples, Kruskal against Prim, plus exact brute-force enumeration of small spanning trees.
SCC against BFS reachability for every vertex on random graphs (up to 200 vertices); Tarjan agreement.
100,000-vertex chain test for iterative stack safety.
Kahn cycle detection and edge-order verification.
DAG shortest paths against Bellman-Ford with negative weights and parent path restoration.
Data file parsing, parallel edges, self-loops, disconnected forests.
Condensation DAG and critical-path correctness.
Fixed Random(42) reference cases, plus all 10 cases in the benchmark.
Git history and branches
The repository contains main and feature/dsu, feature/mst, feature/scc, feature/paths, feature/metrics; the feature branches are merged to main. v1.0 marks the working release. If you extract a ZIP that doesn't retain the hidden .git folder, follow STEPS_RU.md to recreate the history. Upload to your own GitHub repository; this project has not been pushed to your account.

AI use acknowledgement
OpenAI ChatGPT was used to assist with code structure, implementation, tests and report drafting. Reference results were checked by actually running the deterministic Java generator and algorithms. The student should read, understand, verify and be able to defend all the code.

