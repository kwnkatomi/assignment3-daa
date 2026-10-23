package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Graph {
    private final int vertexCount;
    private final boolean directed;

    private final List<List<Edge>> adjacency;
    private final List<Edge> edges;
    private final int[] inDegrees;

    public Graph(int vertexCount, boolean directed) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException(
                    "Vertex count cannot be negative"
            );
        }

        this.vertexCount = vertexCount;
        this.directed = directed;

        this.adjacency = new ArrayList<>(vertexCount);
        this.edges = new ArrayList<>();
        this.inDegrees = new int[vertexCount];

        for (int v = 0; v < vertexCount; v++) {
            adjacency.add(new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);

        Edge edge = new Edge(from, to, weight);

        edges.add(edge);
        adjacency.get(from).add(edge);
        inDegrees[to]++;

        if (!directed) {
            adjacency.get(to).add(
                    new Edge(to, from, weight)
            );

            inDegrees[from]++;
        }
    }

    public int vertexCount() {
        return vertexCount;
    }

    public int edgeCount() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    public List<Edge> outgoing(int vertex) {
        checkVertex(vertex);

        return Collections.unmodifiableList(
                adjacency.get(vertex)
        );
    }

    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }

    public int inDegree(int vertex) {
        checkVertex(vertex);
        return inDegrees[vertex];
    }

    public Graph reversed() {
        Graph result = new Graph(vertexCount, directed);

        for (Edge edge : edges) {
            if (directed) {
                result.addEdge(
                        edge.to(),
                        edge.from(),
                        edge.weight()
                );
            } else {
                result.addEdge(
                        edge.from(),
                        edge.to(),
                        edge.weight()
                );
            }
        }

        return result;
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new IndexOutOfBoundsException(
                    "Invalid vertex: " + vertex
            );
        }
    }
}