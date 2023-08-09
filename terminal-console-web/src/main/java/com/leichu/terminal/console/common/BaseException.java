package com.leichu.terminal.console.common;

import org.springframework.http.HttpStatus;

/**
 * 所有自定义异常的基类.
 *
 * @author leichu 2022/3/14.
 */
public class BaseException extends RuntimeException {

    private static final long serialVersionUID = -4455779312617041257L;
    /**
     * HTTP状态码
     */
    private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    public BaseException() {
        super();
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public BaseException(HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
