package com.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Manages the complex ending sequence triggered after completing even-numbered
 * levels.
 * Sequence of events:
 * 1. Medal descends from the top.
 * 2. Felix receives the medal.
 * 3. Citizens emerge from the building and carry Ralph away.
 * 4. Ralph is thrown off the roof.
 * 5. Ralph lands in mud and returns to the roof for the next level.
 */
public class VictoryCutscene {

    // --- Sprites ---
    // Backgrounds for the cutscene (top part and base of the building)
    private BufferedImage background;
    private BufferedImage backgroundBase;

    // Felix animations
    private BufferedImage felixStanding;
    private BufferedImage felixStandingMedal;

    // Ralph animations
    private BufferedImage ralphStanding;
    private BufferedImage ralphOriz; // Ralph being carried horizontally
    private BufferedImage ralphFalling1, ralphFalling2, ralphFalling3, ralphFalling4;
    private BufferedImage ralphLanding1, ralphLanding2, ralphLanding3; // Landing in mud
    private BufferedImage ralphMoveLeft0, ralphMoveLeft1; // Walking back
    private BufferedImage ralphBack1, ralphBack2; // Climbing back
    private BufferedImage ralphStanding2; // Final pose

    // Medal and Citizens
    private BufferedImage medalImage1, medalImage2, medalImage3;
    private final BufferedImage[] peopleImagesHappy = new BufferedImage[4];
    private final BufferedImage[] peopleImagesAngry = new BufferedImage[4];

    // --- State & Position ---
    // Entity coordinates during the cutscene
    private int felixX, felixY;
    private int ralphX, ralphY;
    private int ralphInitialY;

    // Ralph Physics for the throw
    private int ralphLaunchStartX, ralphLaunchStartY;
    private float ralphVelocityX;
    private float ralphVelocityY;
    private float ralphFallSpeed;
    private boolean ralphFalling;

    // Animation States
    private int ralphSpriteIndex;
    private int ralphLandingSpriteIndex;
    private long lastSpriteChange;

    // --- Camera Logic ---
    // Handles horizontal scrolling to follow Ralph when he is thrown off the roof
    private float cameraX; // Horizontal offset applied to all drawing operations
    private boolean cameraFollowingRalph;
    private float targetCameraX;
    private boolean cameraReturning;
    private static final float CAMERA_SMOOTH_FACTOR = 0.08f; // Higher = faster camera snapping
    private static final int CAMERA_RIGHT_EDGE = 850; // Threshold to start scrolling

    // --- Citizens Logic ---
    // Arrays to manage multiple citizens simultaneously
    private int[] peopleX;
    private int[] peopleY;
    private int[] peopleBaseY;
    private int[] peopleFirstTargetX; // Moving out of door
    private int[] peopleSecondTargetX; // Moving to Ralph
    private int[] peopleThirdTargetX; // Moving to edge of roof
    private boolean[] peopleStarted;
    private boolean[] peopleReachedFirstTarget;
    private boolean[] peopleReachedSecondTarget;
    private boolean peopleMoving;
    private long lastPeopleJump;
    private boolean peopleJumpUp; // For hopping animation

    // --- General Cutscene State ---
    private int phase; // Current stage of the cutscene
    private long phaseStartTime;
    private boolean completed;
    private boolean active;

    // Background Scroll (Vertical)
    private int backgroundY;
    private float medalX, medalY;
    private float medalTargetY;

    // --- Constants (Phases) ---
    // Each phase represents a distinct part of the animation sequence
    private static final int PHASE_INITIAL = 0;
    private static final int PHASE_MEDAL_DESCENDS = 1;
    private static final int PHASE_MEDAL_AT_NECK = 2;
    private static final int PHASE_PEOPLE_ARRIVE_FIRST = 3;
    private static final int PHASE_PEOPLE_CELEBRATE = 4;
    private static final int PHASE_PEOPLE_GET_ANGRY = 5;
    private static final int PHASE_PEOPLE_MOVE_TO_RALPH = 6;
    private static final int PHASE_PEOPLE_LIFT_RALPH = 7;
    private static final int PHASE_PEOPLE_MOVE_TO_EDGE = 8;
    private static final int PHASE_RALPH_PARABOLIC_LAUNCH = 9;
    private static final int PHASE_RALPH_FALLS = 10;
    private static final int PHASE_SWITCH_TO_BASE = 11; // Swaps background image
    private static final int PHASE_RALPH_LANDING = 12;
    private static final int PHASE_RALPH_COMING_BACK = 13;
    private static final int PHASE_END = 14;

