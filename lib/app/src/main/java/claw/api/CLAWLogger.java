package claw.api;

import java.util.HashSet;
import java.util.Set;

import claw.internal.logs.LogHandler;
import claw.internal.logs.LoggerDomain;
import claw.internal.rct.network.messages.LogDataMessage.LogData;

public class CLAWLogger {
    
    private static final Set<String> usedLogDomains = new HashSet<>();
    
    /**
     * Gets a new {@link CLAWLogger} given a new logger domain string. Logger domain strings
     * should be unique for each log. If two {@code CLAWLogger}s are created with the same
     * domain, a {@link LogDomainConflict} exception will be thrown.
     * Logger domains are used for allowing the driver station to easily start and stop watching
     * groups of logs at a time. Log data from a watched domain will be sent to the driverstation,
     * whereas log data from an unwatched domain will be ignored.
     * <br></br>
     * For this reason, {@code CLAWLogger}s can be used everywhere without consequence, allowing
     * for easy debugging when necessary and no drawback at any other time.
     * @param domain The unique logger domain to use. Logger domains should follow java package
     * naming conventions. It is a good idea to coordinate this domain with the domains of other loggers,
     * as logical groupings of domains will allow the driverstation to more easily enable
     * and disable logical groupings of types of debug data it may need.
     * @return A {@code CLAWLogger}
     */
    public static CLAWLogger getLogger (String domain) throws LogDomainConflict, IllegalLoggerDomainException {
        // Check if the domain is valid
        if (!LoggerDomain.isValidDomain(domain))
            throw new IllegalLoggerDomainException(domain);
        
        if (usedLogDomains.contains(domain))
            throw new LogDomainConflict(domain);
        usedLogDomains.add(domain);
        return new CLAWLogger(domain);
    }
    
    private final String domain;
    
    private CLAWLogger (String domain) {
        this.domain = domain;
    }
    
    public String getDomain () {
        return domain;
    }
    
    public void out (String message) {
        LogHandler.getInstance().addData(new LogData(domain, message, false));
    }
    
    public void err (String message) {
        LogHandler.getInstance().addData(new LogData(domain, message, true));
    }
    
    public static Set<String> getUsedDomains () {
        HashSet<String> usedDomains = new HashSet<>();
        usedLogDomains.forEach(domain -> usedDomains.add(domain));
        return usedDomains;
    }
    
    public static class LogDomainConflict extends RuntimeException {
        public LogDomainConflict (String name) {
            super("The logger domain '"+name+"' is already in use.");
        }
    }
    
    public static class IllegalLoggerDomainException extends RuntimeException {
        public IllegalLoggerDomainException (String name) {
            super("The logger domain '"+name+"' is invalid.");
        }
    }
    
}
