#!/bin/bash

# Emergency debugging script for Myra AI Assistant

echo "╔════════════════════════════════════════════════════════════╗"
echo "║      Myra AI Assistant - Emergency Debug Toolkit          ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

show_menu() {
    echo -e "${BLUE}Debug Options:${NC}"
    echo "1. View device logs (Myra)"
    echo "2. View all logs"
    echo "3. Check installed apps"
    echo "4. Check app permissions"
    echo "5. Uninstall app"
    echo "6. Clear app cache"
    echo "7. Check connected devices"
    echo "8. Run app with verbose logging"
    echo "9. Check service status"
    echo "10. Export logs to file"
    echo "11. Back to menu"
    echo "0. Exit"
    echo ""
}

check_devices() {
    echo -e "${YELLOW}Connected devices:${NC}"
    adb devices -l
    echo ""
}

view_myra_logs() {
    echo -e "${YELLOW}Myra AI logs (Ctrl+C to stop):${NC}"
    adb logcat | grep "Myra\|com.myra"
}

view_all_logs() {
    echo -e "${YELLOW}All device logs (Ctrl+C to stop):${NC}"
    adb logcat
}

check_apps() {
    echo -e "${YELLOW}Installed packages:${NC}"
    adb shell pm list packages | grep myra
    echo ""
}

check_permissions() {
    echo -e "${YELLOW}App permissions:${NC}"
    adb shell dumpsys package com.myra.ai.assistant | grep -A 50 "permissions:"
    echo ""
}

uninstall_app() {
    echo -e "${YELLOW}Uninstalling Myra...${NC}"
    adb uninstall com.myra.ai.assistant
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ App uninstalled${NC}"
    else
        echo -e "${RED}✗ Failed to uninstall${NC}"
    fi
    echo ""
}

clear_cache() {
    echo -e "${YELLOW}Clearing app cache...${NC}"
    adb shell pm clear com.myra.ai.assistant
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Cache cleared${NC}"
    else
        echo -e "${RED}✗ Failed to clear cache${NC}"
    fi
    echo ""
}

check_services() {
    echo -e "${YELLOW}Running services:${NC}"
    adb shell dumpsys activity services | grep -i myra
    echo ""
}

run_verbose() {
    echo -e "${YELLOW}Running app with verbose logging...${NC}"
    adb shell setprop log.tag.Myra DEBUG
    adb shell am start -n com.myra.ai.assistant/.ui.MainActivity
    echo -e "${YELLOW}View logs with: adb logcat | grep Myra${NC}"
    echo ""
}

export_logs() {
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    LOGFILE="myra_logs_$TIMESTAMP.txt"
    echo -e "${YELLOW}Exporting logs to $LOGFILE...${NC}"
    adb logcat -d > "$LOGFILE"
    echo -e "${GREEN}✓ Logs exported to $LOGFILE${NC}"
    echo ""
}

# Main loop
while true; do
    show_menu
    read -p "Select option: " choice
    
    case $choice in
        1) view_myra_logs ;;
        2) view_all_logs ;;
        3) check_apps ;;
        4) check_permissions ;;
        5) uninstall_app ;;
        6) clear_cache ;;
        7) check_devices ;;
        8) run_verbose ;;
        9) check_services ;;
        10) export_logs ;;
        11) clear; show_menu ;;
        0) 
            echo -e "${GREEN}Goodbye!${NC}"
            exit 0
            ;;
        *)
            echo -e "${RED}Invalid option${NC}"
            ;;
    esac
    
    read -p "Press Enter to continue..."
done