    // --- Durations (ms) ---
    // How long each static phase lasts
    private static final long INITIAL_DURATION = 2500;
    private static final long MEDAL_AT_NECK_DURATION = 1500;
    private static final long PEOPLE_ARRIVE_FIRST_DURATION = 4000;
    private static final long PEOPLE_CELEBRATE_DURATION = 2000;
    private static final long PEOPLE_ANGRY_DURATION = 1000;
    private static final long PEOPLE_MOVE_TO_RALPH_DURATION = 2500;
    private static final long PEOPLE_LIFT_RALPH_DURATION = 1000;
    private static final long PEOPLE_MOVE_TO_EDGE_DURATION = 3000;
    private static final long RALPH_PARABOLIC_LAUNCH_DURATION = 1500;
    private static final long LAST_PHASE_TIME = 2000;

    // --- Physics Constants ---
    private static final float MEDAL_SIZE = 96;
    private static final int JUMP_HEIGHT = 8;
    private static final float MEDAL_DESCENT_SPEED = 4;
    private static final int RALPH_ORIZ_Y_OFFSET = 20;
    private static final float GRAVITY = 0.8f;
    private static final float LAUNCH_VELOCITY_X = 8f;
    private static final float LAUNCH_VELOCITY_Y = -12f;
    private static final float CONSTANT_FALL_SPEED = 15f;
    private static final int RALPH_GROUND_Y = 665;

    private int screenWidth;
    private int screenHeight;

    public VictoryCutscene() {
        loadImages();
        reset();
    }

    private void loadImages() {
        ResourceManager rm = ResourceManager.get();

        // Load Backgrounds
        background = rm.getImage("/map/PalazzoCompleto.png");
        backgroundBase = rm.getImage("/map/Palazzo3.png");

        // Load Felix
        felixStanding = rm.getImage("/felix/Static1.png");
        felixStandingMedal = rm.getImage("/felix/Victory.png");

        // Load Ralph (Standing, Falling, Landing, Moving)
        ralphStanding = rm.getImage("/ralph/Final1.png");
        ralphOriz = rm.getImage("/ralph/Final2.png");
        ralphFalling1 = rm.getImage("/ralph/Final3.png");
        ralphFalling2 = rm.getImage("/ralph/Final4.png");
        ralphFalling3 = rm.getImage("/ralph/Final5.png");
        ralphFalling4 = rm.getImage("/ralph/Final6.png");

        ralphLanding1 = rm.getImage("/ralph/Mud1.png");
        ralphLanding2 = rm.getImage("/ralph/Mud2.png");
        ralphLanding3 = rm.getImage("/ralph/Mud3.png");

        ralphMoveLeft0 = rm.getImage("/ralph/RalphMoveLeft0.png");
        ralphMoveLeft1 = rm.getImage("/ralph/RalphMoveLeft1.png");

        ralphBack1 = rm.getImage("/ralph/RalphBack1.png");
        ralphBack2 = rm.getImage("/ralph/RalphBack2.png");
        ralphStanding2 = rm.getImage("/ralph/Move9.png");

        // Load Medal
        medalImage1 = rm.getImage("/felix/medaglia1.png");
        medalImage2 = rm.getImage("/felix/medaglia2.png");
        medalImage3 = rm.getImage("/felix/medaglia3.png");

        // Load Citizens
        for (int i = 0; i < 4; i++) {
            peopleImagesHappy[i] = rm.getImage("/abitanti/Abitante" + (i + 1) + "_f.png");
            peopleImagesAngry[i] = rm.getImage("/abitanti/Abitante" + (i + 1) + ".png");
        }
    }

