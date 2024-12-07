package com.pro.framework.api.model;


import lombok.Data;

@Data
public class FrameworkException extends RuntimeException {
    private String template;
    private Object[] params;

    public FrameworkException() {
    }

    public FrameworkException(String template, Object... params) {
        super(template);
        this.template = template;
        this.params = params;
    }

//    public FrameworkException(String message) {
//        super(message);
//    }
//
//    public FrameworkException(String message, Throwable cause) {
//        super(message, cause);
//    }
//
//    public FrameworkException(Throwable cause) {
//        super(cause);
//    }

}
