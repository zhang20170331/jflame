package org.jflame.toolkit.exception;

/**
 * 数据库处理异常类
 * 
 * @author zyc
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException() {
        super();
    }

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(Throwable exception) {
        super(exception);
    }

    public DataAccessException(String message, Throwable exception) {
        super(message, exception);
    }
}
