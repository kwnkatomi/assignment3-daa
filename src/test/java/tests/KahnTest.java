package tests;

import algorithms.Kahn;
import algorithms.TopologicalResult;
import benchmark.GraphGenerator;
import graph.Edge;
import graph.Graph;
import graph.GraphIO;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class KahnTest {

    @Test
    void loadsDagAndPreservesInDegreesAcrossRuns() throws IOException {
        Graph graph = GraphIO.read("data/dag.txt");

        int[] before = new int[graph.vertexCount()];

        for (int vertex = 0; vertex < before.length; vertex++) {
            before[vertex] = graph.inDegree(vertex);
        }

        for (int run = 0; run < 2; run++) {
            Metrics metrics = new Metrics();
            TopologicalResult result = Kahn.run(graph, metrics);

            assertValidOrder(graph, result);

            assertEquals(
                    (long) graph.edgeCount(),
                    metrics.edgeScans
            );

            for (int vertex = 0; vertex < before.length; vertex++) {
                assertEquals(
                        before[vertex],
                        graph.inDegree(vertex)
                );
            }
        }
    }

    @Test
    void validates100RandomDagOrders() {
        Random random = new Random(42);

        for (int trial = 0; trial < 100; trial++) {
            int n = 2 + random.nextInt(99);
            int m = random.nextInt(6 * n + 1);

            Graph graph = GraphGenerator.dag(n, m);

            assertValidOrder(graph, Kahn.run(graph));
        }
    }

    @Test
    void detectsCyclesAndReturnsNoPartialOrder() throws IOException {
        Graph cyclic = GraphIO.read("data/directed_cyclic.txt");

        Graph prefix = new Graph(4, true);
        prefix.addEdge(0, 1, 1);
        prefix.addEdge(1, 2, 1);
        prefix.addEdge(2, 1, 1);

        Graph selfLoop = new Graph(1, true);
        selfLoop.addEdge(0, 0, 1);

        for (Graph graph : List.of(cyclic, prefix, selfLoop)) {
            TopologicalResult result = Kahn.run(graph);

            assertTrue(result.hasCycle());
            assertTrue(result.order().isEmpty());
        }
    }

    @Test
    void handlesParallelEdgesIsolatedVerticesAndEmptyGraph() {
        Graph graph = new Graph(4, true);

        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 1, 7);
        graph.addEdge(2, 1, -3);

        assertValidOrder(graph, Kahn.run(graph));

        Graph empty = new Graph(0, true);

        assertValidOrder(empty, Kahn.run(empty));
    }

    @Test
    void rejectsInvalidOrders() {
        Graph graph = new Graph(3, true);

        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);

        assertTrue(
                Kahn.isValidOrder(graph, List.of(0, 1, 2))
        );

        assertFalse(
                Kahn.isValidOrder(graph, List.of(2, 1, 0))
        );

        assertFalse(
                Kahn.isValidOrder(graph, List.of(0, 0, 2))
        );

        assertFalse(
                Kahn.isValidOrder(graph, List.of(0, 1))
        );

        assertFalse(
                Kahn.isValidOrder(graph, List.of(0, 1, 3))
        );

        assertFalse(
                Kahn.isValidOrder(graph, Arrays.asList(0, null, 2))
        );

        assertFalse(Kahn.isValidOrder(graph, null));
    }

    @Test
    void rejectsUndirectedGraphs() {
        Graph graph = new Graph(2, false);
        graph.addEdge(0, 1, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> Kahn.run(graph)
        );
    }

    private static void assertValidOrder(
            Graph graph,
            TopologicalResult result
    ) {
        assertFalse(result.hasCycle());

        List<Integer> order = result.order();
        int n = graph.vertexCount();

        assertEquals(n, order.size());

        Set<Integer> vertices = new HashSet<>(order);

        assertEquals(n, vertices.size());

        for (int vertex = 0; vertex < n; vertex++) {
            assertTrue(vertices.contains(vertex));
        }

        for (Edge edge : graph.edges()) {
            assertTrue(
                    order.indexOf(edge.from()) < order.indexOf(edge.to()),
                    "Every edge must go forward in the order"
            );
        }

        assertTrue(Kahn.isValidOrder(graph, order));
    }
}