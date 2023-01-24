package claw.logs;

import java.util.HashMap;
import java.util.Map;

public class LoggerDomain {
    
    private final Map<String, LoggerDomain> subdomains = new HashMap<>();
    private boolean includeAllSubdomains = false;
    
    public synchronized void clearSubdomains () {
        subdomains.clear();
    }
    
    private LoggerDomain addSubdomain (String name) throws InvalidLoggerDomainException {
        if (name.isEmpty() || !isAlphabetic(name))
            throw new InvalidLoggerDomainException();
        
        if (!subdomains.containsKey(name))
            subdomains.put(name, new LoggerDomain());
        return subdomains.get(name);
    }
    
    private LoggerDomain getSubdomain (String name) {
        return subdomains.get(name);
    }
    
    public void addDomainPath (String domainPath) throws InvalidLoggerDomainException {
        String[] subdomains = domainPath.split("\\.");
        LoggerDomain nextDomain = this;
        
        for (int i = 0; i < subdomains.length; i ++)
            nextDomain = nextDomain.addSubdomain(subdomains[i]);
        nextDomain.includeAllSubdomains = true;
    }
    
    public boolean doesDomainPathExist (String domainPath) {
        String[] subdomains = domainPath.split("\\.");
        LoggerDomain nextDomain = this;
        
        for (int i = 0; i < subdomains.length; i ++) {
            if (nextDomain.includeAllSubdomains) return true;
            nextDomain = nextDomain.getSubdomain(subdomains[i]);
            if (nextDomain == null) return false;
        } return true;
    }
    
    private static boolean isAlphabetic (String string) {
        for (int i = 0; i < string.length(); i ++) {
            char c = string.charAt(i);
            if (!Character.isAlphabetic(c))
                return false;
        } return true;
    }
    
    public static boolean isValidDomain (String domain) {
        LoggerDomain x = new LoggerDomain();
        
        try {
            x.addDomainPath(domain);
            return true;
        } catch (InvalidLoggerDomainException e) {
            return false;
        }
    }
    
    public static class InvalidLoggerDomainException extends Exception {
        public InvalidLoggerDomainException () {
            super("Invalid logger domain.");
        }
    }
    
}
