package bg.sofia.uni.fmi.mjt.itinerary.graph;

import java.util.List;

public interface WeightedGraphAPI<K, V> {

    void addEdge(K from, V branch);

    void addVertex(K vertex);

    void removeEdge(K from, V branch);

    void removeVertex(K vertex);

    boolean existsVertex(K vertex);

    boolean existsEdge(K from, V branch);

    List<V> getNeighbours(K vertex);

}
