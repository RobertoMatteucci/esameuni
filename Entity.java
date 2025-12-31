package com.game;

import java.awt.Rectangle;
import java.awt.Graphics2D;

/**
 * Base abstract class for all moving game objects.
 * Refactored to strictly contain shared logic (position, speed, collision)
 * rather than holding sprites for specific subclasses.
 */
public abstract class Entity {

    // World Position
    public int x, y;
    public int speed;

    // Movement State
    public String direction = "left"; // Default direction to prevent null errors

    // Animation State
    public int spriteCounter = 0;
    public int spriteNum = 1;

    // Collision Detection
    public Rectangle solidArea;
    public boolean collisionOn = false;

    /**
     * Updates the entity's logic (movement, AI, animation frames).
     * Must be implemented by subclasses.
     */
    public abstract void update();

    /**
     * Draws the entity to the screen.
     * Must be implemented by subclasses.
     * @param g2 The graphics context.
     */
    public abstract void draw(Graphics2D g2);

    /**
     * Helper to retrieve the entity's physical bounds.
     * @return The collision rectangle.
     */
    public Rectangle getHitbox() {
        return solidArea;
    }
}