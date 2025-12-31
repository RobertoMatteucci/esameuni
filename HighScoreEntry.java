package com.game;

import java.io.Serializable;

/**
 * Represents a single high score entry (Initials + Score).
 * Implements Serializable to allow saving to disk.
 * Implements Comparable to allow automatic sorting.
 */
public class HighScoreEntry implements Serializable, Comparable<HighScoreEntry> {
    
    private static final long serialVersionUID = 1L;
    
    private final String initials;
    private final int score;
    
    /**
     * Creates a new high score entry.
     * @param initials Player's initials (truncated to 3 chars).
     * @param score The score achieved.
     */
    public HighScoreEntry(String initials, int score) {
        // Ensure initials are max 3 characters
        if (initials != null && initials.length() > 3) {
            this.initials = initials.substring(0, 3);
        } else {
            this.initials = initials;
        }
        this.score = score;
    }
    
    public String getInitials() {
        return initials;
    }
    
    public int getScore() {
        return score;
    }
    
    /**
     * Compare by score in descending order (Highest first).
     */
    @Override
    public int compareTo(HighScoreEntry other) {
        return Integer.compare(other.score, this.score);
    }
    
    @Override
    public String toString() {
        return String.format("%3s  %06d", initials, score);
    }
}