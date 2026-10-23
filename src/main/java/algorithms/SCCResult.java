package algorithms;

import java.util.ArrayList;
import java.util.List;

public final class SCCResult {
    private final int[] componentIds;
    private final List<List<Integer>> components;

    public SCCResult(
            int[] componentIds,
            List<List<Integer>> components
    ) {
        this.componentIds = componentIds.clone();

        List<List<Integer>> copy = new ArrayList<>();

        for (List<Integer> component : components) {
            copy.add(List.copyOf(component));
        }

        this.components = List.copyOf(copy);
    }

    public int count() {
        return components.size();
    }

    public int componentOf(int vertex) {
        return componentIds[vertex];
    }

    public List<List<Integer>> components() {
        return components;
    }
}