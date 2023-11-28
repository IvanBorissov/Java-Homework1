package bg.sofia.uni.fmi.mjt.itinerary;

public record Location(int x, int y) {

    public static final int METERS_IN_A_KILOMETER = 1000;
    public long calculateManhattanDistance(Location other) {
        return ((long)Math.abs(this.x - other.x) + (long)Math.abs(this.y - other.y)) / METERS_IN_A_KILOMETER;
    }

}