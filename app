#!/bin/bash

PROJECT_ROOT="/home/ze/CSE606/Project0/app"

SRC_DIR="$PROJECT_ROOT/src/main/java"
BIN_DIR="$PROJECT_ROOT/bin"

cd $PROJECT_ROOT

if [ ! -d "$BIN_DIR" ]; then
    mkdir -p "$BIN_DIR"
fi

javac -d $BIN_DIR $(find $SRC_DIR -name "*.java")

if [ $? -eq 0 ]; then
    java -cp $BIN_DIR app.App "$@"
else
    echo "Compilation failed."
fi