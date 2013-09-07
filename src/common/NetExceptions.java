package common;

/**
 *
 * @author alexisvincent
 */
public class NetExceptions {
    
    public static class BadPacketException extends Exception {}
    public static class BadSessionException extends Exception {}
    public static class RequestTimedOutException extends Exception {}
    
}