    /**
     * Resets the cutscene state variables to their initial values.
     */
    private void reset() {
        phase = PHASE_INITIAL;
        completed = false;
        active = false;
        ralphFalling = false;
        peopleMoving = false;
        ralphSpriteIndex = 0;
        ralphLandingSpriteIndex = 0;
        lastSpriteChange = 0;
        lastPeopleJump = 0;
        peopleJumpUp = true;

        // Reset Camera
        cameraX = 0;
        targetCameraX = 0;
        cameraFollowingRalph = false;
        cameraReturning = false;

        // Reset Entity Positions
        felixX = 550;
        felixY = 435;
        ralphX = 750;
        ralphY = 345;
        ralphInitialY = ralphY;

        backgroundY = 0;

        medalX = felixX + 8;
        medalY = felixY - 200;
        medalTargetY = felixY - 20;

        ralphFallSpeed = 0;
        ralphVelocityX = 0;
        ralphVelocityY = 0;

        // Reset Citizens
        int doorX = 486;
        peopleX = new int[] { doorX, doorX, doorX, doorX };
        peopleBaseY = new int[] { 490, 490, 490, 490 };
        peopleY = new int[] { 490, 490, 490, 490 };

        // Define movement targets for citizens
        peopleFirstTargetX = new int[] { 726, 690, 650, 615 };
        peopleSecondTargetX = new int[] { 906, 870, 830, 800 };
        peopleThirdTargetX = new int[] { ralphX + 190, ralphX + 160, ralphX + 128, ralphX + 100 };

        peopleStarted = new boolean[] { false, false, false, false };
        peopleReachedFirstTarget = new boolean[] { false, false, false, false };
        peopleReachedSecondTarget = new boolean[] { false, false, false, false };
    }

    public void start() {
        reset();
        active = true;
        phaseStartTime = System.currentTimeMillis();
    }

    /**
     * Main update loop. Handles state transitions based on time or position.
     */
    public void update() {
        if (!active || completed)
            return;

        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - phaseStartTime;

        switch (phase) {
            case PHASE_INITIAL:
                if (elapsed >= INITIAL_DURATION)
                    nextPhase();
                break;

            case PHASE_MEDAL_DESCENDS:
                medalY += MEDAL_DESCENT_SPEED;
                if (medalY >= medalTargetY) {
                    medalY = medalTargetY;
                    nextPhase();
                }
                break;

            case PHASE_MEDAL_AT_NECK:
                if (elapsed >= MEDAL_AT_NECK_DURATION)
                    nextPhase();
                break;

            case PHASE_PEOPLE_ARRIVE_FIRST:
                // jingle abitanti che festeggiano
                SoundEffects.stopSound("background");
                SoundEffects.playSound("Jingle_abitanti");
                updatePeopleArrival(elapsed, currentTime);
                break;

            case PHASE_PEOPLE_CELEBRATE:
                updatePeopleCelebrating(elapsed, currentTime);
                break;

            case PHASE_PEOPLE_GET_ANGRY:
                // suono passi abitanti
                SoundEffects.playSoundLoop("passi_abitanti");
                break;

            case PHASE_PEOPLE_MOVE_TO_RALPH:
                SoundEffects.stopSound("passi_abitanti");
                updatePeopleMoveToRalph(elapsed);
                break;

            case PHASE_PEOPLE_LIFT_RALPH:
                ralphY = ralphInitialY + RALPH_ORIZ_Y_OFFSET;
                if (elapsed >= PEOPLE_LIFT_RALPH_DURATION)
                    nextPhase();
                break;

            case PHASE_PEOPLE_MOVE_TO_EDGE:
                updatePeopleMoveToEdge(elapsed);
                break;

            case PHASE_RALPH_PARABOLIC_LAUNCH:
                updateRalphLaunch(elapsed);
                break;

            case PHASE_RALPH_FALLS:
                // urla ralph
                SoundEffects.playSound("urla_Ralph");
                updateRalphFall(currentTime);
                break;

            case PHASE_SWITCH_TO_BASE:
                resetForBaseScene(currentTime);
                break;

            case PHASE_RALPH_LANDING:
                updateRalphLanding(currentTime);
                break;

            case PHASE_RALPH_COMING_BACK:
                updateRalphReturn(currentTime);
                break;

            case PHASE_END:
                if (elapsed >= LAST_PHASE_TIME) {
                    completed = true;
                    active = false;
                }
                break;
        }
    }

    // --- Phase Logic Helpers ---

