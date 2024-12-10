package com.pro.framework.javatodb.util;

public class JTDException extends Exception{
    public JTDException() {
    }

    public JTDException(String message) {
        super(message);
    }

    public JTDException(String message, Throwable cause) {
        super(message, cause);
    }

    public JTDException(Throwable cause) {
        super(cause);
    }

    public JTDException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
