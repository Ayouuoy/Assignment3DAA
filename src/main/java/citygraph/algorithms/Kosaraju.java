
package citygraph.algorithms;

import citygraph.Graph;
import citygraph.Metrics;

import java.util.Arrays;

public final class Kosaraju {

    private Kosaraju() {}

    public record Result(int count, int[] componentOf) {}

    public static Result run(Graph g, Metrics metrics) {
        if (!g.directed()) {
            throw new IllegalArgumentException(
                    "SCC requires directed graph"
            );
        }

        int n = g.n();

        boolean[] seen = new boolean[n];

        int[] finish = new int[n];
        int[] stack = new int[n];
        int[] cursor = new int[n];

        int used = 0;

        // First iterative DFS pass
        for (int start = 0; start < n; start++) {
            if (seen[start]) {
                continue;
            }

            int top = 0;

            stack[0] = start;
            cursor[0] = g.firstArc(start);
            seen[start] = true;

            metrics.observeDepth(1);

            while (top >= 0) {
                int arc = cursor[top];

                if (arc == -1) {
                    finish[used++] = stack[top--];
                    continue;
                }

                cursor[top] = g.nextArc(arc);
                metrics.edgeScans++;

                int w = g.arcTo(arc);

                if (!seen[w]) {
                    seen[w] = true;
                    ++top;

                    stack[top] = w;
                    cursor[top] = g.firstArc(w);

                    metrics.observeDepth(top + 1);
                }
            }
        }

        // Build reversed adjacency lists
        int[] head = new int[n];
        Arrays.fill(head, -1);

        int[] next = new int[g.m()];
        int[] to = new int[g.m()];

        int idx = 0;

        for (Graph.Edge e : g.edges()) {
            to[idx] = e.u();
            next[idx] = head[e.v()];
            head[e.v()] = idx++;
        }

        int[] component = new int[n];
        Arrays.fill(component, -1);

        int components = 0;

        // Second DFS pass
        for (int i = used - 1; i >= 0; i--) {
            int start = finish[i];

            if (component[start] != -1) {
                continue;
            }

            int top = 0;
            stack[0] = start;

            component[start] = components;

            metrics.observeDepth(1);

            while (top >= 0) {
                int v = stack[top--];

                for (int arc = head[v];
                     arc != -1;
                     arc = next[arc]) {

                    metrics.edgeScans++;

                    int w = to[arc];

                    if (component[w] == -1) {
                        component[w] = components;

                        stack[++top] = w;

                        metrics.observeDepth(top + 1);
                    }
                }
            }

            components++;
        }

        return new Result(components, component);
    }
}
