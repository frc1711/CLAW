package claw.rct.local;

/**
 * The entry point for the driverstation RCT client program.
 */
public class LocalMain {
    
    public static void main (String[] args) {
        try {
            new RobotControlTerminal().start();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
}
