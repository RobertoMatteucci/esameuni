package com.game;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Represents the main player character, Felix.
 * Handles movement, jumping, fixing windows, eating pies, and death states.
 */
public class Player extends Entity {

    private final GamePanel gp;
    private final KeyHandler keyH;
    private final CollisionManager collisionManager;

    // --- Sprites ---
    private BufferedImage left1, left2, left3;
    private BufferedImage right1, right2, right3;
    private BufferedImage steadyR, steadyL;
    private BufferedImage fixR, fixL;
    private BufferedImage changeDirectionL, changeDirectionR;
    private BufferedImage jumpL, jumpR;
    private BufferedImage goingDownL, goingDownR;
    
    // UI Sprites (Lives)
    private BufferedImage felixHead1, felixHead2, felixHead3;
    
    // Death Sprites
    private BufferedImage death1, death2, death3, death4;
    private final BufferedImage[] finalDeathSprites = new BufferedImage[9];
    
    // Eating Sprites
    private final BufferedImage[] eatingLeft = new BufferedImage[7];
    private final BufferedImage[] eatingRight = new BufferedImage[7];

    // --- Animation State ---
    private int animationFrame = 0;
    private int frameCounter = 0;
    private final int animationSpeed = 5;
    private boolean isMoving = false;

    // --- Collision State ---
    private boolean ignoreCollision = false;
    private int ignoreCollisionCounter = 0;
    private static final int IGNORE_COLLISION_DURATION = 10;
    
    private final int hitboxWidth = 20;
    private final int hitboxHeight = 60;
    private final int offsetXLeft = 40;
    private final int offsetXRight = 4;
    
    // Hammer Collision
    private Rectangle hitboxMartello;
    private static final int OFFSET_X_MARTELLO = 39;
    private static final int OFFSET_Y_MARTELLO = 50;
    private static final int HITBOX_MARTELLO_WIDTH = 18;
    private static final int HITBOX_MARTELLO_HEIGHT = 15;

    // --- Action State ---
    private boolean isChangingDirection = false;
    private int changeDirectionCounter = 0;
    private static final int CHANGE_DIRECTION_DURATION = 3;

    private boolean isFixing = false;
    private int fixAnimationCount = 0;
    private static final int MAX_FIX_ANIMATIONS = 4;

    // Jump Physics
    private boolean isJumping = false;
    private boolean isFalling = false;
    private boolean isGoingDown = false;
    private int startY = 0;
    private static final int JUMP_HEIGHT = 130;
    private static final int JUMP_SPEED = 11;
    private static final int FALL_SPEED = 10;

    // --- Gameplay Status ---
    private boolean isActive = true;
    private int felixHealth = 4;
    
    // Invulnerability / PowerUp
    private boolean isPoweredUp = false;
    private long powerUpStartTime = 0;
    private static final int POWERUP_DURATION = 10000;
    private long lastPowerUpBlinkTime = 0;
    private boolean powerUpVisible = true;

    // Death Handling
    private long deathTimeStart = 0;
    private static final int IMMUNITY_TIME = 4000;
    private boolean isDead = false;
    private int deathAnimationFrame = 0;
    private int deathAnimationCounter = 0;
    private static final int DEATH_ANIMATION_SPEED = 20;
    private static final int MAX_DEATH_FRAMES = 4;
    
    public boolean finalDeath = false;
    private boolean felixFinalDeath = false; // Trigger for final animation
    
    // Final Death Animation
    private int finalDeathAnimationFrame = 0;
    private int finalDeathAnimationCounter = 0;
    private static final int FINAL_DEATH_ANIMATION_SPEED = 15;
    private static final int MAX_FINAL_DEATH_FRAMES = 9;
    private boolean finalDeathAnimationComplete = false;

    // Eating Animation
    private boolean isEating = false;
    private int eatingAnimationFrame = 0;
    private int eatingAnimationCounter = 0;
    private static final int EATING_ANIMATION_SPEED = 8;
    private static final int MAX_EATING_FRAMES = 7;

