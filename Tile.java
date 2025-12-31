package com.game;

import java.awt.image.BufferedImage;

/**
 * Represents a single map tile (e.g., wall, window part).
 * Refactored to use proper encapsulation.
 */
public class Tile {
    
    private BufferedImage image;
    private boolean collision = false;
    private boolean isFixed = false;

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public boolean isCollision() {
        return collision;
    }

    public void setCollision(boolean collision) {
        this.collision = collision;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public void setFixed(boolean fixed) {
        isFixed = fixed;
    }
}