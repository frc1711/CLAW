package claw;

import claw.logs.LogHandler;
import claw.logs.LoggerDomain;
import claw.rct.network.messages.LogDataMessage.LogData;

public class CLAWLogger {
    
    /**
     * Gets a new {@link CLAWLogger} given a new logger domain string.
     * Logger domains are used for allowing the driver station to easily start and stop watching
     * groups of logs at a time. Log data from a watched domain will be sent to the driverstation,
     * whereas log data from an unwatched domain will be ignored.
     * <br></br>
     * For this reason, {@code CLAWLogger}s can be used everywhere without consequence, allowing
     * for easy debugging when necessary and no drawback at any other time.
     * @param domain The logger domain to use. Logger domains should follow java package
     * naming conventions. It is a good idea to coordinate this domain with the domains of other loggers,
     * as logical groupings of domains will allow the driverstation to more easily enable
     * and disable logical groupings of types of debug data it may need. An example for a logger domain
     * could be {@code subsystems.swerve.frontLeft.drive}.
     * @return A {@code CLAWLogger}
     */
    public static CLAWLogger getLogger (String domain) throws IllegalLoggerDomainException {
        // Check if the domain is valid
        if (!LoggerDomain.isValidDomain(domain))
            throw new IllegalLoggerDomainException(domain);
        
        LogHandler.getInstance().registerDomain(domain);
        return new CLAWLogger(domain);
    }
    
    private final String domain;
    
    private CLAWLogger (String domain) {
        this.domain = domain;
    }
    
    public CLAWLogger sublog (String subdomain) {
        return getLogger(domain + "." + subdomain);
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
    
    public static class IllegalLoggerDomainException extends RuntimeException {
        public IllegalLoggerDomainException (String name) {
            super("The logger domain '"+name+"' is invalid.");
        }
    }
    
}
