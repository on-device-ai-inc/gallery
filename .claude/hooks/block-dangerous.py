#!/usr/bin/env python3
"""
Stub hook for blocking dangerous commands.
Currently allows all commands through.
"""
import sys
import os

# Read the command from environment or stdin
command = os.environ.get('CLAUDE_COMMAND', '')

# For now, allow everything through (success exit code)
sys.exit(0)
