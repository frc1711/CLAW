package localrct.console;

import java.util.function.Supplier;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.platform.win32.WinUser;

/**
 * The DriverStation app has a windows hook to check if the enter key or the spacebar was pressed, no matter what
 * window is focused. If the robot is enabled, enter will disable the robot and space will e-stop the robot. The purpose
 * of this class is to install another key hook which intercepts these keys before the DriverStation app and prevents it
 * from receiving these keys only if the driverstation Robot Control Terminal is focused.
 * <br></br>
 * SAFETY NOTICE: Although this type of behavior may be considered unsafe among some FRC teams, we justify this use case
 * with the following information. A safety notice is printed on startup explaining the key hook and all the following as well.
 * If the driver station RCT is running, then the DriverStation app should be focused whenever the robot is actively being
 * controlled. The RCT should only be focused while the robot is enabled if the robot is in a state where it cannot hurt
 * anyone (propped on its side, for example). Additionally, the enter key can be double pressed at any time while the RCT is focused
 * in order to disable the robot.
 * TODO: DS hook safety notice
 * <br></br>
 * The following class is adapted from an answer on Stack Overflow.
 * https://stackoverflow.com/a/7793900
 */
class DriverStationDisableKeysHook {
    
    private static final int
        VK_RETURN = 0x0D,
        VK_SPACE = 0x20;
    
    private static final long RETURN_KEY_DOUBLE_TAP_INTERVAL_MILLIS = 250;
    private static long lastReturnKeyTapEpoch = 0;
    
    private static HHOOK hhk;
    private static LowLevelKeyboardProc keyboardHook;
    private static User32 lib;
    
    private static boolean hasInstalledHook = false;
    
    public static void installKeyHook (Supplier<Boolean> isRCTFocused, Runnable onEnterKey, Runnable onSpaceKey) {
        if (!hasInstalledHook && isWindows()) {
            hasInstalledHook = true;
            new Thread(() -> keysHookThreadRunnable(isRCTFocused, onEnterKey, onSpaceKey)).start();
        }
    }
    
    private static void keysHookThreadRunnable (Supplier<Boolean> isRCTFocused, Runnable onEnterKey, Runnable onSpaceKey) {
        lib = User32.INSTANCE;
        HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
        
        // This is a callback for whenever the process sees a key being pressed
        keyboardHook = (int nCode, WPARAM wParam, KBDLLHOOKSTRUCT info) -> {
            
            // See https://learn.microsoft.com/en-us/previous-versions/windows/desktop/legacy/ms644985(v=vs.85)
            // nCode should only ever be 0 but according to microsoft we must make the nCode >= 0 check
            // Also, only ever try to intercept anything if the RCT is focused
            if (nCode >= 0 && isRCTFocused.get()) {
                
                int wParamCode = wParam.intValue();
                
                // Only do anything to the key event if the key was pressed down (rather than released)
                // Ignore WM_SYSKEYDOWN as system keys won't interfere
                if (wParamCode == WinUser.WM_KEYDOWN) {
                    
                    // If the enter or spacebar was pressed, intercept the event
                    
                    if (info.vkCode == VK_RETURN) {
                        
                        // Get the current time in milliseconds
                        long epoch = System.currentTimeMillis();
                        
                        // If the time is outside the range of being considered a double tap,
                        // then intercept the event and prevent the DS from seeing the return key press
                        if (epoch > lastReturnKeyTapEpoch + RETURN_KEY_DOUBLE_TAP_INTERVAL_MILLIS) {
                            lastReturnKeyTapEpoch = epoch;
                            onEnterKey.run();
                            return new LRESULT(1);
                        } else onEnterKey.run();
                        
                        // Otherwise, do not capture the input so that the DS can disable (return key
                        // event was considered to be a double tap)
                        
                        // The enter key runnable should still be run though, so that
                        // any functionality built off the runnable doesn't need to also be implemented
                        // to work for enter keys received through stdin
                        
                    } else if (info.vkCode == VK_SPACE) {
                        onSpaceKey.run();
                        return new LRESULT(1);
                    }
                    
                }
            }
            
            // Pass the event to the next key hook (no special behavior)
            return lib.CallNextHookEx(hhk, nCode, wParam, new LPARAM(info.getPointer().getLong(0)));
        };
        
        hhk = lib.SetWindowsHookEx(13, keyboardHook, hMod, 0);

        // This bit never returns from GetMessage
        int result;
        MSG msg = new MSG();
        while ((result = lib.GetMessage(msg, null, 0, 0)) != 0) {
            if (result == -1) {
                break;
            } else {
                lib.TranslateMessage(msg);
                lib.DispatchMessage(msg);
            }
        }
        
        lib.UnhookWindowsHookEx(hhk);
    }
    
    private static boolean isWindows () {
        String os = System.getProperty("os.name").toLowerCase();
        return os.indexOf("win") >= 0;
    }
    
}
