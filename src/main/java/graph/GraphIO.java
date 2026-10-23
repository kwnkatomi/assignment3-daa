package graph;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Scanner;

public final class GraphIO {

    private GraphIO() {
    }

    public static Graph read(String filename) throws IOException {
        return read(Path.of(filename));
    }

    public static Graph read(Path path) throws IOException {
        try (Scanner scanner = new Scanner(
                path,
                StandardCharsets.UTF_8
        )) {
            int n = scanner.nextInt();
            int m = scanner.nextInt();
            String type = scanner.next();

            if (n < 0 || m < 0) {
                throw new IOException(
                        "Vertex and edge counts cannot be negative"
                );
            }

            boolean directed;

            if (type.equals("directed")) {
                directed = true;
            } else if (type.equals("undirected")) {
                directed = false;
            } else {
                throw new IOException(
                        "Graph type must be directed or undirected"
                );
            }

            Graph graph = new Graph(n, directed);

            for (int i = 0; i < m; i++) {
                int from = scanner.nextInt();
                int to = scanner.nextInt();
                int weight = scanner.nextInt();

                if (from < 0 || from >= n || to < 0 || to >= n) {
                    throw new IOException(
                            "Invalid vertex on edge " + (i + 1)
                    );
                }

                graph.addEdge(from, to, weight);
            }

            if (scanner.hasNext()) {
                throw new IOException(
                        "Unexpected data after " + m + " edges"
                );
            }

            if (scanner.ioException() != null) {
                throw scanner.ioException();
            }

            return graph;

        } catch (NoSuchElementException e) {
            throw new IOException(
                    "Missing or invalid value in graph file: " + path,
                    e
            );
        }
    }
}