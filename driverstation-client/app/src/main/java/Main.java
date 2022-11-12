import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import rct.low.DriverStationSocket;
import rct.low.InstructionMessage;

public class Main {
    
    public static void main (String[] args) throws Exception {
        new Main().run();
    }
    
    public void run () {
        try {
            DriverStationSocket s = new DriverStationSocket(1711, 5800);
            
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