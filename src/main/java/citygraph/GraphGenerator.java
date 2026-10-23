
package citygraph;

import java.util.Random;

public final class GraphGenerator {

    private GraphGenerator() {}

    public enum Kind {
        UNDIRECTED,
        DIRECTED,
        DAG
    }

    public static Graph generate(int n, int m, Kind kind) {
        if (n <= 0 || m < 0) {
            throw new IllegalArgumentException(
                    "invalid generator size"
            );
        }

        if (kind == Kind.UNDIRECTED && m < n - 1) {
            throw new IllegalArgumentException(
                    "need spanning tree budget"
            );
        }

        if (n == 1 && m > 0) {
            throw new IllegalArgumentException(
                    "no distinct endpoints"
            );
        }

        Random rnd = new Random(42);

        Graph g = new Graph(
                n,
                kind != Kind.UNDIRECTED,
                m
        );

        int[] perm = new int[n];

        for (int i = 0; i < n; i++) {
            perm[i] = i;
        }

        if (kind != Kind.DIRECTED) {
            for (int i = n - 1; i > 0; i--) {
                int j = rnd.nextInt(i + 1);

                int t = perm[i];
                perm[i] = perm[j];
                perm[j] = t;
            }
        }

        if (kind == Kind.UNDIRECTED) {
            for (int i = 1; i < n; i++) {
                g.addEdge(
                        perm[rnd.nextInt(i)],
                        perm[i],
                        1 + rnd.nextInt(1000)
                );
            }
        }

        while (g.m() < m) {
            int a = rnd.nextInt(n);
            int b;

            do {
                b = rnd.nextInt(n);
            } while (b == a);

            if (kind == Kind.DAG && perm[a] > perm[b]) {
                int t = a;
                a = b;
                b = t;
            }

            g.addEdge(a, b, 1 + rnd.nextInt(1000));
        }

        return g;
    }
}
