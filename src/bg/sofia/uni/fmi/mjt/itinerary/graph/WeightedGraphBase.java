package bg.sofia.uni.fmi.mjt.itinerary.graph;

import bg.sofia.uni.fmi.mjt.itinerary.exception.EdgeDoesntExistException;
import bg.sofia.uni.fmi.mjt.itinerary.exception.VertexDoesntExistException;
import bg.sofia.uni.fmi.mjt.itinerary.graph.WeightedGraphAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class WeightedGraphBase<K, V> implements WeightedGraphAPI<K, V> {

    protected Map<K, List<V>> edges;

    public WeightedGraphBase() {

        edges = new HashMap<>();

    }

    @Override
    public void addEdge(K from, V branch) {

        if (from == null || branch == null) {
            throw new IllegalArgumentException("can't add null objects to graph");
        }

        if (!existsVertex(from)) {
            addVertex(from);
        }

        edges.get(from).add(branch);

    }

    @Override
    public void addVertex(K vertex) {

        if (vertex == null) {
            throw new IllegalArgumentException("can't add null vertex to graph");
        }

        edges.put(vertex, new ArrayList<>());

    }

    @Override
    public void removeEdge(K from, V branch) {

        if (!existsEdge(from, branch)) {
            throw new EdgeDoesntExistException("There's no such edge in the graph");
        }

        edges.get(from).remove(branch);

    }

    @Override
    public void removeVertex(K vertex) {

        if (!existsVertex(vertex)) {
            throw new VertexDoesntExistException("There's no such vertex in the graph");
        }

        edges.remove(vertex);

    }

    @Override
    public boolean existsVertex(K vertex) {

        if (vertex == null) {
            throw new IllegalArgumentException("Vertex is null");
        }

        return edges.containsKey(vertex);

    }

    @Override
    public boolean existsEdge(K from, V branch) {

        if (from == null || branch == null) {
            throw new IllegalArgumentException("Graph doesn't have null objects");
        }

        return edges.get(from).contains(branch);

    }

    @Override
    public List<V> getNeighbours(K vertex) {

        if (!existsVertex(vertex)) {
            throw new VertexDoesntExistException("There's no such vertex in the graph");
        }

        List<V> vertexNeighbours = new ArrayList<>();
        vertexNeighbours.addAll(edges.get(vertex));

        return vertexNeighbours;

    }

}