    private void updatePeopleArrival(long elapsed, long currentTime) {
        SoundEffects.stopSound("background");
        peopleMoving = true;
        // Stagger citizen arrival
        int peopleToShow = (int) Math.min(peopleX.length, elapsed / 600);

        for (int i = 0; i < peopleToShow; i++)
            peopleStarted[i] = true;

        for (int i = 0; i < peopleX.length; i++) {
            if (peopleStarted[i] && peopleX[i] < peopleFirstTargetX[i]) {
                peopleX[i] += 4;
                // Hopping animation
                if (currentTime - lastPeopleJump > 200) {
                    peopleJumpUp = !peopleJumpUp;
                    lastPeopleJump = currentTime;
                }
                peopleY[i] = peopleBaseY[i] + (peopleJumpUp ? -JUMP_HEIGHT : 0);
            } else if (peopleStarted[i] && peopleX[i] >= peopleFirstTargetX[i]) {
                peopleReachedFirstTarget[i] = true;
                peopleY[i] = peopleBaseY[i];
            }
        }

        if (elapsed >= PEOPLE_ARRIVE_FIRST_DURATION) {
            peopleMoving = false;
            for (int i = 0; i < peopleY.length; i++)
                peopleY[i] = peopleBaseY[i];
            nextPhase();
        }
    }

    private void updatePeopleCelebrating(long elapsed, long currentTime) {
        // Citizens jump in place
        SoundEffects.stopSound("background");
        SoundEffects.playSound("Jingle_abitanti");
        if (currentTime - lastPeopleJump > 300) {
            peopleJumpUp = !peopleJumpUp;
            lastPeopleJump = currentTime;
        }
        for (int i = 0; i < peopleY.length; i++) {
            if (peopleReachedFirstTarget[i]) {
                peopleY[i] = peopleBaseY[i] + (peopleJumpUp ? -JUMP_HEIGHT : 0);
            }
        }
        if (elapsed >= PEOPLE_CELEBRATE_DURATION) {
            for (int i = 0; i < peopleY.length; i++)
                peopleY[i] = peopleBaseY[i];
            nextPhase();
        }
    }

    private void updatePeopleMoveToRalph(long elapsed) {
        SoundEffects.stopSound("background");
        SoundEffects.playSoundLoop("passi_abitanti");
        boolean allReachedSecond = true;
        for (int i = 0; i < peopleX.length; i++) {
            if (peopleX[i] < peopleSecondTargetX[i]) {
                peopleX[i] += 3;
                allReachedSecond = false;
            } else {
                peopleX[i] = peopleSecondTargetX[i];
                peopleReachedSecondTarget[i] = true;
            }
        }
        if (elapsed >= PEOPLE_MOVE_TO_RALPH_DURATION || allReachedSecond)
            nextPhase();
        SoundEffects.stopSound("passi_abitanti");
    }

    private void updatePeopleMoveToEdge(long elapsed) {
        boolean allReachedThird = true;
        for (int i = 0; i < peopleX.length; i++) {
            if (peopleX[i] < peopleThirdTargetX[i]) {
                int movement = 3;
                peopleX[i] += movement;
                // Ralph moves with the citizens
                if (i == 0)
                    ralphX += movement;
                allReachedThird = false;
            }
        }
        if (elapsed >= PEOPLE_MOVE_TO_EDGE_DURATION || allReachedThird) {
            // Prepare physics for the throw
            ralphLaunchStartX = ralphX;
            ralphLaunchStartY = ralphY;
            ralphVelocityX = LAUNCH_VELOCITY_X;
            ralphVelocityY = LAUNCH_VELOCITY_Y;
            cameraFollowingRalph = true;
            nextPhase();
        }
    }

    private void updateRalphLaunch(long elapsed) {
        ralphX += (int) ralphVelocityX;
        ralphVelocityY += GRAVITY;
        ralphY += (int) ralphVelocityY;
        updateCamera();

        if (elapsed >= RALPH_PARABOLIC_LAUNCH_DURATION || ralphY > ralphLaunchStartY + 100) {
            ralphFallSpeed = CONSTANT_FALL_SPEED;
            nextPhase();
        }
    }

