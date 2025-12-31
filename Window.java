package com.game;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Represents a window in the building that Felix needs to fix.
 * Refactored to optimize memory usage by sharing the static image resource.
 */
public class Window {
    
    private final int x, y;
    private boolean riparata;
    private int lampeggioCounter;
    private boolean visibile;

    // Optimization: Load the image once and share it among all Window instances
    private static BufferedImage fixedWindowImage;

    /**
     * Creates a window at specific coordinates.
     * @param x X coordinate.
     * @param y Y coordinate.
     */
    public Window(int x, int y) {
        this.x = x;
        this.y = y;
        this.riparata = false;
        this.lampeggioCounter = 15; // Duration of the blink effect
        this.visibile = true;

        // Load the image via ResourceManager only if it hasn't been loaded yet
        if (fixedWindowImage == null) {
            fixedWindowImage = ResourceManager.get().getImage("/map/aggiustata.png");
        }
    }

    /**
     * Marks the window as fixed and starts the blinking animation.
     */
    public void ripara() {
        this.riparata = true;
        this.visibile = true;
    }

    /**
     * Updates the blinking animation logic.
     */
    public void update() {
        if (riparata && lampeggioCounter > 0) {
            // Blink effect: toggle visibility every 2 frames
            if (lampeggioCounter % 2 == 0) {
                visibile = !visibile; 
            }
            lampeggioCounter--;
        } else if (lampeggioCounter == 0) {
            visibile = true; // Ensure it stays visible after blinking ends
        }
    }

    /**
     * Draws the fixed window if it has been repaired.
     * @param g2 Graphics context.
     */
    public void draw(Graphics2D g2) {
        if (riparata && visibile && fixedWindowImage != null) {
            // Offsets (-18, -48) and dimensions (79, 132) preserved from original logic
            g2.drawImage(fixedWindowImage, x - 18, y - 48, 79, 132, null);
        }
    }

    public boolean isRiparata() {
        return riparata;
    }
}