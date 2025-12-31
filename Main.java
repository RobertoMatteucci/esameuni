package com.game;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * The application entry point.
 * Initializes the sound system, displays the main menu, and launches the game
 * window.
 */
public class Main {

    public static void main(String[] args) {
        // 1. Initialize Sound System
        // Maps "snd/" calls to the "res/" folder
        SoundEffects.init("res/");

        // 2. Show Menu
        // We create the window on the GUI thread, but WAIT on the Main thread.
        final Menu[] menuContainer = new Menu[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                menuContainer[0] = new Menu();
                // NOTE: Do NOT call waitForMenuResponse() here, or the window freezes!
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        // This blocks the MAIN thread (not the drawing thread) until a selection is
        // made
        int scelta = menuContainer[0].waitForMenuResponse();

        // Handle Exit Selection
        if (scelta == Menu.EXIT) {
            System.exit(0);
        }

        // 3. Launch Game Window
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame();
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);
            window.setUndecorated(true);
            window.setTitle("Fix-it FELIX");

            // Full Screen Support
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (gd.isFullScreenSupported()) {
                gd.setFullScreenWindow(window);
            } else {
                window.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }

            // Initialize Game Panel
            GamePanel gamePanel = new GamePanel();
            window.add(gamePanel);
            window.pack();

            // Input Focus
            gamePanel.setFocusable(true);
            gamePanel.requestFocusInWindow();

            // Global ESC Handler
            gamePanel.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        System.exit(0);
                    }
                }
            });

            // 4. Preload Sounds & Start Timers
            preloadSounds();
            startGameTimers();

            // Show Window and Start Loop
            window.setVisible(true);
            gamePanel.startGameThread();
        });
    }

    private static void preloadSounds() {
        // Loading core game sounds into memory
        SoundEffects.loadSound("Background", "snd/bgm.wav");
        SoundEffects.loadSound("level_theme", "snd/level_theme.wav");
        SoundEffects.loadSound("ralph_angry", "snd/ralph_angry.wav");
        SoundEffects.loadSound("voice1", "snd/voice1.wav");
        SoundEffects.loadSound("piccone", "snd/piccone.wav");
        SoundEffects.loadSound("next_level", "snd/next_level.wav");
        SoundEffects.loadSound("level_completed", "snd/level_completed.wav");
        SoundEffects.loadSound("passi_abitanti", "snd/passi_abitanti.wav");
        SoundEffects.loadSound("jingle_abitanti", "snd/jingle_abitanti.wav");
        SoundEffects.loadSound("urla_Ralph", "snd/urla_Ralph.wav");
        SoundEffects.loadSound("tonfo_ralph", "snd/tonfo_ralph.wav");

        // Load shutter sounds for destruction effects
        for (int i = 0; i <= 7; i++) {
            SoundEffects.loadSound("shatter" + i, "snd/shatter" + i + ".wav");
        }
    }

    private static void startGameTimers() {
        // Timers for intro voice lines and effects
        Timer timerVoice0 = new Timer(25000, e -> SoundEffects.playSound("voice0"));
        timerVoice0.setRepeats(false);
        timerVoice0.start();

        Timer timerPiccone = new Timer(22000, e -> SoundEffects.playSound("piccone"));
        timerPiccone.setRepeats(false);
        timerPiccone.start();

        Timer timerVoice4 = new Timer(6000, e -> SoundEffects.playSound("voice4"));
        timerVoice4.setRepeats(false);
        timerVoice4.start();

        Timer timerStopConstruction = new Timer(5000, e -> SoundEffects.stopSound("costruzioneTorre"));
        timerStopConstruction.setRepeats(false);
        timerStopConstruction.start();

        Timer timerRalphAngry = new Timer(5300, e -> SoundEffects.playSound("ralph_angry"));
        timerRalphAngry.setRepeats(false);
        timerRalphAngry.start();
    }
}