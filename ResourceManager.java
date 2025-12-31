package com.game;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class ResourceManager {

    private static ResourceManager instance;
    private final Map<String, BufferedImage> imageCache;
    private Font arcadeFont; // Cached Font

    private ResourceManager() {
        imageCache = new HashMap<>();
    }

    public static ResourceManager get() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    public BufferedImage getImage(String path) {
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        InputStream stream = null;
        try {
            stream = getClass().getResourceAsStream(path);
            if (stream == null) stream = getClass().getResourceAsStream("/images" + path);
            
            // Fallback to file system
            if (stream == null) {
                File f = new File("res/images" + path);
                if (f.exists()) stream = new FileInputStream(f);
                else {
                    File f2 = new File("res" + path);
                    if (f2.exists()) stream = new FileInputStream(f2);
                }
            }

            if (stream == null) {
                System.err.println("❌ Resource not found: " + path);
                imageCache.put(path, null);
                return null;
            }

            BufferedImage image = ImageIO.read(stream);
            imageCache.put(path, image);
            return image;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Loads the Arcade Font safely.
     */
    public Font getFont() {
        if (arcadeFont != null) return arcadeFont;
        
        try {
            InputStream is = getClass().getResourceAsStream("res/fonts/PressStart2P.ttf");
            if (is == null) is = getClass().getResourceAsStream("/fonts/PressStart2P.ttf");
            
            if (is == null) {
                File f = new File("res/fonts/PressStart2P.ttf");
                if (f.exists()) is = new FileInputStream(f);
            }

            if (is != null) {
                arcadeFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.BOLD, 18f);
            } else {
                System.err.println("⚠️ Font not found, using default.");
                arcadeFont = new Font("Monospaced", Font.BOLD, 18);
            }
        } catch (Exception e) {
            arcadeFont = new Font("Monospaced", Font.BOLD, 18);
        }
        return arcadeFont;
    }

    public void clearCache() {
        imageCache.clear();
    }
}