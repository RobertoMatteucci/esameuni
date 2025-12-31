package com.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;

/**
 * Manages the Game Over sequence.
 * Displays the final score, handles High Score entry (if applicable), and provides Restart/Quit options.
 * Includes a finite state machine to transition between "High Score List", "Name Entry", and "Menu".
 */
public class GameOverScreen {
    
    private final GamePanel gp;
    private final int finalScore;
    
    // --- Screen States ---
    // STATE_HIGH_SCORE_DISPLAY: Shows the list of top scores.
    // STATE_ENTER_INITIALS: Allows user to input AAA if they got a high score.
    // STATE_FINAL_MENU: Shows Restart / Quit options.
    private static final int STATE_HIGH_SCORE_DISPLAY = 0;
    private static final int STATE_ENTER_INITIALS = 1;
    private static final int STATE_FINAL_MENU = 2;
    private int currentState = STATE_HIGH_SCORE_DISPLAY;
    
    // --- Menu Selection ---
    private int menuSelection = 0; // 0 = Restart, 1 = Quit
    private long lastMenuBlink = 0;
    
    // --- High Score Logic ---
    private List<HighScoreEntry> highScores;
    private boolean isNewHighScore = false;
    private int newScorePosition = -1;
    
    // --- Initials Entry Logic ---
    private final char[] initials = {'A', 'A', 'A'};
    private int currentInitialIndex = 0;
    private long lastBlinkTime = 0;
    private boolean cursorVisible = true;
    
    // --- Resources ---
    private Font arcadeFont;
    // Caches the digit images (0-9) for drawing the score with graphical numbers.
    private final BufferedImage[] digitImages = new BufferedImage[10];
    
    // --- Flags for GamePanel to check ---
    private boolean restartGame = false;
    private boolean quitGame = false;
    
