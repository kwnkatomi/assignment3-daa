package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.Arrays;
import java.util.Objects;

public final class DagShortestPath {

    private DagShortestPath() {
    }

    public static ShortestPathResult run(Graph graph) {
        return run(graph, new Metrics());
    }

    public static ShortestPathResult run(
            Graph graph,
            Metrics metrics
    ) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(metrics, "Metrics cannot be null");

        TopologicalResult topological = Kahn.run(graph);

        if (topological.hasCycle()) {
            throw new IllegalArgumentException(
                    "DAG shortest paths require an acyclic graph"
            );
        }

        int n = graph.vertexCount();

        long[] distances = new long[n];
        int[] parents = new int[n];

        Arrays.fill(distances, Long.MAX_VALUE);
        Arrays.fill(parents, -1);

        for (int vertex = 0; vertex < n; vertex++) {
            if (graph.inDegree(vertex) == 0) {
                distances[vertex] = 0;
            }
        }

        for (int vertex : topological.order()) {
            for (Edge edge : graph.outgoing(vertex)) {
                metrics.edgeScans++;

                int next = edge.to();
                long candidate = distances[vertex] + edge.weight();

                metrics.comparisons++;

                if (candidate < distances[next]) {
                    distances[next] = candidate;
                    parents[next] = vertex;
                }
            }
        }

        return new ShortestPathResult(distances, parents);
    }
}