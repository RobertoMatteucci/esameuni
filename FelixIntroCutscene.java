package com.game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * Manages the "I Can Fix It!" intro sequence.
 * This class handles the cinematic sequence where Felix walks onto the screen, 
 * grabs his hammer, and shouts his catchphrase before the game begins.
 */
public class FelixIntroCutscene {

    private final GamePanel gp;
    
    // --- Sprites ---
    // Standard walking sprites
    private BufferedImage walk1, walk2, walk3;
    // Walking sprites holding the hammer
    private BufferedImage walk1h, walk2h, walk3h; 
    // Animation frames for picking up the hammer
    private BufferedImage grab1, grab2, grab3;    
    // Static image of the hammer on the ground
    private BufferedImage hammerImage;
    // The sprite currently being drawn
    private BufferedImage currentImage;

    // --- State Machine ---
    // Controls the sequence of events in the cutscene
    private enum State {
        ENTERING, // Felix walks from right to center
        GRABBING, // Felix picks up the hammer
        EXITING,  // Felix walks off screen to the left
        FINISHED  // Cutscene complete
    }
    private State currentState;

    // --- Positioning ---
    private int x, y;
    // Fixed Y position for Felix during the intro (aligned with ground)
    private static final int BASE_FELIX_Y_POS = 450;
    
    // --- Animation Counters ---
    private int animationFrame = 0;
    private int frameCounter = 0;
    // Controls how fast Felix walks (lower is faster)
    private static final int WALK_ANIMATION_SPEED = 5;
    // Controls how long the "grab" animation frames last
    private static final int GRAB_ANIMATION_SPEED = 15;
    
    // --- Movement Targets ---
    private final int startX;   // Spawn point (off-screen right)
    private final int hammerX;  // Where the hammer is located (center)
    private final int endX;     // Exit point (off-screen left)
    private static final int WALK_SPEED = 5; // Pixels moved per frame

    // --- Visuals ---
    private boolean showHammer = true; // Toggles hammer visibility on ground
    private Font arcadeFont;
    private static final int TEXT_Y = 400; // Y position for "I CAN FIX IT!" text

    /**
     * Constructor. Initializes the cutscene and loads resources.
     * @param playerToActivate Reference to the main player (unused, but kept for potential syncing)
     * @param gp Reference to the GamePanel for screen dimensions
     */
    public FelixIntroCutscene(Player playerToActivate, GamePanel gp) {
        this.gp = gp;
        
        // Calculate Positions relative to the defined Game Area (360px offset)
        // The hammer is placed exactly in the center of the playable area.
        this.hammerX = GamePanel.GAME_AREA_X + (GamePanel.GAME_AREA_WIDTH / 2) - 32; 
        
        // Start off-screen to the right
        this.startX = GamePanel.GAME_AREA_X + GamePanel.GAME_AREA_WIDTH + 50;
        
        // End off-screen to the left
        this.endX = GamePanel.GAME_AREA_X - 100;

        loadResources();
    }

    /**
     * Loads all necessary images and fonts using the ResourceManager.
     */
    private void loadResources() {
        ResourceManager rm = ResourceManager.get();

        // Load Walking Sprites (No Hammer)
        walk1 = rm.getImage("/felix/2.png");
        walk2 = rm.getImage("/felix/3.png");
        walk3 = rm.getImage("/felix/4.png");

        // Load Walking Sprites (With Hammer)
        walk1h = rm.getImage("/felix/left1.png");
        walk2h = rm.getImage("/felix/left2.png");
        walk3h = rm.getImage("/felix/left3.png");
        
        // Load Grab Animation Sprites
        grab1 = rm.getImage("/felix/5.png");
        grab2 = rm.getImage("/felix/6.png");
        grab3 = rm.getImage("/felix/7.png");

        // Load Hammer Prop
        hammerImage = rm.getImage("/felix/hammer.png"); 
        
        currentImage = walk1;

        // Load Arcade Font (with fallback)
        try (InputStream is = getClass().getResourceAsStream("res/fonts/PressStart2P.ttf")) {
            if (is != null) {
                // Load custom font at 24pt size
                arcadeFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(24f);
            } else {
                arcadeFont = new Font("Monospaced", Font.BOLD, 24);
            }
        } catch (Exception e) {
            // Fallback if file read fails
            arcadeFont = new Font("Monospaced", Font.BOLD, 24);
        }
    }

    /**
     * Resets and starts the cutscene from the beginning.
     * Called by CutsceneManager when triggering the intro.
     */
    public void start() {
        currentState = State.ENTERING;
        x = startX;
        y = BASE_FELIX_Y_POS;
        animationFrame = 0;
        frameCounter = 0;
        showHammer = true;
        currentImage = walk1;
    }

    /**
     * Updates the cutscene logic based on the current state.
     * Called every frame by the game loop.
     */
    public void update() {
        if (currentState == State.FINISHED) return;

        frameCounter++;

        switch (currentState) {
            case ENTERING:
                handleEnteringState();
                break;

            case GRABBING:
                handleGrabbingState();
                break;
            
            case EXITING:
                handleExitingState();
                break;

            case FINISHED:
                break;
        }
    }

