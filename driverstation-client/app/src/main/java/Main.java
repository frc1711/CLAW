import rct.low.TerminalConnector;

public class Main {
    
    public static void main (String[] args) throws Exception {
        new Main().run();
    }
    
    public void run () throws Exception {
        TerminalConnector c = new TerminalConnector(true);
        try {
            while (true) {
                Thread.sleep(4000);
                System.out.println("making call... ");
                c.put(("new value is: "+Math.round(Math.random()*1000)).getBytes());
                c.updateOutputBuffer();
                
                Thread.sleep(1000);
                for (int i = 1; i <= 10; i ++) {
                    c.put((i+") Several calls at once").getBytes());
                }
                c.updateOutputBuffer();
            }
        } catch (Exception e) {
            
        }
    }
    
}