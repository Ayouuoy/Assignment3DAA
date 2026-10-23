
package citygraph;

public final class DSU {

    private final int[] parent;
    private final int[] size;

    private int count;

    private final Metrics metrics;

    private DSU(int n, Metrics metrics) {
        if (n < 0) {
            throw new IllegalArgumentException("negative n");
        }

        parent = new int[n];
        size = new int[n];

        count = n;
        this.metrics = metrics;

        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    public static DSU makeSet(int n, Metrics metrics) {
        return new DSU(n, metrics);
    }

    public static DSU makeSet(int n) {
        return new DSU(n, new Metrics());
    }

    public int find(int x) {
        if (x < 0 || x >= parent.length) {
            throw new IllegalArgumentException(
                    "invalid vertex"
            );
        }

        metrics.findCalls++;

        int root = x;

        while (parent[root] != root) {
            root = parent[root];
        }

        while (parent[x] != x) {
            int p = parent[x];
            parent[x] = root;
            x = p;
        }

        return root;
    }

    public boolean union(int x, int y) {
        int a = find(x);
        int b = find(y);

        if (a == b) {
            return false;
        }

        if (size[a] < size[b]) {
            int t = a;
            a = b;
            b = t;
        }

        parent[b] = a;
        size[a] += size[b];

        count--;
        metrics.successfulUnions++;

        return true;
    }

    public int components() {
        return count;
    }
}
