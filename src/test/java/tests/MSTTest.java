package tests;

import algorithms.DSU;
import algorithms.Kruskal;
import algorithms.MSTResult;
import algorithms.Prim;
import graph.Edge;
import graph.Graph;
import graph.GraphIO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class MSTTest {

    @Test
    void bothAlgorithmsAgreeOn100RandomConnectedGraphs() {
        Random random = new Random(42);

        for (int trial = 0; trial < 100; trial++) {
            int n = 2 + random.nextInt(79);
            Graph graph = new Graph(n, false);

            for (int vertex = 1; vertex < n; vertex++) {
                graph.addEdge(
                        vertex,
                        random.nextInt(vertex),
                        random.nextInt(101) - 50
                );
            }

            for (int edge = 0; edge < 3 * n; edge++) {
                graph.addEdge(
                        random.nextInt(n),
                        random.nextInt(n),
                        random.nextInt(101) - 50
                );
            }

            MSTResult kruskal = Kruskal.run(graph);
            MSTResult prim = Prim.run(graph);

            assertEquals(
                    kruskal.totalWeight(),
                    prim.totalWeight(),
                    "MST disagreement in trial " + trial
            );

            assertValidForest(graph, kruskal, 1);
            assertValidForest(graph, prim, 1);
        }
    }

    @Test
    void loadsConnectedRoadNetwork() throws IOException {
        Graph graph = GraphIO.read("data/mst_connected.txt");

        assertBoth(graph, 6, 1);
    }

    @Test
    void loadsDisconnectedRoadNetwork() throws IOException {
        Graph graph = GraphIO.read("data/mst_disconnected.txt");

        assertBoth(graph, 5, 3);
    }

    @Test
    void ignoresSelfLoopsAndChoosesCheapestParallelEdge() {
        Graph graph = new Graph(4, false);

        graph.addEdge(0, 0, -1000);
        graph.addEdge(0, 1, 10);
        graph.addEdge(0, 1, -5);
        graph.addEdge(1, 2, 2);
        graph.addEdge(0, 2, 8);
        graph.addEdge(2, 3, 3);
        graph.addEdge(3, 3, -1000);

        assertBoth(graph, 0, 1);
    }

    @Test
    void usesLongForTotalWeight() {
        Graph graph = new Graph(3, false);

        graph.addEdge(0, 1, Integer.MAX_VALUE);
        graph.addEdge(1, 2, Integer.MAX_VALUE);

        assertBoth(graph, 4294967294L, 1);
    }

    @Test
    void handlesEmptyAndSingleVertexGraphs() {
        assertBoth(new Graph(0, false), 0, 0);

        Graph single = new Graph(1, false);
        single.addEdge(0, 0, -5);

        assertBoth(single, 0, 1);
    }

    @Test
    void rejectsDirectedGraphs() {
        Graph graph = new Graph(2, true);
        graph.addEdge(0, 1, 7);

        assertThrows(
                IllegalArgumentException.class,
                () -> Kruskal.run(graph)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> Prim.run(graph)
        );
    }

    private static void assertBoth(
            Graph graph,
            long expectedWeight,
            int expectedComponents
    ) {
        for (MSTResult result : List.of(
                Kruskal.run(graph),
                Prim.run(graph)
        )) {
            assertEquals(expectedWeight, result.totalWeight());
            assertValidForest(graph, result, expectedComponents);
        }
    }

    private static void assertValidForest(
            Graph graph,
            MSTResult result,
            int expectedComponents
    ) {
        assertEquals(expectedComponents, result.components());

        assertEquals(
                graph.vertexCount() - expectedComponents,
                result.edges().size()
        );

        DSU verifier = new DSU(graph.vertexCount());
        long total = 0;

        for (Edge edge : result.edges()) {
            Edge reversed = new Edge(
                    edge.to(),
                    edge.from(),
                    edge.weight()
            );

            assertTrue(
                    graph.edges().contains(edge)
                            || graph.edges().contains(reversed),
                    "Returned edge must exist in the input graph"
            );

            assertTrue(
                    verifier.union(edge.from(), edge.to()),
                    "Returned forest must be acyclic"
            );

            total += edge.weight();
        }

        assertEquals(total, result.totalWeight());
        assertEquals(expectedComponents, verifier.components());

        for (Edge edge : graph.edges()) {
            assertEquals(
                    verifier.find(edge.from()),
                    verifier.find(edge.to()),
                    "Forest must preserve every input component"
            );
        }
    }
}