package com.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.InputStream;

/**
 * Central controller for all game cutscenes.
 * Manages transitions between Intro, Citizens, Felix, and Victory sequences.
 * Coordinates rendering and logic updates for cutscenes so GamePanel doesn't get cluttered.
 */
public class CutsceneManager {

    private final GamePanel gp;
    
    // --- Resources ---
    private final Image[] backgrounds;
    private Image nuvola1, nuvola2;
    
    // --- Scene Objects ---
    private Cloud[] clouds, clouds1, clouds2, clouds3, clouds4, clouds5, clouds6;
    private final CraneManager craneManager;
    
    // Sub-Cutscenes (Delegates specific cutscene logic to these classes)
    private final RalphCutscene ralphCutscene;
    private final RalphCutscene2 ralphCutscene2;
    private final VictoryCutscene victoryCutscene;
    private final FelixIntroCutscene felixIntroCutscene;
    private final CitizensCutscene citizensCutscene;

    // --- State Enum ---
    public enum CutsceneType {
        NONE,
        INTRO,
        CITIZENS,
        FELIX_INTRO,
        RALPH_CLIMB,
        YOU_FIXED_IT,
        VICTORY
    }

    // Tracks which cutscene is currently playing
    private CutsceneType activeCutscene = CutsceneType.INTRO;
    
    // Intro Specific State
    private int currentBackground = 0;
    private boolean ralphSceneActive = false;
    private boolean ralphFinished = false;
    private int backgroundTimer = 0;
    private int craneTimer = 0;
    private static final int BACKGROUND_CHANGE_DELAY = 30; // Frames to wait before changing intro background
    private static final int CRANE_UPDATE_DELAY = 1;

    // "YOU FIXED IT" State
    private boolean finalCutsceneActive = false;
    private long finalCutsceneStartTime;
    private static final int FINAL_TEXT_DURATION = 3000; // 3 seconds duration
    private long lastColorChange = 0;
    private Color currentColor = Color.YELLOW;
    private Font arcadeFont;

    /**
     * Constructor initializes all sub-managers and loads resources.
     * @param gp Reference to the main GamePanel.
     */
    public CutsceneManager(GamePanel gp) {
        this.gp = gp;
        
        // Initialize Sub-Managers
        this.craneManager = new CraneManager();
        this.victoryCutscene = new VictoryCutscene();
        this.citizensCutscene = new CitizensCutscene(gp);
        this.felixIntroCutscene = new FelixIntroCutscene(gp.player, gp);
        
        // Initialize Complex Cutscenes with specific coordinates
        this.ralphCutscene2 = new RalphCutscene2(655, 135, -50, 4, gp);
        this.ralphCutscene = new RalphCutscene(430, 692, 10, 10, 910, 453, 385, 140, 648);
        
        this.backgrounds = new Image[8];
        
        loadResources();
        createClouds();
        startIntroCutscene();
    }

    /**
     * Loads images and fonts using the ResourceManager.
     */
    private void loadResources() {
        ResourceManager rm = ResourceManager.get();
        
        // Load Background Sequence for Intro
        backgrounds[0] = rm.getImage("/map/Palazzo0.png");
        backgrounds[1] = rm.getImage("/map/Palazzo1.png");
        backgrounds[2] = rm.getImage("/map/Palazzo2.png");
        backgrounds[3] = rm.getImage("/map/Palazzo3.png"); 
        backgrounds[4] = rm.getImage("/map/Palazzo3.png");
        backgrounds[5] = rm.getImage("/map/Palazzo3.png");
        backgrounds[6] = rm.getImage("/map/Palazzo3.png");
        backgrounds[7] = rm.getImage("/map/Palazzo4.png");

        // Load Clouds
        nuvola1 = rm.getImage("/map/Nuvola1.png");
        nuvola2 = rm.getImage("/map/Nuvola2.png");
        
        // Load Font
        this.arcadeFont = rm.getFont().deriveFont(36f);
    }

    /**
     * Initializes cloud objects for the intro background.
     */
    private void createClouds() {
        clouds = new Cloud[4];
        clouds1 = new Cloud[3];
        clouds2 = new Cloud[4];
        clouds3 = new Cloud[3];
        clouds4 = new Cloud[4];
        clouds5 = new Cloud[3];
        clouds6 = new Cloud[4];

        for (int i = 0; i < clouds.length; i++) {
            int x = 430 + i * 150;
            int y = 750;
            clouds[i] = new Cloud(nuvola1, nuvola2, x, y, 150, 100);
            clouds2[i] = new Cloud(nuvola1, nuvola2, x, y - 50, 150, 100);
            clouds4[i] = new Cloud(nuvola1, nuvola2, x, y - 100, 150, 100);
            clouds6[i] = new Cloud(nuvola1, nuvola2, x, y - 150, 150, 100);
        }

        for (int i = 0; i < clouds1.length; i++) {
            int x = 430 + i * 150;
            int y = 725;
            clouds1[i] = new Cloud(nuvola1, nuvola2, x + 75, y, 150, 100);
            clouds3[i] = new Cloud(nuvola1, nuvola2, x + 75, y - 75, 150, 100);
            clouds5[i] = new Cloud(nuvola1, nuvola2, x + 75, y - 125, 150, 100);
        }
    }

