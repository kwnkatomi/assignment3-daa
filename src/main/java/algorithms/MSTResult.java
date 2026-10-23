package algorithms;

import graph.Edge;

import java.util.List;

public record MSTResult(
        List<Edge> edges,
        long totalWeight,
        int components
) {
    public MSTResult {
        edges = List.copyOf(edges);
    }
}