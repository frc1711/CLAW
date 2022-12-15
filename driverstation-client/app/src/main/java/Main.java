import rct.local.RobotControlTerminal;

public class Main {
    
    public static void main (String[] args) throws Exception {
        new Main().run();
    }
    
    public void run () {
        
        ColorConsoleManager mgr = new ColorConsoleManager();
        RobotControlTerminal terminal = new RobotControlTerminal(mgr);
        
        terminal.start();
        
    }
    
}