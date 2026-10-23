package algorithms;

import metrics.Metrics;

import java.util.Objects;

public class DSU {
    private int[] parent;
    private int[] size;
    private int componentCount;

    private final Metrics metrics;

    public DSU(int n) {
        this(n, new Metrics());
    }

    public DSU(int n, Metrics metrics) {
        this.metrics = Objects.requireNonNull(
                metrics,
                "Metrics cannot be null"
        );

        makeSet(n);
    }

    public void makeSet(int n) {
        if (n < 0) {
            throw new IllegalArgumentException(
                    "Size cannot be negative"
            );
        }

        parent = new int[n];
        size = new int[n];
        componentCount = n;

        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    public int find(int x) {
        checkIndex(x);
        metrics.recordFind();

        int root = x;

        while (root != parent[root]) {
            root = parent[root];
        }

        // Path compression
        while (x != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }

        return root;
    }

    public boolean union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);

        if (rootX == rootY) {
            return false;
        }

        // Keep rootX as the root of the larger component
        if (size[rootX] < size[rootY]) {
            int temp = rootX;
            rootX = rootY;
            rootY = temp;
        }

        parent[rootY] = rootX;
        size[rootX] += size[rootY];

        componentCount--;
        metrics.recordSuccessfulUnion();

        return true;
    }

    public int components() {
        return componentCount;
    }

    public Metrics metrics() {
        return metrics;
    }

    private void checkIndex(int x) {
        if (x < 0 || x >= parent.length) {
            throw new IndexOutOfBoundsException(
                    "Invalid element: " + x
            );
        }
    }
}