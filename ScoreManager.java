package com.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * Manages the player's score, high score tracking, and visual notifications.
 * Handles extra lives and milestone bonuses.
 */
public class ScoreManager {
    
    private int score = 0;
    private final BufferedImage[] digitImages = new BufferedImage[10];
    private Font arcadeFont;
    
    // Extra Life System
    private static final int POINTS_PER_EXTRA_LIFE = 20000;
    private Player player; 
    
    // Notifications
    private boolean showingExtraLifeNotification = false;
    private long extraLifeNotificationStart = 0;
    private static final long NOTIFICATION_DURATION = 3000;
    
    private boolean showingMilestoneNotification = false;
    private long milestoneNotificationStart = 0;
    private int lastMilestone = 0;
    private static final int[] MILESTONES = {10000, 50000, 100000, 250000, 500000};

    public ScoreManager() {
        loadDigitImages();
        loadArcadeFont();
    }
    
    public ScoreManager(Player player) {
        this();
        this.player = player;
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }

    private void loadDigitImages() {
        for (int i = 0; i < 10; i++) {
            // Optimization: Use ResourceManager
            digitImages[i] = ResourceManager.get().getImage("/map/digits" + i + ".png");
        }
    }
    
    private void loadArcadeFont() {
        try (InputStream is = getClass().getResourceAsStream("res/fonts/PressStart2P.ttf")) {
            if (is != null) {
                arcadeFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.BOLD, 14f);
            } else {
                arcadeFont = new Font("Monospaced", Font.BOLD, 14);
            }
        } catch (Exception e) {
            System.err.println("⚠️ ScoreManager: Could not load font, using default.");
            arcadeFont = new Font("Monospaced", Font.BOLD, 14);
        }
    }

    public void addPoints(int points) {
        int oldScore = score;
        score += points;
        
        // Cap score at 999,999
        if (score > 999999) {
            score = 999999;
        }
        
        checkExtraLife(oldScore);
        checkMilestone(oldScore);
    }
    
    private void checkExtraLife(int oldScore) {
        if (player == null) return;
        
        int oldLives = oldScore / POINTS_PER_EXTRA_LIFE;
        int newLives = score / POINTS_PER_EXTRA_LIFE;
        
        if (newLives > oldLives && player.getHealth() < 4) {
            player.addExtraLife();
            
            // Trigger visual notification
            showingExtraLifeNotification = true;
            extraLifeNotificationStart = System.currentTimeMillis();
            System.out.println("🎉 EXTRA LIFE EARNED! Score: " + score);
        }
    }
    
    private void checkMilestone(int oldScore) {
        for (int milestone : MILESTONES) {
            if (oldScore < milestone && score >= milestone) {
                showingMilestoneNotification = true;
                milestoneNotificationStart = System.currentTimeMillis();
                lastMilestone = milestone;
                System.out.println("🏆 MILESTONE REACHED: " + milestone);
                break;
            }
        }
    }

    public int getScore() {
        return score;
    }

    public void draw(Graphics2D g2, int x, int y) {
        // 1. Draw "SCORE" Label
        if (arcadeFont != null) {
            g2.setFont(arcadeFont);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            
            String label = "SCORE";
            int labelWidth = g2.getFontMetrics().stringWidth(label);
            int scoreWidth = 6 * 55; // Approx width of 6 digits
            int labelX = x + (scoreWidth - labelWidth) / 2;
            
            // Shadow
            g2.setColor(Color.BLACK);
            g2.drawString(label, labelX + 2, y - 12);
            // Main Text
            g2.setColor(Color.WHITE);
            g2.drawString(label, labelX, y - 14);
        }
        
        // 2. Draw Score Digits
        String scoreString = String.format("%06d", score);
        for (int i = 0; i < scoreString.length(); i++) {
            int digit = Character.getNumericValue(scoreString.charAt(i));
            if (digitImages[digit] != null) {
                g2.drawImage(digitImages[digit], x + i * 55, y, 40, 50, null);
            }
        }
        
        // 3. Draw Notifications
        long currentTime = System.currentTimeMillis();
        
        if (showingExtraLifeNotification) {
            long elapsed = currentTime - extraLifeNotificationStart;
            if (elapsed < NOTIFICATION_DURATION) {
                drawExtraLifeNotification(g2, elapsed);
            } else {
                showingExtraLifeNotification = false;
            }
        }
        
        if (showingMilestoneNotification) {
            long elapsed = currentTime - milestoneNotificationStart;
            if (elapsed < NOTIFICATION_DURATION) {
                drawMilestoneNotification(g2, elapsed);
            } else {
                showingMilestoneNotification = false;
            }
        }
    }
    
    private void drawExtraLifeNotification(Graphics2D g2, long elapsed) {
        if (arcadeFont == null) return;
        
        g2.setFont(arcadeFont.deriveFont(20f));
        String text = "EXTRA LIFE!";
        int textWidth = g2.getFontMetrics().stringWidth(text);
        int x = 683 - textWidth / 2; // Centered
        int y = 200;
        
        // Blink effect
        if ((elapsed / 300) % 2 == 0) {
            // Shadow
            g2.setColor(Color.BLACK);
            g2.drawString(text, x + 2, y + 2);
            // Text
            g2.setColor(Color.GREEN);
            g2.drawString(text, x, y);
        }
        
        // Subtext
        g2.setFont(arcadeFont.deriveFont(12f));
        String subtext = POINTS_PER_EXTRA_LIFE + " POINTS";
        int subX = 683 - g2.getFontMetrics().stringWidth(subtext) / 2;
        
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString(subtext, subX, y + 25);
    }
    
    private void drawMilestoneNotification(Graphics2D g2, long elapsed) {
        if (arcadeFont == null) return;
        
        g2.setFont(arcadeFont.deriveFont(18f));
        String text = lastMilestone + " POINTS!";
        int textWidth = g2.getFontMetrics().stringWidth(text);
        int x = 683 - textWidth / 2;
        int y = 250;
        
        // Color transition (Yellow -> White)
        float progress = (float)elapsed / NOTIFICATION_DURATION;
        Color color = (progress < 0.5f) ? Color.YELLOW : Color.WHITE;
        
        g2.setColor(Color.BLACK);
        g2.drawString(text, x + 2, y + 2);
        
        g2.setColor(color);
        g2.drawString(text, x, y);
    }

    public void reset() {
        score = 0;
        showingExtraLifeNotification = false;
        showingMilestoneNotification = false;
    }
    
    public int getPointsUntilNextLife() {
        return POINTS_PER_EXTRA_LIFE - (score % POINTS_PER_EXTRA_LIFE);
    }
    
    public int getTotalExtraLivesEarned() {
        return score / POINTS_PER_EXTRA_LIFE;
    }
}