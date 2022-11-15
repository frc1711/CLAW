import java.io.IOException;
import java.util.function.Consumer;

import rct.low.DriverStationSocketHandler;
import rct.low.InstructionMessage;
import rct.low.ResponseMessage;

public class Main {
    
    public static void main (String[] args) throws Exception {
        new Main().run();
    }
    
    public void run () {
        Consumer<IOException> excHandler = (IOException e) -> {
            e.printStackTrace();
            System.exit(1);
        };
        
        try {
            DriverStationSocketHandler s = new DriverStationSocketHandler(1711, 5800, (ResponseMessage m) -> {
                System.out.println("Received a response message: " + m);
            }, excHandler);
            
            for (int i = 1; i <= 50; i ++) {
                System.out.println("Sending message #"+i);
                s.sendInstructionMessage(new InstructionMessage.StreamsList());
                Thread.sleep(i*10);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        // } catch (UnknownHostException e) {
        //     e.printStackTrace();
        //     System.exit(1);
        // } catch (SocketException e) {
        //     System.out.println("Socket closed unexpectedly: " + e);
        //     System.exit(1);
        // } catch (IOException e) {
        //     e.printStackTrace();
        //     System.exit(1);
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        //     System.exit(1);
        // }
    }
    
}