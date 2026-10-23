package tests;

import algorithms.Kosaraju;
import algorithms.SCCResult;
import graph.Edge;
import graph.Graph;
import graph.GraphIO;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class SCCTest {

    @Test
    void matchesBruteForceOnRandomGraphs() {
        Random random = new Random(42);

        for (int trial = 0; trial < 100; trial++) {
            int n = 1 + random.nextInt(200);
            int m = random.nextInt(4 * n + 1);

            Graph graph = new Graph(n, true);

            for (int edge = 0; edge < m; edge++) {
                graph.addEdge(
                        random.nextInt(n),
                        random.nextInt(n),
                        1
                );
            }

            assertMatchesBruteForce(graph);
        }
    }

    @Test
    void loadsCyclicGraph() throws IOException {
        Graph graph = GraphIO.read("data/directed_cyclic.txt");

        SCCResult result = assertMatchesBruteForce(graph);

        assertEquals(3, result.count());

        assertEquals(result.componentOf(0), result.componentOf(1));
        assertEquals(result.componentOf(1), result.componentOf(2));
        assertEquals(result.componentOf(3), result.componentOf(4));

        assertNotEquals(result.componentOf(2), result.componentOf(3));
        assertNotEquals(result.componentOf(4), result.componentOf(5));
    }

    @Test
    void handlesIsolatedVerticesSelfLoopsAndParallelEdges() {
        Graph graph = new Graph(5, true);

        graph.addEdge(0, 0, 1);
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 0, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 2, 1);
        graph.addEdge(3, 3, 1);

        SCCResult result = assertMatchesBruteForce(graph);

        assertEquals(3, result.count());
    }

    @Test
    void handlesEmptyGraph() {
        SCCResult result = assertMatchesBruteForce(
                new Graph(0, true)
        );

        assertEquals(0, result.count());
        assertTrue(result.components().isEmpty());
    }

    @Test
    void handles100000VertexPathWithoutStackOverflow() {
        int n = 100000;
        Graph graph = new Graph(n, true);

        for (int vertex = 0; vertex < n - 1; vertex++) {
            graph.addEdge(vertex, vertex + 1, 1);
        }

        Metrics metrics = new Metrics();

        SCCResult result = assertDoesNotThrow(
                () -> Kosaraju.run(graph, metrics)
        );

        assertEquals(n, result.count());
        assertEquals(2L * (n - 1), metrics.edgeScans);
        assertEquals(n, metrics.maxDepth);

        assertComponentLists(result, n);
    }

    @Test
    void rejectsUndirectedGraphs() {
        Graph graph = new Graph(2, false);
        graph.addEdge(0, 1, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> Kosaraju.run(graph)
        );
    }

    private static SCCResult assertMatchesBruteForce(Graph graph) {
        int n = graph.vertexCount();

        boolean[][] reachable = new boolean[n][];

        for (int source = 0; source < n; source++) {
            reachable[source] = bfs(graph, source);
        }

        Metrics metrics = new Metrics();
        SCCResult result = Kosaraju.run(graph, metrics);

        for (int a = 0; a < n; a++) {
            for (int b = 0; b < n; b++) {
                boolean mutuallyReachable =
                        reachable[a][b] && reachable[b][a];

                assertEquals(
                        mutuallyReachable,
                        result.componentOf(a) == result.componentOf(b)
                );
            }
        }

        assertEquals(2L * graph.edgeCount(), metrics.edgeScans);
        assertComponentLists(result, n);

        return result;
    }

    private static boolean[] bfs(Graph graph, int source) {
        boolean[] visited = new boolean[graph.vertexCount()];
        ArrayDeque<Integer> queue = new ArrayDeque<>();

        visited[source] = true;
        queue.addLast(source);

        while (!queue.isEmpty()) {
            int vertex = queue.removeFirst();

            for (Edge edge : graph.outgoing(vertex)) {
                int next = edge.to();

                if (!visited[next]) {
                    visited[next] = true;
                    queue.addLast(next);
                }
            }
        }

        return visited;
    }

    private static void assertComponentLists(
            SCCResult result,
            int n
    ) {
        boolean[] listed = new boolean[n];

        for (int id = 0; id < result.count(); id++) {
            List<Integer> component = result.components().get(id);

            assertFalse(component.isEmpty());

            for (int vertex : component) {
                assertTrue(vertex >= 0 && vertex < n);

                assertFalse(
                        listed[vertex],
                        "Vertex listed more than once"
                );

                assertEquals(id, result.componentOf(vertex));

                listed[vertex] = true;
            }
        }

        for (boolean present : listed) {
            assertTrue(
                    present,
                    "Vertex missing from component lists"
            );
        }
    }
}