    // Visuals
    private boolean isFelixHead1Visible = true;
    private boolean isFelixHead2Visible = true;
    private boolean isFelixHead3Visible = true;
    private boolean felixVisible = true;
    private long lastBlinkTime = 0;

    public Player(GamePanel gp, KeyHandler keyH, CollisionManager collisionManager) {
        this.gp = gp;
        this.keyH = keyH;
        this.collisionManager = collisionManager;

        setDefaultValues();
        loadImages();

        // Initialize hitboxes
        solidArea = new Rectangle(x + offsetXRight, y, hitboxWidth, hitboxHeight);
        hitboxMartello = new Rectangle();
    }

    public void setDefaultValues() {
        x = 1052;
        y = 764;
        speed = 4;
        direction = "left";
    }

    private void loadImages() {
        ResourceManager rm = ResourceManager.get();

        // Movement
        left1 = rm.getImage("/felix/Left1.png");
        left2 = rm.getImage("/felix/Left2.png");
        left3 = rm.getImage("/felix/Left3.png");
        right1 = rm.getImage("/felix/Right1.png");
        right2 = rm.getImage("/felix/Right2.png");
        right3 = rm.getImage("/felix/Right3.png");
        
        // Actions
        steadyR = rm.getImage("/felix/StaticR.png");
        steadyL = rm.getImage("/felix/StaticL.png");
        fixR = rm.getImage("/felix/FixitR1.png");
        fixL = rm.getImage("/felix/FixitL1.png");
        changeDirectionL = rm.getImage("/felix/DirectionChangeL.png");
        changeDirectionR = rm.getImage("/felix/DirectionChangeR.png");
        
        // Jumping
        jumpL = rm.getImage("/felix/JumpL.png");
        jumpR = rm.getImage("/felix/JumpR.png");
        goingDownL = rm.getImage("/felix/DownL.png");
        goingDownR = rm.getImage("/felix/DownR.png");

        // Death
        death1 = rm.getImage("/felix/death1.png");
        death2 = rm.getImage("/felix/death2.png");
        death3 = rm.getImage("/felix/death3.png");
        death4 = rm.getImage("/felix/death4.png");

        // UI
        felixHead1 = rm.getImage("/felix/felixHead.png");
        felixHead2 = rm.getImage("/felix/felixHead.png");
        felixHead3 = rm.getImage("/felix/felixHead.png");

        // Eating Sequence
        for (int i = 0; i < MAX_EATING_FRAMES; i++) {
            eatingLeft[i] = rm.getImage("/felix/TortaL" + (i + 1) + ".png");
            eatingRight[i] = rm.getImage("/felix/TortaR" + (i + 1) + ".png");
        }

        // Final Death Sequence
        for (int i = 0; i < MAX_FINAL_DEATH_FRAMES; i++) {
            finalDeathSprites[i] = rm.getImage("/felix/FDeath" + (i + 1) + ".png");
        }
    }

    @Override
    public void update() {
        if (!isActive) return;

        isMoving = false;

        // 1. Handle Final Death Animation
        if (finalDeath && felixFinalDeath) {
            updateFinalDeath();
            return;
        }

        // 2. Handle Eating Animation
        if (isEating) {
            updateEating();
            return;
        }

        // 3. Handle PowerUp Effects
        if (isPoweredUp) {
            updatePowerUp();
        }

        // 4. Handle Standard Death / Respawn
        if (isDead) {
            updateDeath();
            return;
        }

        // Immunity Blink
        if (isDead && (System.currentTimeMillis() - deathTimeStart) < IMMUNITY_TIME) {
            return;
        }

        // 5. Normal Gameplay Input
        handleInput();
        
        // 6. Physics & Collision Updates
        updatePhysics();
        
        // 7. Update Hitboxes
        updateHitboxes();
        
        // 8. Screen Boundaries
        checkScreenBounds();
    }

