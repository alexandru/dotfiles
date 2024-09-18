#!/usr/bin/env bash

FNAME="$1"
if [[ -z "$FNAME" ]]; then
  echo "ERROR: name expected as argument!" >&2
  exit 1
fi

find "$HOME/Library" -maxdepth 1 -iname "*$FNAME*" 2>/dev/null
find "$HOME/Library/Preferences" -maxdepth 1 -iname "*$FNAME*" 2>/dev/null
find "$HOME/Library/Application Support" -maxdepth 1 -iname "*$FNAME*" 2>/dev/null
find "$HOME/Library/Application Scripts" -maxdepth 1 -iname "*$FNAME*" 2>/dev/null
find "$HOME/Library/Caches" -maxdepth 1 -iname "*$FNAME*" 2>/dev/null
find "$HOME/Library/Internet Plug-Ins" -maxdepth 1 -iname "*$FNAME*" 2>/dev/null
find "$HOME/Library/Application Support/CrashReporter" -maxdepth 1 -iname "*$FNAME*" 2>/dev/null
find "$HOME/Library/Saved Application State" -maxdepth 1 -iname "*$FNAME*" 2>/dev/null
find "$HOME/Library/Logs" -maxdepth 1 -iname "*$FNAME*" 2>/dev/null
