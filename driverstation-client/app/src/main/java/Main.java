import rct.local.RobotControlTerminal;

public class Main {
    
    public static void main (String[] args) throws Exception {
        new Main().run();
    }
    
    public void run () {
        
        RobotControlTerminal terminal = new RobotControlTerminal(new LocalConsoleManager());
        
        terminal.start();
        
    }
    
}