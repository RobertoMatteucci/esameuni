package com.game;

/**
 * Manages the positioning of Felix at the start of levels and during transitions.
 * Ensures Felix appears on the correct floor based on level parity.
 */
public class LevelTransitionManager {
    
    // Y Positions for each floor (Ground -> Top)
    private static final int[] FELIX_Y_POSITIONS = {
        764,  // Floor 0 (Ground)
        661,  // Floor 1
        519,  // Floor 2
        394   // Floor 3 (Top)
    };
    
    // Default X start position (Center-Right of the first window column)
    private static final int FELIX_START_X = 560;
    
    private final Player player;
    private final LivelloN livelloN;
    
    public LevelTransitionManager(Player player, LivelloN livelloN) {
        this.player = player;
        this.livelloN = livelloN;
    }
    
    /**
     * Places Felix at the correct starting position for the current level.
     */
    public void positionFelixForCurrentLevel() {
        int numeroLivello = livelloN.getNumeroLivello();
        
        // Determine starting floor
        // Odd levels: Start at bottom (0)
        // Even levels: After scrolling up, logically start at bottom of new building section
        int piano = getPianoForLevel(numeroLivello);
        
        // Set Position
        player.x = FELIX_START_X;
        player.y = FELIX_Y_POSITIONS[piano];
        
        // Reset Physics/State
        player.setDefaultValues();
        
        // System.out.println("📍 Felix positioned at Floor " + piano); // Debug
    }
    
    /**
     * Determines the floor index based on level logic.
     */
    private int getPianoForLevel(int livello) {
        // Logic: currently always resets to ground floor (0) for gameplay continuity
        // logic implies standard arcade progression (bottom-up)
        return 0; 
    }
    
    /**
     * Prepares internal state for a level transition.
     * Currently a placeholder for more complex transition logic (animations).
     */
    public void prepareTransition() {
        // Even levels involve a scroll, handled by TileManager.
        // This method signals readiness.
    }
    
    /**
     * Called after visual transitions (like scrolling) are complete.
     */
    public void completeTransition() {
        positionFelixForCurrentLevel();
    }
    
    /**
     * Checks if Felix is aligned with the expected Y position.
     * Useful for anti-cheat or debug validation.
     */
    public boolean isFelixInCorrectPosition() {
        int expectedY = FELIX_Y_POSITIONS[getPianoForLevel(livelloN.getNumeroLivello())];
        int tolerance = 5; // Pixel tolerance
        return Math.abs(player.y - expectedY) <= tolerance;
    }
    
    // Accessors for external debug tools
    public static int[] getAllYPositions() {
        return FELIX_Y_POSITIONS;
    }
    
    public static int getStartX() {
        return FELIX_START_X;
    }
}