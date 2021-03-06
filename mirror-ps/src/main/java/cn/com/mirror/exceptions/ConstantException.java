/**
 *
 */
package cn.com.mirror.exceptions;

/**
 * @author Piggy
 * @description
 * @date 2018年4月27日
 */
public class ConstantException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ConstantException(String message) {
        super(message);
    }

    public ConstantException(String message, Throwable cause) {
        super(message, cause);
    }
}
