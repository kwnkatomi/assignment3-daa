# DAA Assignment 3 Graph Algorithms

City Network and Job Scheduler implements DSU, Kruskal, Prim, Kosaraju, Kahn, and multi-source shortest paths in a DAG. One adjacency-list graph type supports directed and undirected graphs, parallel edges, self-loops, isolated vertices, and disconnected inputs.

## Build and run tests

Requirements: JDK 17 or newer and Maven 3.6.3 or newer. The POM targets Java 17 and uses JUnit Jupiter 5.10.2, Maven Compiler Plugin 3.13.0, and Surefire 3.2.5.

From the project root:

```sh
mvn clean test
```

In IntelliJ IDEA, reload Maven and run Maven > Lifecycle > test. The complete suite contains **31 tests**:

| Class | Tests | Main checks |
| --- | --- | --- |
| DSUTest | 5 | Random unions against a label-array reference; components and counters |
| MSTTest | 7 | 100 connected random graphs; forests, edge cases, and long totals |
| SCCTest | 6 | BFS mutual reachability for graphs up to 200 vertices; 100,000-vertex path |
| KahnTest | 6 | Valid orders, cycles, invalid-order rejection, and unchanged in-degrees |
| DagShortestPathTest | 7 | Bellman-Ford cross-checks, negative weights, all sources, and restored paths |

## Run the benchmark

Run `benchmark.BenchmarkRunner.main()` in IDEA with the project root as its working directory.

The runner uses sizes 1000, 3162, 10000, 31623, and 100000 at E=4V and E=20V. Separate graphs are generated for MST, SCC, and DAG algorithms, with a fresh `Random(42)` for every generator call. It performs one warm-up and five timed runs, reports the median, checks repeatable counters, verifies MST agreement and all reference results, and validates topological orders. Failed checks throw an exception. The CSV is written after all checks pass.

Output: `results/results.csv`, with **50 data rows** and this header:

```text
algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result
```
## AI use acknowledgment

I used ChatGPT for explanations of the assignment and for assistance in writing an appropriate README and plotting script. AI was especially useful to check the correctness of my values. 