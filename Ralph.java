package com.game;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents the main antagonist, Ralph.
 * Handles AI movement, brick throwing mechanics, and difficulty scaling.
 */
public class Ralph extends Entity {

    // --- State Management ---
    private enum State {
        THROWING, // Standing still, throwing bricks
        MOVING    // Moving between windows
    }

    private State currentState = State.THROWING;
    
    // Movement Targets
    private static final int CENTER_X = 635;
    private static final int RIGHT_X = 790;
    private static final int LEFT_X = 480;
    private final int[] positionSequence = { CENTER_X, RIGHT_X, CENTER_X, LEFT_X };
    private int sequenceIndex = 0;
    private int targetX = CENTER_X;
    private int moveDirection = 0; // -1 left, 1 right, 0 idle

    // --- Animation & Rendering ---
    private Image[] moveImages; 
    private Image[] throwImages; 
    
    // Throw Animation Sequence (Frame names mapped to indices)
    private final String[] throwSequenceNames = {
            "move9", "move10", "move5", "move3", "move4",
            "move6", "move5", "move3", "move4", "move6"
    };
    
    private int currentFrame = 0;     // For walking
    private int throwAnimationFrame = 0; // For throwing
    private int frameDelay = 12;
    private int frameCounter = 0;
    private double scaleFactor = 2.0;
    public boolean transition = false; // Used during cutscenes

    // --- Gameplay Logic ---
    private final Player felix;
    private final List<Brick> bricks = new ArrayList<>();
    private int bricksThrown = 0;
    private static final int BRICKS_PER_THROW = 5;
    private boolean hasPlayedVoice4 = false;
    private boolean isActive = true;

    // --- Difficulty Scaling ---
    private int currentLevel = 1;
    private double speedMultiplier = 1.0;
    private int brickSpeedBonus = 0;
    private int animationSpeedReduction = 0;
    private LivelloN livelloN;

    // Timing accumulator for precise speed control
    private double accumulatedUpdates = 0.0;
    private static final double UPDATES_PER_FRAME = 1.5;

    public Ralph(Player felix) {
        this.felix = felix;
        this.x = CENTER_X;
        this.y = 135;
        this.targetX = CENTER_X;
        
        loadImages();
    }

    public Ralph(Player felix, LivelloN livelloN) {
        this(felix);
        this.livelloN = livelloN;
    }

    private void loadImages() {
        // Load Movement Sprites
        moveImages = new Image[2];
        moveImages[0] = ResourceManager.get().getImage("/ralph/move1.png");
        moveImages[1] = ResourceManager.get().getImage("/ralph/move8.png");

        // Load Throwing Sequence
        throwImages = new Image[throwSequenceNames.length];
        for (int i = 0; i < throwSequenceNames.length; i++) {
            throwImages[i] = ResourceManager.get().getImage("/ralph/" + throwSequenceNames[i] + ".png");
        }
    }

    /**
     * Updates Ralph's logic (AI, Movement, Projectiles).
     */
    @Override
    public void update() {
        if (!isActive) return;

        // Sync difficulty if level changed
        if (livelloN != null && livelloN.getNumeroLivello() != currentLevel) {
            setLevel(livelloN.getNumeroLivello());
        }

        // Logic throttling based on difficulty speed
        double adjustedUpdatesPerFrame = UPDATES_PER_FRAME * speedMultiplier;
        accumulatedUpdates += adjustedUpdatesPerFrame;

        while (accumulatedUpdates >= 1.0) {
            runGameLogic();
            accumulatedUpdates -= 1.0;
        }
    }

    private void runGameLogic() {
        if (currentState == State.MOVING) {
            updateMovement();
        } else if (currentState == State.THROWING) {
            updateThrowing();
        }

        // Update Bricks
        for (int i = 0; i < bricks.size(); i++) {
            Brick brick = bricks.get(i);
            brick.update();
            
            // Collision Check with Felix
            // Note: We access Felix's hitbox directly for performance
            if (brick.brickGetHitbox().intersects(felix.getHitbox())) {
                felix.handleCollisionWithBrick(brick);
            }
        }

        // Remove off-screen bricks
        bricks.removeIf(Brick::isOutOfScreen);
    }

    private void updateMovement() {
        int moveSpeed = (int) (2 * speedMultiplier);

        // Check if reached target (within margin of error)
        if (Math.abs(x - targetX) <= moveSpeed) {
            x = targetX;
            currentState = State.THROWING;
            throwAnimationFrame = 0;
            bricksThrown = 0;
            frameCounter = 0;
            moveDirection = 0;
        } else {
            // Move
            x += moveDirection * moveSpeed;

            // Animate walking
            frameCounter++;
            if (frameCounter >= frameDelay / 2) {
                currentFrame = (currentFrame + 1) % 2;
                frameCounter = 0;
            }
        }
    }

