package com.game;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Represents a falling brick obstacle thrown by Ralph.
 * Refactored to use ResourceManager and proper encapsulation.
 */
public class Brick {
    
    private int x, y;
    private int speed;
    private int width = 32;
    private int height = 28;
    
    private BufferedImage image;
    private Rectangle brickHitbox;

    /**
     * Standard constructor with default speed.
     * @param x Starting X position.
     * @param y Starting Y position.
     */
    public Brick(int x, int y) {
        this(x, y, 4); // Chain to the main constructor
    }
    
    /**
     * Constructor with custom speed for difficulty progression.
     * @param x Starting X position.
     * @param y Starting Y position.
     * @param speed Vertical falling speed.
     */
    public Brick(int x, int y, int speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        
        // Optimize: Load image via ResourceManager (Cached)
        this.image = ResourceManager.get().getImage("/map/brick.png");
        
        // Hitbox centered on the visible part of the brick
        this.brickHitbox = new Rectangle(x + 12, y + 15, width, height);
    }

    /**
     * Updates the brick's position and hitbox.
     */
    public void update() {
        y += speed;
        // Efficiently update hitbox position without creating a new Rectangle
        brickHitbox.setLocation(x + 12, y + 15);
    }

    /**
     * Draws the brick sprite.
     * @param g Graphics context.
     */
    public void draw(Graphics g) {
        if (image != null) {
            g.drawImage(image, x, y, null);
        }
    }

    /**
     * Checks if the brick has fallen off the bottom of the screen.
     * @return true if off-screen.
     */
    public boolean isOutOfScreen() {
        return y > 850; 
    }

    public Rectangle brickGetHitbox() {
        return brickHitbox;
    }
    
    public int getSpeed() {
        return speed;
    }
    
    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
