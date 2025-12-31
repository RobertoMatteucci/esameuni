package com.game;

import java.awt.Rectangle;
import java.util.List;

/**
 * Manages the layout configuration for each level.
 * Defines where windows and solid platforms are placed based on the level number (Even vs Odd).
 * This class is responsible for generating the "stage" layout for Felix to play on.
 */
public class LivelloCompletato {

    private final GamePanel gp;
    private final LivelloN livN;
    // Lists to store collision boxes for static tiles (floors) and interactive windows
    private final List<Rectangle> collisionTiles;
    private final List<Rectangle> collisionWindows;
    
    // Layout constants for consistent spacing
    private final int windowWidth;
    private final int windowHeight;
    private final int gapX;
    private final int gapY;

    /**
     * Constructor: Initializes the level layout manager.
     * @param gp Reference to the main GamePanel.
     * @param collisionTiles List to populate with floor hitboxes.
     * @param collisionWindows List to populate with window hitboxes.
     * @param windowWidth Width of a window tile.
     * @param windowHeight Height of a window tile.
     * @param gapX Horizontal gap between windows.
     * @param gapY Vertical gap (offset) between window rows.
     * @param livN Reference to the level manager to check current level number.
     */
    public LivelloCompletato(GamePanel gp,
                             List<Rectangle> collisionTiles,
                             List<Rectangle> collisionWindows,
                             int windowWidth, int windowHeight,
                             int gapX, int gapY, LivelloN livN) {
        this.gp = gp;
        this.collisionTiles = collisionTiles;
        this.collisionWindows = collisionWindows;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.gapX = gapX;
        this.gapY = gapY;
        this.livN = livN;
    }

    /**
     * Clears current collisions and rebuilds the stage layout based on the level number.
     * Generates a fresh array of Window objects for the new level.
     * @return Array of Window objects representing the visual state of the new stage.
     */
    public Window[] rebuildStage() {
        // Safety check to prevent crashes if lists are null
        if (collisionTiles == null || collisionWindows == null) {
            return new Window[0];
        }

        // Clear old level data
        collisionTiles.clear();
        collisionWindows.clear();

        // --- EVEN LEVELS: Full Building Layout ---
        // Used for levels 2, 4, 6, etc. Represents a "complete" section of the building.
        if (livN.getNumeroLivello() % 2 == 0) {
            
            // 1. Add Solid Platforms (Floors)
            // The values are hardcoded coordinates matching the background image "PalazzoCompleto.png"
            collisionTiles.add(new Rectangle(513, 661, 440, 1)); // Bottom floor
            collisionTiles.add(new Rectangle(513, 789, 440, 1)); // Very bottom
            collisionTiles.add(new Rectangle(513, 519, 440, 1)); // Middle floor
            collisionTiles.add(new Rectangle(513, 394, 440, 1)); // Top floor

            // 2. Add Center Windows (Vertical column in the middle)
            collisionWindows.add(new Rectangle(714, 340, windowWidth - 30, windowHeight - 65));
            collisionWindows.add(new Rectangle(714, 465, windowWidth - 30, windowHeight - 65));
            collisionWindows.add(new Rectangle(714, 612, windowWidth - 30, windowHeight - 65));
            collisionWindows.add(new Rectangle(714, 736, windowWidth - 30, windowHeight - 65));

            // 3. Add Grid Windows (Left/Right sections) using loops
            // Iterate through 2 rows (i) and 2 columns (j) for each quadrant
            for (int i = 0; i < 2; i++) {
                // Bottom-Left Quadrant
                for (int j = 0; j < 2; j++) {
                    int x = 520 + j * (windowWidth + gapX);
                    int y = 612 + i * (windowHeight + (gapY - 1));
                    collisionWindows.add(new Rectangle(x, y, windowWidth - 30, windowHeight - 65));
                }
                // Bottom-Right Quadrant
                for (int j = 0; j < 2; j++) {
                    int x = 811 + j * (windowWidth + 12);
                    int y = 612 + i * (windowHeight + gapY);
                    collisionWindows.add(new Rectangle(x, y, windowWidth - 30, windowHeight - 65));
                }
                // Upper-Left Quadrant
                for (int j = 0; j < 2; j++) {
                    int x = 520 + j * (windowWidth + gapX);
                    int y = 340 + i * (windowHeight + (gapY - 1));
                    collisionWindows.add(new Rectangle(x, y, windowWidth - 30, windowHeight - 65));
                }
                // Upper-Right Quadrant
                for (int j = 0; j < 2; j++) {
                    int x = 811 + j * (windowWidth + 13);
                    int y = 340 + i * (windowHeight - 3);
                    collisionWindows.add(new Rectangle(x, y, windowWidth - 30, windowHeight - 65));
                }
            }
        } 
        // --- ODD LEVELS: Partial Building Layout ---
        // Used for levels 1, 3, 5, etc. Has a slightly different floor structure.
        else {
            
            // 1. Add Solid Platforms
            collisionTiles.add(new Rectangle(513, 661, 440, 1));
            collisionTiles.add(new Rectangle(510, 789, 152, 1)); // Split floor left
            collisionTiles.add(new Rectangle(803, 789, 152, 1)); // Split floor right
            collisionTiles.add(new Rectangle(513, 519, 440, 1));
            collisionTiles.add(new Rectangle(513, 394, 440, 1));

            // 2. Add Center Windows (Only 2 in the center for this layout)
            collisionWindows.add(new Rectangle(714, 341, windowWidth - 30, windowHeight - 65));
            collisionWindows.add(new Rectangle(714, 467, windowWidth - 30, windowHeight - 65));

            // 3. Add Grid Windows (Loops for quadrants)
            for (int i = 0; i < 2; i++) {
                // Bottom-Left
                for (int j = 0; j < 2; j++) {
                    int x = 520 + j * (windowWidth + gapX);
                    int y = 609 + i * (windowHeight + gapY);
                    collisionWindows.add(new Rectangle(x, y, windowWidth - 30, windowHeight - 65));
                }
                // Bottom-Right
                for (int j = 0; j < 2; j++) {
                    int x = 814 + j * (windowWidth + 13);
                    int y = 609 + i * (windowHeight + gapY);
                    collisionWindows.add(new Rectangle(x, y, windowWidth - 30, windowHeight - 65));
                }
                // Upper-Left
                for (int j = 0; j < 2; j++) {
                    int x = 520 + j * (windowWidth + gapX);
                    int y = 341 + i * (windowHeight + gapY);
                    collisionWindows.add(new Rectangle(x, y, windowWidth - 30, windowHeight - 65));
                }
                // Upper-Right
                for (int j = 0; j < 2; j++) {
                    int x = 811 + j * (windowWidth + 13);
                    int y = 341 + i * (windowHeight - 2);
                    collisionWindows.add(new Rectangle(x, y, windowWidth - 30, windowHeight - 65));
                }
            }
        }

        // Generate visual Window objects matching the collision boxes.
        // Creating 'new Window()' ensures 'riparata' is false (glass is broken).
        Window[] animazioneFinestre = new Window[collisionWindows.size()];
        for (int i = 0; i < collisionWindows.size(); i++) {
            Rectangle r = collisionWindows.get(i);
            animazioneFinestre[i] = new Window(r.x, r.y);
        }

        return animazioneFinestre;
    }
    
    /**
     * Returns the total number of windows generated for the current layout.
     */
    public int getNumeroFinestre() {
        return collisionWindows.size();
    }
}