package com.game;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * The Main Menu screen.
 * Handles game startup, displaying instructions, and showing the high score.
 */
public class Menu extends JFrame {
    
    private final BufferedImage[] digitImages = new BufferedImage[10];
    private int record = 0;

    // Menu Actions
    public static final int START = 1;
    public static final int EXIT = 2;
    private volatile int scelta = 0;

    // Layout
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    
    // Background Resource
    private final Image backgroundImage;

    public Menu() {
        // Load Resources
        loadDigitImages();
        backgroundImage = ResourceManager.get().getImage("/intro/Menu.png");
        
        // Load Sounds
        SoundEffects.loadSound("intro1", "snd/intro1.wav");
        SoundEffects.loadSound("game_start", "snd/game_start.wav");
        SoundEffects.loadSound("jump", "snd/jump.wav"); // UI Navigation sound

        // Load High Score
        record = HighScoreManager.loadHighScore();

        // Window Setup
        setTitle("Fix-it FELIX - Menu");
        setSize(600, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // CardLayout for switching between Main Menu and Instructions
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // --- 1. Main Menu Panel ---
        JPanel menuPanel = createMenuPanel();
        mainPanel.add(menuPanel, "menu");

        // --- 2. Instructions Panel ---
        JPanel instructionsPanel = createInstructionsPanel();
        mainPanel.add(instructionsPanel, "instructions");

        // Show Menu
        cardLayout.show(mainPanel, "menu");
        add(mainPanel);
        
        // Play Intro Music
        SoundEffects.playSound("intro1");
        
        setVisible(true);
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw Background
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, -50, getWidth(), getHeight(), this);
                }

                // Draw High Score Digits
                drawHighScore(g);
            }
        };
        panel.setBackground(Color.BLACK);
        panel.setLayout(null);

        // "Record" Label Image
        ImageIcon recordIcon = new ImageIcon(ResourceManager.get().getImage("/intro/Record.png"));
        ImageIcon scaledRecordIcon = scaleImage(recordIcon, 250, 200);
        JLabel recordLabel = new JLabel(scaledRecordIcon);
        recordLabel.setBounds(20, 450, 200, 90);
        panel.add(recordLabel);

        // Buttons
        JButton startButton = createButton("/intro/JBStartGame.png", "/intro/JBStartGame_Hover.png", START);
        JButton instructionsButton = createButton("/intro/JBInfo.png", "/intro/JBInfo_Hover.png", 0);
        JButton exitButton = createButton("/intro/JBQuitGame.png", "/intro/JBQuitGame_Hover.png", EXIT);

        startButton.setBounds(60, 550, 170, 50);
        instructionsButton.setBounds(200, 630, 150, 50);
        exitButton.setBounds(300, 550, 170, 50);

        // Wiring "Instructions" button to switch panels
        instructionsButton.addActionListener(e -> {
            SoundEffects.playSound("jump");
            cardLayout.show(mainPanel, "instructions");
        });

        panel.add(startButton);
        panel.add(instructionsButton);
        panel.add(exitButton);
        
        return panel;
    }

    private JPanel createInstructionsPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.BLACK);

        // Background Image
        ImageIcon backgroundIcon = new ImageIcon(ResourceManager.get().getImage("/intro/Scenary1.png"));
        JLabel backgroundLabel = new JLabel(scaleImage(backgroundIcon, 600, 450));
        backgroundLabel.setBounds(0, 113, 600, 450);

        // Instruction Icons
        ImageIcon instr1 = new ImageIcon(ResourceManager.get().getImage("/intro/Instruction1.png"));
        JLabel imageLabel1 = new JLabel(scaleImage(instr1, 120, 100));
        imageLabel1.setBounds(10, 0, 110, 110);

        ImageIcon instr2 = new ImageIcon(ResourceManager.get().getImage("/intro/Instruction2.png"));
        JLabel imageLabel2 = new JLabel(scaleImage(instr2, 100, 100));
        imageLabel2.setBounds(150, 58, 100, 39);

        // Text
        JLabel label1 = new JLabel(
                "<html>Use ARROW KEYS to move,<br>SPACE BAR to fix windows.</html>");
        label1.setFont(new Font("Monospaced", Font.BOLD, 14));
        label1.setForeground(Color.WHITE);
        label1.setBounds(290, 35, 300, 60);

        // Back Button
        ImageIcon backIcon = scaleImage(new ImageIcon(ResourceManager.get().getImage("/intro/JBack.png")), 170, 50);
        ImageIcon backHoverIcon = scaleImage(new ImageIcon(ResourceManager.get().getImage("/intro/JBack_Hover.png")), 170, 50);

        JButton backButton = new JButton(backIcon);
        backButton.setRolloverIcon(backHoverIcon);
        backButton.setBounds(200, 600, 170, 50);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        
        backButton.addActionListener(e -> {
            SoundEffects.playSound("jump");
            cardLayout.show(mainPanel, "menu");
        });

        // Layering
        panel.add(backgroundLabel);
        panel.add(imageLabel1);
        panel.add(imageLabel2);
        panel.add(label1);
        panel.add(backButton);

        // Push background to the back
        panel.setComponentZOrder(backgroundLabel, panel.getComponentCount() - 1);
        
        return panel;
    }

    private void drawHighScore(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        String scoreString = String.format("%06d", record);
        int startX = 230;
        int y = 470;

        for (int i = 0; i < scoreString.length(); i++) {
            int digit = Character.getNumericValue(scoreString.charAt(i));
            if (digitImages[digit] != null) {
                g2.drawImage(digitImages[digit], startX + i * 40, y, 30, 45, null);
            }
        }
    }

    private void loadDigitImages() {
        for (int i = 0; i < 10; i++) {
            // Use ResourceManager to get digits
            digitImages[i] = ResourceManager.get().getImage("/map/digits" + i + ".png");
        }
    }

    private JButton createButton(String iconPath, String hoverPath, int action) {
        // Load images via ResourceManager
        ImageIcon icon = new ImageIcon(ResourceManager.get().getImage(iconPath));
        ImageIcon hoverIcon = new ImageIcon(ResourceManager.get().getImage(hoverPath));
        
        JButton button = new JButton(icon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setRolloverIcon(hoverIcon);
        
        button.addActionListener(e -> {
            if (action != 0) {
                handleSelection(action);
            }
        });
        return button;
    }

    private synchronized void handleSelection(int action) {
        scelta = action;
        if (action == START) {
            SoundEffects.playSound("game_start");
        }
        SoundEffects.stopSound("intro1");
        dispose(); // Close Menu Window
        notifyAll(); // Wake up Main thread
    }

    /**
     * Blocks the main thread until the user makes a selection.
     * @return START or EXIT constant.
     */
    public synchronized int waitForMenuResponse() {
        while (scelta == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return scelta;
    }

    private ImageIcon scaleImage(ImageIcon icon, int width, int height) {
        if (icon.getImage() == null) return icon;
        Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}