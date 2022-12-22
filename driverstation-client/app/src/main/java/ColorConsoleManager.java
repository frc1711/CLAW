import java.io.IOException;
import java.io.PrintStream;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.Ansi.Color;

import rct.local.ConsoleManager;

public class ColorConsoleManager extends ConsoleManager {
    
    private final PrintStream out;
    
    public ColorConsoleManager () {
        AnsiConsole.systemInstall();
        out = AnsiConsole.out();
    }
    
    @Override
    public void print (String msg) {
        out.print(msg);
    }
    
    @Override
    public void printErr (String msg) {
        out.print(Ansi.ansi().fgRed().a(msg).fg(Color.WHITE));
    }
    
    @Override
    public void printSys (String msg) {
        out.print(Ansi.ansi().fgYellow().a(msg).fg(Color.WHITE));
    }
    
    @Override
    public void clear () {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception e) {
            println("\n".repeat(20));
        }
    }
    
    @Override
    public void moveUp (int lines) {
        out.print(Ansi.ansi().cursorUpLine(lines));
    }
    
    @Override
    public void clearLine () {
        out.print(Ansi.ansi().eraseLine());
    }
    
    @Override
    public void saveCursorPos () {
        out.print(Ansi.ansi().saveCursorPosition());
    }
    
    @Override
    public void restoreCursorPos () {
        out.print(Ansi.ansi().restoreCursorPosition());
    }
    
}
