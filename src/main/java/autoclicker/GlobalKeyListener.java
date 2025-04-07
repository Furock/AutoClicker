package autoclicker;

import java.util.ArrayList;
import java.util.List;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

/**
 * Note for myself: two GlobalKeyListener don't seem to work
 */
public class GlobalKeyListener implements NativeKeyListener {

    private boolean isRunning = false;
    //private OutputStream oStream =
    List<Integer> keyIdList;
    List<Integer> pressedKeys = new ArrayList<>();
    Runnable action;
    boolean actionAlreadyRun = false;
    
    /**
     * 
     * @param action that is triggered when all expected keys are pressed
     * @param keyIds NativeKeyEvent keys to listen for
     */
    public GlobalKeyListener(Runnable action, Integer... keyIds) {
        this.action = action;
        if (keyIds == null || keyIds.length == 0) {
            keyIdList = List.of(NativeKeyEvent.VC_SHIFT);
        } else {
            keyIdList = List.of(keyIds);
        }
    } 

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        //System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        if (keyIdList.contains(e.getKeyCode())) {
            if (!pressedKeys.contains(e.getKeyCode())) {
                pressedKeys.add(e.getKeyCode());
            }
        }

        if (!actionAlreadyRun && keyIdList.equals(pressedKeys)) {
            action.run();
            actionAlreadyRun = true;
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        //System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        
        if (keyIdList.contains(e.getKeyCode())) {
            boolean contained = pressedKeys.remove(Integer.valueOf(e.getKeyCode()));
            if (contained) {
                //reset the already run flag so that it can run next time when the correct keys are pressed 
                actionAlreadyRun = false;
            }
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        //do nothing; typing is not recognized
        //System.out.print(e.getKeyChar());
    }

    public void run() {
        
        if (isRunning) {
            System.out.println("GlobalKeyListener is already running.");
            return;
        }

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
        }

        //Construct the example object and initialze native hook.
        GlobalScreen.addNativeKeyListener(this);

        isRunning = true;
    }

    public void stop() {
        if (!isRunning) {
            System.out.println("GlobalKeyListener was not running.");
            return;
        }

        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem unregistering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
        }
    }
}