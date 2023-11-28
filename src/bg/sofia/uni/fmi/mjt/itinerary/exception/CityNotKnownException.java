package bg.sofia.uni.fmi.mjt.itinerary.exception;

public class CityNotKnownException extends Exception {

    public CityNotKnownException(String message) {
        super(message);
    }

    public CityNotKnownException(String message, Exception e) {
        super(message, e);
    }

}
