package com.game;

import java.awt.Graphics2D;
import java.awt.Image;

/**
 * Represents a decorative cloud in the background.
 * Refactored to use the game loop for animation instead of Swing Timers.
 */
public class Cloud {
    
    private final Image[] cloudImages;  // Array holding the two animation states
    private int x, y;
    private final int width, height;
    
    // Animation state
    private boolean useSecondImage = false;  
    private int animationCounter = 0;
    
    // Animation speed: 15 frames @ 30FPS is approx 500ms (matching original Timer)
    private static final int ANIMATION_DELAY = 15; 
    
    private int moveSpeed = 2; // Vertical movement speed during cutscenes

    /**
     * Creates a cloud object.
     * @param image1 First frame of animation.
     * @param image2 Second frame of animation.
     * @param x X position.
     * @param y Y position.
     * @param width Display width.
     * @param height Display height.
     */
    public Cloud(Image image1, Image image2, int x, int y, int width, int height) {
        this.cloudImages = new Image[]{image1, image2};
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    /**
     * Updates the cloud's animation state.
     * Called every frame by the game loop.
     */
    public void update() {
        // Toggle image based on frame counter
        animationCounter++;
        if (animationCounter >= ANIMATION_DELAY) {
            useSecondImage = !useSecondImage;
            animationCounter = 0;
        }
    }
    
    /**
     * Draws the current frame of the cloud.
     * @param g2 Graphics context.
     */
    public void draw(Graphics2D g2) {
        g2.drawImage(cloudImages[useSecondImage ? 1 : 0], x, y, width, height, null);
    }

    /**
     * Moves the cloud vertically (used during cutscene scrolling).
     * @param deltaY Pixels to move up.
     */
    public void move(int deltaY) {
        this.y -= deltaY; 
    }
    
    public void setMoveSpeed(int speed) {
        this.moveSpeed = speed;
    }

    public int getMoveSpeed() {
        return moveSpeed;
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
}
