package claw.internal.rct.local;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;

/**
 * The entry point for the driverstation RCT client program.
 */
public class LocalMain {
    
    public static void main (String[] args) {
        try {
            new RobotControlTerminal().start();
        } catch (Throwable exception) {
            StringWriter exceptionMessage = new StringWriter();
            exception.printStackTrace(new PrintWriter(exceptionMessage));
            JOptionPane.showMessageDialog(null, exceptionMessage, exception.getClass().getName(), JOptionPane.ERROR_MESSAGE);
        }
    }
    
}
