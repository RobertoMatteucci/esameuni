package com.game;

import java.awt.Graphics2D;
import java.awt.Image;

/**
 * Manages the specific cutscene where Ralph climbs up the building.
 * Refactored to use ResourceManager and proper encapsulation.
 */
public class RalphCutscene2 {

    private final GamePanel gp;
    private final int x;
    private int y;
    private final int startY; // Store for reset
    private final int finalY;
    private final int speed;
    private boolean finished = false;

    // Animation resources
    private Image frame1, frame2, frame3;
    private int frameCounter = 0;
    private static final int FRAME_DELAY = 10;

    /**
     * Creates the climbing cutscene.
     * @param startX Starting X position.
     * @param startY Starting Y position.
     * @param finalY Target Y position (top).
     * @param speed Climbing speed.
     * @param gp Reference to GamePanel.
     */
    public RalphCutscene2(int startX, int startY, int finalY, int speed, GamePanel gp) {
        this.gp = gp;
        this.x = startX;
        this.y = startY;
        this.startY = startY;
        this.finalY = finalY;
        this.speed = speed;

        loadImages();
    }

    private void loadImages() {
        // Optimization: Load images via ResourceManager
        frame1 = ResourceManager.get().getImage("/ralph/RalphBack1.png");
        frame2 = ResourceManager.get().getImage("/ralph/RalphBack2.png");
        frame3 = ResourceManager.get().getImage("/ralph/Move9.png"); // Using Move9 as an idle/climb frame
    }

    /**
     * Updates the climbing logic.
     */
    public void update() {
        if (finished) {
            return;
        }

        // Move Ralph Up
        if (y > finalY) {
            y -= speed;
        } else {
            // Reached the top
            y = finalY;
            
            // Sync actual Ralph entity position if needed
            if (gp.ralph != null) {
                gp.ralph.x = 644; 
            }
            
            finished = true;
        }

        frameCounter++;
    }

    /**
     * Draws the climbing animation.
     * @param g2 Graphics context.
     */
    public void draw(Graphics2D g2) {
        // Toggle frames for climbing effect
        Image currentFrame = (frameCounter / FRAME_DELAY) % 2 == 0 ? frame1 : frame2;
        
        // If images failed to load, avoid crash
        if (currentFrame != null) {
            int width = 180;
            int height = 180;
            g2.drawImage(currentFrame, x, y, width, height, null);
        }
    }

    public boolean isFinished() {
        return finished;
    }

    /**
     * Resets the cutscene for reuse.
     */
    public void reset() {
        finished = false;
        frameCounter = 0;
        y = startY; 
    }
}   