package claw.internal.rct.local;

import javax.swing.JOptionPane;

/**
 * The entry point for the driverstation RCT client program.
 */
public class LocalMain {
    
    public static void main (String[] args) {
        try {
            new RobotControlTerminal().start();
        } catch (Throwable exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), exception.getClass().getName(), JOptionPane.ERROR_MESSAGE);
        }
    }
    
}
