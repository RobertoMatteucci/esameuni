# Fix-It Felix Jr. - Refactored Java Clone

## 🛠️ Refactoring Overview
This project has undergone a massive refactoring to improve performance, stability, and code maintainability. The code has been moved from a flat file structure to a proper package (`com.game`), asset loading has been optimized to prevent lag, and critical logic bugs regarding level transitions and animations have been resolved.

## 📂 Key Architectural Changes
1.  **Package Management:** All files moved to `package com.game;`.
2.  **Resource Caching:** Introduced `ResourceManager` to load images/fonts *once* and store them in memory, rather than reading from the hard drive 60 times a second.
3.  **Centralized Audio:** `SoundEffects` class now robustly handles loading sounds from both classpath and file system.
### 4\. File System Loading Strategy

  * **Change:** The project is configured to load resources from a **`res/` folder located outside** the `src/` folder.
  * **Why:** As requested, this allows for easy editing of assets (images/sounds) without needing to recompile the code or mess with Classpath settings inside a JAR.

### 5\. Code Cleanup & Documentation

  * **Comments:** Extensive **Italian comments** (`//`) have been added to all files to explain the logic, methods, and variables without altering the original variable names.
  * **Formatting:** Removed redundant `try-catch` blocks from individual entity classes (like `Brick` or `Player`), delegating resource safety to the Managers.

-----

## 📂 New Project Structure

To run the game correctly, your folder structure **must** look exactly like this:

```text
ProjectRoot/
├── src/
│   └── com/
│       └── game/
│           ├── Main.java
│           ├── GamePanel.java
│           ├── ResourceManager.java  <-- New!
│           ├── Player.java
│           └── ... (All 35 .java files)
│
└── res/                              <-- External Resource Folder
    ├── images/
    │   ├── map/      (contains Palazzo0.png, brick.png, etc.)
    │   ├── felix/    (contains Left1.png, hammer.png, etc.)
    │   ├── ralph/    (contains RalphBack1.png, etc.)
    │   ├── abitanti/ (contains ABitanteA.png, etc.)
    │   ├── torta/    (contains Pie1.png, etc.)
    │   ├── Duck/     (contains DuckR1.png, etc.)
    │   └── intro/    (contains Menu.png, etc.)
    ├── snd/          (contains bgm.wav, jump.wav, etc.)
    └── fonts/        (contains PressStart2P.ttf)
```

-----


## 📝 File-by-File Refactoring Log

### 🏗️ Core & Entry Point
| File | Changes Made | Reason |
| :--- | :--- | :--- |
| **Main.java** | Added thread-safe UI launching (`SwingUtilities`), initialized `SoundEffects` with `res/` path. | Prevents UI freezing and ensures resources load correctly. |
| **GamePanel.java** | **Major Logic Fixes.** Added `levelLoaded` flags to prevent auto-wins. Synced `paintComponent` handled bugs time still working in cutscene causing low score Optimized render loop. |solved demage after completing the level |

### ⚙️ Managers & Utilities
| File | Changes Made | Reason |
| :--- | :--- | :--- |
| **ResourceManager.java** | **NEW FILE.** Singleton class that caches `BufferedImage` and `Font` objects. | drastically improves FPS and eliminates stutter during gameplay/cutscenes. |
| **SoundEffects.java** | Added `init()`, `closeAll()`, and smart path detection. | Prevents memory leaks and allows sounds to play from `res` folder or JAR. |
| **TileManager.java** | Added state flags (`destructionFinished`) to sync with GamePanel. Added logic to wipe debris on reset. | Prevents "Shattered Glass" carrying over to new levels. |
| **CollisionManager.java** | Standardized collision checks and loops. | OOP Best practices and readability. |
| **ScoreManager.java** | Uses cached images/fonts. Added logic for "Extra Life" notifications. | Performance optimization for UI rendering. |
| **LevelTransitionManager.java** | Centralized logic for Felix's spawn position based on level parity. | Clean code practice; separates logic from rendering. |
| **HighScoreManager.java** | Added error handling for file I/O. | Prevents crashes if save file is corrupted. |
| **KeyHandler.java** | Cleaned up unused boolean flags. | Code cleanup. |
| **CutsceneManager.java** | Central controller for all cutscenes. Uses `ResourceManager` for backgrounds. | clean separation of concerns. |
| **CraneManager.java** | Optimized image loading. | Performance. |

### 👾 Entities (Game Objects)
| File | Changes Made | Reason |
| :--- | :--- | :--- |
| **Entity.java** | Made `abstract`. Removed specific sprite fields. | True OOP inheritance. Reduces memory footprint of base class. |
| **Player.java** | Extends `Entity`. Pauses input during transitions. Uses cached sprites. | Prevents Felix from dying during level scrolls. |
| **Ralph.java** | Extends `Entity`. Added `cleanup()` method to clear bricks instantly. | Stops bricks from killing player during cutscenes. |
| **Duck.java** | Uses `ThreadLocalRandom` for performance. Cached sprites. | Optimized game loop performance. |
| **Brick.java** | Removed static initializer blocks. Uses `ResourceManager`. | Prevents class loading errors. |
| **FallingObject.java** | Optimized random number generation for "shaking" effect. | Performance. |
| **Cloud.java** | Replaced Swing `Timer` with frame-counter update loop. | Ensures animations pause correctly when game pauses. |
| **Pie.java** | Optimized sprite loading and collision logic. | Performance and clean code. |

### 🎬 Cutscenes
| File | Changes Made | Reason |
| :--- | :--- | :--- |
| **RalphCutscene.java** | Uses cached images. | Performance. |
| **RalphCutscene2.java** | Uses cached images. | Performance. |
| **FelixIntroCutscene.java** | Uses cached images. | Performance. |
| **CitizensCutscene.java** | Uses cached images. | Performance. |
| **VictoryCutscene.java** | Uses cached images. Optimized draw loop. | Performance. |

### 🖥️ UI & Data
| File | Changes Made | Reason |
| :--- | :--- | :--- |
| **Menu.java** | Loaded assets via Manager. Thread-safe launch. | Stability. |
| **GameOverScreen.java** | Standardized font and image loading. | Consistency. |
| **TimeBar.java** | Optimized drawing logic. | UI Performance. |
| **BonusText.java** | Passing Font as parameter instead of creating new Font every frame. | Massive performance gain in text rendering. |
| **LivelloN.java** | Encapsulated level logic. | Clean Code. |
| **LivelloCompletato.java** | Logic to rebuild window arrays dynamically. | Enables infinite level generation. |
| **HighScoreEntry.java** | Added serialization UID. | Safe file saving. |
| **Tile.java** | Encapsulated fields. | OOP Best practices. |
| **Window.java** | Uses static shared image reference. | Saves memory (loads image once instead of 20 times). |
| **DebugDrawer.java** | Helper for hitboxes. | Development utility. |

---

## 🚀 How to Compile & Run
1. Ensure your folder structure is: `src/com/game/*.java` and `res/` folders are in the root.
2. Double click **`rungame.bat`** for windows.
3. make executable **`rungame_mac.sh` and run script


## Further you can manually use these two commands to run game 
1. `javac -encoding UTF-8 -d bin -sourcepath src src/com/game/*.java` for compiling
2. `java -cp bin com.game.Main` for running game 