    private void updateThrowing() {
        frameCounter++;

        if (frameCounter >= frameDelay) {
            frameCounter = 0;

            // Trigger brick spawn on specific animation frames (Smashing frames)
            if (isThrowFrame(throwAnimationFrame)) {
                if (bricksThrown < BRICKS_PER_THROW) {
                    releaseBrick();
                    bricksThrown++;
                }
            }

            // Play voice effect on specific frame
            if (throwAnimationFrame == 1 && !hasPlayedVoice4) {
                SoundEffects.playSound("voice4");
                hasPlayedVoice4 = true;
            }

            throwAnimationFrame++;

            // End of Throw Sequence?
            if (throwAnimationFrame >= throwSequenceNames.length) {
                hasPlayedVoice4 = false;

                // Select next position
                sequenceIndex = (sequenceIndex + 1) % positionSequence.length;
                targetX = positionSequence[sequenceIndex];

                // Determine direction
                moveDirection = Integer.compare(targetX, x);

                currentState = State.MOVING;
                currentFrame = 0;
                frameCounter = 0;
            }
        }
    }

    private boolean isThrowFrame(int frameIndex) {
        // Frames where Ralph hits the roof
        return frameIndex == 2 || frameIndex == 3 || 
               frameIndex == 4 || frameIndex == 6 || 
               frameIndex == 8;
    }

    private void releaseBrick() {
        // Random horizontal offset for the brick
        int randomOffset = ThreadLocalRandom.current().nextInt(20, 115);
        int brickSpeed = 4 + brickSpeedBonus;

        bricks.add(new Brick(x + randomOffset, y + 130, brickSpeed));
        SoundEffects.playSound("block");
    }

    @Override
    public void draw(Graphics2D g2) {
        if (!isActive || transition) return;

        Image img = null;

        if (currentState == State.MOVING) {
            img = moveImages[currentFrame % 2];
        } else if (currentState == State.THROWING) {
            if (throwAnimationFrame < throwImages.length) {
                img = throwImages[throwAnimationFrame];
            }
        }

        if (img != null) {
            int width = (int) (img.getWidth(null) * scaleFactor);
            int height = (int) (img.getHeight(null) * scaleFactor);
            g2.drawImage(img, x, y, width, height, null);
        }

        // Draw active bricks
        for (Brick brick : bricks) {
            brick.draw(g2);
        }
    }

    /**
     * Calculates difficulty variables based on current level.
     */
    public void setLevel(int level) {
        this.currentLevel = level;
        
        // 1. Calculate Speed Multiplier
        if (currentLevel <= 2) speedMultiplier = 1.0;
        else if (currentLevel <= 5) speedMultiplier = 1.0 + (currentLevel - 2) * 0.12;
        else if (currentLevel <= 10) speedMultiplier = 1.36 + (currentLevel - 5) * 0.10;
        else if (currentLevel <= 15) speedMultiplier = 1.86 + (currentLevel - 10) * 0.08;
        else speedMultiplier = Math.min(3.0, 2.26 + (currentLevel - 15) * 0.04);

        // 2. Calculate Brick Speed Bonus
        if (currentLevel <= 3) brickSpeedBonus = 0;
        else if (currentLevel <= 6) brickSpeedBonus = 1;
        else if (currentLevel <= 10) brickSpeedBonus = 2;
        else if (currentLevel <= 15) brickSpeedBonus = 3;
        else brickSpeedBonus = 4;

        // 3. Calculate Animation Frequency
        if (currentLevel <= 4) animationSpeedReduction = 0;
        else if (currentLevel <= 8) animationSpeedReduction = 1;
        else if (currentLevel <= 12) animationSpeedReduction = 2;
        else if (currentLevel <= 16) animationSpeedReduction = 3;
        else animationSpeedReduction = 4;

        frameDelay = Math.max(6, 12 - animationSpeedReduction);
        
        System.out.println("🎮 Ralph Level " + level + " | Speed: " + String.format("%.2f", speedMultiplier));
    }

    public void setActive(boolean active) {
        this.isActive = active;
        if (!active) accumulatedUpdates = 0.0;
    }

    public void cleanup() {
        bricks.clear();
        accumulatedUpdates = 0.0;
        frameCounter = 0;
        currentFrame = 0;
        x = CENTER_X;
        y = 135;
        targetX = CENTER_X;
        currentLevel = 1;
        speedMultiplier = 1.0;
        brickSpeedBonus = 0;
        animationSpeedReduction = 0;
        frameDelay = 12;
        currentState = State.THROWING;
        sequenceIndex = 0;
        throwAnimationFrame = 0;
        bricksThrown = 0;
        moveDirection = 0;
    }
    
    public double getSpeedMultiplier() { return speedMultiplier; }
    public int getCurrentLevel() { return currentLevel; }
    public int getBrickSpeedBonus() { return brickSpeedBonus; }
}