
package citygraph.algorithms;

import citygraph.Graph;
import citygraph.Metrics;

import java.util.Arrays;

public final class Kahn {

    private Kahn() {}

    public record Result(int[] order, boolean acyclic) {}

    public static Result sort(Graph g, Metrics metrics) {
        if (!g.directed()) {
            throw new IllegalArgumentException(
                    "Topological order requires directed graph"
            );
        }

        int[] indeg = g.indegrees();
        int[] queue = new int[g.n()];
        int[] order = new int[g.n()];

        int front = 0;
        int back = 0;
        int used = 0;

        for (int v = 0; v < g.n(); v++) {
            if (indeg[v] == 0) {
                queue[back++] = v;
            }
        }

        while (front < back) {
            int u = queue[front++];
            order[used++] = u;

            for (int a = g.firstArc(u);
                 a != -1;
                 a = g.nextArc(a)) {

                metrics.edgeScans++;

                int v = g.arcTo(a);

                if (--indeg[v] == 0) {
                    queue[back++] = v;
                }
            }
        }

        return new Result(
                Arrays.copyOf(order, used),
                used == g.n()
        );
    }

    public static boolean validate(Graph g, Result result) {
        if (!result.acyclic()
                || result.order().length != g.n()) {
            return false;
        }

        int[] pos = new int[g.n()];
        Arrays.fill(pos, -1);

        for (int i = 0; i < result.order().length; i++) {
            int v = result.order()[i];

            if (v < 0 || v >= g.n() || pos[v] != -1) {
                return false;
            }

            pos[v] = i;
        }

        for (Graph.Edge e : g.edges()) {
            if (pos[e.u()] >= pos[e.v()]) {
                return false;
            }
        }

        return true;
    }
}
