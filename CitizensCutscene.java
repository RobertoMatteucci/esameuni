package com.game;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Manages the cutscene where citizens appear in windows to thank Felix.
 * Refactored to use ResourceManager and optimizing render loops.
 */
public class CitizensCutscene {
    
    private final GamePanel gp;
    
    // Citizen Sprites
    private BufferedImage citizen1, citizen2, citizen3, citizen4;
    private BufferedImage speechBubble;
    
    // Window Coordinates (Matched to TileManager layout)
    private static final int[][] WINDOW_POSITIONS = {
        {714, 341},  // Win 1 (Center-Top)
        {714, 467},  // Win 2 (Center-Bottom)
        {520, 609},  // Win 3 (Left-Bottom)
        {595, 609},  // Win 4 (Right-Bottom 1)
        {814, 609},  // Win 5 (Right-Bottom 2)
        {903, 609},  // Win 6 (Right-Bottom 3)
        {520, 341},  // Win 7 (Left-Top)
        {595, 341},  // Win 8 (Right-Top 1)
        {811, 341},  // Win 9 (Right-Top 2)
        {900, 341}   // Win 10 (Right-Top 3)
    };
    
    // Dimensions & Scaling
    private static final int SPRITE_SCALE = 2;
    private static final int BUBBLE_WIDTH = 400;
    private static final int BUBBLE_HEIGHT = 84;
    
    // Timing (Frames @ 30FPS)
    private static final int CITIZENS_APPEAR_FRAME = 30; // 1 second
    private static final int CUTSCENE_END_FRAME = 120;   // 4 seconds
    
    // State
    private int frameCounter = 0;
    private boolean finished = false;
    private boolean citizensVisible = false;
    private boolean speechVisible = false;
    
    // Window Selection
    private final int[] selectedWindows = {0, 2, 4, 1}; // Indices of windows to use

    public CitizensCutscene(GamePanel gp) {
        this.gp = gp;
        loadSprites();
    }
    
    private void loadSprites() {
        // Optimization: Use ResourceManager to cache images
        citizen1 = ResourceManager.get().getImage("/abitanti/AbitanteA.png");
        citizen2 = ResourceManager.get().getImage("/abitanti/AbitanteB.png");
        citizen3 = ResourceManager.get().getImage("/abitanti/Abitante_t_2.png");
        citizen4 = ResourceManager.get().getImage("/abitanti/Abitante_t1_2.png");
        
        // Load speech bubble
        speechBubble = ResourceManager.get().getImage("/abitanti/speech_bubble.png");
        
        // Pre-load sound
        SoundEffects.loadSound("voice1", "snd/voice1.wav");
    }
    
    /**
     * Resets and starts the cutscene.
     */
    public void start() {
        reset();
        System.out.println("🎬 CitizensCutscene started");
    }
    
    public void reset() {
        frameCounter = 0;
        finished = false;
        citizensVisible = false;
        speechVisible = false;
    }
    
    public void update() {
        if (finished) return;
        
        frameCounter++;
        
        // Trigger appearance
        if (frameCounter == CITIZENS_APPEAR_FRAME) {
            citizensVisible = true;
            speechVisible = true;
            SoundEffects.playSound("voice1");
        }
        
        // End cutscene
        if (frameCounter >= CUTSCENE_END_FRAME) {
            finished = true;
            System.out.println("✅ CitizensCutscene completed");
        }
    }
    
    public void draw(Graphics2D g2) {
        if (!citizensVisible) return;
        
        BufferedImage[] citizens = {citizen1, citizen2, citizen3, citizen4};
        
        // Draw citizens in their respective windows
        for (int i = 0; i < selectedWindows.length; i++) {
            int windowIndex = selectedWindows[i];
            
            // Bounds check
            if (windowIndex >= WINDOW_POSITIONS.length) continue;

            int x = WINDOW_POSITIONS[windowIndex][0];
            int y = WINDOW_POSITIONS[windowIndex][1];
            
            BufferedImage currentSprite = citizens[i % citizens.length];
            
            if (currentSprite != null) {
                // Apply offsets and scaling to match window inner size
                int width = currentSprite.getWidth() * SPRITE_SCALE;
                int height = currentSprite.getHeight() * SPRITE_SCALE;
                g2.drawImage(currentSprite, x + 6, y + 23, width, height, null);
            }
        }
        
        // Draw Speech Bubble
        if (speechVisible && speechBubble != null) {
            // Center the bubble between the top central windows (Index 0 and 1)
            int centerX = (WINDOW_POSITIONS[0][0] + WINDOW_POSITIONS[1][0]) / 2;
            int bubbleX = centerX - BUBBLE_WIDTH / 2 + 20;
            int bubbleY = WINDOW_POSITIONS[0][1] - BUBBLE_HEIGHT - 20;
            
            g2.drawImage(speechBubble, bubbleX, bubbleY, BUBBLE_WIDTH, BUBBLE_HEIGHT, null);
        }
    }
    
    public boolean isFinished() {
        return finished;
    }
}