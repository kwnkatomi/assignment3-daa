package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class Kahn {

    private Kahn() {
    }

    public static TopologicalResult run(Graph graph) {
        return run(graph, new Metrics());
    }

    public static TopologicalResult run(
            Graph graph,
            Metrics metrics
    ) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(metrics, "Metrics cannot be null");

        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Kahn requires a directed graph"
            );
        }

        int n = graph.vertexCount();

        int[] inDegree = new int[n];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        List<Integer> order = new ArrayList<>(n);

        for (int vertex = 0; vertex < n; vertex++) {
            inDegree[vertex] = graph.inDegree(vertex);

            if (inDegree[vertex] == 0) {
                queue.addLast(vertex);
            }
        }

        while (!queue.isEmpty()) {
            int vertex = queue.removeFirst();
            order.add(vertex);

            for (Edge edge : graph.outgoing(vertex)) {
                metrics.edgeScans++;

                int next = edge.to();
                inDegree[next]--;

                if (inDegree[next] == 0) {
                    queue.addLast(next);
                }
            }
        }

        if (order.size() != n) {
            return new TopologicalResult(
                    List.of(),
                    true
            );
        }

        return new TopologicalResult(order, false);
    }

    public static boolean isValidOrder(
            Graph graph,
            List<Integer> order
    ) {
        if (graph == null || !graph.isDirected() || order == null) {
            return false;
        }

        int n = graph.vertexCount();

        if (order.size() != n) {
            return false;
        }

        int[] position = new int[n];
        Arrays.fill(position, -1);

        int index = 0;

        for (Integer vertex : order) {
            if (vertex == null
                    || vertex < 0
                    || vertex >= n
                    || position[vertex] != -1) {
                return false;
            }

            position[vertex] = index++;
        }

        for (Edge edge : graph.edges()) {
            if (position[edge.from()] >= position[edge.to()]) {
                return false;
            }
        }

        return true;
    }
}