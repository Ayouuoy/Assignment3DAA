
package citygraph.bonus;

import citygraph.Graph;
import citygraph.Metrics;
import citygraph.algorithms.Kahn;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public final class Condensation {

    private Condensation() {}

    public record Result(
            Graph graph,
            int[] componentOf
    ) {}

    public static Result build(
            Graph original,
            int[] componentOf,
            int count
    ) {
        if (!original.directed()
                || componentOf.length != original.n()
                || count < 0) {
            throw new IllegalArgumentException(
                    "invalid SCC mapping"
            );
        }

        Graph condensed = new Graph(count, true);

        for (Graph.Edge e : original.edges()) {
            int a = componentOf[e.u()];
            int b = componentOf[e.v()];

            if (a != b) {
                condensed.addEdge(a, b, e.weight());
            }
        }

        if (!Kahn.sort(
                condensed,
                new Metrics()
        ).acyclic()) {
            throw new IllegalStateException(
                    "SCC condensation must be acyclic"
            );
        }

        return new Result(
                condensed,
                componentOf.clone()
        );
    }

    public record CriticalPath(
            long length,
            List<Integer> componentPath,
            long[] longestDistances
    ) {}

    public static CriticalPath criticalPath(
            Graph dag,
            Metrics metrics
    ) {
        Kahn.Result topo = Kahn.sort(dag, new Metrics());

        if (!topo.acyclic()) {
            throw new IllegalArgumentException(
                    "critical path requires DAG"
            );
        }

        long infinity = Long.MAX_VALUE / 4;

        long[] negDist = new long[dag.n()];
        Arrays.fill(negDist, infinity);

        int[] parent = new int[dag.n()];
        Arrays.fill(parent, -1);

        int[] indegree = dag.indegrees();

        for (int i = 0; i < dag.n(); i++) {
            if (indegree[i] == 0) {
                negDist[i] = 0;
            }
        }

        for (int u : topo.order()) {
            for (int arc = dag.firstArc(u);
                 arc != -1;
                 arc = dag.nextArc(arc)) {

                metrics.edgeScans++;
                metrics.comparisons++;

                int v = dag.arcTo(arc);

                long candidate =
                        negDist[u] - (long) dag.arcWeight(arc);

                if (negDist[u] != infinity
                        && candidate < negDist[v]) {
                    negDist[v] = candidate;
                    parent[v] = u;
                }
            }
        }

        if (dag.n() == 0) {
            return new CriticalPath(
                    0,
                    List.of(),
                    new long[0]
            );
        }

        int end = 0;

        long[] longest = new long[dag.n()];

        for (int i = 0; i < dag.n(); i++) {
            longest[i] = -negDist[i];

            if (negDist[i] < negDist[end]) {
                end = i;
            }
        }

        ArrayList<Integer> path = new ArrayList<>();

        for (int at = end; at != -1; at = parent[at]) {
            path.add(at);
        }

        Collections.reverse(path);

        return new CriticalPath(
                longest[end],
                path,
                longest
        );
    }
}
