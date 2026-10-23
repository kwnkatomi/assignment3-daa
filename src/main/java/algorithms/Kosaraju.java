package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public final class Kosaraju {

    private Kosaraju() {
    }

    private static final class Frame {
        final int vertex;
        final Iterator<Edge> neighbors;

        Frame(int vertex, Iterator<Edge> neighbors) {
            this.vertex = vertex;
            this.neighbors = neighbors;
        }
    }

    public static SCCResult run(Graph graph) {
        return run(graph, new Metrics());
    }

    public static SCCResult run(Graph graph, Metrics metrics) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        Objects.requireNonNull(metrics, "Metrics cannot be null");

        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Kosaraju requires a directed graph"
            );
        }

        int n = graph.vertexCount();

        boolean[] visited = new boolean[n];
        List<Integer> finishOrder = new ArrayList<>(n);
        ArrayDeque<Frame> dfsStack = new ArrayDeque<>();

        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }

            visited[start] = true;

            dfsStack.push(
                    new Frame(
                            start,
                            graph.outgoing(start).iterator()
                    )
            );

            metrics.recordDepth(dfsStack.size());

            while (!dfsStack.isEmpty()) {
                Frame current = dfsStack.peek();

                if (current.neighbors.hasNext()) {
                    Edge edge = current.neighbors.next();
                    metrics.edgeScans++;

                    int next = edge.to();

                    if (!visited[next]) {
                        visited[next] = true;

                        dfsStack.push(
                                new Frame(
                                        next,
                                        graph.outgoing(next).iterator()
                                )
                        );

                        metrics.recordDepth(dfsStack.size());
                    }
                } else {
                    finishOrder.add(current.vertex);
                    dfsStack.pop();
                }
            }
        }

        Graph reversed = graph.reversed();

        int[] componentIds = new int[n];
        Arrays.fill(componentIds, -1);

        List<List<Integer>> components = new ArrayList<>();
        ArrayDeque<Integer> componentStack = new ArrayDeque<>();

        for (int i = finishOrder.size() - 1; i >= 0; i--) {
            int start = finishOrder.get(i);

            if (componentIds[start] != -1) {
                continue;
            }

            int componentId = components.size();
            List<Integer> component = new ArrayList<>();

            componentIds[start] = componentId;
            componentStack.push(start);

            metrics.recordDepth(componentStack.size());

            while (!componentStack.isEmpty()) {
                int vertex = componentStack.pop();
                component.add(vertex);

                for (Edge edge : reversed.outgoing(vertex)) {
                    metrics.edgeScans++;

                    int next = edge.to();

                    if (componentIds[next] == -1) {
                        componentIds[next] = componentId;
                        componentStack.push(next);

                        metrics.recordDepth(componentStack.size());
                    }
                }
            }

            components.add(component);
        }

        return new SCCResult(componentIds, components);
    }
}