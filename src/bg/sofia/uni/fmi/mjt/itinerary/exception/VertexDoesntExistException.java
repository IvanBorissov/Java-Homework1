package bg.sofia.uni.fmi.mjt.itinerary.exception;

public class VertexDoesntExistException extends RuntimeException {

    public VertexDoesntExistException(String message) {
        super(message);
    }

    public VertexDoesntExistException(String message, Exception e) {
        super(message, e);
    }

}
