package com.game;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents debris or objects falling during cutscenes.
 * Refactored to optimize random number generation in the render loop.
 */
public class FallingObject {
    
    private int x, y;
    private final int speed;
    private final Image image;
    
    // Offsets stored to maintain relative position logic if needed later
    private final int offsetX;
    private final int offsetY;

    /**
     * Creates a falling object relative to a parent position (usually Ralph).
     * @param ralphX Parent X position.
     * @param ralphY Parent Y position.
     * @param offsetX Relative X offset.
     * @param offsetY Relative Y offset.
     * @param speed Falling speed.
     * @param image The sprite to draw.
     */
    public FallingObject(int ralphX, int ralphY, int offsetX, int offsetY, int speed, Image image) {
        this.x = ralphX + offsetX;
        this.y = ralphY + offsetY;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.speed = speed;
        this.image = image;
    }

    /**
     * Updates the vertical position.
     */
    public void update() {
        y += speed;
    }

    /**
     * Draws the object with a "shaking" effect.
     * @param g2 Graphics context.
     */
    public void draw(Graphics2D g2) {
        if (image == null) return;
        
        int width = 50;
        int height = 50;
    
        // Optimization: Use ThreadLocalRandom instead of Math.random() for better performance
        // Generates a random jitter between -5 and +5
        int randomOffsetX = ThreadLocalRandom.current().nextInt(-5, 6);
        int randomOffsetY = ThreadLocalRandom.current().nextInt(-5, 6);
    
        g2.drawImage(image, x + randomOffsetX, y + randomOffsetY, width, height, null);
    }

    /**
     * Checks if the object has fallen off the screen.
     * @return true if the object is below the screen bounds.
     */
    public boolean isOffScreen() {
        return y > 850; 
    }

    // Getters
    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }
}