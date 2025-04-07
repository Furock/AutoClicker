package autoclicker;

import java.awt.GridLayout;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;

public class Main {

    /**
     * List of components that should be deactivated, when AutoClicker is on
     */
    private static List<JComponent> deactivatables = new ArrayList<>();

    private static JLabel autoClickingOn = new JLabel("OFF");
    private static Thread clickCountingThread = null;
    private static JLabel clickCountingLabel = new JLabel("N/A");

    private static JFrame infoFrame;
    private static NumberFormatter numberFormatter = new NumberFormatter() {
        @Override
        public Object stringToValue(String text) throws ParseException {
            if (text == null || text.isBlank()) {
                return null;
            } else {
                return super.stringToValue(text);
            }
        }
    };

    private static JFormattedTextField timeBetweenPressAndReleaseField = new JFormattedTextField(numberFormatter);
    private static JFormattedTextField timeBetweenClicksField = new JFormattedTextField(numberFormatter);

    static {
        infoFrame = new JFrame("");
        infoFrame.setAlwaysOnTop(true);
        infoFrame.setUndecorated(true);
        infoFrame.add(new JLabel("AutoClicker ON"));
        infoFrame.pack();

        numberFormatter.setFormat(NumberFormat.getIntegerInstance());
        numberFormatter.setAllowsInvalid(false);
        numberFormatter.setMinimum(0);
        numberFormatter.setCommitsOnValidEdit(true);

        timeBetweenPressAndReleaseField.setValue(AutoClicker.getMsBetweenPressAndRelease());;
        deactivatables.add(timeBetweenPressAndReleaseField);

        timeBetweenClicksField.setValue(AutoClicker.getMsBetweenClicks());
        deactivatables.add(timeBetweenClicksField);
    }

    private static void waitMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void infoFrame(boolean on) {
        infoFrame.setVisible(on);
    }

    private static void setupFrame() {
        JFrame window = new JFrame("AutoClicker");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new GridLayout(4, 2));
        window.add(panel);

        panel.add(new JLabel("Press SHIFT to toggle autoclicking. Currently: "));
        panel.add(autoClickingOn);

        JButton clickCountButton = new JButton("Stats > ON");
        deactivatables.add(clickCountButton);
        clickCountButton.addActionListener(e -> {
            if (!AutoClicker.isOn()) {
                boolean isCounting = AutoClicker.toggleCounting();
                if (isCounting) {
                    clickCountButton.setText("Stats > OFF");
                } else {
                    clickCountButton.setText("Stats > ON");
                    clickCountingLabel.setText("N/A");
                }   
            }
        });
        panel.add(clickCountButton);
        panel.add(clickCountingLabel);
        clickCountingLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel timeBetweenPressAndReleaseLabel = new JLabel("Time between clicking and \r\nreleasing the click (in ms):");

        panel.add(timeBetweenPressAndReleaseLabel);
        panel.add(timeBetweenPressAndReleaseField);

        JLabel timeBetweenClicksLabel = new JLabel("Time between two clicks (in ms):");

        panel.add(timeBetweenClicksLabel);
        panel.add(timeBetweenClicksField);

        window.pack();
        window.setVisible(true);
    }


    public static void main(String[] args) {
        Thread acThread = new Thread(() -> {
            AutoClicker.run();
            System.out.println("Unexpected end of AutoClicker Thread");
        });
        acThread.setDaemon(true);
        acThread.start();
        GlobalKeyListener shiftL = new GlobalKeyListener(() -> {
            //When autoClicker is off, it is gonna be activated now, so we want to stop interaction with the window beforehand
            boolean on = AutoClicker.isOn();
            for (JComponent comp : deactivatables) {
                if (comp instanceof JButton) {
                    comp.setEnabled(on);
                } else if (comp instanceof JTextField) {
                    ((JTextField) comp).setEditable(on);
                } else {
                    System.err.println("Not handled deactivatable component of type " + comp.getClass().getSimpleName());
                }
            }
            
            on = AutoClicker.toggle();
            infoFrame(on);
            autoClickingOn.setText(on ? "ON" : "OFF");
            

            if (on) {
                Integer msBetweenPressAndRelease = (Integer) timeBetweenPressAndReleaseField.getValue();
                if (msBetweenPressAndRelease == null) {
                    msBetweenPressAndRelease = 0;
                    timeBetweenPressAndReleaseField.setValue(0);
                }
                AutoClicker.setMsBetweenPressAndRelease(msBetweenPressAndRelease);


                Integer msBetweenClicks = (Integer) timeBetweenClicksField.getValue();
                if (msBetweenClicks == null) {
                    msBetweenClicks = 0;
                    timeBetweenClicksField.setValue(0);
                }
                AutoClicker.setMsBetweenClicks(msBetweenClicks);
            }

            if (on && AutoClicker.isCounting()) {
                clickCountingThread = new Thread(() -> {
                    AutoClicker.getCount();
                    waitMs(1000);
                    List<Integer> stats = new ArrayList<>();

                    while (AutoClicker.isOn() && AutoClicker.isCounting()) {
                        stats.add(AutoClicker.getCount());

                        if (stats.size() == 11) stats.remove(0);

                        double average = stats.stream().mapToInt(x -> x).average().getAsDouble();
                        clickCountingLabel.setText(String.format("%.1f 1/s", average));
                        waitMs(1000);
                    }
                });
                clickCountingThread.start();
            } else if (!on) {
                if (clickCountingThread != null && clickCountingThread.isAlive()) {
                    try {
                        boolean clickCountingThreadFinished = clickCountingThread.join(Duration.ofSeconds(1));
                        if (!clickCountingThreadFinished) {
                            System.out.println("ClickCounting Thread is still alive after 1s although expected to be finished.");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                }
                clickCountingThread = null;
            }
        });
        shiftL.run();

        setupFrame();
    }
}
