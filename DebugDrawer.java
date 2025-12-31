package com.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

/**
 * Utility class to visualize hitboxes for debugging purposes.
 * Should be commented out or disabled in the production release.
 */
public class DebugDrawer {
    
    private final Player player;
    private final List<Rectangle> collisionTiles;
    private final List<Rectangle> collisionWindows;

    /**
     * Creates the debug drawer.
     * @param player The player entity to track.
     * @param collisionTiles List of solid map tiles.
     * @param collisionWindows List of window hitboxes.
     */
    public DebugDrawer(Player player, List<Rectangle> collisionTiles, List<Rectangle> collisionWindows) {
        this.player = player;
        this.collisionTiles = collisionTiles;
        this.collisionWindows = collisionWindows;
    }

    /**
     * Draws all registered hitboxes in red.
     * @param g2 Graphics context.
     */
    public void draw(Graphics2D g2) {
        if (g2 == null) return;

        g2.setColor(Color.RED);

        // 1. Draw Player Hitbox
        if (player != null) {
            Rectangle hitbox = player.getHitbox();
            if (hitbox != null) {
                g2.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
            }

            Rectangle hitboxMartello = player.getHitboxMartello();
            if (hitboxMartello != null) {
                g2.drawRect(hitboxMartello.x, hitboxMartello.y, hitboxMartello.width, hitboxMartello.height);
            }
        }

        // 2. Draw Map Collision Tiles
        if (collisionTiles != null) {
            for (Rectangle rect : collisionTiles) {
                if (rect != null) {
                    g2.drawRect(rect.x, rect.y, rect.width, rect.height);
                }
            }
        }

        // 3. Draw Window Hitboxes
        if (collisionWindows != null) {
            for (Rectangle rect : collisionWindows) {
                if (rect != null) {
                    g2.drawRect(rect.x, rect.y, rect.width, rect.height);
                }
            }
        }
    }
}