package algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ShortestPathResult {
    private final long[] distances;
    private final int[] parents;

    public ShortestPathResult(long[] distances, int[] parents) {
        if (distances.length != parents.length) {
            throw new IllegalArgumentException(
                    "Distances and parents must have the same length"
            );
        }

        this.distances = distances.clone();
        this.parents = parents.clone();
    }

    public long distanceTo(int vertex) {
        return distances[vertex];
    }

    public int parentOf(int vertex) {
        return parents[vertex];
    }

    public long[] distances() {
        return distances.clone();
    }

    public List<Integer> pathTo(int vertex) {
        if (distances[vertex] == Long.MAX_VALUE) {
            return List.of();
        }

        List<Integer> path = new ArrayList<>();

        for (
                int current = vertex;
                current != -1;
                current = parents[current]
        ) {
            path.add(current);
        }

        Collections.reverse(path);
        return path;
    }

    public long sumDistances() {
        long total = 0;

        for (long distance : distances) {
            total += distance;
        }

        return total;
    }
}