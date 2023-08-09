package com.leichu.terminal.console.common;

import org.springframework.http.HttpStatus;

/**
 * 业务异常.
 *
 * @author leichu 2022/3/14.
 */
public class BizException extends BaseException {

    private static final long serialVersionUID = -8546923065773782721L;
    private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    public BizException() {
        super();
    }

    public BizException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
        this.httpStatus = httpStatus;
    }

    public BizException(String message) {
        super(message);
    }


    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

}