    private void handleInput() {
        // Fix (Hammer)
        if (keyH.spacePressed && !isFixing && !isJumping && !isFalling && !isGoingDown) {
            isFixing = true;
            fixAnimationCount = 0;
            SoundEffects.playSound("hammer");
        }

        if (isFixing) {
            hitboxMartello = new Rectangle(x + OFFSET_X_MARTELLO, y + OFFSET_Y_MARTELLO, HITBOX_MARTELLO_WIDTH, HITBOX_MARTELLO_HEIGHT);
            frameCounter++;
            if ("left".equals(direction)) {
                hitboxMartello.x -= offsetXLeft - 7;
            }
            if (frameCounter >= animationSpeed) {
                frameCounter = 0;
                fixAnimationCount++;
                if (fixAnimationCount >= MAX_FIX_ANIMATIONS) {
                    isFixing = false;
                }
            }
            // Notify TileManager of repair attempt
            gp.tileM.aggiustaFinestra(hitboxMartello);
        }

        // Jump
        if (keyH.upPressed && !isJumping && !isFalling && !isFixing && !isGoingDown) {
            isJumping = true;
            startY = y;
            SoundEffects.stopSound("jump");
            SoundEffects.playSound("jump");
        }
        
        // Direction Change Logic
        if (!isJumping && !isFalling && !isFixing && !isChangingDirection && !isGoingDown) {
            if (keyH.leftPressed && !"left".equals(direction)) {
                isChangingDirection = true;
                changeDirectionCounter = 0;
                direction = "left";
            }
            if (keyH.rightPressed && !"right".equals(direction)) {
                isChangingDirection = true;
                changeDirectionCounter = 0;
                direction = "right";
            }
        }

        if (isChangingDirection) {
            changeDirectionCounter++;
            if (changeDirectionCounter >= CHANGE_DIRECTION_DURATION) {
                isChangingDirection = false;
            }
        }

        // Jump Down
        if (keyH.downPressed && !isFixing && !isJumping && !isFalling && !isGoingDown) {
            isGoingDown = true;
            ignoreCollision = true;
            ignoreCollisionCounter = 0;
            SoundEffects.playSound("jumpdown");
        }
    }

    private void updatePhysics() {
        // Jumping Up
        if (isJumping) {
            y -= JUMP_SPEED;
            handleHorizontalMovement();
            if (startY - y >= JUMP_HEIGHT) {
                isJumping = false;
                isFalling = true;
            }
        }

        // Falling
        if (isFalling) {
            y += FALL_SPEED;
            handleHorizontalMovement();
            
            Rectangle futureHitbox = new Rectangle(solidArea.x + speed, solidArea.y + FALL_SPEED, solidArea.width, solidArea.height);
            if (!ignoreCollision && collisionManager.checkTileCollision(futureHitbox)) {
                y = collisionManager.getDavanzaleYPosition(futureHitbox);
                isFalling = false;
                isJumping = false;
            }
        }

        // Going Down (Dropping through floor)
        if (isGoingDown) {
            y += FALL_SPEED;
            handleHorizontalMovement();
            ignoreCollisionCounter++;
            if (ignoreCollisionCounter >= IGNORE_COLLISION_DURATION) {
                isGoingDown = false;
                ignoreCollision = false;
            }
        }

        // Walking
        if (!isJumping && !isFixing && !isChangingDirection && !isGoingDown) {
            if (keyH.leftPressed || keyH.rightPressed) {
                frameCounter++;
                if (frameCounter >= animationSpeed) {
                    animationFrame = (animationFrame + 1) % 4;
                    frameCounter = 0;
                }
                handleHorizontalMovement();
            } else {
                animationFrame = 0;
            }
        }

        // Gravity check when walking off ledges
        if (!isFalling && !isJumping) {
            if (!collisionManager.checkTileCollision(solidArea)) {
                isFalling = true;
                isJumping = false;
            }
        }
    }

