#!/usr/bin/env bash

# set -e

SOURCE_DIR="$1"
DEST_DIR="$2"

print_usage()
{
    echo "" >&2
    echo "Usage:" >&2
    echo "" >&2
    echo "import-camera.sh <source-dir> <destination-dir>" >&2
    echo "" >&2
}

if [ -z "$SOURCE_DIR" ]; then
    echo "" >&2
    echo "ERROR — please specify source directory!" >&2
    print_usage
    exit 1
elif [ ! -d "$SOURCE_DIR" ]; then
    echo "" >&2
    echo "ERROR — not a directory: $SOURCE_DIR" >&2
    print_usage
    exit 1
elif [ -z "$DEST_DIR" ]; then
    echo "" >&2
    echo "ERROR — please specify destination directory!" >&2
    print_usage
    exit 1
elif [ ! -d "$DEST_DIR" ]; then
    echo "" >&2
    echo "ERROR — not a directory: $DEST_DIR" >&2
    print_usage
    exit 1
fi

exiftool -if '$duration#>10' "-FileName<CreateDate" -d "$DEST_DIR/Movies/%Y%m%d-%%f.%%e" -r "$SOURCE_DIR"

exiftool -if '$duration#>10' "-FileName<FileModifyDate" -d "$DEST_DIR/Movies/%Y%m%d-%%f.%%e" -r "$SOURCE_DIR"

exiftool "-FileName<CreateDate" -d "$DEST_DIR/Pictures/%Y/%Y%m%d-%%f.%%e" -r "$SOURCE_DIR"

exiftool "-FileName<FileModifyDate" -d "$DEST_DIR/Pictures/%Y/%Y%m%d-%%f.%%e" -r "$SOURCE_DIR"