    private void moveClouds() {
        Cloud[][] allClouds = {clouds, clouds1, clouds2, clouds3, clouds4, clouds5, clouds6};
        for (Cloud[] group : allClouds) {
            for (Cloud cloud : group) {
                if (cloud != null) cloud.move(270);
            }
        }
    }

    /**
     * Starts the game intro sequence.
     */
    private void startIntroCutscene() {
        activeCutscene = CutsceneType.INTRO;
        gp.ralph.setActive(false);
        backgroundTimer = 0;
        craneTimer = 0;
        loadAllSounds();
    }

    private void loadAllSounds() {
        SoundEffects.loadSound("die0", "snd/die0.wav");
        SoundEffects.loadSound("die1", "snd/die1.wav");
        SoundEffects.loadSound("jump", "snd/jump.wav");
        SoundEffects.loadSound("jumpdown", "snd/jumpdown.wav");
        SoundEffects.loadSound("block", "snd/block.wav");
        SoundEffects.loadSound("hammer", "snd/hammer.wav");
        SoundEffects.loadSound("voice0", "snd/voice0.wav");
        SoundEffects.loadSound("voice4", "snd/voice4.wav");
    }

    /**
     * Main update loop for cutscenes. Delegates to specific handlers based on activeCutscene.
     */
    public void update() {
        switch (activeCutscene) {
            case INTRO:
                updateIntroCutscene();
                break;

            case CITIZENS:
                citizensCutscene.update();
                if (citizensCutscene.isFinished()) {
                    startFelixIntroCutscene();
                }
                break;

            case FELIX_INTRO:
                felixIntroCutscene.update();
                if (felixIntroCutscene.isFinished()) {
                    // Cutscene chain finished -> Start Gameplay
                    activeCutscene = CutsceneType.NONE;
                    gp.ralph.setActive(true);
                    gp.player.setActive(true);
                }
                break;

            case RALPH_CLIMB:
                ralphCutscene2.update();
                if (ralphCutscene2.isFinished()) {
                    activeCutscene = CutsceneType.NONE;
                    gp.ralph.setActive(true);
                    ralphCutscene2.reset();
                }
                break;

            case YOU_FIXED_IT:
                updateYouFixedIt();
                break;

            case VICTORY:
                victoryCutscene.update();
                if (victoryCutscene.isCompleted()) {
                    activeCutscene = CutsceneType.NONE;
                }
                break;

            case NONE:
                break;
        }
        
        updateCloudAnimations();
    }
    
    private void updateCloudAnimations() {
        Cloud[][] allClouds = {clouds, clouds1, clouds2, clouds3, clouds4, clouds5, clouds6};
        for (Cloud[] group : allClouds) {
            for (Cloud cloud : group) {
                if (cloud != null) cloud.update();
            }
        }
    }

    /**
     * Updates the intro sequence logic (scrolling background, Ralph breaking windows).
     */
    private void updateIntroCutscene() {
        backgroundTimer++;

        if (backgroundTimer >= BACKGROUND_CHANGE_DELAY && (!ralphSceneActive || ralphFinished)) {
            backgroundTimer = 0;
            currentBackground++;

            if (currentBackground == 4) {
                ralphSceneActive = true;
                ralphFinished = false;
            }

            if (currentBackground >= 8 && ralphFinished) {
                startCitizensCutscene();
                return;
            }

            moveClouds();
        }

        craneTimer++;
        if (craneTimer >= CRANE_UPDATE_DELAY) {
            craneTimer = 0;

            if (ralphSceneActive && !ralphFinished) {
                ralphCutscene.update();

                if (ralphCutscene.getPhase() == 7) {
                    ralphFinished = true;
                    ralphSceneActive = false;
                    currentBackground = 6;
                    backgroundTimer = 0;
                }
            }

            craneManager.update();
        }
    }
    
    /**
     * Updates the "You Fixed It" blinking text screen.
     */
    private void updateYouFixedIt() {
        long now = System.currentTimeMillis();
        
        // Wait for text duration, then start victory sequence
        if (now - finalCutsceneStartTime >= FINAL_TEXT_DURATION) {
            finalCutsceneActive = false;
            startVictoryCutscene();
        }

        // Blink color
        if (now - lastColorChange >= 250) {
            currentColor = new Color(
                    (int) (Math.random() * 255),
                    (int) (Math.random() * 255),
                    (int) (Math.random() * 255));
            lastColorChange = now;
        }
    }

