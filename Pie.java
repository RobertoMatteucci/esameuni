package com.game;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Represents the Pie (Bonus item) that spawns in windows.
 * Contains logic for the spawning animation (citizen placing pie) and consumption.
 */
public class Pie {
    
    private int x, y;
    private boolean active = false;
    private boolean spawning = false;
    private boolean spawned = false;
    private boolean eating = false;
    private boolean consumed = false;
    
    // Sprites: [citizenType][frame]
    private final BufferedImage[][] abitanti; 
    private BufferedImage torta1, torta2;
    
    private final Random random;
    
    // State
    private int currentAbitante = 0;
    private static final int NUM_ABITANTI = 3;
    
    // Spawning Animation
    private int spawnFrame = 0;
    private int spawnFrameCounter = 0;
    private static final int SPAWN_ANIMATION_SPEED = 15;
    private static final int FRAME_2_DURATION = 45; // Longer pause when placing pie
    
    // Pie Idle Animation
    private int pieFrame = 0;
    private int pieFrameCounter = 0;
    private static final int PIE_ANIMATION_SPEED = 30;
    
    // Transition state (showing both citizen and pie)
    private boolean showingTransition = false; 
    
    // Hitbox
    private final Rectangle hitbox;
    private static final int HITBOX_WIDTH = 25;
    private static final int HITBOX_HEIGHT = 15;
    
    private static final int SPRITE_SCALE = 2;
    
    public Pie() {
        abitanti = new BufferedImage[NUM_ABITANTI][2];
        random = new Random();
        hitbox = new Rectangle(0, 0, HITBOX_WIDTH, HITBOX_HEIGHT);
        loadImages();
    }
    
    private void loadImages() {
        // Load Citizen Sprites via ResourceManager
        // Type 1
        abitanti[0][0] = ResourceManager.get().getImage("/abitanti/Abitante_t_1.png");
        abitanti[0][1] = ResourceManager.get().getImage("/abitanti/Abitante_t_2.png");
        
        // Type 2
        abitanti[1][0] = ResourceManager.get().getImage("/abitanti/Abitante_t2_1.png");
        abitanti[1][1] = ResourceManager.get().getImage("/abitanti/Abitante_t2_2.png");
        
        // Type 3
        abitanti[2][0] = ResourceManager.get().getImage("/abitanti/Abitante_t1_1.png");
        abitanti[2][1] = ResourceManager.get().getImage("/abitanti/Abitante_t1_2.png");
        
        // Load Pie Sprites
        torta1 = ResourceManager.get().getImage("/torta/Pie1.png");
        torta2 = ResourceManager.get().getImage("/torta/Pie2.png");
    }
    
    /**
     * Activates the pie at a specific window location.
     * @param windowPosition The bounds of the window where the pie appears.
     */
    public void spawn(Rectangle windowPosition) {
        if (!active && !consumed) {
            this.x = windowPosition.x;
            this.y = windowPosition.y;
            this.active = true;
            this.spawning = true;
            this.spawned = false;
            
            // Reset Animation
            this.spawnFrame = 0;
            this.spawnFrameCounter = 0;
            this.showingTransition = false;
            
            // Pick random citizen
            currentAbitante = random.nextInt(NUM_ABITANTI);
            
            // Sync Hitbox
            hitbox.setLocation(x, y);
            
            // System.out.println("🍰 Pie spawned at " + x + "," + y); // Debug
        }
    }
    
    public void update() {
        if (!active) return;
        
        // 1. Spawning Phase (Citizen places pie)
        if (spawning) {
            spawnFrameCounter++;
            
            // Frame 0 -> 1
            if (spawnFrame == 0 && spawnFrameCounter >= SPAWN_ANIMATION_SPEED) {
                spawnFrameCounter = 0;
                spawnFrame = 1;
            }
            // Frame 1 -> 2 (Long pause)
            else if (spawnFrame == 1 && spawnFrameCounter >= FRAME_2_DURATION) {
                spawnFrameCounter = 0;
                spawnFrame = 2;
                showingTransition = true; // Show both
            }
            // Frame 2 -> Done
            else if (spawnFrame == 2 && spawnFrameCounter >= SPAWN_ANIMATION_SPEED) {
                spawning = false;
                spawned = true;
                showingTransition = false;
                spawnFrame = 0;
            }
        }
        
        // 2. Idle Phase (Pie pulsing)
        if (spawned && !eating) {
            pieFrameCounter++;
            if (pieFrameCounter >= PIE_ANIMATION_SPEED) {
                pieFrameCounter = 0;
                pieFrame = (pieFrame + 1) % 2;
            }
        }
    }
    
    public void draw(Graphics2D g2) {
        if (!active) return;
        
        BufferedImage currentImage = null;
        
        // Draw Citizen Animation
        if (spawning) {
            if (spawnFrame == 0) {
                currentImage = abitanti[currentAbitante][0];
            } else if (spawnFrame == 1) {
                currentImage = abitanti[currentAbitante][1];
            } else if (spawnFrame == 2) {
                // Transition frame: show citizen holding it/leaving
                currentImage = abitanti[currentAbitante][1];
            }
            
            if (currentImage != null) {
                int width = currentImage.getWidth() * SPRITE_SCALE;
                int height = currentImage.getHeight() * SPRITE_SCALE;
                // Hardcoded offsets preserved from original to align with window
                g2.drawImage(currentImage, x + 6, y + 23, width, height, null);
            }
            
            // Draw Pie during transition
            if (showingTransition && torta1 != null) {
                int pieWidth = torta1.getWidth() * SPRITE_SCALE;
                int pieHeight = torta1.getHeight() * SPRITE_SCALE;
                g2.drawImage(torta1, x + 6, y + 23, pieWidth, pieHeight, null);
            }
        }
        // Draw Pie Alone
        else if (spawned && !eating) {
            currentImage = (pieFrame == 0) ? torta1 : torta2;
            
            if (currentImage != null) {
                int width = currentImage.getWidth() * SPRITE_SCALE;
                int height = currentImage.getHeight() * SPRITE_SCALE;
                g2.drawImage(currentImage, x + 6, y + 23, width, height, null);
            }
        }
    }
    
    public boolean checkCollision(Rectangle felixHitbox) {
        if (spawned && !eating && !consumed) {
            return hitbox.intersects(felixHitbox);
        }
        return false;
    }
    
    public void startEating() {
        if (spawned && !eating && !consumed) {
            eating = true;
        }
    }
    
    public void consume() {
        consumed = true;
        active = false;
        spawned = false;
        eating = false;
    }
    
    public void reset() {
        active = false;
        spawning = false;
        spawned = false;
        eating = false;
        consumed = false;
        spawnFrame = 0;
        pieFrame = 0;
        showingTransition = false;
    }
    
    // Getters
    public boolean isActive() { return active; }
    public boolean isSpawned() { return spawned; }
    public boolean isEating() { return eating; }
    public boolean isConsumed() { return consumed; }
    public Rectangle getHitbox() { return hitbox; }
}