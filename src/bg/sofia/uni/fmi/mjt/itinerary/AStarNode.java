package bg.sofia.uni.fmi.mjt.itinerary;

import java.math.BigDecimal;

public class AStarNode<T> implements Comparable<AStarNode<T>> {

    T node;
    BigDecimal heuristic;

    public AStarNode(T node, BigDecimal heuristic) {
        this.node = node;
        this.heuristic = heuristic;
    }

    public BigDecimal getHeuristic() {
        return heuristic;
    }

    @Override
    public int compareTo(AStarNode other) {
        return heuristic.compareTo(other.heuristic);
    }

}
