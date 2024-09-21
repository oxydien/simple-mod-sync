package dev.oxydien.logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Log {
    public static Log Log = null;

    private final String name;
    private final Logger logger;
    private final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{}");

    public Log(String logName, boolean setStatic) {
        this.name = logName;
        this.logger = LoggerFactory.getLogger(this.name);

        if (setStatic) {
            Log = this;
        }
    }

    public enum LogLevel {
        INFO, WARN, DEBUG, DEV, ERROR, FATAL
    }

    public void log(LogLevel level, String placement, String message, Object... args) {
        String formattedMessage = this.formatMessage(message, args);
        String logMessage = String.format("[%s] (%s): %s", this.name, placement, formattedMessage);

        switch (level) {
            case INFO:
                logger.info(logMessage);
                break;
            case WARN:
                logger.warn(logMessage);
                break;
            case DEBUG:
                logger.info("[DEBUG] {}", logMessage);
                break;
            case DEV:
                logger.info("[DEV] {}", logMessage);
                break;
            case ERROR:
                logger.error(logMessage);
                break;
            case FATAL:
                logger.error("[FATAL] {}", logMessage);
                break;
        }
    }

    private String formatMessage(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(message);
        StringBuilder result = new StringBuilder();
        int argIndex = 0;

        while (matcher.find()) {
            if (argIndex < args.length) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(String.valueOf(args[argIndex])));
                argIndex++;
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement("{}"));
            }
        }
        matcher.appendTail(result);

        while (argIndex < args.length) {
            result.append(" ").append(args[argIndex]);
            argIndex++;
        }

        return result.toString();
    }

    public void info(String message, Object... args) {
        this.log(LogLevel.INFO, "", message, args);
    }

    public void info(String placement, String message, Object... args) {
        this.log(LogLevel.INFO, placement, message, args);
    }

    public void warn(String message, Object... args) {
        this.log(LogLevel.WARN, "", message, args);
    }

    public void warn(String placement, String message, Object... args) {
        this.log(LogLevel.WARN, placement, message, args);
    }

    public void debug(String message, Object... args) {
        this.log(LogLevel.DEBUG, "", message, args);
    }

    public void debug(String placement, String message, Object... args) {
        this.log(LogLevel.DEBUG, placement, message, args);
    }

    // Dev does not have a method without placement, so the developer can find the logs
    public void dev(String placement, String message, Object... args) {
        this.log(LogLevel.DEV, placement, message, args);
    }

    public void error(String message, Object... args) {
        this.log(LogLevel.ERROR, "", message, args);
    }

    public void error(String placement, String message, Object... args) {
        this.log(LogLevel.ERROR, placement, message, args);
    }

    public void fatal(String message, Object... args) {
        this.log(LogLevel.FATAL, "", message, args);
    }

    public void fatal(String placement, String message, Object... args) {
        this.log(LogLevel.FATAL, placement, message, args);
    }
}