package com.game;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the persistence of high scores.
 * Handles loading, saving, and migration from legacy file formats.
 */
public class HighScoreManager {
    
    private static final String FILE_PATH = "highscores.dat";
    private static final String LEGACY_FILE_PATH = "highscore.dat";
    private static final int MAX_SCORES = 6;
    
    // Private constructor to prevent instantiation of utility class
    private HighScoreManager() {}
    
    /**
     * Loads all high scores from the file.
     * Attempts to migrate from legacy format if the new format doesn't exist.
     * @return List of high score entries, sorted by score (highest first).
     */
    @SuppressWarnings("unchecked")
    public static List<HighScoreEntry> loadHighScores() {
        List<HighScoreEntry> scores = new ArrayList<>();
        
        File file = new File(FILE_PATH);
        
        if (!file.exists()) {
            // Try to migrate from legacy file if main file missing
            int legacyScore = loadLegacyHighScore();
            if (legacyScore > 0) {
                scores.add(new HighScoreEntry("???", legacyScore));
                initializeDefaultScores(scores);
                saveHighScores(scores);
            } else {
                initializeDefaultScores(scores);
            }
            return scores;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                scores = (List<HighScoreEntry>) obj;
            } else {
                initializeDefaultScores(scores);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("⚠️ Error loading high scores, resetting to defaults: " + e.getMessage());
            initializeDefaultScores(scores);
        }
        
        return scores;
    }
    
    /**
     * Saves the high scores to the file.
     * @param scores List of high score entries to save.
     */
    public static void saveHighScores(List<HighScoreEntry> scores) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(scores);
        } catch (IOException e) {
            System.err.println("❌ Error saving high scores: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Adds a new score to the high score list if it qualifies.
     * @param initials Player's initials (max 3 characters).
     * @param score The score achieved.
     * @return The position in the high score list (1-6), or -1 if it didn't qualify.
     */
    public static int addHighScore(String initials, int score) {
        List<HighScoreEntry> scores = loadHighScores();
        HighScoreEntry newEntry = new HighScoreEntry(initials, score);
        
        scores.add(newEntry);
        Collections.sort(scores);
        
        // Find the position of the new entry
        int position = -1;
        for (int i = 0; i < Math.min(scores.size(), MAX_SCORES); i++) {
            if (scores.get(i) == newEntry) {
                position = i + 1;
                break;
            }
        }
        
        // Keep only top MAX_SCORES
        if (scores.size() > MAX_SCORES) {
            scores = new ArrayList<>(scores.subList(0, MAX_SCORES));
        }
        
        saveHighScores(scores);
        return position;
    }
    
    /**
     * Checks if a score qualifies for the high score list.
     * @param score The score to check.
     * @return true if the score qualifies for the top 6.
     */
    public static boolean isHighScore(int score) {
        if (score <= 0) return false;
        
        List<HighScoreEntry> scores = loadHighScores();
        
        if (scores.size() < MAX_SCORES) {
            return true;
        }
        
        // Check if score is higher than the lowest high score
        return score > scores.get(scores.size() - 1).getScore();
    }
    
    /**
     * Initializes the high score list with default entries.
     */
    private static void initializeDefaultScores(List<HighScoreEntry> scores) {
        if (scores.isEmpty()) {
            scores.add(new HighScoreEntry("AAA", 50000));
            scores.add(new HighScoreEntry("BBB", 40000));
            scores.add(new HighScoreEntry("CCC", 30000));
            scores.add(new HighScoreEntry("DDD", 20000));
            scores.add(new HighScoreEntry("EEE", 10000));
            scores.add(new HighScoreEntry("FFF", 5000));
        }
    }
    
    /**
     * Legacy method for backward compatibility.
     * Returns the highest score from the list.
     */
    public static int loadHighScore() {
        List<HighScoreEntry> scores = loadHighScores();
        return scores.isEmpty() ? 0 : scores.get(0).getScore();
    }
    
    /**
     * Legacy method for backward compatibility.
     * Saves a score without initials (uses "CPU" as placeholder).
     */
    public static void saveHighScore(int score) {
        if (isHighScore(score)) {
            addHighScore("CPU", score);
        }
    }
    
    /**
     * Attempts to load the legacy single high score from old file format.
     */
    private static int loadLegacyHighScore() {
        File legacyFile = new File(LEGACY_FILE_PATH);
        if (!legacyFile.exists()) return 0;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(legacyFile))) {
            return dis.readInt();
        } catch (IOException e) {
            return 0;
        }
    }
    
    /**
     * Gets the rank position a score would achieve.
     * @param score The score to check.
     * @return The rank (1-6) or -1 if not qualified.
     */
    public static int getScoreRank(int score) {
        List<HighScoreEntry> scores = loadHighScores();
        
        int rank = 1;
        for (HighScoreEntry entry : scores) {
            if (score > entry.getScore()) {
                return rank;
            }
            rank++;
            if (rank > MAX_SCORES) {
                return -1;
            }
        }
        
        return rank <= MAX_SCORES ? rank : -1;
    }
    
    public static void resetHighScores() {
        List<HighScoreEntry> scores = new ArrayList<>();
        initializeDefaultScores(scores);
        saveHighScores(scores);
    }
}