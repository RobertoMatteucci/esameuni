package com.game;

import java.awt.Rectangle;
import java.util.List;

/**
 * Handles collision detection between the player, tiles, and windows.
 * Refactored for null-safety and readability.
 */
public class CollisionManager {

    private final TileManager tileManager;

    public CollisionManager(TileManager tileManager) {
        this.tileManager = tileManager;
    }

    /**
     * Checks if the player's hitbox intersects with any solid tile (e.g., floors/platforms).
     * @param playerHitbox The player's collision rectangle.
     * @return True if a collision occurs.
     */
    public boolean checkTileCollision(Rectangle playerHitbox) {
        List<Rectangle> tiles = tileManager.getCollisionTiles();
        if (tiles == null) return false;

        for (Rectangle tileHitbox : tiles) {
            if (tileHitbox.intersects(playerHitbox)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the hammer hitbox intersects with a broken window to repair it.
     * @param hammerHitbox The hitbox of the hammer strike.
     * @return True if a window was successfully repaired.
     */
    public boolean checkWindowRepairCollision(Rectangle hammerHitbox) {
        List<Rectangle> windows = tileManager.getCollisioneFinestre();
        if (windows == null) return false;

        // Iterate through all window hitboxes
        for (int i = 0; i < windows.size(); i++) {
            Rectangle windowHitbox = windows.get(i);
            
            if (windowHitbox.intersects(hammerHitbox)) {
                // Delegate the repair logic to TileManager
                // Note: The original code logic for "aggiustaFinestra" handles 
                // checking if it's already fixed or not.
                tileManager.aggiustaFinestra(hammerHitbox);
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the Y-coordinate to align the player on top of a platform (davanzale).
     * Used to snap the player to the floor when falling.
     * @param playerHitbox The player's collision rectangle.
     * @return The corrected Y coordinate.
     */
    public int getDavanzaleYPosition(Rectangle playerHitbox) {
        List<Rectangle> tiles = tileManager.getCollisionTiles();
        if (tiles == null) return playerHitbox.y;

        for (Rectangle tileHitbox : tiles) {
            if (tileHitbox.intersects(playerHitbox)) {
                // Snap player to just above the tile
                // -6 is a specific offset from original game logic for visual alignment
                return tileHitbox.y - playerHitbox.height - 6; 
            }
        }
        // No collision, return original Y
        return playerHitbox.y;
    }
}