    private void updateRalphFall(long currentTime) {
        SoundEffects.playSound("urla_Ralph");
        ralphFalling = true;
        ralphY += (int) ralphFallSpeed;
        // Parallax effect for background
        backgroundY += (int) (ralphFallSpeed * 0.4f);
        updateCamera();

        if (currentTime - lastSpriteChange > 200) {
            ralphSpriteIndex = (ralphSpriteIndex + 1) % 4;
            lastSpriteChange = currentTime;
        }
        if (ralphY - backgroundY > screenHeight) {
            cameraFollowingRalph = false;
            nextPhase();
        }
    }

    private void resetForBaseScene(long currentTime) {
        ralphY = -100; // Reset Ralph to top off-screen
        ralphX = 1000;
        backgroundY = 0;
        ralphFallSpeed = CONSTANT_FALL_SPEED;
        ralphSpriteIndex = 0;
        lastSpriteChange = currentTime;
        nextPhase();
    }

    private void updateRalphLanding(long currentTime) {
        if (cameraReturning)
            updateCameraSmooth();

        if (ralphY < RALPH_GROUND_Y) {
            ralphY += (int) ralphFallSpeed;
            if (currentTime - lastSpriteChange > 200) {
                ralphSpriteIndex = (ralphSpriteIndex + 1) % 4;
                lastSpriteChange = currentTime;
            }
        } else {
            SoundEffects.playSound("tonfo_Ralph");
            ralphY = RALPH_GROUND_Y;
            ralphSpriteIndex = 0;
            long spriteDuration = (ralphLandingSpriteIndex < 2) ? 300 : 2000;

            if (currentTime - lastSpriteChange > spriteDuration) {
                ralphLandingSpriteIndex++;
                lastSpriteChange = currentTime;

                if (ralphLandingSpriteIndex >= 3) {
                    cameraReturning = true;
                    SoundEffects.playSound("tonfo_Ralph");
                    targetCameraX = 0;
                    nextPhase();
                }
            }
        }
    }

    private void updateRalphReturn(long currentTime) {
        if (ralphY > 135) {
            if (currentTime - lastSpriteChange > 300) {
                ralphSpriteIndex = (ralphSpriteIndex == 0) ? 1 : 0;
                lastSpriteChange = currentTime;
            }

            // Move Ralph back to starting position
            if (ralphX > 644)
                ralphX -= 7;
            else if (ralphY > 135)
                ralphY -= 7;

            updateCameraSmooth();
        } else {
            nextPhase();
        }
    }

    // --- Camera Logic ---

    private void updateCamera() {
        if (!cameraFollowingRalph)
            return;
        if (ralphX - cameraX > CAMERA_RIGHT_EDGE) {
            targetCameraX = ralphX - CAMERA_RIGHT_EDGE;
        }
        updateCameraSmooth();
    }

    private void updateCameraSmooth() {
        cameraX += (targetCameraX - cameraX) * CAMERA_SMOOTH_FACTOR;
    }

    private void nextPhase() {
        phase++;
        phaseStartTime = System.currentTimeMillis();
    }

    // --- Rendering ---

    public void draw(Graphics2D g2, int screenWidth, int screenHeight) {
        if (!active)
            return;

        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        int drawOffsetX = -(int) cameraX;
        BufferedImage currentBackground = (phase >= PHASE_SWITCH_TO_BASE) ? backgroundBase : background;

        if (currentBackground != null) {
            if (phase < PHASE_SWITCH_TO_BASE) {
                g2.drawImage(currentBackground, 401 + drawOffsetX, 440, 1052 + drawOffsetX, 854, 0, 0, 232, 180, null);
            } else {
                g2.drawImage(currentBackground, 401 + drawOffsetX, 21, 1052 + drawOffsetX, 848, 0, 0, 232, 316, null);
            }
        }

        // Draw Felix
        if (phase < PHASE_SWITCH_TO_BASE) {
            BufferedImage felixSprite = (phase >= PHASE_MEDAL_AT_NECK) ? felixStandingMedal : felixStanding;
            if (felixSprite != null) {
                g2.drawImage(felixSprite, felixX + drawOffsetX, felixY,
                        (int) (felixSprite.getWidth() * 2.5), (int) (felixSprite.getHeight() * 2.5), null);
            }
        }

        // Draw Medal
        if (phase >= PHASE_MEDAL_DESCENDS && phase < PHASE_MEDAL_AT_NECK) {
            BufferedImage medalImage = getMedalImageByProgress();
            if (medalImage != null) {
                g2.drawImage(medalImage, (int) medalX + drawOffsetX, (int) (medalY - backgroundY),
                        (int) MEDAL_SIZE, (int) MEDAL_SIZE, null);
            }
        }

        // Draw Ralph
        drawRalph(g2, drawOffsetX);

        // Draw People
        drawPeople(g2, drawOffsetX);
    }

