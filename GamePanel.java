package com.game;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * The main game container.
 * Handles the Game Loop, rendering pipeline, and orchestrates all sub-managers.
 * This class is the "brain" of the game, deciding what to update and draw.
 */
public class GamePanel extends JPanel implements Runnable {

    // --- Screen Settings ---
    public final int screenWidth;
    public final int screenHeight;
    
    // Define the playable area to keep the game centered on different screens
    // These coordinates define the clipping region for the main gameplay view.
    public static final int GAME_AREA_X = 360;
    public static final int GAME_AREA_WIDTH = 745;
    public static final int GAME_AREA_Y = 0;
    public static final int GAME_AREA_HEIGHT = 848;

    private static final int FPS = 30; // Target Frames Per Second
    private Thread gameThread; // Thread for the main game loop

    // --- Managers & Entities ---
    // Logic managers for levels, map tiles, input, and collisions
    public LivelloN livelloN;
    public TileManager tileM;
    public KeyHandler keyH;
    public CollisionManager collisionManager;
    
    // Game Entities
    public Player player;
    public Ralph ralph;
    public Duck duck;
    
    // High-level Managers for cutscenes, score, and transitions
    public CutsceneManager cutsceneManager;
    public ScoreManager scoreManager;
    public LevelTransitionManager transitionManager;
    
    // Utility for drawing debug hitboxes (development only)
    private DebugDrawer debugDrawer;

    // --- Game State ---
    private GameOverScreen gameOverScreen;
    private boolean isGameOver = false;
    private boolean isPlayingDeathAnimation = false;
    private boolean levelThemePlaying = false;
    
    // Level Completion State
    public boolean livelloClear = false; // Triggers "Level Clear" text display
    private boolean levelLoaded = false; // Safety flag: prevents logic updates until level is built
    private long tempoClearInizio = 0; // Timer for level clear logic
    private static final int DURATA_CLEAR_MS = 2000; // Duration to show "Level Clear" text (2 seconds)

    // Graphics Resources
    private BufferedImage backgroundImage; // Cached background image to optimize rendering
    private Font arcadeFont; // Custom pixel font

