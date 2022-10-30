import java.util.Scanner;

import rct.low.DataMessage;
import rct.low.TerminalConnector;

public class Main {
    
    public static void main (String[] args) throws Exception {
        new Main().run();
    }
    
    public void run () throws Exception {
        Scanner sc = new Scanner(System.in);
        TerminalConnector c = new TerminalConnector((DataMessage message) -> {
            System.out.println(message.dataString);
        }, true);
        while (true) {
            String line = sc.nextLine();
            c.put(new DataMessage(DataMessage.MessageType.INSTRUCTION, 1, line));
        }
        
    }
    
}