    private void drawRalph(Graphics2D g2, int drawOffsetX) {
        BufferedImage ralphSprite = null;

        if (phase >= PHASE_INITIAL) {
            if (phase < PHASE_PEOPLE_LIFT_RALPH)
                ralphSprite = ralphStanding;
            else if (phase >= PHASE_PEOPLE_LIFT_RALPH && phase < PHASE_RALPH_FALLS)
                ralphSprite = ralphOriz;
            else if (phase >= PHASE_RALPH_FALLS && phase < PHASE_RALPH_LANDING) {
                ralphSprite = getRalphFallingSprite();
            } else if (phase == PHASE_RALPH_LANDING) {
                if (ralphY < RALPH_GROUND_Y)
                    ralphSprite = getRalphFallingSprite();
                else {
                    if (ralphLandingSpriteIndex == 0)
                        ralphSprite = ralphLanding1;
                    else if (ralphLandingSpriteIndex == 1)
                        ralphSprite = ralphLanding2;
                    else
                        ralphSprite = ralphLanding3;
                }
            } else if (phase >= PHASE_RALPH_COMING_BACK) {
                if (ralphX >= 644)
                    ralphSprite = (ralphSpriteIndex == 0) ? ralphMoveLeft0 : ralphMoveLeft1;
                else if (ralphY >= 136)
                    ralphSprite = (ralphSpriteIndex == 0) ? ralphBack1 : ralphBack2;
                else if (phase >= PHASE_END)
                    ralphSprite = ralphStanding2;
            }

            if (ralphSprite != null) {
                int drawY = (phase < PHASE_SWITCH_TO_BASE) ? ralphY - backgroundY : ralphY;
                // Minor vertical correction for return phase
                if (phase >= PHASE_RALPH_COMING_BACK && ralphX >= 644)
                    drawY += 25;

                g2.drawImage(ralphSprite, ralphX + drawOffsetX, drawY,
                        (int) (ralphSprite.getWidth() * 2.5), (int) (ralphSprite.getHeight() * 2.5), null);
            }
        }
    }

    private BufferedImage getRalphFallingSprite() {
        switch (ralphSpriteIndex) {
            case 0:
                return ralphFalling1;
            case 1:
                return ralphFalling2;
            case 2:
                return ralphFalling3;
            default:
                return ralphFalling4;
        }
    }

    private void drawPeople(Graphics2D g2, int drawOffsetX) {
        if (phase < PHASE_SWITCH_TO_BASE) {
            BufferedImage[] currentPeopleImages = (phase >= PHASE_PEOPLE_GET_ANGRY) ? peopleImagesAngry
                    : peopleImagesHappy;

            if (currentPeopleImages != null && phase >= PHASE_PEOPLE_ARRIVE_FIRST) {
                for (int i = 0; i < currentPeopleImages.length; i++) {
                    if (peopleStarted[i] && currentPeopleImages[i] != null) {
                        g2.drawImage(currentPeopleImages[i], peopleX[i] + drawOffsetX, peopleY[i],
                                (int) (currentPeopleImages[i].getWidth() * 2.5),
                                (int) (currentPeopleImages[i].getHeight() * 2.5), null);
                    }
                }
            }
        }
    }

    private BufferedImage getMedalImageByProgress() {
        float totalDistance = medalTargetY - (felixY - 200);
        float currentDistance = medalY - (felixY - 200);
        float progress = currentDistance / totalDistance;

        if (progress < 0.33f)
            return medalImage1;
        else if (progress < 0.66f)
            return medalImage2;
        else
            return medalImage3;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isCompleted() {
        return completed;
    }
}