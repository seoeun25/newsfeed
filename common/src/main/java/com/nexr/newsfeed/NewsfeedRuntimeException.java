package com.nexr.newsfeed;

public class NewsfeedRuntimeException extends RuntimeException {
    public NewsfeedRuntimeException() {
        super();
    }

    public NewsfeedRuntimeException(String message) {
        super(message);
    }

    public NewsfeedRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NewsfeedRuntimeException(Throwable cause) {
        super(cause);
    }

}
