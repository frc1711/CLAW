package rct.local;

public abstract class ConsoleManager {
    
    public abstract void print (String msg);
    public void println (String msg) {
        print(msg + "\n");
    }
    
    public abstract void printErr (String msg);
    public void printlnErr (String msg) {
        printErr(msg + "\n");
    }
    
    public abstract void printSys (String msg);
    public void printlnSys (String msg) {
        printSys(msg + "\n");
    }
}
