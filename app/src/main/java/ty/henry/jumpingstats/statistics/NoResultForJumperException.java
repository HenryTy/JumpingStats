package ty.henry.jumpingstats.statistics;

public class NoResultForJumperException extends Exception {

    public NoResultForJumperException() {}

    public NoResultForJumperException(String message) {
        super(message);
    }

    public NoResultForJumperException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoResultForJumperException(Throwable cause) {
        super(cause);
    }
}
