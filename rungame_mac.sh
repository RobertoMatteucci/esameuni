#!/bin/bash

echo "=========================================="
echo "     FIX-IT FELIX JR. - MAC LAUNCHER"
echo "=========================================="

# 1. Create BIN directory if it doesn't exist
if [ ! -d "bin" ]; then
  echo "[+] Creating bin directory..."
  mkdir -p bin
fi

# 2. Compile the Java files
echo "[+] Compiling Java source files..."
# Compiles all files in com/game package
javac -encoding UTF-8 -d bin -sourcepath src src/com/game/*.java

# Check for compilation errors ($? holds the exit code of the last command)
if [ $? -ne 0 ]; then
    echo ""
    echo "[!] COMPILATION FAILED!"
    echo "    Please check the errors above."
    exit 1
fi

echo "[+] Compilation Successful!"
echo ""

# 3. Run the Game
echo "[+] Launching Game..."
echo "=========================================="
# -cp bin sets the classpath to the bin folder
# com.game.Main tells Java to look for the Main class inside the com.game package
java -cp bin com.game.Main