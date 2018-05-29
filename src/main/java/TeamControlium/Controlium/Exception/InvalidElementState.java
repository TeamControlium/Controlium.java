package TeamControlium.Controlium.Exception;

public class InvalidElementState extends RuntimeException{

    public InvalidElementState() {}

    public InvalidElementState(String message, RuntimeException innerException) {
        super(message,innerException);
    }

    public InvalidElementState(String message) {
        super(message);
    }

}
