package com.nexr.newsfeed;

public class NewsfeedException extends Exception {
    public NewsfeedException() {
        super();
    }

    public NewsfeedException(String message) {
        super(message);
    }

    public NewsfeedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NewsfeedException(Throwable cause) {
        super(cause);
    }

}