    /**
     * Logic for Felix walking onto the screen.
     */
    private void handleEnteringState() {
        updateWalkAnimation(false); // False = No hammer in hand
        x -= WALK_SPEED; // Move left
        
        // Check if Felix has reached the hammer position
        if (x <= hammerX) {
            x = hammerX; // Snap to exact position
            currentState = State.GRABBING; // Switch state
            
            // Reset animation counters for the grabbing sequence
            animationFrame = 0;
            frameCounter = 0;
            showHammer = false; // Hide the ground hammer (he picked it up)
            currentImage = grab1; // Show first grab frame
            y = BASE_FELIX_Y_POS;
        }
    }

    /**
     * Logic for the grabbing animation and pause.
     */
    private void handleGrabbingState() {
        // Advance frame only after delay (GRAB_ANIMATION_SPEED)
        if (frameCounter >= GRAB_ANIMATION_SPEED) {
            frameCounter = 0;
            animationFrame++;
            
            // Cycle through grab frames
            if (animationFrame == 1) currentImage = grab2;
            else if (animationFrame == 2) currentImage = grab3; // This frame usually holds the pose
            else if (animationFrame > 2) {
                // Animation done, start exiting
                currentState = State.EXITING;
                animationFrame = 0;
                frameCounter = 0;
            }
        }
    }

    /**
     * Logic for Felix walking off-screen with the hammer.
     */
    private void handleExitingState() {
        updateWalkAnimation(true); // True = Walking with hammer
        x -= WALK_SPEED; // Move left
        
        // Check if Felix is fully off-screen
        if (x <= endX) {
            currentState = State.FINISHED; // Cutscene complete
        }
    }

    /**
     * Updates the walking sprite cycle.
     * @param withHammer If true, uses the sprite set where Felix holds the hammer.
     */
    private void updateWalkAnimation(boolean withHammer) {
        // Cycle frames based on speed
        if (frameCounter >= WALK_ANIMATION_SPEED) {
            frameCounter = 0;
            animationFrame = (animationFrame + 1) % 4; // 4 frames in walk cycle
        }
        
        if (withHammer) {
            // Select sprite based on frame index (0-3)
            if (animationFrame == 0) currentImage = walk1h;
            else if (animationFrame == 1) currentImage = walk2h;
            else if (animationFrame == 2) currentImage = walk3h;
            else currentImage = walk2h; // Repeat walk2 for smooth animation
        } else {
            if (animationFrame == 0) currentImage = walk1;
            else if (animationFrame == 1) currentImage = walk2;
            else if (animationFrame == 2) currentImage = walk3;
            else currentImage = walk2;
        }
        y = BASE_FELIX_Y_POS;
    }

    /**
     * Draws the cutscene elements to the screen.
     * @param g2 The Graphics context.
     */
    public void draw(Graphics2D g2) {
        // 1. Draw Black Background covering the entire screen
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        
        // 2. Set Clipping Region
        // Ensures visuals don't bleed outside the game area (important for wide screens)
        g2.setClip(GamePanel.GAME_AREA_X, GamePanel.GAME_AREA_Y, 
                   GamePanel.GAME_AREA_WIDTH, GamePanel.GAME_AREA_HEIGHT);
        
        // 3. Draw Hammer on ground (Only if entering and not picked up yet)
        if (currentState == State.ENTERING && showHammer && hammerImage != null) {
            g2.drawImage(hammerImage, hammerX, BASE_FELIX_Y_POS - 20, 
                        hammerImage.getWidth() * 2, hammerImage.getHeight() * 2, null); 
        }

        // 4. Draw Felix Sprite (Scaled 2x)
        if (currentImage != null && currentState != State.FINISHED) {
            int scale = 2;
            int scaledWidth = currentImage.getWidth() * scale;
            int scaledHeight = currentImage.getHeight() * scale;
            g2.drawImage(currentImage, x, y, scaledWidth, scaledHeight, null);
        }

        // 5. Remove Clipping for Text (We want text centered on the full screen)
        g2.setClip(null);

        // 6. Draw "I CAN FIX IT!" Text
        // Only visible during grabbing and exiting phases
        if (currentState == State.GRABBING || currentState == State.EXITING) { 
            drawCenteredText(g2, "I CAN FIX IT!");
        }
        
        // 7. Draw Game Area Border
        g2.setColor(new Color(40, 40, 40));
        g2.setStroke(new BasicStroke(4));
        g2.drawRect(GamePanel.GAME_AREA_X, GamePanel.GAME_AREA_Y, 
                    GamePanel.GAME_AREA_WIDTH, GamePanel.GAME_AREA_HEIGHT);
    }

    /**
     * Helper to draw centered text with a shadow effect.
     */
    private void drawCenteredText(Graphics2D g2, String text) {
        if (arcadeFont != null) {
            g2.setFont(arcadeFont);
        }
        // Enable anti-aliasing for text
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        
        int textWidth = g2.getFontMetrics().stringWidth(text);
        // Calculate X to center text horizontally relative to the full window width
        int textX = (gp.screenWidth - textWidth) / 2; 
        
        // Draw Shadow (Iterate offsets to create a thick outline effect)
        g2.setColor(Color.BLACK);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx != 0 || dy != 0) {
                    g2.drawString(text, textX + dx, TEXT_Y + dy);
                }
            }
        }
        
        // Draw Main Text (White)
        g2.setColor(Color.WHITE);
        g2.drawString(text, textX, TEXT_Y);
    }

    /**
     * Returns true if the cutscene has completed all phases.
     */
    public boolean isFinished() {
        return currentState == State.FINISHED;
    }
}