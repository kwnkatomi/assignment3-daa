package metrics;

public class Metrics {
    public long findCalls;
    public long successfulUnions;
    public long dsuOps;
    public long edgeScans;
    public long comparisons;
    public int maxDepth;

    public void recordFind() {
        findCalls++;
        dsuOps++;
    }

    public void recordSuccessfulUnion() {
        successfulUnions++;
        dsuOps++;
    }

    public void recordDepth(int depth) {
        maxDepth = Math.max(maxDepth, depth);
    }

    public void reset() {
        findCalls = 0;
        successfulUnions = 0;
        dsuOps = 0;
        edgeScans = 0;
        comparisons = 0;
        maxDepth = 0;
    }
}