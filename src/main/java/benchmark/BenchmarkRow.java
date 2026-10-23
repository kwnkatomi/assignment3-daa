package benchmark;

import java.util.Locale;

public record BenchmarkRow(
        String algorithm,
        String graph,
        int n,
        int m,
        double timeMs,
        long dsuOps,
        long edgeScans,
        long comparisons,
        int maxDepth,
        long result
) {
    public static final String HEADER =
            "algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result";

    public String toCsv() {
        return String.format(
                Locale.ROOT,
                "%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d",
                algorithm, graph, n, m, timeMs,
                dsuOps, edgeScans, comparisons, maxDepth, result
        );
    }
}