    private void handleHorizontalMovement() {
        if (keyH.leftPressed) {
            direction = "left";
            x -= speed;
            isMoving = true;
        }
        if (keyH.rightPressed) {
            direction = "right";
            x += speed;
            isMoving = true;
        }
    }

    private void updateHitboxes() {
        if ("left".equals(direction)) {
            solidArea.x = x + offsetXLeft;
        } else if ("right".equals(direction)) {
            solidArea.x = x + offsetXRight;
        }
        solidArea.y = y + 16;
    }

    private void checkScreenBounds() {
        if (x < 400) x = 400;
        if (x > gp.getWidth() - hitboxWidth - offsetXLeft - 485) {
            x = gp.getWidth() - hitboxWidth - offsetXLeft - 485;
        }
        
        // Ground Floor Floor
        if (y >= 764) {
            y = 764;
            isFalling = false;
            isJumping = false;
        }
        if (y < 300) {
            y = 300;
            isFalling = true;
            isJumping = false;
        }
    }

    private void updateFinalDeath() {
        finalDeathAnimationCounter++;
        if (finalDeathAnimationCounter >= FINAL_DEATH_ANIMATION_SPEED) {
            finalDeathAnimationCounter = 0;
            finalDeathAnimationFrame++;

            if (finalDeathAnimationFrame >= MAX_FINAL_DEATH_FRAMES) {
                finalDeathAnimationFrame = MAX_FINAL_DEATH_FRAMES - 1;
                finalDeathAnimationComplete = true;
            }
        }
        ignoreCollision = true;
    }

    private void updateEating() {
        eatingAnimationCounter++;
        if (eatingAnimationCounter >= EATING_ANIMATION_SPEED) {
            eatingAnimationCounter = 0;
            eatingAnimationFrame++;

            if (eatingAnimationFrame >= MAX_EATING_FRAMES) {
                isEating = false;
                eatingAnimationFrame = 0;
                
                isPoweredUp = true;
                powerUpStartTime = System.currentTimeMillis();
                lastPowerUpBlinkTime = System.currentTimeMillis();
                System.out.println("💪 Felix is powered up!");
            }
        }
    }
    
