import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.Ansi.Color;

import rct.local.ConsoleManager;

public class ColorConsoleManager extends ConsoleManager {
    
    private boolean installed = false;
    
    @Override
    public void print (String msg) {
        install();
        System.out.print(msg);
    }
    
    @Override
    public void printErr (String msg) {
        install();
        System.out.print(Ansi.ansi().fgRed().a(msg).fg(Color.WHITE));
    }
    
    @Override
    public void printSys (String msg) {
        install();
        System.out.print(Ansi.ansi().fgYellow().a(msg).fg(Color.WHITE));
    }
    
    private void install () {
        if (!installed)
            AnsiConsole.systemInstall();
        installed = true;
    }
    
}
