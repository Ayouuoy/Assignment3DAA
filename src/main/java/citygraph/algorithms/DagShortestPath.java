
package citygraph.algorithms;

import citygraph.Graph;
import citygraph.Metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DagShortestPath {

    private DagShortestPath() {}

    public static final long INF = Long.MAX_VALUE / 4;

    public record Result(long[] distance, int[] parent) {

        public long sumDistances() {
            long sum = 0;

            for (long d : distance) {
                if (d == INF) {
                    throw new IllegalStateException(
                            "unreachable vertex"
                    );
                }

                sum += d;
            }

            return sum;
        }

        public List<Integer> pathTo(int v) {
            if (v < 0 || v >= parent.length) {
                throw new IllegalArgumentException(
                        "invalid vertex"
                );
            }

            if (distance[v] == INF) {
                return List.of();
            }

            ArrayList<Integer> path = new ArrayList<>();

            for (int at = v; at != -1; at = parent[at]) {
                path.add(at);
            }

            Collections.reverse(path);

            return path;
        }
    }

    public static Result run(Graph g, Metrics metrics) {
        if (!g.directed()) {
            throw new IllegalArgumentException(
                    "DAG paths require directed graph"
            );
        }

        Kahn.Result topo = Kahn.sort(g, new Metrics());

        if (!topo.acyclic()) {
            throw new IllegalArgumentException(
                    "graph contains a directed cycle"
            );
        }

        long[] dist = new long[g.n()];
        Arrays.fill(dist, INF);

        int[] parent = new int[g.n()];
        Arrays.fill(parent, -1);

        int[] indegrees = g.indegrees();

        // Virtual super-source
        for (int i = 0; i < g.n(); i++) {
            if (indegrees[i] == 0) {
                dist[i] = 0;
            }
        }

        // Relax edges in topological order
        for (int u : topo.order()) {
            for (int a = g.firstArc(u);
                 a != -1;
                 a = g.nextArc(a)) {

                metrics.edgeScans++;

                int v = g.arcTo(a);
                long candidate = dist[u] + g.arcWeight(a);

                metrics.comparisons++;

                if (dist[u] != INF && candidate < dist[v]) {
                    dist[v] = candidate;
                    parent[v] = u;
                }
            }
        }

        return new Result(dist, parent);
    }
}
