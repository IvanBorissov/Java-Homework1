package bg.sofia.uni.fmi.mjt.itinerary;

import java.math.BigDecimal;

public record City(String name, Location location) {

    public static final int AVERAGE_PRICE_BY_KM = 20;

    public BigDecimal calculateHeuristic(City to) {
        return BigDecimal.valueOf(location.calculateManhattanDistance(to.location) * AVERAGE_PRICE_BY_KM);
    }

}