    /**
     * Constructor: Initializes the panel, screen size, and game systems.
     */
    public GamePanel() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.screenWidth = screenSize.width;
        this.screenHeight = screenSize.height;

        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true); // Enable double buffering to prevent flickering
        this.setFocusable(true); // Allow panel to receive key events
        
        this.keyH = new KeyHandler();
        this.addKeyListener(keyH);

        initGame();
    }

    /**
     * Initializes all game objects and managers.
     * Called once at startup and also used to reset the game state.
     */
    private void initGame() {
        // Initialize core logic
        this.livelloN = new LivelloN(1);
        this.scoreManager = new ScoreManager(); 
        
        // Initialize map and physics
        this.tileM = new TileManager(this, livelloN);
        this.collisionManager = new CollisionManager(tileM);
        
        // Initialize entities (Player, Enemy, Obstacle)
        this.player = new Player(this, keyH, collisionManager);
        this.scoreManager.setPlayer(player); 
        this.ralph = new Ralph(player, livelloN);
        this.duck = new Duck(this, player, livelloN);
        
        // Initialize high-level managers
        this.cutsceneManager = new CutsceneManager(this);
        this.transitionManager = new LevelTransitionManager(player, livelloN);
        this.debugDrawer = new DebugDrawer(player, tileM.getCollisionTiles(), tileM.getCollisioneFinestre());
        
        // Load font via ResourceManager for performance
        this.arcadeFont = ResourceManager.get().getFont().deriveFont(Font.BOLD, 18f);
        
        // Pre-render static background elements
        generateBackgroundImage();
        
        // Level 1 is ready immediately
        this.levelLoaded = true;
        
        System.out.println("🎮 GamePanel initialized.");
    }

    /**
     * Renders static tile elements onto a buffered image to avoid redrawing them every frame.
     */
    public void generateBackgroundImage() {
        if (screenWidth == 0 || screenHeight == 0) return;

        backgroundImage = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = backgroundImage.createGraphics();
        tileM.draw(g2); 
        g2.dispose();
    }

    /**
     * Starts the main game loop thread.
     */
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    /**
     * The main game loop. Updates logic and repaints the screen at a fixed FPS (30).
     */
    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    /**
     * Main logic update method. 
     * Controls game states: Game Over, Death Animation, Cutscenes, and Gameplay.
     */
    public void update() {
        // 1. Game Over Logic
        if (isGameOver) {
            if (gameOverScreen != null) {
                gameOverScreen.update();
                handleGameOverInput();
            }
            return;
        }

        // 2. Final Death Animation (Player loses last life)
        if (isPlayingDeathAnimation) {
            player.update();
            duck.setActive(false);

            if (player.isFinalDeathAnimationComplete()) {
                isPlayingDeathAnimation = false;
                isGameOver = true;
                gameOverScreen = new GameOverScreen(this, scoreManager.getScore());
                ralph.setActive(false);
            }
            return;
        }

        // 3. Check if Player Died
        if (player.finalDeath && !isPlayingDeathAnimation && !isGameOver) {
            isPlayingDeathAnimation = true;
            player.setActive(true);
            ralph.setActive(false);
            duck.setActive(false);
            return;
        }

        // 4. Handle Main Cutscenes (Intro, Victory, You Fixed It)
        if (cutsceneManager.isAnyCutsceneActive()) {
            cutsceneManager.update();
            player.setActive(false);
            ralph.setActive(false);
            duck.setActive(false);
            return;
        } 
        
        // 5. Handle Gameplay Transitions (Destruction / Scrolling)
        else if (tileM.isCutsceneActive() || tileM.livelloInTransizione) {
            player.setActive(false);
            
            // Clear bricks instantly to prevent unfair hits during transitions
            if (ralph != null) ralph.cleanup(); 
            ralph.setActive(false);
            
            duck.setActive(false);
        }
        else {
            // Normal Gameplay: Active
            if (!player.isEating()) player.setActive(true);
            if (player.isEating()){
                 player.update();
                 tileM.pie.update();
                 duck.setActive(false);
                 return;
            }
            ralph.setActive(true);
            if (!isPlayingDeathAnimation) duck.setActive(true);
        }

        // 6. Music Management
        if (!levelThemePlaying && !livelloClear) { 
            SoundEffects.playSoundLoop("level_theme"); 
            levelThemePlaying = true; 
        }

        // 7. Core Updates (Map, Player, Enemies)
        tileM.update();
        
        // Only update entities if NOT in a transition state
        if (!tileM.isCutsceneActive() && !tileM.livelloInTransizione) {
            player.update();
            ralph.update();
            duck.update();
        }

        checkLevelState();
    }
    
    /**
     * Checks if the level is complete, rebuilding, or transitioning.
     */
    private void checkLevelState() {
        // 1. Handle Rebuild (If scroll just finished)
        // This generates the new windows for the next level
        if (tileM.attivaRicostruzione) {
            tileM.setAnimazioneFinestre(tileM.livelloCompletatoManager.rebuildStage());
            tileM.attivaRicostruzione = false;
            tileM.resetTimer();
            generateBackgroundImage();
            this.levelLoaded = true; // Mark level as fully loaded
            livelloClear = false; // Hide "Level Clear" text
            return; 
        }
        
        // 2. DETECT WIN (Place this BEFORE the transition checks!)
        // This ensures "livelloClear" becomes true so the text draws, 
        // even if the cutscene starts immediately after.
        int remaining = getRemainingWindows();
        if (!livelloClear && remaining == 0 && tileM.getAnimazioneFinestre() != null && tileM.getAnimazioneFinestre().length > 0) {
            startLevelClearSequence();
        }

        // 3. Handle Level Advance (After Destruction Animation finishes)
        if (tileM.isDestructionFinished()) {
            tileM.resetTimer(); 
            advanceToNextLevel();
            return;
        }

        // 4. Guard Clause (Prevents Infinite Loop)
        // Now that we've checked the win condition above, we can safely return 
        // if we are busy destroying or scrolling.
        if (tileM.livelloInTransizione || tileM.isCutsceneActive()) {
            return;
        }
    }
    
    private int getRemainingWindows() {
        if (tileM != null) { 
            return tileM.getCount();
        }
        return 20; // Fallback safe value
    }
    
    /**
     * Triggers the "Level Clear" sequence (Text + Sound + Score).
     */
    private void startLevelClearSequence() {
        livelloClear = true;
        // Note: TileManager starts the visual destruction immediately.
        // We play the sound and show the text here.
        scoreManager.addPoints(livelloN.getNumeroLivello() * 1000);

        SoundEffects.stopSound("level_theme");
        levelThemePlaying = false;
        SoundEffects.playSound("level_completed");
    }
    
    /**
     * Advances the game logic to the next level.
     */
    private void advanceToNextLevel() {
        livelloClear = false;
        // Win checks will be ignored until 'attivaRicostruzione' finishes (Step 1).
        levelLoaded = false; 
        
        SoundEffects.stopSound("level_completed");
        SoundEffects.playSound("next_level");

        int completedLevel = livelloN.getNumeroLivello();
        
        // Increment Level Number
        livelloN.prossimoLivello();
        
        // Start Visual Scroll
        transitionManager.prepareTransition();
        tileM.startScroll();
        
        // Update Entity Difficulty
        ralph.setLevel(livelloN.getNumeroLivello());
        duck.setLevel(livelloN.getNumeroLivello());

        // Logic: even numbers trigger the "You Fixed It" cutscene
        if (completedLevel % 2 == 0) {
            SoundEffects.playSoundLoop("level_theme");
            levelThemePlaying = true;
            cutsceneManager.startYouFixedItCutscene();
        } else {
            // Odd Levels (1->2, 3->4): Start gameplay immediately after scroll
            SoundEffects.playSoundLoop("level_theme");
            levelThemePlaying = true;
        }
    }

    /**
     * Handles input specifically for the Game Over screen.
     */
    private void handleGameOverInput() {
        if (gameOverScreen == null) return;
        
        if (keyH.upPressed) gameOverScreen.handleKeyPress(java.awt.event.KeyEvent.VK_UP);
        if (keyH.downPressed) gameOverScreen.handleKeyPress(java.awt.event.KeyEvent.VK_DOWN);
        if (keyH.leftPressed) gameOverScreen.handleKeyPress(java.awt.event.KeyEvent.VK_LEFT);
        if (keyH.rightPressed) gameOverScreen.handleKeyPress(java.awt.event.KeyEvent.VK_RIGHT);
        if (keyH.enterPressed) {
            gameOverScreen.handleKeyPress(java.awt.event.KeyEvent.VK_ENTER);
            
            if (gameOverScreen.shouldRestartGame()) {
                restartGame();
            } else if (gameOverScreen.shouldQuitGame()) {
                System.exit(0);
            }
        }
        
        // Reset keys to prevent rapid firing
        keyH.upPressed = false;
        keyH.downPressed = false;
        keyH.leftPressed = false;
        keyH.rightPressed = false;
        keyH.enterPressed = false;
    }

    /**
     * Resets the entire game state to start over.
     */
    private void restartGame() {
        System.out.println("🔄 Restarting Game...");
        SoundEffects.stopSound("level_theme");
        SoundEffects.stopSound("level_completed");

        isGameOver = false;
        isPlayingDeathAnimation = false;
        gameOverScreen = null;
        levelThemePlaying = false;
        livelloClear = false;

        scoreManager.reset();
        livelloN = new LivelloN(1);
        
        // Cleanup entities
        if (ralph != null) ralph.cleanup();
        if (cutsceneManager != null) cutsceneManager.cleanup();
        if (duck != null) duck.cleanup();
        if (tileM != null) tileM.resetTimer();

        // Re-initialize Managers
        this.tileM = new TileManager(this, livelloN);
        this.collisionManager = new CollisionManager(tileM);
        this.player = new Player(this, keyH, collisionManager);
        
        this.scoreManager.setPlayer(player);
        this.ralph = new Ralph(player, livelloN);
        this.duck = new Duck(this, player, livelloN);
        this.transitionManager = new LevelTransitionManager(player, livelloN);
        this.debugDrawer = new DebugDrawer(player, tileM.getCollisionTiles(), tileM.getCollisioneFinestre());

        generateBackgroundImage();
        
        //Unlock Level 1 so it's playable
        this.levelLoaded = true;
        System.out.println("✅ Game Restarted.");
    }

    /**
     * Standard Java Swing paint component.
     * Draws all game elements to the screen.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Fill Background Black
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Clip drawing to the specific game area
        Shape oldClip = g2.getClip();
        g2.setClip(GAME_AREA_X, GAME_AREA_Y, GAME_AREA_WIDTH, GAME_AREA_HEIGHT);

        if (isGameOver && gameOverScreen != null) {
            gameOverScreen.draw(g2);
        } 
        else if (cutsceneManager.isAnyCutsceneActive()) {
            cutsceneManager.draw(g2);
        } 
        else {
            // Draw Map & Destruction Cutscene
            tileM.draw(g2);
            // Only draw gameplay Ralph if TileManager is NOT running the destruction cutscene
            ralph.draw(g2);
            
            
            duck.draw(g2);
            player.draw(g2);
            
            //"Level Clear" text only if active and no cutscene blocks it
            if (livelloClear && levelLoaded && !cutsceneManager.isAnyCutsceneActive() && !tileM.livelloInTransizione) {
                drawLevelClear(g2);
            }
        }

        // Restore full screen drawing for UI
        g2.setClip(oldClip);
        
        // Draw Borders
        g2.setColor(new Color(40, 40, 40));
        g2.setStroke(new BasicStroke(4));
        g2.drawRect(GAME_AREA_X, GAME_AREA_Y, GAME_AREA_WIDTH, GAME_AREA_HEIGHT);

        // Draw UI (Score, Timer, Level) if playing
        if (!isGameOver && !cutsceneManager.isAnyCutsceneActive()) {
            scoreManager.draw(g2, 30, 50);
            tileM.drawTimer(g2);
            drawLevelIndicator(g2);
        }
    }

    /**
     * Draws the Level Indicator and Difficulty Text in the top-right corner.
     */
    private void drawLevelIndicator(Graphics2D g2) {
        if (arcadeFont != null) g2.setFont(arcadeFont);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        String levelText = "LEVEL " + livelloN.getNumeroLivello();
        int x = screenWidth - 200;
        int y = 50;

        g2.setColor(Color.BLACK);
        g2.drawString(levelText, x + 2, y + 2);
        g2.setColor(Color.CYAN);
        g2.drawString(levelText, x, y);
        
        String diff = getDifficultyText();
        if (arcadeFont != null) g2.setFont(arcadeFont.deriveFont(12f));
        int w = g2.getFontMetrics().stringWidth(diff);
        g2.setColor(getDifficultyColor());
        g2.drawString(diff, x + (150 - w) / 2, y + 25);
    }
    
    private String getDifficultyText() {
        int l = livelloN.getNumeroLivello();
        if (l <= 3) return "EASY";
        if (l <= 7) return "NORMAL";
        if (l <= 12) return "HARD";
        if (l <= 18) return "VERY HARD";
        return "EXTREME";
    }
    
    private Color getDifficultyColor() {
        int l = livelloN.getNumeroLivello();
        if (l <= 3) return Color.GREEN;
        if (l <= 7) return Color.YELLOW;
        if (l <= 12) return Color.ORANGE;
        if (l <= 18) return Color.RED;
        return Color.MAGENTA;
    }

    /**
     * Draws the "LEVEL X CLEAR" text in the center of the screen.
     */
    private void drawLevelClear(Graphics2D g2) {
        if (arcadeFont != null) g2.setFont(arcadeFont.deriveFont(36f));
        else g2.setFont(new Font("SansSerif", Font.BOLD, 36));
        
        String text = "LEVEL " + livelloN.getNumeroLivello() + " CLEAR";
        int x = 480;
        int y = 400;

        // Shadow effect
        g2.setColor(Color.BLACK);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx != 0 || dy != 0) g2.drawString(text, x + dx, y + dy);
            }
        }
        // Main text
        g2.setColor(Color.YELLOW);
        g2.drawString(text, x, y);
    }
}