
package citygraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Graph {

    public record Edge(int u, int v, int weight) {}

    private final int n;
    private final boolean directed;

    private final int[] head;
    private final int[] indegree;

    private int[] to;
    private int[] next;
    private int[] weight;

    private int arcs;

    private final ArrayList<Edge> edges;

    public Graph(int n, boolean directed) {
        this(n, directed, 8);
    }

    public Graph(int n, boolean directed, int expectedEdges) {
        if (n < 0 || expectedEdges < 0) {
            throw new IllegalArgumentException("negative size");
        }

        this.n = n;
        this.directed = directed;

        this.head = new int[n];
        Arrays.fill(head, -1);

        this.indegree = new int[n];

        long estimate = (long) expectedEdges
                * (directed ? 1 : 2);

        int cap = (int) Math.max(
                1,
                Math.min(estimate, Integer.MAX_VALUE - 8L)
        );

        this.to = new int[cap];
        this.next = new int[cap];
        this.weight = new int[cap];

        this.edges = new ArrayList<>(expectedEdges);
    }

    public int n() {
        return n;
    }

    public int m() {
        return edges.size();
    }

    public boolean directed() {
        return directed;
    }

    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }

    public int firstArc(int u) {
        check(u);
        return head[u];
    }

    public int nextArc(int arc) {
        return next[arc];
    }

    public int arcTo(int arc) {
        return to[arc];
    }

    public int arcWeight(int arc) {
        return weight[arc];
    }

    public int[] indegrees() {
        return indegree.clone();
    }

    public void addEdge(int u, int v, int w) {
        check(u);
        check(v);

        edges.add(new Edge(u, v, w));
        arc(u, v, w);

        if (directed) {
            indegree[v]++;
        } else {
            arc(v, u, w);
        }
    }

    private void arc(int u, int v, int w) {
        if (arcs == to.length) {
            int size = to.length
                    > (Integer.MAX_VALUE - 8) / 2
                    ? Integer.MAX_VALUE - 8
                    : to.length * 2;

            if (size <= to.length) {
                throw new IllegalStateException("too many arcs");
            }

            to = Arrays.copyOf(to, size);
            next = Arrays.copyOf(next, size);
            weight = Arrays.copyOf(weight, size);
        }

        to[arcs] = v;
        weight[arcs] = w;
        next[arcs] = head[u];
        head[u] = arcs++;
    }

    private void check(int v) {
        if (v < 0 || v >= n) {
            throw new IllegalArgumentException(
                    "invalid vertex: " + v
            );
        }
    }
}
