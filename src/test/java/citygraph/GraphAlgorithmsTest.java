
package citygraph;

import citygraph.algorithms.DagShortestPath;
import citygraph.algorithms.Kahn;
import citygraph.algorithms.Kosaraju;
import citygraph.algorithms.MST;

import citygraph.bonus.Condensation;
import citygraph.bonus.Tarjan;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class GraphAlgorithmsTest {

    @Test
    void dsuMatchesNaiveReference() {
        Random random = new Random(91);

        for (int round = 0; round < 10; round++) {
            int n = 80;
            int[] label = new int[n];

            for (int i = 0; i < n; i++) {
                label[i] = i;
            }

            Metrics metrics = new Metrics();
            DSU dsu = DSU.makeSet(n, metrics);

            for (int step = 0; step < 500; step++) {
                int x = random.nextInt(n);
                int y = random.nextInt(n);

                int lx = label[x];
                int ly = label[y];

                boolean expectMerged = lx != ly;

                assertEquals(expectMerged, dsu.union(x, y));

                if (expectMerged) {
                    for (int i = 0; i < n; i++) {
                        if (label[i] == ly) {
                            label[i] = lx;
                        }
                    }
                }

                for (int k = 0; k < 8; k++) {
                    int a = random.nextInt(n);
                    int b = random.nextInt(n);

                    assertEquals(
                            label[a] == label[b],
                            dsu.find(a) == dsu.find(b)
                    );
                }

                assertEquals(
                        Arrays.stream(label).distinct().count(),
                        dsu.components()
                );
            }

            assertEquals(
                    n - dsu.components(),
                    metrics.successfulUnions
            );

            assertEquals(
                    metrics.findCalls + metrics.successfulUnions,
                    metrics.dsuOps()
            );
        }
    }

    @Test
    void graphIOAndEdgeCases() throws Exception {
        Graph g = GraphIO.read(
                Path.of("data/disconnected.txt")
        );

        assertFalse(g.directed());
        assertEquals(6, g.n());
        assertEquals(5, g.m());

        MST.Result k = MST.kruskal(g, new Metrics());
        MST.Result p = MST.prim(g, new Metrics());

        assertEquals(11, k.totalWeight());
        assertEquals(k.totalWeight(), p.totalWeight());

        assertEquals(3, k.components());
        assertEquals(3, p.components());

        assertEquals(3, k.edgeCount());
        assertEquals(3, p.edgeCount());

        Graph empty = new Graph(0, false);

        assertEquals(
                0,
                MST.kruskal(empty, new Metrics()).components()
        );

        assertEquals(
                0,
                MST.prim(empty, new Metrics()).components()
        );

        Graph directed = GraphIO.read(
                Path.of("data/cyclic.txt")
        );

        assertFalse(
                Kahn.sort(directed, new Metrics()).acyclic()
        );

        assertEquals(
                3,
                Kosaraju.run(directed, new Metrics()).count()
        );
    }

    @Test
    void mstAgreesOn100RandomConnectedGraphs() {
        Random rnd = new Random(42);

        for (int t = 0; t < 100; t++) {
            int n = 2 + rnd.nextInt(50);

            Graph g = new Graph(n, false);

            for (int v = 1; v < n; v++) {
                g.addEdge(
                        rnd.nextInt(v),
                        v,
                        rnd.nextInt(501) - 100
                );
            }

            for (int j = 0; j < n * 3; j++) {
                int a = rnd.nextInt(n);
                int b = rnd.nextInt(n);

                g.addEdge(
                        a, b,
                        rnd.nextInt(501) - 100
                );
            }

            MST.Result k = MST.kruskal(g, new Metrics());
            MST.Result p = MST.prim(g, new Metrics());

            assertEquals(k.totalWeight(), p.totalWeight());
            assertEquals(n - 1, k.edgeCount());
            assertEquals(n - 1, p.edgeCount());
            assertEquals(1, k.components());
            assertEquals(1, p.components());
        }
    }

    private static long bruteMst(Graph g) {
        int n = g.n();
        int m = g.m();

        long best = Long.MAX_VALUE;

        for (int mask = 0; mask < (1 << m); mask++) {
            if (Integer.bitCount(mask) != n - 1) {
                continue;
            }

            DSU dsu = DSU.makeSet(n);

            long weight = 0;
            boolean noCycle = true;

            for (int i = 0; i < m; i++) {
                if ((mask & (1 << i)) != 0) {
                    Graph.Edge e = g.edges().get(i);

                    if (!dsu.union(e.u(), e.v())) {
                        noCycle = false;
                        break;
                    }

                    weight += e.weight();
                }
            }

            if (noCycle && dsu.components() == 1) {
                best = Math.min(best, weight);
            }
        }

        return best;
    }

    @Test
    void mstAgreesWithBruteForce() {
        Random rnd = new Random(7);

        for (int t = 0; t < 80; t++) {
            int n = 2 + rnd.nextInt(5);

            Graph g = new Graph(n, false);

            for (int v = 1; v < n; v++) {
                g.addEdge(
                        rnd.nextInt(v),
                        v,
                        rnd.nextInt(21) - 10
                );
            }

            for (int i = 0; i < 3; i++) {
                g.addEdge(
                        rnd.nextInt(n),
                        rnd.nextInt(n),
                        rnd.nextInt(21) - 10
                );
            }

            assertEquals(
                    bruteMst(g),
                    MST.kruskal(g, new Metrics()).totalWeight()
            );

            assertEquals(
                    bruteMst(g),
                    MST.prim(g, new Metrics()).totalWeight()
            );
        }
    }

    private static boolean[][] reachability(Graph g) {
        int n = g.n();

        boolean[][] reachable = new boolean[n][n];

        for (int start = 0; start < n; start++) {
            int[] queue = new int[n];

            int front = 0;
            int back = 0;

            queue[back++] = start;
            reachable[start][start] = true;

            while (front < back) {
                int u = queue[front++];

                for (int a = g.firstArc(u);
                     a != -1;
                     a = g.nextArc(a)) {

                    int v = g.arcTo(a);

                    if (!reachable[start][v]) {
                        reachable[start][v] = true;
                        queue[back++] = v;
                    }
                }
            }
        }

        return reachable;
    }

    @Test
    void sccMatchesBruteForceAndTarjanOnRandomGraphs() {
        Random rnd = new Random(414);

        for (int iteration = 0; iteration < 50; iteration++) {
            int n = 1 + rnd.nextInt(200);

            Graph g = new Graph(n, true);

            for (int e = 0; e < n * 5; e++) {
                g.addEdge(
                        rnd.nextInt(n),
                        rnd.nextInt(n),
                        1
                );
            }

            Metrics m1 = new Metrics();
            Metrics m2 = new Metrics();

            Kosaraju.Result kos = Kosaraju.run(g, m1);
            Tarjan.Result tar = Tarjan.run(g, m2);

            assertEquals(kos.count(), tar.count());

            assertTrue(
                    Tarjan.samePartition(
                            kos.componentOf(),
                            tar.componentOf()
                    )
            );

            assertEquals(2L * g.m(), m1.edgeScans);
            assertEquals(g.m(), m2.edgeScans);

            boolean[][] reach = reachability(g);

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    assertEquals(
                            reach[i][j] && reach[j][i],
                            kos.componentOf()[i]
                                    == kos.componentOf()[j]
                    );
                }
            }
        }
    }

    @Test
    void iterativeDfsIsStackSafeAt100000Vertices() {
        int n = 100000;

        Graph g = new Graph(n, true, n - 1);

        for (int i = 1; i < n; i++) {
            g.addEdge(i - 1, i, 1);
        }

        Metrics k = new Metrics();
        Metrics t = new Metrics();

        assertEquals(
                n,
                Kosaraju.run(g, k).count()
        );

        assertEquals(
                n,
                Tarjan.run(g, t).count()
        );

        assertEquals(n, k.maxDepth);
        assertEquals(n, t.maxDepth);

        assertEquals(2L * (n - 1), k.edgeScans);
        assertEquals(n - 1L, t.edgeScans);
    }

    @Test
    void kahnFindsOrderAndDetectsCycles() throws Exception {
        Graph dag = GraphIO.read(
                Path.of("data/dag_negative.txt")
        );

        Metrics count = new Metrics();

        Kahn.Result r = Kahn.sort(dag, count);

        assertTrue(r.acyclic());
        assertTrue(Kahn.validate(dag, r));
        assertEquals(dag.m(), count.edgeScans);

        Graph cyc = GraphIO.read(
                Path.of("data/cyclic.txt")
        );

        assertFalse(
                Kahn.sort(cyc, new Metrics()).acyclic()
        );

        Graph loop = new Graph(1, true);
        loop.addEdge(0, 0, 0);

        assertFalse(
                Kahn.sort(loop, new Metrics()).acyclic()
        );
    }

    private static long[] referenceBellmanFordFromAllSources(
            Graph g
    ) {
        long[] dist = new long[g.n()];

        Arrays.fill(dist, DagShortestPath.INF);

        int[] indegree = g.indegrees();

        for (int i = 0; i < g.n(); i++) {
            if (indegree[i] == 0) {
                dist[i] = 0;
            }
        }

        for (int i = 0; i < g.n() - 1; i++) {
            boolean changed = false;

            for (Graph.Edge e : g.edges()) {
                if (dist[e.u()] == DagShortestPath.INF) {
                    continue;
                }

                long candidate =
                        dist[e.u()] + e.weight();

                if (candidate < dist[e.v()]) {
                    dist[e.v()] = candidate;
                    changed = true;
                }
            }

            if (!changed) {
                break;
            }
        }

        return dist;
    }

    @Test
    void shortestPathMatchesBellmanFordIncludingNegativeEdges()
            throws Exception {

        Graph example = GraphIO.read(
                Path.of("data/dag_negative.txt")
        );

        DagShortestPath.Result shortest =
                DagShortestPath.run(example, new Metrics());

        assertArrayEquals(
                referenceBellmanFordFromAllSources(example),
                shortest.distance()
        );

        assertEquals(
                List.of(1, 2, 3, 5),
                shortest.pathTo(5)
        );

        Random rnd = new Random(48);

        for (int rep = 0; rep < 100; rep++) {
            int n = 2 + rnd.nextInt(35);

            Graph g = new Graph(n, true);

            for (int j = 0; j < n * 5; j++) {
                int a = rnd.nextInt(n);
                int b = rnd.nextInt(n);

                if (a < b) {
                    g.addEdge(
                            a, b,
                            rnd.nextInt(41) - 20
                    );
                }
            }

            Metrics c = new Metrics();

            DagShortestPath.Result result =
                    DagShortestPath.run(g, c);

            assertArrayEquals(
                    referenceBellmanFordFromAllSources(g),
                    result.distance()
            );

            assertEquals(g.m(), c.edgeScans);

            for (int vertex = 0; vertex < n; vertex++) {
                List<Integer> path = result.pathTo(vertex);

                assertEquals(
                        vertex,
                        path.get(path.size() - 1).intValue()
                );

                assertEquals(
                        0,
                        g.indegrees()[path.get(0)]
                );
            }
        }

        Graph cycle = GraphIO.read(
                Path.of("data/cyclic.txt")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> DagShortestPath.run(
                        cycle, new Metrics()
                )
        );
    }

    @Test
    void condensationIsDagAndCriticalPathIsCorrect() {
        Graph g = new Graph(4, true);

        g.addEdge(0, 1, 0);
        g.addEdge(1, 0, 0);
        g.addEdge(1, 2, 5);
        g.addEdge(2, 3, 4);
        g.addEdge(0, 3, 2);

        Tarjan.Result tar =
                Tarjan.run(g, new Metrics());

        assertEquals(3, tar.count());

        Condensation.Result condensed =
                Condensation.build(
                        g,
                        tar.componentOf(),
                        tar.count()
                );

        assertTrue(
                Kahn.validate(
                        condensed.graph(),
                        Kahn.sort(
                                condensed.graph(),
                                new Metrics()
                        )
                )
        );

        Condensation.CriticalPath path =
                Condensation.criticalPath(
                        condensed.graph(),
                        new Metrics()
                );

        assertEquals(9, path.length());
        assertEquals(3, path.componentPath().size());

        Graph bad = new Graph(2, true);

        bad.addEdge(0, 1, 1);
        bad.addEdge(1, 0, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> Condensation.criticalPath(
                        bad, new Metrics()
                )
        );
    }

    @Test
    void referenceGeneratorSmallCases() {
        int[] factors = {4, 20};

        long[] mst = {147970, 30363};
        long[] paths = {396274, 252411};
        int[] scc = {27, 1};

        for (int i = 0; i < 2; i++) {
            int n = 1000;
            int m = n * factors[i];

            assertEquals(
                    mst[i],
                    MST.kruskal(
                            GraphGenerator.generate(
                                    n, m,
                                    GraphGenerator.Kind.UNDIRECTED
                            ),
                            new Metrics()
                    ).totalWeight()
            );

            assertEquals(
                    scc[i],
                    Kosaraju.run(
                            GraphGenerator.generate(
                                    n, m,
                                    GraphGenerator.Kind.DIRECTED
                            ),
                            new Metrics()
                    ).count()
            );

            assertEquals(
                    paths[i],
                    DagShortestPath.run(
                            GraphGenerator.generate(
                                    n, m,
                                    GraphGenerator.Kind.DAG
                            ),
                            new Metrics()
                    ).sumDistances()
            );
        }
    }
}
