import java.util.Scanner;

import rct.local.LocalSystem;
import rct.local.StreamDataStorage;

public class Main {
    
    public static void main (String[] args) throws Exception {
        new Main().run();
    }
    
    public void run () {
        
        ColorConsoleManager mgr = new ColorConsoleManager();
        Scanner scanner = new Scanner(System.in);
        
        try {
            LocalSystem system = new LocalSystem(1711, 5800, 5, new StreamDataStorage(), mgr);
            while (true) {
                System.out.print("\n> ");
                System.out.flush();
                system.processCommand(scanner.nextLine());
                System.out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        scanner.close();
        
    }
    
}