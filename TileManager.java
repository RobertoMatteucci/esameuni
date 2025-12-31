package com.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the map (background), windows, and core level logic.
 * Handles scrolling, window repair verification, and the transition between level phases.
 * This is effectively the "Level Controller".
 */
public class TileManager {

    // Reference to the main game panel for accessing shared resources
    private final GamePanel gp;
    private final LivelloN livN;
    
    // Sub-Managers
    private final RalphCutscene rcs; // Handles the destruction animation between levels
    public final Pie pie; // Handles the bonus pie mechanic
    public LivelloCompletato livelloCompletatoManager; // Generates window layouts
    
    // Map State Flags
    public boolean livelloInTransizione = false; // True if screen is scrolling
    public boolean attivaRicostruzione = false; // True if windows need to be regenerated
    private boolean destructionFinished = false; // Signal for GamePanel that cutscene is done
    
    // Rendering
    private BufferedImage background;
    private int backgroundY; // Y position of background (for scrolling)
    private int backgroundY1;
    private static final int SCREEN_HEIGHT = 848;
    private static final int SCROLL_SPEED = 4; // How fast the building scrolls down
    
    // Windows & Collision Lists
    private final List<Rectangle> collisionTiles = new ArrayList<>(); // Floors/Platforms
    private final List<Rectangle> collisionWindows = new ArrayList<>(); // Repairable windows
    private Window[] animazioneFinestre; // Array of visual window objects
    private int count; // Number of broken windows remaining
    
    // Constants for Window Layout (Grid size)
    private final int windowWidth = 75;
    private final int windowHeight = 128;
    private final int gapX = 15;
    private final int gapY = -2;

    // Ralph Cutscene (Destruction sequence variables)
    private BufferedImage ralphBack1, ralphBack2;
    private int ralphX = 655;
    private int ralphY = 135;
    private boolean showCutscene = false;
    private boolean toggleImage = false; // For animation flickering
    private long cutsceneStartTime;
    private static final int CUTSCENE_DURATION = 2000; // 2 seconds duration
    private static final int RALPH_SPEED = 4;
    
    // Timer & Bonus System
    private final List<BonusText> bonusTexts = new ArrayList<>();
    private TimeBar timeBar;
    private long startTime;
    private boolean timerAvviato = false;
    private int bonusVisual = 0;
    private boolean showBonus = false;
    private long bonusStartTime;
    private Font arcadeFont;

    // Pie System (Bonus item spawning)
    private long pieSystemStartTime = 0;
    private long nextPieSpawnDelay = 0;
    private static final long MIN_PIE_SPAWN_INTERVAL = 20000; // 20 seconds minimum
    private static final long MAX_PIE_SPAWN_INTERVAL = 35000; // 35 seconds maximum
    private static final long PIE_DURATION = 10000; // Pie stays for 10 seconds
    private long pieSpawnedAt = 0;

    /**
     * Constructor. Initializes the level manager.
     */
    public TileManager(GamePanel gp, LivelloN livN) {
        this.gp = gp;
        this.livN = livN;
        // Initialize Ralph's destruction cutscene controller
        this.rcs = new RalphCutscene(ralphX, ralphY, RALPH_SPEED, 0, 0, 0, 0, 0, 0);
        this.pie = new Pie();
        
        // Get initial window count
        this.count = livN.getFinestreDaAggiustare();
        
        loadResources();
        initializeCollisionData();

        // Setup the level layout generator
        this.livelloCompletatoManager = new LivelloCompletato(
            gp, collisionTiles, collisionWindows,
            windowWidth, windowHeight, gapX, gapY, livN
        );
        
        // Build the initial stage
        this.animazioneFinestre = livelloCompletatoManager.rebuildStage();
        if (this.animazioneFinestre != null) {
            this.count = this.animazioneFinestre.length;
        }
    }

    /**
     * Loads static resources (background, fonts) via ResourceManager.
     */
    private void loadResources() {
        background = ResourceManager.get().getImage("/map/PalazzoCompleto.png");
        // Set initial background position
        backgroundY = background.getHeight() - 318;
        backgroundY1 = background.getHeight();

        ralphBack1 = ResourceManager.get().getImage("/ralph/RalphBack1.png");
        ralphBack2 = ResourceManager.get().getImage("/ralph/RalphBack2.png");
        
        // Load custom font
        this.arcadeFont = ResourceManager.get().getFont().deriveFont(Font.BOLD, 24f);
    }
    
    private void initializeCollisionData() {
        collisionTiles.clear();
        collisionWindows.clear();
    }

