package com.game;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;

/**
 * Manages loading, playing, and looping of sound effects.
 * Refactored to support 'res/' folder structure and package system.
 */
public class SoundEffects {

    // Map to store loaded sound clips
    private static final Map<String, Clip> soundEffects = new HashMap<>();
    private static String basePath = "res/";

    /**
     * Initializes the sound system with a base path (e.g., "res/").
     */
    public static void init(String path) {
        basePath = path;
        System.out.println("🔊 SoundEffects initialized with base path: " + basePath);
    }

    /**
     * Loads a sound file into memory.
     * It attempts to load from the classpath first (ideal for src/res structure),
     * then falls back to the file system.
     * * @param key The name used to reference the sound later.
     * @param filePath The relative path to the sound file (e.g., "snd/jump.wav").
     */
    public static void loadSound(String key, String filePath) {
        if (soundEffects.containsKey(key)) {
            return; // Sound already loaded
        }

        try {
            AudioInputStream audioStream = null;
            String fullPath = basePath + filePath;

            // 1. Try loading from Classpath (Standard for 'res' folders inside src)
            // We append "/" to ensure absolute path from root of classpath
            URL url = SoundEffects.class.getResource("/" + filePath);
            
            if (url == null) {
                // Try with the base path prepended
                url = SoundEffects.class.getResource("/" + fullPath);
            }

            if (url != null) {
                audioStream = AudioSystem.getAudioInputStream(url);
            } else {
                // 2. Fallback: Try loading from File System (External 'res' folder)
                File audioFile = new File(fullPath);
                if (!audioFile.exists()) {
                    // Try without base path
                    audioFile = new File(filePath);
                }
                
                if (audioFile.exists()) {
                    audioStream = AudioSystem.getAudioInputStream(audioFile);
                }
            }

            if (audioStream == null) {
                System.err.println("❌ SoundEffects: Could not find file: " + filePath);
                return;
            }

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            soundEffects.put(key, clip);
            // System.out.println("✅ Sound loaded: " + key); // Uncomment for debug

        } catch (Exception e) {
            System.err.println("❌ SoundEffects: Error loading sound '" + key + "': " + e.getMessage());
            // e.printStackTrace(); // Uncomment for deep debugging
        }
    }

    /**
     * Plays a sound effect once.
     * If the sound is already playing, it stops and restarts it.
     * @param key The key of the sound to play.
     */
    public static void playSound(String key) {
        Clip clip = soundEffects.get(key);
        if (clip == null) return;

        if (clip.isRunning()) {
            clip.stop(); 
        }
        
        clip.setFramePosition(0); // Rewind to start
        clip.start();
    }

    /**
     * Plays a sound effect in a continuous loop.
     * REQUIRED for: Level Theme, Background Music.
     * @param key The key of the sound to loop.
     */
    public static void playSoundLoop(String key) {
        Clip clip = soundEffects.get(key);
        if (clip == null) {
            System.err.println("⚠️ SoundEffects: Sound not found for loop: " + key);
            return;
        }

        if (clip.isRunning()) {
            clip.stop();
        }
        
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /**
     * Stops a specific sound if it is playing.
     * @param key The key of the sound to stop.
     */
    public static void stopSound(String key) {
        Clip clip = soundEffects.get(key);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    /**
     * Stops and closes all loaded sounds.
     * Should be called when closing the application to release resources.
     */
    public static void closeAll() {
        for (Clip clip : soundEffects.values()) {
            if (clip != null) {
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.close();
            }
        }
        soundEffects.clear();
        System.out.println("🔇 All sounds closed.");
    }
}