#!/usr/bin/env bash

FNAME="$1"
if [[ -z "$FNAME" ]]; then
    echo "ERROR: name expected as argument!" >&2
    exit 1
fi

find "$HOME/Library/Preferences" -iname "*$FNAME*" 2>/dev/null
find "$HOME/Library/Application Support" -iname "*$FNAME*" 2>/dev/null
find "$HOME/Library/Caches" -iname "*$FNAME*" 2>/dev/null
find "$HOME/Library/Internet Plug-Ins" -iname "*$FNAME*" 2>/dev/null
find "$HOME/Library/Application Support/CrashReporter" -iname "*$FNAME*" 2>/dev/null
find "$HOME/Library/Saved Application State" -iname "*$FNAME*" 2>/dev/null
find "$HOME/Library/Logs" -iname "*$FNAME*" 2>/dev/null
