
package citygraph;

public final class Metrics {

    public long findCalls;
    public long successfulUnions;
    public long edgeScans;
    public long comparisons;

    public int maxDepth;

    public long dsuOps() {
        return findCalls + successfulUnions;
    }

    public void observeDepth(int depth) {
        maxDepth = Math.max(maxDepth, depth);
    }
}
