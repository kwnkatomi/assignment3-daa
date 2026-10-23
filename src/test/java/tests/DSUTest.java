package tests;

import algorithms.DSU;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class DSUTest {

    @Test
    void startsWithOneComponentPerElement() {
        DSU dsu = new DSU(6);

        assertEquals(6, dsu.components());

        for (int i = 0; i < 6; i++) {
            assertEquals(i, dsu.find(i));
        }
    }

    @Test
    void randomUnionsMatchSimpleReference() {
        int n = 100;
        DSU dsu = new DSU(n);

        int[] reference = new int[n];

        for (int i = 0; i < n; i++) {
            reference[i] = i;
        }

        int expectedComponents = n;
        Random random = new Random(42);

        for (int step = 0; step < 1000; step++) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);

            int groupA = reference[a];
            int groupB = reference[b];

            boolean shouldMerge = groupA != groupB;

            assertEquals(
                    shouldMerge,
                    dsu.union(a, b)
            );

            if (shouldMerge) {
                for (int i = 0; i < n; i++) {
                    if (reference[i] == groupB) {
                        reference[i] = groupA;
                    }
                }

                expectedComponents--;
            }

            assertEquals(
                    expectedComponents,
                    dsu.components()
            );

            for (int vertex = 0; vertex < n; vertex++) {
                assertEquals(
                        reference[vertex] == reference[a],
                        dsu.find(vertex) == dsu.find(a)
                );
            }
        }
    }

    @Test
    void countsFindsAndSuccessfulUnions() {
        Metrics metrics = new Metrics();
        DSU dsu = new DSU(5, metrics);

        assertTrue(dsu.union(0, 1));
        assertTrue(dsu.union(1, 2));

        assertFalse(dsu.union(0, 2));
        assertFalse(dsu.union(2, 2));

        dsu.find(2);

        assertEquals(3, dsu.components());
        assertEquals(9L, metrics.findCalls);
        assertEquals(2L, metrics.successfulUnions);
        assertEquals(11L, metrics.dsuOps);
    }

    @Test
    void makeSetReinitializesGroups() {
        DSU dsu = new DSU(4);

        dsu.union(0, 1);
        dsu.union(2, 3);
        dsu.union(0, 2);

        assertEquals(1, dsu.components());

        dsu.makeSet(3);

        assertEquals(3, dsu.components());

        for (int i = 0; i < 3; i++) {
            assertEquals(i, dsu.find(i));
        }
    }

    @Test
    void handlesEmptySetsAndInvalidArguments() {
        DSU empty = new DSU(0);

        assertEquals(0, empty.components());

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> empty.find(0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new DSU(-1)
        );

        DSU dsu = new DSU(3);

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> dsu.find(-1)
        );

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> dsu.find(3)
        );

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> dsu.union(0, 3)
        );

        assertEquals(3, dsu.components());
    }
}