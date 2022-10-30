import java.util.Scanner;

import rct.commands.Command;
import rct.commands.Command.ParseException;
import rct.low.DataMessage;
import rct.low.TerminalConnector;

public class Main {
    
    public static void main (String[] args) throws Exception {
        new Main().run();
    }
    
    public void run () {
        Scanner sc = new Scanner(System.in);
        
        TerminalConnector c = new TerminalConnector((DataMessage message) -> {
            System.out.println(message.dataString);
        }, true);
        
        while (true) {
            String line = sc.nextLine();
            
            try {
                // Command is created in order to ensure the line being sent is a properly formatted command
                Command command = new Command(line);
                if (command.getCommand().toLowerCase().equals("exit")) System.exit(0);
            } catch (ParseException e) {
                System.err.println(e);
            }
            
            c.put(new DataMessage(DataMessage.MessageType.INSTRUCTION, 1, line));
        }
        
    }
    
}