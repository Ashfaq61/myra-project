#!/bin/bash

# Quick start script for Myra AI Assistant

echo "╔════════════════════════════════════════════════════════════╗"
echo "║        Myra AI Assistant - Quick Start Script             ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Check Java installation
if ! command -v java &> /dev/null; then
    echo -e "${RED}✗ Java not found. Please install Java 11+${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Java found: $(java -version 2>&1 | head -n 1)${NC}"

# Check if in correct directory
if [ ! -f "settings.gradle" ]; then
    echo -e "${RED}✗ Please run this script from the android/ directory${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}Select an option:${NC}"
echo "1. Build debug APK"
echo "2. Build release APK"
echo "3. Install to device"
echo "4. Run tests"
echo "5. Clean project"
echo "6. Full setup (build + install)"
echo "7. View logs"
echo "8. Exit"
echo ""

read -p "Enter choice (1-8): " choice

case $choice in
    1)
        echo -e "${YELLOW}Building debug APK...${NC}"
        ./gradlew assembleDebug
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ Debug APK built successfully!${NC}"
            echo "Location: app/build/outputs/apk/debug/app-debug.apk"
        fi
        ;;
    2)
        echo -e "${YELLOW}Building release APK...${NC}"
        ./gradlew assembleRelease
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ Release APK built successfully!${NC}"
            echo "Location: app/build/outputs/apk/release/app-release.apk"
        fi
        ;;
    3)
        echo -e "${YELLOW}Available devices:${NC}"
        adb devices
        echo ""
        read -p "Enter device ID (or press Enter for default): " device_id
        
        APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
        if [ ! -f "$APK_PATH" ]; then
            echo -e "${YELLOW}Debug APK not found. Building...${NC}"
            ./gradlew assembleDebug
        fi
        
        if [ -z "$device_id" ]; then
            adb install -r "$APK_PATH"
        else
            adb -s "$device_id" install -r "$APK_PATH"
        fi
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ Installation successful!${NC}"
            echo -e "${YELLOW}Launching app...${NC}"
            if [ -z "$device_id" ]; then
                adb shell am start -n com.myra.ai.assistant/.ui.MainActivity
            else
                adb -s "$device_id" shell am start -n com.myra.ai.assistant/.ui.MainActivity
            fi
        fi
        ;;
    4)
        echo -e "${YELLOW}Running tests...${NC}"
        ./gradlew test
        ./gradlew connectedAndroidTest
        ;;
    5)
        echo -e "${YELLOW}Cleaning project...${NC}"
        ./gradlew clean
        echo -e "${GREEN}✓ Project cleaned${NC}"
        ;;
    6)
        echo -e "${YELLOW}Full setup: Building debug APK...${NC}"
        ./gradlew assembleDebug
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ Build successful!${NC}"
            echo ""
            echo -e "${YELLOW}Available devices:${NC}"
            adb devices
            echo ""
            read -p "Enter device ID (or press Enter for default): " device_id
            
            APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
            
            if [ -z "$device_id" ]; then
                adb install -r "$APK_PATH"
            else
                adb -s "$device_id" install -r "$APK_PATH"
            fi
            
            if [ $? -eq 0 ]; then
                echo -e "${GREEN}✓ Installation successful!${NC}"
                echo -e "${YELLOW}Launching app...${NC}"
                if [ -z "$device_id" ]; then
                    adb shell am start -n com.myra.ai.assistant/.ui.MainActivity
                else
                    adb -s "$device_id" shell am start -n com.myra.ai.assistant/.ui.MainActivity
                fi
            fi
        fi
        ;;
    7)
        echo -e "${YELLOW}Reading device logs (press Ctrl+C to stop)...${NC}"
        echo ""
        adb logcat | grep "Myra"
        ;;
    8)
        echo -e "${GREEN}Goodbye!${NC}"
        exit 0
        ;;
    *)
        echo -e "${RED}Invalid choice${NC}"
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}Done!${NC}"
