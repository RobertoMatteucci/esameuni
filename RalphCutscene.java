package com.game;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages the cutscene where Ralph destroys the building.
 * Includes logic to distinguish between the "Game Intro" (Bubble + Debris) 
 * and "Level Transition" (Fast + Clean).
 */
public class RalphCutscene {
    
    private int x, y;
    private final int startX, startY;
    private final int speed, jumpSpeed;
    private final int targetX1, targetX2, targetY1, targetY2, finalX;
    
    private int phase;
    private boolean movingLeft;
    private int frameCount;
    private int finalAnimationFrames;
    private boolean cutsceneFinished = false;
    
    private boolean skipIntro = false;

    private Image image1, image2, jump1, jump2, jump3, exit1, exit2, finalPose1, finalPose2, extraImage;
    private Image debris1, debris2, debris3, debris4;
    private final List<FallingObject> fallingObjects;

    public RalphCutscene(int startX, int startY, int speed, int jumpSpeed, 
                         int targetX1, int targetX2, int targetY1, int targetY2, int finalX) {
        this.x = startX;
        this.y = startY;
        this.startX = startX;
        this.startY = startY;
        this.speed = speed;
        this.jumpSpeed = jumpSpeed;
        this.targetX1 = targetX1;
        this.targetX2 = targetX2;
        this.targetY1 = targetY1;
        this.targetY2 = targetY2;
        this.finalX = finalX;
        
        this.phase = 0;
        this.movingLeft = true;
        this.fallingObjects = new ArrayList<>();

        loadImages();
    }

    private void loadImages() {
        ResourceManager rm = ResourceManager.get();
        image1 = rm.getImage("/ralph/RalphBack1.png");
        image2 = rm.getImage("/ralph/RalphBack2.png");
        jump1 = rm.getImage("/ralph/RalphUp0.png");
        jump2 = rm.getImage("/ralph/RalphUp1.png");
        jump3 = rm.getImage("/ralph/RalphUp2.png");
        exit1 = rm.getImage("/ralph/RalphMoveLeft0.png");
        exit2 = rm.getImage("/ralph/RalphMoveLeft1.png");
        finalPose1 = rm.getImage("/ralph/Move9.png");
        finalPose2 = rm.getImage("/ralph/Move10.png");
        extraImage = rm.getImage("/ralph/MessageRalph0.png");
        debris1 = rm.getImage("/map/rotta1.png");
        debris2 = rm.getImage("/map/rotta2.png");
        debris3 = rm.getImage("/map/rotta3.png");
        debris4 = rm.getImage("/map/rotta4.png");
    }

    public void update() {
        if (cutsceneFinished) return;

        if (phase == 0 && skipIntro) {
            phase = 1;
        }

        switch (phase) {
            case 0: // Intro Bubble (Only happens if skipIntro is false)
                finalAnimationFrames++;
                if (finalAnimationFrames > 100) {
                    phase = 1;
                    finalAnimationFrames = 0;
                }
                break;
                
            case 1: if (x < targetX1) { x += speed; spawnFallingObjects(); } else phase = 2; break;
            case 2: if (y > targetY1) y -= jumpSpeed; else phase = 3; break;
            case 3: if (x > targetX2) { x -= speed; spawnFallingObjects(); } else phase = 4; break;
            case 4: if (x < targetX1) { x += speed; spawnFallingObjects(); } else phase = 5; break;
            case 5: if (y > targetY2) y -= jumpSpeed; else phase = 6; break;
            case 6: if (x > finalX) x -= speed; else { phase = 7; fallingObjects.clear(); } break;
            
            case 7: 
                if (skipIntro) {
                    cutsceneFinished = true;
                    fallingObjects.clear();
                } else {
                    // Only play full tantrum for the very first game start
                    finalAnimationFrames++;
                    if (finalAnimationFrames > 100) {
                        cutsceneFinished = true;
                        fallingObjects.clear();
                    }
                }
                break;
        }

        if (phase < 7) {
            frameCount++;
            if (frameCount % 10 == 0) movingLeft = !movingLeft;
        }

        if (phase >= 7 || cutsceneFinished) {
            fallingObjects.clear();
        } else {
            fallingObjects.removeIf(obj -> {
                obj.update();
                return obj.isOffScreen();
            });
        }
    }

    public void draw(Graphics2D g2) {
        if (cutsceneFinished) return;

        // This prevents the "Ghost Ralph" from appearing.
        if (skipIntro) {
            return;
        }

        Image currentImage = image1;

        if (phase == 0) {
            if (extraImage != null) g2.drawImage(extraImage, x + 50, y - 50, 250, 70, null);
            currentImage = (frameCount % 50 < 25) ? finalPose1 : finalPose2;
        } else if (phase == 2 || phase == 5) {
            int f = frameCount % 30;
            if (f < 10) currentImage = jump1; else if (f < 20) currentImage = jump2; else currentImage = jump3;
        } else if (phase == 6) {
            currentImage = movingLeft ? exit1 : exit2;
        } else if (phase == 7) {
            currentImage = (finalAnimationFrames % 50 < 25) ? finalPose1 : finalPose2;
        } else {
            currentImage = movingLeft ? image1 : image2;
        }

        if (currentImage != null) {
            g2.drawImage(currentImage, x, y, currentImage.getWidth(null) * 2, currentImage.getHeight(null) * 2, null);
        }

        for (FallingObject obj : fallingObjects) obj.draw(g2);
    }

    public void spawnFallingObjects() {
        //Disable debris if we are in a level transition (skipIntro is true)
        if (skipIntro) return;

        if (ThreadLocalRandom.current().nextDouble() < 0.15 && fallingObjects.size() < 8) {
            if (fallingObjects.isEmpty() || fallingObjects.size() % 3 == 0) {
                SoundEffects.playSound("shatter" + ThreadLocalRandom.current().nextInt(8));
            }
            Image img = switch (ThreadLocalRandom.current().nextInt(4)) {
                case 0 -> debris1; case 1 -> debris2; case 2 -> debris3; default -> debris4;
            };
            int ox = ThreadLocalRandom.current().nextInt(-30, 31);
            int oy = ThreadLocalRandom.current().nextInt(-15, 16);
            fallingObjects.add(new FallingObject(x, y, ox, oy, 5, img));
        }
    }

    public void reset() {
        this.x = startX;
        this.y = startY;
        this.phase = 0;
        this.frameCount = 0;
        this.finalAnimationFrames = 0;
        this.cutsceneFinished = false;
        fallingObjects.clear();
    }
    
    public void clearDebris() {
        fallingObjects.clear();
    }
    
    // ✅ This method is called by TileManager to enable "Transition Mode"
    public void setSkipIntro(boolean skip) {
        this.skipIntro = skip;
    }

    public int getPhase() { return phase; }
    public boolean isFinished() { return cutsceneFinished; }
}