    /**
     * Main draw loop for cutscenes. Delegates to specific drawers.
     */
    public void draw(Graphics2D g2) {
        switch (activeCutscene) {
            case INTRO:
                drawIntroCutscene(g2);
                break;

            case CITIZENS:
                drawCitizensBackground(g2);
                citizensCutscene.draw(g2);
                break;

            case FELIX_INTRO:
                drawFelixIntroBackground(g2);
                felixIntroCutscene.draw(g2);
                break;

            case RALPH_CLIMB:
                drawRalphClimbCutscene(g2);
                break;

            case YOU_FIXED_IT:
                drawYouFixedItCutscene(g2);
                break;

            case VICTORY:
                victoryCutscene.draw(g2, gp.screenWidth, gp.screenHeight);
                break;

            case NONE:
                break;
        }
    }

    private void drawIntroCutscene(Graphics2D g2) {
        if (currentBackground < backgrounds.length && backgrounds[currentBackground] != null) {
            g2.drawImage(backgrounds[currentBackground], 401, 21, 651, 825, null);
        }

        if (currentBackground < 7) {
            Cloud[][] allClouds = {clouds, clouds1, clouds2, clouds3, clouds4, clouds5, clouds6};
            for (Cloud[] group : allClouds) {
                for (Cloud cloud : group) {
                    if (cloud != null) cloud.draw(g2);
                }
            }
        }

        if (ralphSceneActive && !ralphFinished) {
            ralphCutscene.draw(g2);
        } else if (ralphFinished && currentBackground >= 6 && currentBackground < 8) {
            ralphCutscene.draw(g2);
        }

        if (currentBackground < 7) {
            craneManager.draw(g2);
        }
    }

    private void drawCitizensBackground(Graphics2D g2) {
        if (backgrounds[7] != null) {
            g2.drawImage(backgrounds[7], 401, 21, 651, 825, null);
        }
        if (ralphFinished && ralphCutscene != null) {
            ralphCutscene.draw(g2);
        }
    }

    private void drawFelixIntroBackground(Graphics2D g2) {
        if (backgrounds[7] != null) {
            g2.drawImage(backgrounds[7], 401, 21, 651, 825, null);
        }
        if (ralphFinished && ralphCutscene != null) {
            ralphCutscene.draw(g2);
        }
    }

    private void drawRalphClimbCutscene(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());
        ralphCutscene2.draw(g2);
    }

    private void drawYouFixedItCutscene(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());

        if (arcadeFont != null) g2.setFont(arcadeFont);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        String text = "YOU FIXED IT";
        int x = 480;
        int y = 400;

        g2.setColor(currentColor);
        g2.drawString(text, x, y);
    }

    // Public methods to trigger specific cutscenes from GamePanel

    public void startCitizensCutscene() {
        System.out.println("🎬 Cutscene: Citizens");
        activeCutscene = CutsceneType.CITIZENS;
        gp.player.setActive(false);
        gp.ralph.setActive(false);
        citizensCutscene.start();
    }

    public void startFelixIntroCutscene() {
        System.out.println("🎬 Cutscene: Felix Intro");
        activeCutscene = CutsceneType.FELIX_INTRO;
        gp.player.setActive(false);
        gp.ralph.setActive(false);
        felixIntroCutscene.start();
    }

    public void startRalphClimbCutscene() {
        System.out.println("🎬 Cutscene: Ralph Climb");
        activeCutscene = CutsceneType.RALPH_CLIMB;
        ralphCutscene2.reset();
        gp.ralph.cleanup(); 
    }

    public void startYouFixedItCutscene() {
        System.out.println("🎬 Cutscene: YOU FIXED IT");
        activeCutscene = CutsceneType.YOU_FIXED_IT;
        finalCutsceneActive = true;
        finalCutsceneStartTime = System.currentTimeMillis();
        lastColorChange = finalCutsceneStartTime;
    }

    public void startVictoryCutscene() {
        System.out.println("🎬 Cutscene: Victory");
        activeCutscene = CutsceneType.VICTORY;
        victoryCutscene.start();
    }

    public boolean isAnyCutsceneActive() {
        return activeCutscene != CutsceneType.NONE;
    }

    /**
     * Resets all cutscenes. Called when restarting the game.
     */
    public void cleanup() {
        if (ralphCutscene != null) ralphCutscene.reset();
        if (ralphCutscene2 != null) ralphCutscene2.reset();
        if (felixIntroCutscene != null) felixIntroCutscene.start(); 
        if (citizensCutscene != null) citizensCutscene.reset();

        ralphSceneActive = false;
        ralphFinished = false;
        currentBackground = 0;
        backgroundTimer = 0;
        craneTimer = 0;
    }
}