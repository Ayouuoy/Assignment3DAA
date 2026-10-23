
package citygraph.benchmark;

import citygraph.Graph;
import citygraph.GraphGenerator;
import citygraph.Metrics;

import citygraph.algorithms.DagShortestPath;
import citygraph.algorithms.Kahn;
import citygraph.algorithms.Kosaraju;
import citygraph.algorithms.MST;

import citygraph.bonus.Condensation;
import citygraph.bonus.Tarjan;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class Benchmark {

    private Benchmark() {}

    private static final int[] SIZES = {
            1000, 3162, 10000, 31623, 100000
    };

    private static final long[] EXPECTED_MST = {
            147970, 30363,
            468911, 97589,
            1486139, 309550,
            4715515, 968517,
            15012094, 3056681
    };

    private static final int[] EXPECTED_SCC = {
            27, 1,
            137, 1,
            373, 1,
            1188, 1,
            3923, 1
    };

    private static final long[] EXPECTED_DAG = {
            396274, 252411,
            1271911, 723157,
            3941828, 2381342,
            12551891, 7376612,
            40096539, 23512087
    };

    private static final String HEADER =
            "algorithm,graph,n,m,time_ms,dsu_ops," +
                    "edge_scans,comparisons,max_depth,result";

    private record Run(long result, Metrics metrics) {}

    @FunctionalInterface
    private interface Measure {
        Run run();
    }

    private static String measure(
            String algorithm,
            String graph,
            int n,
            int m,
            long expected,
            Measure fn
    ) {
        long[] times = new long[5];

        Run exemplar = null;

        for (int i = 0; i < 5; i++) {
            long begin = System.nanoTime();

            Run sample = fn.run();

            times[i] = System.nanoTime() - begin;

            if (sample.result() != expected) {
                throw new AssertionError(
                        algorithm + " " + graph +
                                " n=" + n +
                                ": got " + sample.result() +
                                ", expected " + expected
                );
            }

            if (exemplar != null &&
                    (exemplar.metrics().dsuOps()
                            != sample.metrics().dsuOps()
                            || exemplar.metrics().edgeScans
                            != sample.metrics().edgeScans
                            || exemplar.metrics().comparisons
                            != sample.metrics().comparisons
                            || exemplar.metrics().maxDepth
                            != sample.metrics().maxDepth)) {

                throw new AssertionError(
                        "non-deterministic operation counters for "
                                + algorithm
                );
            }

            exemplar = sample;
        }

        Arrays.sort(times);

        Metrics c = exemplar.metrics();

        return String.format(
                Locale.ROOT,
                "%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d",
                algorithm,
                graph,
                n,
                m,
                times[2] / 1_000_000.0,
                c.dsuOps(),
                c.edgeScans,
                c.comparisons,
                c.maxDepth,
                expected
        );
    }

    private static void writeRows(
            Path path,
            String header,
            List<String> rows
    ) throws IOException {

        try (BufferedWriter out =
                     Files.newBufferedWriter(path)) {

            out.write(header);
            out.newLine();

            for (String row : rows) {
                out.write(row);
                out.newLine();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of("results"));

        List<String> rows = new ArrayList<>();
        List<String> bonus = new ArrayList<>();

        for (int i = 0; i < SIZES.length; i++) {
            int n = SIZES[i];

            for (int factor : new int[]{4, 20}) {
                int m = n * factor;

                int k = i * 2 + (factor == 4 ? 0 : 1);

                String description =
                        factor == 4 ? "sparse" : "dense";

                String mstName =
                        "undirected_" + description;

                System.out.printf(
                        "Checking n=%d m=%d ...%n", n, m
                );

                // Undirected graph: MST
                final Graph undirected =
                        GraphGenerator.generate(
                                n, m,
                                GraphGenerator.Kind.UNDIRECTED
                        );

                rows.add(measure(
                        "Kruskal",
                        mstName,
                        n, m,
                        EXPECTED_MST[k],
                        () -> {
                            Metrics c = new Metrics();

                            MST.Result result =
                                    MST.kruskal(undirected, c);

                            if (result.components() != 1 ||
                                    result.edgeCount() != n - 1) {
                                throw new AssertionError(
                                        "Kruskal not a spanning tree"
                                );
                            }

                            return new Run(
                                    result.totalWeight(), c
                            );
                        }
                ));

                rows.add(measure(
                        "Prim",
                        mstName,
                        n, m,
                        EXPECTED_MST[k],
                        () -> {
                            Metrics c = new Metrics();

                            MST.Result result =
                                    MST.prim(undirected, c);

                            if (result.components() != 1 ||
                                    result.edgeCount() != n - 1) {
                                throw new AssertionError(
                                        "Prim not a spanning tree"
                                );
                            }

                            return new Run(
                                    result.totalWeight(), c
                            );
                        }
                ));

                // Directed graph: SCC
                final Graph directed =
                        GraphGenerator.generate(
                                n, m,
                                GraphGenerator.Kind.DIRECTED
                        );

                String directedName =
                        "directed_" + description;

                rows.add(measure(
                        "Kosaraju",
                        directedName,
                        n, m,
                        EXPECTED_SCC[k],
                        () -> {
                            Metrics c = new Metrics();

                            Kosaraju.Result result =
                                    Kosaraju.run(directed, c);

                            if (c.edgeScans != 2L * m) {
                                throw new AssertionError(
                                        "Kosaraju scan count != 2m"
                                );
                            }

                            return new Run(
                                    result.count(), c
                            );
                        }
                ));

                // Bonus: Tarjan vs Kosaraju
                Metrics kc = new Metrics();
                Metrics tc = new Metrics();

                Kosaraju.Result kr =
                        Kosaraju.run(directed, kc);

                Tarjan.Result tr =
                        Tarjan.run(directed, tc);

                if (kr.count() != tr.count() ||
                        !Tarjan.samePartition(
                                kr.componentOf(),
                                tr.componentOf())) {

                    throw new AssertionError(
                            "Tarjan and Kosaraju disagree"
                    );
                }

                if (tc.edgeScans != m ||
                        kc.edgeScans != 2L * m) {
                    throw new AssertionError(
                            "SCC scan counts wrong"
                    );
                }

                bonus.add(String.format(
                        Locale.ROOT,
                        "%s,%d,%d,%d,%d,%d",
                        description,
                        n,
                        m,
                        kr.count(),
                        kc.edgeScans,
                        tc.edgeScans
                ));

                // Bonus: condensation and critical path
                if (n == 1000) {
                    Condensation.Result cd =
                            Condensation.build(
                                    directed,
                                    tr.componentOf(),
                                    tr.count()
                            );

                    if (!Kahn.sort(
                            cd.graph(), new Metrics()
                    ).acyclic()) {

                        throw new AssertionError(
                                "bad condensation"
                        );
                    }

                    Condensation.criticalPath(
                            cd.graph(), new Metrics()
                    );
                }

                // DAG: Kahn and shortest paths
                final Graph dag =
                        GraphGenerator.generate(
                                n, m,
                                GraphGenerator.Kind.DAG
                        );

                String dagName = "dag_" + description;

                rows.add(measure(
                        "Kahn",
                        dagName,
                        n, m,
                        n,
                        () -> {
                            Metrics c = new Metrics();

                            Kahn.Result result =
                                    Kahn.sort(dag, c);

                            if (!Kahn.validate(dag, result)
                                    || c.edgeScans != m) {

                                throw new AssertionError(
                                        "invalid topo order"
                                );
                            }

                            return new Run(
                                    result.order().length, c
                            );
                        }
                ));

                rows.add(measure(
                        "DagShortestPath",
                        dagName,
                        n, m,
                        EXPECTED_DAG[k],
                        () -> {
                            Metrics c = new Metrics();

                            DagShortestPath.Result result =
                                    DagShortestPath.run(dag, c);

                            if (c.edgeScans != m) {
                                throw new AssertionError(
                                        "DAG edge scans != m"
                                );
                            }

                            return new Run(
                                    result.sumDistances(), c
                            );
                        }
                ));

                writeRows(
                        Path.of("results/results.csv"),
                        HEADER,
                        rows
                );

                writeRows(
                        Path.of("results/bonus_metrics.csv"),
                        "density,n,m,scc_count," +
                                "kosaraju_edge_scans,tarjan_edge_scans",
                        bonus
                );

                System.out.printf(
                        "  OK: MST=%d SCC=%d DAG sum=%d; %d/50 rows%n",
                        EXPECTED_MST[k],
                        EXPECTED_SCC[k],
                        EXPECTED_DAG[k],
                        rows.size()
                );
            }
        }

        if (rows.size() != 50 || bonus.size() != 10) {
            throw new AssertionError("missing CSV rows");
        }

        System.out.println(
                "PASS: all ten reference cases, " +
                        "50 primary rows, both bonus algorithms."
        );
    }
}
