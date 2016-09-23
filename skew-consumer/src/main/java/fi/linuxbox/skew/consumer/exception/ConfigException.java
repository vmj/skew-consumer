package fi.linuxbox.skew.consumer.exception;

/**
 *
 */
public class ConfigException extends RuntimeException {
    public ConfigException(final String message) {
        super(message);
    }

    public ConfigException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
