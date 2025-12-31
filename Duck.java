package com.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages the flying ducks that appear in later levels.
 * Handles spawning, movement, and collision detection for all active ducks.
 */
public class Duck {
    
    // --- Inner Class: Represents a single active duck ---
    private static class FlyingDuck {
        int x, y;
        int speed;
        boolean movingRight;
        int animationFrame;
        int animationCounter;
        Rectangle hitbox;
        static final int HITBOX_OFFSET_Y = 20; 
        static final int HITBOX_WIDTH = 40;
        static final int HITBOX_HEIGHT = 20;
        
        FlyingDuck(int x, int y, int speed, boolean movingRight) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.movingRight = movingRight;
            this.animationFrame = 0;
            this.animationCounter = 0;
            this.hitbox = new Rectangle(x, y + HITBOX_OFFSET_Y, HITBOX_WIDTH, HITBOX_HEIGHT);
        }
        
        void update() {
            // Horizontal Movement
            if (movingRight) {
                x += speed;
            } else {
                x -= speed;
            }
            
            // Update Hitbox
            hitbox.x = x;
            hitbox.y = y + HITBOX_OFFSET_Y;
            
            // Animation (Toggle between 2 frames)
            animationCounter++;
            if (animationCounter >= 8) {
                animationCounter = 0;
                animationFrame = (animationFrame + 1) % 2;
            }
        }
        
        boolean isOffScreen(int screenWidth) {
            return (movingRight && x > screenWidth + 100) || (!movingRight && x < -100);
        }
    }
    
    // --- Duck Manager Fields ---
    
    private final GamePanel gp;
    private final Player player;
    private final LivelloN livelloN;
    
    // Sprites
    private BufferedImage duckRight1, duckRight2;
    private BufferedImage duckLeft1, duckLeft2;
    
    // Active Ducks
    private final List<FlyingDuck> ducks;
    
    // Spawning Logic
    private long lastSpawnTime;
    private int spawnInterval;
    private int maxDucks;
    private int duckSpeed; 
    
    // State
    private boolean isActive;
    private int currentLevel;
    
    // Spawn Heights (Vertical Lanes)
    private final int[] possibleHeights = {335, 430, 470, 520, 670, 715};
    
    public Duck(GamePanel gp, Player player, LivelloN livelloN) {
        this.gp = gp;
        this.player = player;
        this.livelloN = livelloN;
        this.ducks = new ArrayList<>();
        this.isActive = false;
        this.currentLevel = 1;
        
        loadSprites();
        updateDifficultyForLevel(1);
    }
    
    private void loadSprites() {
        // Optimization: Use ResourceManager
        duckRight1 = ResourceManager.get().getImage("/Duck/DuckR1.png");
        duckRight2 = ResourceManager.get().getImage("/Duck/DuckR2.png");
        duckLeft1 = ResourceManager.get().getImage("/Duck/DuckL1.png");
        duckLeft2 = ResourceManager.get().getImage("/Duck/DuckL2.png");
    }
    
    /**
     * Adjusts difficulty parameters based on the current level.
     * @param level The current game level.
     */
    private void updateDifficultyForLevel(int level) {
        this.currentLevel = level;
        
        // Ducks only appear from level 5 onwards
        if (level < 5) {
            isActive = false;
            return;
        }
        
        isActive = true;
        
        if (level <= 6) {
            maxDucks = 1;
            duckSpeed = 3;
            spawnInterval = 6000;
        } else if (level <= 9) {
            maxDucks = 2;
            duckSpeed = 4;
            spawnInterval = 4500;
        } else if (level <= 12) {
            maxDucks = 3;
            duckSpeed = 5;
            spawnInterval = 3000;
        } else if (level <= 15) {
            maxDucks = 3;
            duckSpeed = -1; // Variable speed 4-6
            spawnInterval = 2500;
        } else {
            maxDucks = 4;
            duckSpeed = -1; // Variable speed 5-7
            spawnInterval = 2000;
        }
    }
    
    public void setLevel(int level) {
        updateDifficultyForLevel(level);
        ducks.clear(); // Clear existing ducks on level change
        lastSpawnTime = System.currentTimeMillis();
    }
    
    public void update() {
        if (!isActive) return;
        
        // Check if level changed externally via LivelloN
        if (livelloN != null && livelloN.getNumeroLivello() != currentLevel) {
            setLevel(livelloN.getNumeroLivello());
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Spawn logic
        if (ducks.size() < maxDucks && currentTime - lastSpawnTime >= spawnInterval) {
            spawnDuck();
            lastSpawnTime = currentTime;
        }
        
        // Update ducks
        Iterator<FlyingDuck> iterator = ducks.iterator();
        while (iterator.hasNext()) {
            FlyingDuck duck = iterator.next();
            duck.update();
            
            // Remove if off-screen
            if (duck.isOffScreen(gp.screenWidth)) {
                iterator.remove();
                continue;
            }
            
            // Check collision with player
            if (checkCollision(duck)) {
                if (!player.isPoweredUp()) {
                    // Passing null as brick since it's a duck collision, 
                    // handleCollisionWithBrick handles generic damage.
                    player.handleCollisionWithBrick(null);
                    System.out.println("🦆 Felix hit by a duck!");
                }
                // Remove duck after collision
                iterator.remove();
            }
        }
    }
    
    private void spawnDuck() {
        boolean movingRight = ThreadLocalRandom.current().nextBoolean();
        
        // Start off-screen
        int x = movingRight ? -80 : gp.screenWidth + 80;
        
        // Pick random height
        int y = possibleHeights[ThreadLocalRandom.current().nextInt(possibleHeights.length)];
        
        // Determine speed
        int speed;
        if (duckSpeed == -1) {
            // Variable speed logic for high levels
            int base = (currentLevel <= 15) ? 4 : 5;
            speed = base + ThreadLocalRandom.current().nextInt(3);
        } else {
            speed = duckSpeed;
        }
        
        ducks.add(new FlyingDuck(x, y, speed, movingRight));
    }
    
    private boolean checkCollision(FlyingDuck duck) {
        return duck.hitbox.intersects(player.getHitbox());
    }
    
    public void draw(Graphics2D g2) {
        if (!isActive) return;
        
        for (FlyingDuck duck : ducks) {
            BufferedImage sprite;
            
            if (duck.movingRight) {
                sprite = (duck.animationFrame == 0) ? duckRight1 : duckRight2;
            } else {
                sprite = (duck.animationFrame == 0) ? duckLeft1 : duckLeft2;
            }
            
            if (sprite != null) {
                // Draw 2x scaled sprite
                g2.drawImage(sprite, duck.x, duck.y, 
                           sprite.getWidth() * 2, sprite.getHeight() * 2, null);
            }
            
            // Debug Hitbox
            // g2.setColor(Color.RED);
            // g2.draw(duck.hitbox);
        }
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
        if (!active) {
            ducks.clear();
        }
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void cleanup() {
        ducks.clear();
        isActive = false;
    }
    
    public void reset() {
        ducks.clear();
        lastSpawnTime = System.currentTimeMillis();
        updateDifficultyForLevel(1);
    }
}