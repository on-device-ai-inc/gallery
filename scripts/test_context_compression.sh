#!/bin/bash
# Test script to verify context management system works end-to-end
# Sends multiple long messages to trigger 84% compression threshold

set -e

DEVICE="R3CT10HETMM"
PACKAGE="ai.ondevice.app"

echo "=== Context Management System Test ==="
echo "Testing automatic compression at 84% threshold"
echo ""

# Clear logcat
echo "Clearing logcat..."
adb -s $DEVICE logcat -c

# Launch app
echo "Launching app..."
adb -s $DEVICE shell am start -n $PACKAGE/.MainActivity
sleep 3

# Function to send a message via UI
send_message() {
    local message="$1"
    local msg_num="$2"

    echo "[$msg_num] Sending message (~200 tokens): ${message:0:50}..."

    # Tap input field
    adb -s $DEVICE shell input tap 365 1300
    sleep 0.5

    # Type message
    adb -s $DEVICE shell input text "$message"
    sleep 0.5

    # Send (assuming send button coordinates - may need adjustment)
    adb -s $DEVICE shell input tap 650 1300

    # Wait for response
    sleep 15
}

# Generate long messages (each ~200 tokens = 800 chars)
# Need ~15 messages to hit 3000 tokens (84% of 3584)

echo ""
echo "Sending messages to reach 84% context usage..."
echo "Target: 3010 tokens / 3584 usable (84%)"
echo ""

# Message 1-5 (1000 tokens)
for i in {1..5}; do
    msg="Message $i: $(python3 -c "print('This is a test message with enough content to reach approximately two hundred tokens when counted using the four characters per token heuristic that our context management system uses for estimation purposes. ' * 3)")"
    send_message "${msg// /_}" $i
done

echo ""
echo "After 5 messages: ~1000 tokens (28%)"
echo "Checking logcat..."
adb -s $DEVICE logcat -d | grep -i "Context usage" | tail -1

# Message 6-10 (2000 tokens total)
for i in {6..10}; do
    msg="Message $i: $(python3 -c "print('This is a test message with enough content to reach approximately two hundred tokens when counted using the four characters per token heuristic that our context management system uses for estimation purposes. ' * 3)")"
    send_message "${msg// /_}" $i
done

echo ""
echo "After 10 messages: ~2000 tokens (56%)"
echo "Checking logcat..."
adb -s $DEVICE logcat -d | grep -i "Context usage" | tail -1

# Message 11-15 (3000 tokens total - should trigger compression)
for i in {11..15}; do
    msg="Message $i: $(python3 -c "print('This is a test message with enough content to reach approximately two hundred tokens when counted using the four characters per token heuristic that our context management system uses for estimation purposes. ' * 3)")"
    send_message "${msg// /_}" $i

    # Check for compression trigger
    if adb -s $DEVICE logcat -d | grep -q "Context usage at 84%"; then
        echo ""
        echo "✅ COMPRESSION TRIGGERED!"
        echo ""
        break
    fi
done

echo ""
echo "=== Final Results ==="
echo ""

# Show compression logs
echo "Compression events:"
adb -s $DEVICE logcat -d | grep -i "compression" | grep -v "InputMethod"

echo ""
echo "Context usage progression:"
adb -s $DEVICE logcat -d | grep "Context usage:"

echo ""
echo "Quality monitoring:"
adb -s $DEVICE logcat -d | grep -i "quality"

echo ""
echo "=== Test Complete ==="
