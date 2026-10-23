package benchmark;

import algorithms.*;
import graph.Graph;
import metrics.Metrics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.ToLongFunction;

public final class BenchmarkRunner {
    private static final int RUNS = 5;

    private static final long[][] REFERENCES = {
            {1000, 4000, 147970, 27, 396274},
            {1000, 20000, 30363, 1, 252411},
            {3162, 12648, 468911, 137, 1271911},
            {3162, 63240, 97589, 1, 723157},
            {10000, 40000, 1486139, 373, 3941828},
            {10000, 200000, 309550, 1, 2381342},
            {31623, 126492, 4715515, 1188, 12551891},
            {31623, 632460, 968517, 1, 7376612},
            {100000, 400000, 15012094, 3923, 40096539},
            {100000, 2000000, 3056681, 1, 23512087}
    };

    private record Measurement<T>(
            T result,
            Metrics metrics,
            double timeMs,
            long value
    ) {
    }

    private BenchmarkRunner() {
    }

    public static void main(String[] args) throws IOException {
        Path output = args.length == 0
                ? Path.of("results/results.csv")
                : Path.of(args[0]);

        List<BenchmarkRow> rows = new ArrayList<>();

        for (long[] reference : REFERENCES) {
            int n = (int) reference[0];
            int m = (int) reference[1];

            String density = m == 4 * n ? "sparse" : "dense";

            long mst = benchmarkMst(
                    rows, n, m, density, reference[2]
            );

            long scc = benchmarkScc(
                    rows, n, m, density, reference[3]
            );

            long distances = benchmarkDag(
                    rows, n, m, density, reference[4]
            );

            System.out.printf(
                    Locale.ROOT,
                    "n=%d m=%d | MST=%d | SCC=%d | DAG sum=%d%n",
                    n, m, mst, scc, distances
            );
        }

        check(
                rows.size() == 50,
                "Expected exactly 50 benchmark rows"
        );

        Files.createDirectories(
                output.toAbsolutePath().getParent()
        );

        try (BufferedWriter writer = Files.newBufferedWriter(
                output, StandardCharsets.UTF_8
        )) {
            writer.write(BenchmarkRow.HEADER);
            writer.newLine();

            for (BenchmarkRow row : rows) {
                writer.write(row.toCsv());
                writer.newLine();
            }
        }

        System.out.println(
                "All checks passed. Wrote 50 rows to "
                        + output.toAbsolutePath()
        );
    }

    private static long benchmarkMst(
            List<BenchmarkRow> rows,
            int n,
            int m,
            String density,
            long expected
    ) {
        Graph graph = GraphGenerator.undirected(n, m);

        Measurement<MSTResult> kruskal = measure(
                "Kruskal",
                graph,
                Kruskal::run,
                MSTResult::totalWeight
        );

        Measurement<MSTResult> prim = measure(
                "Prim",
                graph,
                Prim::run,
                MSTResult::totalWeight
        );

        check(
                kruskal.value() == prim.value(),
                "Kruskal and Prim disagree"
        );

        check(
                kruskal.value() == expected,
                "MST reference mismatch at n=" + n + " m=" + m
        );

        check(
                kruskal.result().components() == 1
                        && prim.result().components() == 1,
                "Generated MST graph must be connected"
        );

        check(
                kruskal.result().edges().size() == n - 1
                        && prim.result().edges().size() == n - 1,
                "A spanning tree must contain n - 1 edges"
        );

        check(
                prim.metrics().edgeScans == 2L * m,
                "Prim must scan both adjacency entries"
        );

        rows.add(row(
                "Kruskal", "undirected_" + density, graph, kruskal
        ));

        rows.add(row(
                "Prim", "undirected_" + density, graph, prim
        ));

        return kruskal.value();
    }

