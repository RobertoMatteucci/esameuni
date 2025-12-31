package com.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.InputStream;

/**
 * Represents the UI timer bar displayed during the game.
 * Shows remaining time and visual warnings when time is running low.
 */
public class TimeBar {
    
    private final int tempoMassimo;
    private final long startTime;

    // UI Positioning Constants
    private static final int BAR_X = 50;
    private static final int BAR_Y = 150;
    private static final int BAR_WIDTH = 250;
    private static final int BAR_HEIGHT = 20;
    
    // Bonus thresholds logic
    private final int[] soglieBonus = {30, 40, 50, 60, 70, 80, 100, 120};
    
    private Font arcadeFont;

    /**
     * Creates a new TimeBar.
     * @param tempoMassimo The maximum time allowed for the level in seconds.
     */
    public TimeBar(int tempoMassimo) {
        this.tempoMassimo = tempoMassimo;
        this.startTime = System.currentTimeMillis();
        loadArcadeFont();
    }
    
    private void loadArcadeFont() {
        try (InputStream is = getClass().getResourceAsStream("res/fonts/PressStart2P.ttf")) {
            if (is != null) {
                arcadeFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.BOLD, 12f);
            } else {
                arcadeFont = new Font("Monospaced", Font.BOLD, 12);
            }
        } catch (Exception e) {
            System.err.println("⚠️ TimeBar: Could not load custom font, using default.");
            arcadeFont = new Font("Monospaced", Font.BOLD, 12);
        }
    }

    public void update() {
        // Placeholder for future animations or time-freeze mechanics
    }

    /**
     * Calculates the remaining time in seconds.
     */
    public int getTempoResiduo() {
        int tempoTrascorso = (int) ((System.currentTimeMillis() - startTime) / 1000);
        return Math.max(0, tempoMassimo - tempoTrascorso);
    }
    
    public int getTempoTrascorso() {
        return (int) ((System.currentTimeMillis() - startTime) / 1000);
    }

    /**
     * Draws the time bar.
     * @param g2 Graphics context.
     */
    public void draw(Graphics2D g2) {
        int tempoResiduo = getTempoResiduo();
        float percent = (float) tempoResiduo / tempoMassimo;
        
        // Clamp percentage to 0-1 range to prevent drawing errors
        percent = Math.max(0f, Math.min(1f, percent));
        
        int fillWidth = (int) (BAR_WIDTH * percent);

        // Dynamic coloring based on urgency
        Color barColor;
        if (percent > 0.5f) {
            barColor = Color.GREEN;
        } else if (percent > 0.2f) {
            barColor = Color.ORANGE;
        } else {
            barColor = Color.RED;
        }

        // 1. Draw Background (Gray container)
        g2.setColor(Color.GRAY);
        g2.fillRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);

        // 2. Draw Fill (Colored progress)
        g2.setColor(barColor);
        g2.fillRect(BAR_X, BAR_Y, fillWidth, BAR_HEIGHT);

        // 3. Draw Border
        g2.setColor(Color.BLACK);
        g2.drawRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);

        // 4. Draw Text
        g2.setFont(arcadeFont);
        g2.setColor(Color.WHITE);
        g2.drawString("TIME: " + tempoResiduo + "s", BAR_X, BAR_Y - 8);

        // 5. Draw Bonus Threshold Markers
        g2.setColor(Color.CYAN);
        for (int soglia : soglieBonus) {
            if (soglia < tempoMassimo) {
                // Calculate position relative to the bar width
                int x = BAR_X + (int) ((float) (tempoMassimo - soglia) / tempoMassimo * BAR_WIDTH);
                
                // Only draw if inside the bar bounds
                if (x >= BAR_X && x <= BAR_X + BAR_WIDTH) {
                    g2.drawLine(x, BAR_Y, x, BAR_Y + BAR_HEIGHT);
                }
            }
        }
    }
}