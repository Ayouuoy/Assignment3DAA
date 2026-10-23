
package citygraph.algorithms;

import citygraph.DSU;
import citygraph.Graph;
import citygraph.Metrics;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public final class MST {

    private MST() {}

    public record Result(
            long totalWeight,
            int edgeCount,
            int components
    ) {}

    public static Result kruskal(Graph g, Metrics metrics) {
        if (g.directed()) {
            throw new IllegalArgumentException(
                    "MST requires undirected graph"
            );
        }

        Graph.Edge[] sorted =
                g.edges().toArray(new Graph.Edge[0]);

        Arrays.sort(sorted, (a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });

        DSU dsu = DSU.makeSet(g.n(), metrics);

        long total = 0;
        int selected = 0;

        for (Graph.Edge e : sorted) {
            metrics.edgeScans++;

            if (dsu.find(e.u()) != dsu.find(e.v())) {
                dsu.union(e.u(), e.v());

                total += e.weight();
                selected++;
            }
        }

        return new Result(
                total,
                selected,
                dsu.components()
        );
    }

    private record Item(int vertex, int weight) {}

    public static Result prim(Graph g, Metrics metrics) {
        if (g.directed()) {
            throw new IllegalArgumentException(
                    "MST requires undirected graph"
            );
        }

        boolean[] used = new boolean[g.n()];

        PriorityQueue<Item> pq =
                new PriorityQueue<>(new Comparator<>() {

                    @Override
                    public int compare(Item a, Item b) {
                        metrics.comparisons++;
                        return Integer.compare(
                                a.weight(),
                                b.weight()
                        );
                    }
                });

        long total = 0;
        int selected = 0;
        int components = 0;

        for (int root = 0; root < g.n(); root++) {
            if (used[root]) {
                continue;
            }

            components++;
            pq.add(new Item(root, 0));

            while (!pq.isEmpty()) {
                Item cur = pq.poll();

                if (used[cur.vertex()]) {
                    continue;
                }

                used[cur.vertex()] = true;

                if (cur.vertex() != root) {
                    total += cur.weight();
                    selected++;
                }

                for (int arc = g.firstArc(cur.vertex());
                     arc != -1;
                     arc = g.nextArc(arc)) {

                    metrics.edgeScans++;

                    int to = g.arcTo(arc);

                    if (!used[to]) {
                        pq.add(new Item(
                                to,
                                g.arcWeight(arc)
                        ));
                    }
                }
            }
        }

        return new Result(total, selected, components);
    }
}
