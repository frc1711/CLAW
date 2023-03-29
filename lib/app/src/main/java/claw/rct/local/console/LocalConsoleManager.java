package claw.rct.local.console;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Supplier;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import claw.rct.local.LocalSystem.ConnectionStatus;
import claw.rct.network.low.ConsoleManager;

public class LocalConsoleManager implements ConsoleManager {
    
    private static final char
        BEL = '\u0007',
        ESC = '\u001b';
    
    private final Terminal terminal;
    private final PrintWriter out;
    private final InputManager inputManager;
    
    public LocalConsoleManager () throws IOException {
        terminal = TerminalBuilder.builder()
            .system(true)
            .jansi(true)
            .build();
        out = terminal.writer();
        inputManager = new InputManager(terminal);
        
        setTitle("RAPTORS CLAW - Robot Control Terminal");
        // setScrollingRegion(3, terminal.getHeight() - 1);
    }
    
    @Override
    public String readInputLine () {
        return inputManager.readLine();
    }
    
    @Override
    public boolean hasInputReady () {
        return inputManager.ready();
    }
    
    @Override
    public void clearWaitingInputLines () {
        inputManager.clearBuffer();
    }
    
    @Override
    public void flush () {
        System.out.flush();
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
        if (lines != 0)
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
    
    @Override
    public void terminate () { }
    
    @Override
    public boolean isTerminated () {
        return false;
    }
    
    // See https://learn.microsoft.com/en-us/windows/console/console-virtual-terminal-sequences
    
    public void setTitle (String title) {
        out.print(ESC+"]0;"+title+BEL);
    }
    
    private void writeToTop (String text) {
        out.print(Ansi.ansi().saveCursorPosition());
        out.print(ESC+"[1;1H");
        out.print(Ansi.ansi().a(text).restoreCursorPosition());
        // out.print(ESC+"[1;1H");
    }
    
    private void setScrollingRegion (int top, int bottom) {
        out.print(ESC+"["+top+";"+bottom+"r");
    }
    
    private void useAlternateScrollingRegion () {
        out.print(ESC+"[?1049h");
    }
    
    private void setLineDrawMode (boolean enabled) {
        if (enabled) {
            out.print(ESC+"(0");
        } else {
            out.print(ESC+"(B");
        }
    }
    
}
