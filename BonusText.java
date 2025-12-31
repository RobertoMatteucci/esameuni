package com.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Represents the floating score text that appears when an action is performed.
 * Refactored for performance (font handling) and readability.
 */
public class BonusText {
    
    private final int x;
    private int y;
    private final int initialY;
    private final int points;
    private final long startTime;
    
    // Animation Constants
    private static final int FLOAT_DISTANCE = 50; // Pixels to float up
    private static final long DURATION = 1500;    // Duration in milliseconds

    /**
     * Creates a floating bonus text.
     * @param x X coordinate.
     * @param y Y coordinate.
     * @param points The score value to display.
     */
    public BonusText(int x, int y, int points) {
        this.x = x;
        this.y = y;
        this.initialY = y; // Store original Y to calculate offset
        this.points = points;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Updates the position based on elapsed time (Animation).
     */
    public void update() {
        long elapsed = System.currentTimeMillis() - startTime;
        float progress = Math.min(1.0f, elapsed / (float) DURATION);
        
        // Ease-out animation: Starts fast, slows down at the top
        // Formula: 1 - (1 - x)^3
        float easeOut = 1.0f - (float) Math.pow(1.0f - progress, 3);
        
        y = initialY - (int) (FLOAT_DISTANCE * easeOut);
    }

    /**
     * Draws the text with a fade-out effect.
     * @param g2 Graphics context.
     * @param arcadeFont The shared font instance to use (optimization).
     */
    public void draw(Graphics2D g2, Font arcadeFont) {
        long elapsed = System.currentTimeMillis() - startTime;
        float progress = elapsed / (float) DURATION;
        
        // Calculate Alpha (Transparency)
        // Stays fully opaque for the first 70%, then fades out
        float alpha = 1.0f;
        if (progress > 0.7f) {
            alpha = 1.0f - ((progress - 0.7f) / 0.3f);
        }
        
        // Clamp alpha to valid range [0.0, 1.0]
        alpha = Math.max(0, Math.min(1, alpha));
        int alphaValue = (int) (255 * alpha);
        
        // Create colors with the calculated transparency
        Color textColor = new Color(255, 255, 0, alphaValue); // Yellow
        Color shadowColor = new Color(0, 0, 0, alphaValue / 2); // Semi-transparent black shadow
        
        // Configure Font
        g2.setFont(arcadeFont.deriveFont(Font.PLAIN, 18f)); 
        
        // Draw Shadow (Offset +1)
        g2.setColor(shadowColor);
        g2.drawString(String.valueOf(points), x + 1, y + 1);
        
        // Draw Main Text
        g2.setColor(textColor);
        g2.drawString(String.valueOf(points), x, y);
    }

    /**
     * Checks if the animation has finished.
     * @return true if the text should be removed.
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - startTime > DURATION;
    }
}