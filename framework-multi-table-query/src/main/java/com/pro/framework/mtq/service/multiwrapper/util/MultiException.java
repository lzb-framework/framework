package com.pro.framework.mtq.service.multiwrapper.util;

/**
 * @author Administrator
 */
public class MultiException extends RuntimeException {
    public MultiException(String message) {
        super(message);
    }

    public MultiException(Throwable cause) {
        super(cause);
    }
}
