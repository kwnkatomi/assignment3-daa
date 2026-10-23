package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Kruskal {

    private Kruskal() {
    }

    public static MSTResult run(Graph graph) {
        return run(graph, new Metrics());
    }

    public static MSTResult run(Graph graph, Metrics metrics) {
        Objects.requireNonNull(
                graph,
                "Graph cannot be null"
        );

        Objects.requireNonNull(
                metrics,
                "Metrics cannot be null"
        );

        if (graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Kruskal requires an undirected graph"
            );
        }

        List<Edge> sortedEdges = new ArrayList<>(
                graph.edges()
        );

        sortedEdges.sort((a, b) -> {
            metrics.comparisons++;

            return Integer.compare(
                    a.weight(),
                    b.weight()
            );
        });

        DSU dsu = new DSU(
                graph.vertexCount(),
                metrics
        );

        List<Edge> selectedEdges = new ArrayList<>();
        long totalWeight = 0;

        for (Edge edge : sortedEdges) {
            if (dsu.components() <= 1) {
                break;
            }

            metrics.edgeScans++;

            if (dsu.union(edge.from(), edge.to())) {
                selectedEdges.add(edge);
                totalWeight += edge.weight();
            }
        }

        return new MSTResult(
                selectedEdges,
                totalWeight,
                dsu.components()
        );
    }
}