    /**
     * Attempts to fix a window at the hammer's location.
     * Called by Player when Spacebar is pressed.
     * @param martello The hitbox of Felix's hammer.
     */
    public void aggiustaFinestra(Rectangle martello) {
        if (animazioneFinestre == null) return;

        for (int i = 0; i < collisionWindows.size(); i++) {
            Window win = animazioneFinestre[i];
            Rectangle hit = collisionWindows.get(i);

            // Check collision with a BROKEN window
            if (!win.isRiparata() && martello.intersects(hit)) {
                
                // Start the timer on the first repair
                if (!timerAvviato) {
                    startLevelTimer();
                    timerAvviato = true;
                    pieSystemStartTime = System.currentTimeMillis();
                    calculateNextSpawnDelay();
                }

                // Repair logic
                win.ripara();
                count--;
                
                // Show "+100" floating text
                int textX = martello.x + martello.width / 2;
                int textY = martello.y;
                bonusTexts.add(new BonusText(textX, textY, 100));
                
                gp.scoreManager.addPoints(100);
                
                // Check Level Completion (0 windows left)
                if (count <= 0) {
                    count = 0;
                    startDestructionCutscene();
                }
                return;
            }
        }
    }
    
    /**
     * Triggers the sequence where Ralph climbs up and smashes the building.
     */
    private void startDestructionCutscene() {
        if (gp.ralph != null) gp.ralph.transition = true;
        showCutscene = true;
        destructionFinished = false;
        
        rcs.reset();
        // ✅ Tell Ralph to run in "Transition Mode" (No speech bubble, fast animation)
        rcs.setSkipIntro(true);
        
        cutsceneStartTime = System.currentTimeMillis();
    }

    /**
     * Main update loop for map logic.
     */
    public void update() {
        // 1. Scroll State
        if (livelloInTransizione) {
            handleScrolling();
            return;
        }

        // 2. Update Window Animations (Blinking)
        if (animazioneFinestre != null) {
            for (Window window : animazioneFinestre) {
                window.update();
            }
        }

        // 3. Update Destruction Cutscene
        if (showCutscene) {
            handleDestructionCutscene();
        }
        
        // 4. Update Floating Text
        bonusTexts.removeIf(bt -> {
            bt.update();
            return bt.isExpired();
        });
        
        // 5. Hide Bonus Text after delay
        if (showBonus && (System.currentTimeMillis() - bonusStartTime > 2000)) {
            showBonus = false;
        }

        // 6. Gameplay Logic (Pie & Timer)
        if (!livelloInTransizione && !showCutscene && timerAvviato) {
            updateGameplayLogic();
        }
    }
    
    /**
     * Logic for scrolling the background down to simulate climbing up.
     */
    private void handleScrolling() {
        gp.player.setActive(false); // Lock player
        if (backgroundY > 70) {
            gp.player.y += (SCROLL_SPEED * 3); // Move player down with screen
            backgroundY -= SCROLL_SPEED;
            backgroundY1 -= SCROLL_SPEED;
        } else {
            // Scroll complete
            livelloInTransizione = false;
            attivaRicostruzione = true; // Trigger map rebuild
            gp.transitionManager.completeTransition(); // Reset player position
        }
    }
    
    /**
     * Logic for Ralph's destruction animation between levels.
     */
    private void handleDestructionCutscene() {
        long elapsed = System.currentTimeMillis() - cutsceneStartTime;

        if (elapsed < CUTSCENE_DURATION) {
            toggleImage = (elapsed % 300 < 150); // Blink effect
            ralphY -= RALPH_SPEED; // Move Ralph up
            rcs.update(); // Update debris logic
        } else {
            // Cutscene Over
            showCutscene = false;
            if (gp.ralph != null) gp.ralph.transition = false;
            
            applyTimeBonus();
            
            // Signal GamePanel to advance
            destructionFinished = true; 
        }
    }
    
    /**
     * Calculates and adds time bonus to score.
     */
    private void applyTimeBonus() {
        if (timerAvviato) {
            long tempoTrascorso = (System.currentTimeMillis() - startTime) / 1000;
            int bonus = calcolaBonus((int) tempoTrascorso);
            gp.scoreManager.addPoints(bonus);
            
            bonusVisual = bonus;
            showBonus = true;
            bonusStartTime = System.currentTimeMillis();
            
            int totalScore = gp.scoreManager.getScore();
            if (HighScoreManager.isHighScore(totalScore)) {
                HighScoreManager.saveHighScore(totalScore);
            }
        }
    }
    
    /**
     * Logic for spawning pies and checking collision.
     */
    private void updateGameplayLogic() {
        if (pieSystemStartTime > 0) {
            trySpawnPie();
            pie.update();
            
            // Check if Felix ate the pie
            if (gp.player != null && !gp.player.isEating() && pie.checkCollision(gp.player.getHitbox())) {
                gp.player.startEatingAnimation();
                pie.startEating();
                gp.scoreManager.addPoints(50);
                
                new Thread(() -> {
                    try { Thread.sleep(2000); } catch (Exception ignored) {}
                    pie.consume();
                }).start();
            }
        }
        
        if (timeBar != null) {
            timeBar.update();
        }
    }

