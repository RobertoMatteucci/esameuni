package com.game;

import java.awt.Graphics2D;
import java.awt.Image;

/**
 * Manages the crane animation seen in the background/intro.
 * Refactored to use ResourceManager and optimizing image handling.
 */
public class CraneManager {
    
    // Animation Frames
    private final Image[] craneLeftFrames = new Image[2];
    private final Image[] craneRightFrames = new Image[2];
    
    // Position
    private int craneLeftX = 530;
    private int craneRightX = 610;

    // Timing & Speed
    private static final int CRANE_SPEED = 5;
    private static final int FRAME_DELAY = 5; 
    private static final int WAIT_TIME = 140; // Initial delay before movement

    // Animation State
    private int frameCounterLeft = 0;
    private int frameCounterRight = 0;
    private int currentFrameLeft = 0;
    private int currentFrameRight = 0;
    private int waitCounter = 0;
    private boolean moving = false;

    public CraneManager() {
        loadCraneImages();
    }

    private void loadCraneImages() {
        // Optimization: Use ResourceManager to load images
        craneLeftFrames[0] = ResourceManager.get().getImage("/map/gruSx.png");
        craneLeftFrames[1] = ResourceManager.get().getImage("/map/gruSx1.png");
        
        craneRightFrames[0] = ResourceManager.get().getImage("/map/gruDx.png");
        craneRightFrames[1] = ResourceManager.get().getImage("/map/gruDx1.png");
        
        // Load Sound
        SoundEffects.loadSound("costruzioneTorre", "snd/costruzioneTorre.wav");
        SoundEffects.playSound("costruzioneTorre");
    }

    /**
     * Updates crane position and animation frames.
     */
    public void update() {
        // Handle initial delay
        if (!moving) {
            waitCounter++;
            if (waitCounter >= WAIT_TIME) {
                moving = true;
                waitCounter = 0;
            }
            return;
        }

        // Update Animation Frame Counters
        frameCounterLeft++;
        frameCounterRight++;

        if (frameCounterLeft >= FRAME_DELAY) {
            frameCounterLeft = 0;
            currentFrameLeft = (currentFrameLeft + 1) % 2;
        }

        if (frameCounterRight >= FRAME_DELAY) {
            frameCounterRight = 0;
            currentFrameRight = (currentFrameRight + 1) % 2;
        }

        // Move Cranes
        if (craneLeftX > -500) {
            craneLeftX -= CRANE_SPEED;
        }
        if (craneRightX < 1600) {
            craneRightX += CRANE_SPEED;
        }
    }

    /**
     * Draws the cranes.
     * @param g2 Graphics context.
     */
    public void draw(Graphics2D g2) {
        // Null checks to prevent crashes if images failed to load
        if (craneLeftFrames[currentFrameLeft] != null && craneRightFrames[currentFrameRight] != null) {
            
            // Draw Main Cranes
            g2.drawImage(craneLeftFrames[currentFrameLeft], craneLeftX, 350, 328, 496, null);
            g2.drawImage(craneRightFrames[currentFrameRight], craneRightX, 350, 328, 496, null);
            
            // Draw Secondary Cranes (Offset)
            g2.drawImage(craneLeftFrames[currentFrameLeft], craneLeftX - 132, 350, 328, 496, null);
            g2.drawImage(craneRightFrames[currentFrameRight], craneRightX + 132, 350, 328, 496, null);
        }
    }
}