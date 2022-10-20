import rct.low.TerminalConnector;

public class Main {
    
    public static void main (String[] args) throws Exception {
        new Main().run();
    }
    
    public void run () throws Exception {
        TerminalConnector c = new TerminalConnector(true);
        try {
            byte byteval = 0;
            while (true) {
                Thread.sleep(2000);
                System.out.println("making call... " + byteval);
                c.makeCall(new byte[]{byteval});
                byteval ++;
            }
        } catch (Exception e) {
            
        }
    }
    
}