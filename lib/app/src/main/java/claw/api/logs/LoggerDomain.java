package claw.api.logs;

import java.util.HashMap;
import java.util.Map;

import claw.api.logs.CLAWLogger.InvalidDomainPathException;

class LoggerDomain {
    
    private final Map<String, LoggerDomain> subdomains = new HashMap<>();
    
    public synchronized void clearSubdomains () {
        subdomains.clear();
    }
    
    private LoggerDomain addSubdomain (String name) throws InvalidDomainPathException {
        if (name.isEmpty() || !isAlphabetic(name))
            throw new InvalidDomainPathException();
        
        if (!subdomains.containsKey(name))
            subdomains.put(name, new LoggerDomain());
        return subdomains.get(name);
    }
    
    private LoggerDomain getSubdomain (String name) {
        return subdomains.get(name);
    }
    
    public void addDomainPath (String domainPath) throws InvalidDomainPathException {
        String[] subdomains = domainPath.split("\\.");
        LoggerDomain nextDomain = this;
        
        for (int i = 0; i < subdomains.length; i ++)
            nextDomain = nextDomain.addSubdomain(subdomains[i]);
    }
    
    public boolean doesDomainPathExist (String domainPath) {
        String[] subdomains = domainPath.split("\\.");
        LoggerDomain nextDomain = this;
        
        for (int i = 0; i < subdomains.length; i ++) {
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
    
}