    private void updatePowerUp() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - powerUpStartTime >= POWERUP_DURATION) {
            isPoweredUp = false;
            powerUpVisible = true;
            System.out.println("🔻 PowerUp ended!");
        } else {
            if (currentTime - lastPowerUpBlinkTime >= 150) {
                powerUpVisible = !powerUpVisible;
                lastPowerUpBlinkTime = currentTime;
            }
        }
    }

    private void updateDeath() {
        if (deathTimeStart == 0) deathTimeStart = System.currentTimeMillis();

        deathAnimationCounter++;
        if (deathAnimationCounter >= DEATH_ANIMATION_SPEED) {
            deathAnimationCounter = 0;
            deathAnimationFrame++;

            if (deathAnimationFrame >= MAX_DEATH_FRAMES) {
                isDead = false;
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        BufferedImage image = null;

        // Draw HUD Lives (Heads)
        // If powered up, use blinking effect
        if (isPoweredUp && !powerUpVisible) {
             drawHeads(g); 
             return; 
        }

        // Blink when respawning
        if (!finalDeath && (System.currentTimeMillis() - deathTimeStart) < IMMUNITY_TIME) {
            if (System.currentTimeMillis() - lastBlinkTime >= 300) {
                felixVisible = !felixVisible;
                lastBlinkTime = System.currentTimeMillis();
            }
            if (!felixVisible) return;
        }

        // Final Death Draw
        if (finalDeath && felixFinalDeath) {
            image = finalDeathSprites[finalDeathAnimationFrame];
            drawSprite(g, image);
            drawHeads(g);
            return;
        }

        drawHeads(g);

        // Select Animation Sprite
        if (isEating) {
            image = "left".equals(direction) ? eatingLeft[eatingAnimationFrame] : eatingRight[eatingAnimationFrame];
        } else if (isDead) {
            image = getDeathSprite();
        } else if (isFixing) {
            if ("left".equals(direction)) image = (fixAnimationCount % 2 == 0) ? left1 : fixL;
            else image = (fixAnimationCount % 2 == 0) ? right1 : fixR;
        } else if (isChangingDirection) {
            image = "left".equals(direction) ? changeDirectionL : changeDirectionR;
        } else if (isJumping || isFalling) {
            image = "left".equals(direction) ? jumpL : jumpR;
        } else if (isGoingDown) {
            image = "left".equals(direction) ? goingDownL : goingDownR;
        } else if (isMoving) {
            image = getWalkingSprite();
        } else {
            image = "left".equals(direction) ? steadyL : steadyR;
        }

        drawSprite(g, image);
    }
    
    private void drawHeads(Graphics2D g) {
        if (isFelixHead3Visible) g.drawImage(felixHead1, 400, -20, 80, 90, null);
        if (isFelixHead2Visible) g.drawImage(felixHead2, 430, -20, 80, 90, null);
        if (isFelixHead1Visible) g.drawImage(felixHead3, 460, -20, 80, 90, null);
    }

    private BufferedImage getDeathSprite() {
        if ("right".equals(direction)) {
            return (deathAnimationFrame % 2 == 0) ? death1 : death3;
        } else {
            return (deathAnimationFrame % 2 == 0) ? death2 : death4;
        }
    }

    private BufferedImage getWalkingSprite() {
        BufferedImage img = null;
        if ("left".equals(direction)) {
            if (animationFrame == 0) img = left1;
            else if (animationFrame == 1) img = left2;
            else if (animationFrame == 2) img = left3;
            else img = left2;
        } else {
            if (animationFrame == 0) img = right1;
            else if (animationFrame == 1) img = right2;
            else if (animationFrame == 2) img = right3;
            else img = right2;
        }
        return img;
    }

    private void drawSprite(Graphics2D g, BufferedImage img) {
        if (img != null) {
            g.drawImage(img, x, y, img.getWidth() * 2, img.getHeight() * 2, null);
        }
    }

    public void handleCollisionWithBrick(Brick brick) {
        if (felixFinalDeath) return;
        if (isPoweredUp) return;

        if (!isDead && (System.currentTimeMillis() - deathTimeStart) >= IMMUNITY_TIME) {
            isDead = true;
            deathAnimationFrame = 0;
            deathAnimationCounter = 0;
            deathTimeStart = System.currentTimeMillis();
            felixHealth--;

            SoundEffects.playSound("die0");

            if (felixHealth == 3) isFelixHead3Visible = false;
            else if (felixHealth == 2) isFelixHead2Visible = false;
            else if (felixHealth == 1) {
                isFelixHead1Visible = false;
                finalDeath = true;
                felixFinalDeath = true;
                finalDeathAnimationFrame = 0;
                finalDeathAnimationCounter = 0;
                SoundEffects.playSound("die1");
                return;
            }
            lastBlinkTime = System.currentTimeMillis();
        }
    }

    public void startEatingAnimation() {
        if (!isEating) {
            isEating = true;
            eatingAnimationFrame = 0;
            eatingAnimationCounter = 0;
        }
    }
    
    public void addExtraLife() {
        if (felixHealth < 4) {
            felixHealth++;
            if (felixHealth == 2) isFelixHead1Visible = true;
            else if (felixHealth == 3) isFelixHead2Visible = true;
            else if (felixHealth == 4) isFelixHead3Visible = true;
            System.out.println("❤️ Extra Life!");
        }
    }

    public void setActive(boolean active) { this.isActive = active; }
    public boolean isEating() { return isEating; }
    public boolean isPoweredUp() { return isPoweredUp; }
    public boolean isFinalDeathAnimationComplete() { return finalDeathAnimationComplete; }
    public Rectangle getHitboxMartello() { return hitboxMartello; }
    public int getHealth() { return felixHealth; }
}