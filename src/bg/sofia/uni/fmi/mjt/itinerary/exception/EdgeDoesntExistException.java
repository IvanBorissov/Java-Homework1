package bg.sofia.uni.fmi.mjt.itinerary.exception;

public class EdgeDoesntExistException extends RuntimeException {

    public EdgeDoesntExistException(String message) {
        super(message);
    }

    public EdgeDoesntExistException(String message, Exception e) {
        super(message, e);
    }

}