    /**
     * Main render method for the map and background elements.
     */
    public void draw(Graphics2D g2) {
        // Reset background position for odd levels (simulating infinite climb)
        if (livN != null && livN.getNumeroLivello() % 2 != 0 && !livelloInTransizione) {
            backgroundY = background.getHeight() - 318;
            backgroundY1 = background.getHeight();
        }
        
        // Draw Background
        if (background != null) {
            g2.drawImage(background, 401, 21, 1052, SCREEN_HEIGHT,
                         0, backgroundY, 232, backgroundY1, null);
        }

        // Draw Windows
        if (count > 0 && !livelloInTransizione && animazioneFinestre != null) {
            for (Window window : animazioneFinestre) {
                window.draw(g2);
            }
        }

        // Draw Destruction Cutscene (Ralph)
        if (showCutscene) {
            BufferedImage currentImg = toggleImage ? ralphBack1 : ralphBack2;
            if (currentImg != null) {
                int scaledWidth = currentImg.getWidth() * 2;
                int scaledHeight = currentImg.getHeight() * 2;
                g2.drawImage(currentImg, ralphX, ralphY, scaledWidth, scaledHeight, null);
            }
            rcs.spawnFallingObjects();
            rcs.draw(g2);
        }
        
        // Draw Pie
        if (timerAvviato && pieSystemStartTime > 0) {
            pie.draw(g2);
        }
        
        // Draw Time Bonus Text
        if (showBonus) {
            g2.setFont(arcadeFont.deriveFont(36f));
            String text = "+" + bonusVisual + " TIME BONUS!";
            
            // Shadow
            g2.setColor(Color.BLACK);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    if (dx != 0 || dy != 0) g2.drawString(text, 552 + dx, 102 + dy);
                }
            }
            // Main Text
            g2.setColor(Color.YELLOW);
            g2.drawString(text, 552, 102);
        }
        
        // Draw Floating Scores
        for (BonusText bt : bonusTexts) {
            bt.draw(g2, arcadeFont);
        }
    }
    
    public void drawTimer(Graphics2D g2) {
        if (timerAvviato && timeBar != null) {
            timeBar.draw(g2);
        }
    }

    // Logic to randomize pie spawn
    private void trySpawnPie() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - pieSystemStartTime;
        
        if (pie.isActive() && currentTime - pieSpawnedAt >= PIE_DURATION) {
            pie.reset();
            calculateNextSpawnDelay();
        }
        
        if (!pie.isActive() && elapsed >= nextPieSpawnDelay) {
            if (!collisionWindows.isEmpty()) {
                int idx = (int) (Math.random() * collisionWindows.size());
                pie.spawn(collisionWindows.get(idx));
                pieSpawnedAt = currentTime;
                calculateNextSpawnDelay();
                pieSystemStartTime = currentTime;
            }
        }
    }
    
    private void calculateNextSpawnDelay() {
        nextPieSpawnDelay = MIN_PIE_SPAWN_INTERVAL + 
            (long) (Math.random() * (MAX_PIE_SPAWN_INTERVAL - MIN_PIE_SPAWN_INTERVAL));
    }
    
    public void startScroll() {
        gp.player.setActive(false);
        livelloInTransizione = true;
        attivaRicostruzione = false;
    }
    
    public void startLevelTimer() {
        startTime = System.currentTimeMillis();
        int maxTime = calculateMaxTime();
        timeBar = new TimeBar(maxTime);
    }
    
    private int calculateMaxTime() {
        int lvl = livN.getNumeroLivello();
        if (lvl <= 2) return 120;
        if (lvl <= 5) return 100;
        if (lvl <= 10) return 90;
        if (lvl <= 15) return 80;
        return 70;
    }
    
    private int calcolaBonus(int timeTaken) {
        if (timeTaken <= 30) return 2000;
        if (timeTaken <= 40) return 1000;
        if (timeTaken <= 50) return 600;
        if (timeTaken <= 60) return 500;
        if (timeTaken <= 70) return 400;
        if (timeTaken <= 80) return 200;
        if (timeTaken <= 120) return 100;
        return 0;
    }
    
    /**
     * Resets the level logic (timer, pie, cutscenes) for a new game or level.
     */
    public void resetTimer() {
        timerAvviato = false;
        timeBar = null;
        bonusTexts.clear();
        showBonus = false;
        showCutscene = false; 
        destructionFinished = false; 
        
        pie.reset();
        pieSystemStartTime = 0;
        nextPieSpawnDelay = 0;
        pieSpawnedAt = 0;
        
        rcs.reset();
        // ✅ Wipe broken glass immediately when level resets
        rcs.clearDebris(); 
        
        ralphY = 135;
    }

    // --- Getters and Setters ---

    public List<Rectangle> getCollisionTiles() {
        return collisionTiles;
    }

    public List<Rectangle> getCollisioneFinestre() {
        return collisionWindows;
    }
    
    public Window[] getAnimazioneFinestre() {
        return animazioneFinestre;
    }
    
    /**
     * Sets new windows when the level is rebuilt.
     * Automatically updates the window count.
     */
    public void setAnimazioneFinestre(Window[] windows) {
        this.animazioneFinestre = windows;
        this.count = (windows != null) ? windows.length : 0;
    }

    public Pie getPie() {
        return pie;
    }
    
    public int getCount() {
        return count;
    }
    
    public boolean isCutsceneActive() {
        return showCutscene;
    }
    
    public boolean isDestructionFinished() {
        return destructionFinished;
    }
}