    /**
     * Constructor. Initializes the screen and checks for high scores.
     * @param gp Reference to GamePanel.
     * @param finalScore The score the player achieved before dying.
     */
    public GameOverScreen(GamePanel gp, int finalScore) {
        this.gp = gp;
        this.finalScore = finalScore;
        
        loadResources();
        
        // Check High Score Status
        isNewHighScore = HighScoreManager.isHighScore(finalScore);
        if (isNewHighScore) {
            newScorePosition = HighScoreManager.getScoreRank(finalScore);
        }
        
        // Load current list to display
        highScores = HighScoreManager.loadHighScores();
        
        // Auto-transition to name entry if it's a high score
        if (isNewHighScore) {
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Wait 2 seconds before allowing entry
                    if (currentState == STATE_HIGH_SCORE_DISPLAY) {
                        currentState = STATE_ENTER_INITIALS;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    /**
     * Loads images for score digits and the custom font.
     */
    private void loadResources() {
        ResourceManager rm = ResourceManager.get();
        
        // Load Digits
        for (int i = 0; i < 10; i++) {
            digitImages[i] = rm.getImage("/map/digits" + i + ".png");
        }
        
        // Load Font
        try (InputStream is = getClass().getResourceAsStream("/res/fonts/PressStart2P.ttf")) {
            if (is != null) {
                arcadeFont = Font.createFont(Font.TRUETYPE_FONT, is);
            } else {
                arcadeFont = new Font("Monospaced", Font.BOLD, 20);
            }
        } catch (Exception e) {
            arcadeFont = new Font("Monospaced", Font.BOLD, 20);
        }
    }
    
    /**
     * Updates blinking cursors or menu items.
     */
    public void update() {
        long currentTime = System.currentTimeMillis();

        if (currentState == STATE_ENTER_INITIALS) {
            // Blink cursor for initials entry
            if (currentTime - lastBlinkTime > 500) {
                cursorVisible = !cursorVisible;
                lastBlinkTime = currentTime;
            }
        } else if (currentState == STATE_FINAL_MENU) {
            // Blink menu selection
            if (currentTime - lastMenuBlink > 300) {
                lastMenuBlink = currentTime;
            }
        }
    }
    
    /**
     * Draws the current state of the Game Over screen.
     * @param g2 Graphics context.
     */
    public void draw(Graphics2D g2) {
        // Draw Full Black Background
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        
        if (currentState == STATE_HIGH_SCORE_DISPLAY || currentState == STATE_ENTER_INITIALS) {
            drawHighScoreScreen(g2);
        } else if (currentState == STATE_FINAL_MENU) {
            drawFinalMenu(g2);
        }
    }
    
    private void drawHighScoreScreen(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        
        int centerX = gp.screenWidth / 2;
        int startY = 150;
        
        // 1. Title "GAME OVER"
        g2.setFont(arcadeFont.deriveFont(48f));
        String title = "GAME OVER";
        int textWidth = g2.getFontMetrics().stringWidth(title);
        
        g2.setColor(new Color(100, 0, 0)); // Shadow
        g2.drawString(title, centerX - textWidth / 2 + 3, startY + 3);
        g2.setColor(Color.RED); // Main
        g2.drawString(title, centerX - textWidth / 2, startY);
        
        // 2. "YOUR SCORE" Label
        startY += 100;
        g2.setFont(arcadeFont.deriveFont(24f));
        String scoreLabel = "YOUR SCORE";
        textWidth = g2.getFontMetrics().stringWidth(scoreLabel);
        g2.setColor(Color.WHITE);
        g2.drawString(scoreLabel, centerX - textWidth / 2, startY);
        
        // 3. The Score Digits (Graphical)
        startY += 60;
        drawScoreWithDigits(g2, finalScore, centerX - 165, startY);
        
        // 4. "HIGH SCORES" Header
        startY += 120;
        g2.setFont(arcadeFont.deriveFont(28f));
        String hsLabel = "HIGH SCORES";
        textWidth = g2.getFontMetrics().stringWidth(hsLabel);
        g2.setColor(Color.YELLOW);
        g2.drawString(hsLabel, centerX - textWidth / 2, startY);
        
        // 5. The Table
        startY += 60;
        drawHighScoreTable(g2, centerX, startY);
        
        // 6. Prompts
        if (currentState == STATE_ENTER_INITIALS) {
            drawInitialEntry(g2, centerX, startY + 400);
        } else if (currentState == STATE_HIGH_SCORE_DISPLAY && !isNewHighScore) {
            drawContinuePrompt(g2, centerX, startY + 400);
        }
    }
    
    /**
     * Helper to draw a score using the cached digit images.
     */
    private void drawScoreWithDigits(Graphics2D g2, int score, int x, int y) {
        String scoreString = String.format("%06d", score);
        int digitWidth = 55;
        
        for (int i = 0; i < scoreString.length(); i++) {
            int digit = Character.getNumericValue(scoreString.charAt(i));
            if (digit >= 0 && digit <= 9 && digitImages[digit] != null) {
                g2.drawImage(digitImages[digit], x + i * digitWidth, y, 50, 60, null);
            }
        }
    }
    
    private void drawHighScoreTable(Graphics2D g2, int centerX, int startY) {
        g2.setFont(arcadeFont.deriveFont(20f));
        int lineHeight = 50;
        
        // Refresh list if needed (though usually loaded in constructor)
        if (highScores == null) highScores = HighScoreManager.loadHighScores();
        
        for (int i = 0; i < Math.min(highScores.size(), 6); i++) {
            HighScoreEntry entry = highScores.get(i);
            int y = startY + i * lineHeight;
            
            // Highlight the new entry if currently entering initials
            boolean isNewEntry = isNewHighScore && (i == newScorePosition - 1);
            
            if (isNewEntry && currentState == STATE_ENTER_INITIALS) {
                g2.setColor(Color.GREEN);
            } else {
                g2.setColor(Color.CYAN);
            }
            
            // Rank
            g2.drawString((i + 1) + ".", centerX - 300, y);
            
            // Initials (Show live input if entering, otherwise stored initials)
            String text = (isNewEntry && currentState == STATE_ENTER_INITIALS) ? new String(initials) : entry.getInitials();
            g2.drawString(text, centerX - 220, y);
            
            // Score Digits
            drawScoreWithDigits(g2, entry.getScore(), centerX - 80, y - 35);
        }
    }
    
    private void drawInitialEntry(Graphics2D g2, int centerX, int y) {
        g2.setFont(arcadeFont.deriveFont(20f));
        String prompt = "ENTER YOUR INITIALS";
        int width = g2.getFontMetrics().stringWidth(prompt);
        
        if ((System.currentTimeMillis() / 300) % 2 == 0) {
            g2.setColor(Color.YELLOW);
            g2.drawString(prompt, centerX - width / 2, y);
        }
        
        y += 50;
        g2.setFont(arcadeFont.deriveFont(14f));
        g2.setColor(Color.GRAY);
        String help1 = "USE UP/DOWN TO CHANGE LETTER";
        g2.drawString(help1, centerX - g2.getFontMetrics().stringWidth(help1) / 2, y);
        
        y += 30;
        String help2 = "LEFT/RIGHT TO MOVE, ENTER TO CONFIRM";
        g2.drawString(help2, centerX - g2.getFontMetrics().stringWidth(help2) / 2, y);
    }
    
    private void drawContinuePrompt(Graphics2D g2, int centerX, int y) {
        g2.setFont(arcadeFont.deriveFont(16f));
        String msg = "PRESS ENTER TO CONTINUE";
        if ((System.currentTimeMillis() / 500) % 2 == 0) {
            g2.setColor(Color.GRAY);
            g2.drawString(msg, centerX - g2.getFontMetrics().stringWidth(msg) / 2, y);
        }
    }
    
    private void drawFinalMenu(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        
        int centerX = gp.screenWidth / 2;
        int startY = gp.screenHeight / 2 - 100;
        
        g2.setFont(arcadeFont.deriveFont(48f));
        String title = "GAME OVER";
        g2.setColor(Color.RED);
        g2.drawString(title, centerX - g2.getFontMetrics().stringWidth(title) / 2, startY);
        
        startY += 120;
        g2.setFont(arcadeFont.deriveFont(28f));
        
        // Restart Option
        String restartText = "RESTART GAME";
        int w = g2.getFontMetrics().stringWidth(restartText);
        if (menuSelection == 0) {
            if ((System.currentTimeMillis() / 300) % 2 == 0) {
                g2.setColor(Color.YELLOW);
                g2.drawString("> " + restartText + " <", centerX - w / 2 - 40, startY);
            }
        } else {
            g2.setColor(Color.WHITE);
            g2.drawString(restartText, centerX - w / 2, startY);
        }
        
        startY += 80;
        
        // Quit Option
        String quitText = "QUIT GAME";
        w = g2.getFontMetrics().stringWidth(quitText);
        if (menuSelection == 1) {
            if ((System.currentTimeMillis() / 300) % 2 == 0) {
                g2.setColor(Color.YELLOW);
                g2.drawString("> " + quitText + " <", centerX - w / 2 - 40, startY);
            }
        } else {
            g2.setColor(Color.WHITE);
            g2.drawString(quitText, centerX - w / 2, startY);
        }
    }
    
    // --- Input Handling ---
    
    /**
     * Handles keyboard input based on the current screen state.
     * @param keyCode The key code from KeyHandler.
     */
    public void handleKeyPress(int keyCode) {
        if (currentState == STATE_ENTER_INITIALS) {
            handleInitialInput(keyCode);
        } else if (currentState == STATE_HIGH_SCORE_DISPLAY) {
            if (keyCode == KeyEvent.VK_ENTER) {
                currentState = STATE_FINAL_MENU;
            }
        } else if (currentState == STATE_FINAL_MENU) {
            handleFinalMenuInput(keyCode);
        }
    }
    
    private void handleInitialInput(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
                initials[currentInitialIndex]++;
                if (initials[currentInitialIndex] > 'Z') initials[currentInitialIndex] = 'A';
                SoundEffects.playSound("jump");
                break;
            case KeyEvent.VK_DOWN:
                initials[currentInitialIndex]--;
                if (initials[currentInitialIndex] < 'A') initials[currentInitialIndex] = 'Z';
                SoundEffects.playSound("jump");
                break;
            case KeyEvent.VK_LEFT:
                currentInitialIndex--;
                if (currentInitialIndex < 0) currentInitialIndex = 0;
                SoundEffects.playSound("jump");
                break;
            case KeyEvent.VK_RIGHT:
                currentInitialIndex++;
                if (currentInitialIndex > 2) currentInitialIndex = 2;
                SoundEffects.playSound("jump");
                break;
            case KeyEvent.VK_ENTER:
                // Save Score and Transition
                HighScoreManager.addHighScore(new String(initials), finalScore);
                highScores = HighScoreManager.loadHighScores(); // Refresh list
                currentState = STATE_FINAL_MENU;
                SoundEffects.playSound("hammer");
                break;
        }
    }
    
    private void handleFinalMenuInput(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
                menuSelection = 0;
                SoundEffects.playSound("jump");
                break;
            case KeyEvent.VK_DOWN:
                menuSelection = 1;
                SoundEffects.playSound("jump");
                break;
            case KeyEvent.VK_ENTER:
                SoundEffects.playSound("hammer");
                if (menuSelection == 0) {
                    restartGame = true;
                } else {
                    quitGame = true;
                }
                break;
        }
    }
    
    public boolean shouldRestartGame() { return restartGame; }
    public boolean shouldQuitGame() { return quitGame; }
}