    private static long benchmarkScc(
            List<BenchmarkRow> rows,
            int n,
            int m,
            String density,
            long expected
    ) {
        Graph graph = GraphGenerator.directed(n, m);

        Measurement<SCCResult> kosaraju = measure(
                "Kosaraju",
                graph,
                Kosaraju::run,
                SCCResult::count
        );

        check(
                kosaraju.value() == expected,
                "SCC reference mismatch at n=" + n + " m=" + m
        );

        check(
                kosaraju.metrics().edgeScans == 2L * m,
                "Kosaraju must scan exactly 2m edges"
        );

        check(
                kosaraju.metrics().maxDepth > 0
                        && kosaraju.metrics().maxDepth <= n,
                "Invalid explicit stack depth"
        );

        rows.add(row(
                "Kosaraju", "directed_" + density, graph, kosaraju
        ));

        return kosaraju.value();
    }

    private static long benchmarkDag(
            List<BenchmarkRow> rows,
            int n,
            int m,
            String density,
            long expected
    ) {
        Graph graph = GraphGenerator.dag(n, m);

        Measurement<TopologicalResult> kahn = measure(
                "Kahn",
                graph,
                Kahn::run,
                result -> result.order().size()
        );

        check(
                !kahn.result().hasCycle(),
                "Generated DAG contains a cycle"
        );

        check(
                Kahn.isValidOrder(graph, kahn.result().order()),
                "Invalid topological order"
        );

        check(
                kahn.metrics().edgeScans == m,
                "Kahn must scan exactly m edges"
        );

        Measurement<ShortestPathResult> paths = measure(
                "DagShortestPath",
                graph,
                DagShortestPath::run,
                ShortestPathResult::sumDistances
        );

        check(
                paths.value() == expected,
                "DAG distance reference mismatch at n=" + n + " m=" + m
        );

        check(
                paths.metrics().edgeScans == m,
                "DAG relaxation must scan exactly m edges"
        );

        rows.add(row(
                "Kahn", "dag_" + density, graph, kahn
        ));

        rows.add(row(
                "DagShortestPath", "dag_" + density, graph, paths
        ));

        return paths.value();
    }

    private static <T> Measurement<T> measure(
            String name,
            Graph graph,
            BiFunction<Graph, Metrics, T> algorithm,
            ToLongFunction<T> resultValue
    ) {
        algorithm.apply(graph, new Metrics());

        double[] times = new double[RUNS];

        Metrics baseline = null;
        T lastResult = null;
        long value = 0;

        for (int run = 0; run < RUNS; run++) {
            Metrics current = new Metrics();

            long start = System.nanoTime();
            T result = algorithm.apply(graph, current);
            long elapsed = System.nanoTime() - start;

            times[run] = elapsed / 1_000_000.0;

            long currentValue = resultValue.applyAsLong(result);

            if (run == 0) {
                baseline = current;
                value = currentValue;
            } else {
                check(
                        currentValue == value,
                        name + " result changed between runs"
                );

                check(
                        sameCounters(baseline, current),
                        name + " counters changed between runs"
                );
            }

            lastResult = result;
        }

        Arrays.sort(times);

        return new Measurement<>(
                lastResult,
                baseline,
                times[RUNS / 2],
                value
        );
    }

    private static boolean sameCounters(Metrics a, Metrics b) {
        return a.findCalls == b.findCalls
                && a.successfulUnions == b.successfulUnions
                && a.dsuOps == b.dsuOps
                && a.edgeScans == b.edgeScans
                && a.comparisons == b.comparisons
                && a.maxDepth == b.maxDepth;
    }

    private static <T> BenchmarkRow row(
            String algorithm,
            String graphName,
            Graph graph,
            Measurement<T> measurement
    ) {
        Metrics metrics = measurement.metrics();

        return new BenchmarkRow(
                algorithm,
                graphName,
                graph.vertexCount(),
                graph.edgeCount(),
                measurement.timeMs(),
                metrics.dsuOps,
                metrics.edgeScans,
                metrics.comparisons,
                metrics.maxDepth,
                measurement.value()
        );
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}