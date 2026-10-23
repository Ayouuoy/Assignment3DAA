
package citygraph.bonus;

import citygraph.Graph;
import citygraph.Metrics;

import java.util.Arrays;

public final class Tarjan {

    private Tarjan() {}

    public record Result(int count, int[] componentOf) {}

    public static Result run(Graph g, Metrics metrics) {
        if (!g.directed()) {
            throw new IllegalArgumentException(
                    "SCC requires a directed graph"
            );
        }

        int n = g.n();

        int[] index = new int[n];
        int[] low = new int[n];
        int[] parent = new int[n];
        int[] component = new int[n];

        Arrays.fill(index, -1);
        Arrays.fill(parent, -1);
        Arrays.fill(component, -1);

        int[] callVertex = new int[n];
        int[] callArc = new int[n];
        int[] active = new int[n];

        boolean[] onStack = new boolean[n];

        int clock = 0;
        int count = 0;
        int activeTop = -1;

        for (int start = 0; start < n; start++) {
            if (index[start] != -1) {
                continue;
            }

            int top = 0;

            callVertex[top] = start;
            callArc[top] = g.firstArc(start);

            index[start] = low[start] = clock++;

            active[++activeTop] = start;
            onStack[start] = true;

            metrics.observeDepth(1);

            while (top >= 0) {
                int v = callVertex[top];
                int arc = callArc[top];

                if (arc != -1) {
                    callArc[top] = g.nextArc(arc);
                    metrics.edgeScans++;

                    int w = g.arcTo(arc);

                    if (index[w] == -1) {
                        parent[w] = v;

                        callVertex[++top] = w;
                        callArc[top] = g.firstArc(w);

                        index[w] = low[w] = clock++;

                        active[++activeTop] = w;
                        onStack[w] = true;

                        metrics.observeDepth(
                                Math.max(top + 1, activeTop + 1)
                        );
                    } else if (onStack[w]) {
                        low[v] = Math.min(low[v], index[w]);
                    }
                } else {
                    if (low[v] == index[v]) {
                        int w;

                        do {
                            w = active[activeTop--];
                            onStack[w] = false;
                            component[w] = count;
                        } while (w != v);

                        count++;
                    }

                    --top;

                    if (parent[v] != -1) {
                        low[parent[v]] = Math.min(
                                low[parent[v]],
                                low[v]
                        );
                    }
                }
            }
        }

        return new Result(count, component);
    }

    public static boolean samePartition(int[] a, int[] b) {
        if (a.length != b.length) {
            return false;
        }

        int maxA = -1;
        int maxB = -1;

        for (int i = 0; i < a.length; i++) {
            if (a[i] < 0 || b[i] < 0) {
                return false;
            }

            maxA = Math.max(maxA, a[i]);
            maxB = Math.max(maxB, b[i]);
        }

        int[] aToB = new int[maxA + 1];
        int[] bToA = new int[maxB + 1];

        Arrays.fill(aToB, -1);
        Arrays.fill(bToA, -1);

        for (int i = 0; i < a.length; i++) {
            if (aToB[a[i]] == -1) {
                aToB[a[i]] = b[i];
            }

            if (bToA[b[i]] == -1) {
                bToA[b[i]] = a[i];
            }

            if (aToB[a[i]] != b[i]
                    || bToA[b[i]] != a[i]) {
                return false;
            }
        }

        return true;
    }
}
