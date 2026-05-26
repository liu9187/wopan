package cn.com.fanone.wopan;

public class WoPanException extends RuntimeException {
    public WoPanException(String message) {
        super(message);
    }

    public WoPanException(String message, Throwable cause) {
        super(message, cause);
    }
}
