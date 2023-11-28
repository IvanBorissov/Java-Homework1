package bg.sofia.uni.fmi.mjt.itinerary;

public record City(String name, Location location) {

    public static final int AVERAGE_PRICE_BY_KM = 20;

    public long calculateHeuristic(City to) {
        return location.calculateManhattanDistance(to.location) * AVERAGE_PRICE_BY_KM;
    }

}