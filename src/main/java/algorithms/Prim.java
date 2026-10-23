package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

public final class Prim {

    private Prim() {
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
                    "Prim requires an undirected graph"
            );
        }

        boolean[] inTree = new boolean[graph.vertexCount()];
        List<Edge> selectedEdges = new ArrayList<>();

        long totalWeight = 0;
        int components = 0;

        PriorityQueue<Edge> queue = new PriorityQueue<>(
                (a, b) -> {
                    metrics.comparisons++;

                    return Integer.compare(
                            a.weight(),
                            b.weight()
                    );
                }
        );

        for (int start = 0; start < graph.vertexCount(); start++) {
            if (inTree[start]) {
                continue;
            }

            components++;

            queue.add(new Edge(-1, start, 0));

            while (!queue.isEmpty()) {
                Edge best = queue.poll();
                int vertex = best.to();

                if (inTree[vertex]) {
                    continue;
                }

                inTree[vertex] = true;

                if (best.from() != -1) {
                    selectedEdges.add(best);
                    totalWeight += best.weight();
                }

                for (Edge edge : graph.outgoing(vertex)) {
                    metrics.edgeScans++;

                    if (!inTree[edge.to()]) {
                        queue.add(edge);
                    }
                }
            }
        }

        return new MSTResult(
                selectedEdges,
                totalWeight,
                components
        );
    }
}