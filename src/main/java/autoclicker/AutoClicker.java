package autoclicker;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class AutoClicker {
    
    private static Robot r;
    private final static int MOUSE_BUTTON_LEFT = InputEvent.BUTTON1_DOWN_MASK;
    private static int msBetweenPressAndRelease = 0;
    private static int msBetweenClicks = 100;
    private static boolean on = false;
    private static boolean isCounting = false;
    private static Integer count = 0;

    static {
        try {
            r = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static int getMsBetweenPressAndRelease() {
        return msBetweenPressAndRelease;
    }

    public static void setMsBetweenPressAndRelease(int msBetweenPressAndRelease) {
        AutoClicker.msBetweenPressAndRelease = msBetweenPressAndRelease;
    }

    public static int getMsBetweenClicks() {
        return msBetweenClicks;
    }

    public static void setMsBetweenClicks(int msBetweenClicks) {
        AutoClicker.msBetweenClicks = msBetweenClicks;
    }

    /**
     * 
     * @return whether autoClicking is on
     */
    public static boolean toggle() {
        on = !on;
        return on;
    }

    public static boolean isOn() {
        return on;
    }

    /**
     * Returns the number of clicks since last method call (if counting is active) and resets the counter.
     * 
     * @return the number of clicks since last getCount()
     */
    public static Integer getCount() {
        Integer ret = count;
        count = 0;
        return ret;
    }

    public static boolean isCounting() {
        return isCounting;
    }

    /**
     * 
     * @return whether the clicker is counting clicks now
     */
    public static boolean toggleCounting() {
        isCounting = !isCounting;
        return isCounting;
    }

    private static void waitMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void click() {
        r.mousePress(MOUSE_BUTTON_LEFT);
        waitMs(msBetweenPressAndRelease);
        r.mouseRelease(MOUSE_BUTTON_LEFT);

        if (isCounting) {
            count++;
        }
    }

    public static void run() {
        while (true) {
            if (on) {
                click();
            }
            waitMs(msBetweenClicks);
        }
    }
}
