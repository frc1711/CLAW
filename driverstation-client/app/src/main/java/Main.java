import java.util.Scanner;

import rct.low.TerminalConnector;

public class Main {
    
    public static void main (String[] args) throws Exception {
        new Main().run();
    }
    
    public void run () throws Exception {
        Scanner sc = new Scanner(System.in);
        TerminalConnector c = new TerminalConnector(true);
        while (true) {
            String line = sc.nextLine();
            c.put(line.getBytes());
        }
        
    }
    
}