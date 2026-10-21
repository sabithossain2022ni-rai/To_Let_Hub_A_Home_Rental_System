#!/bin/bash
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java") || { echo "Compilation failed"; exit 1; }
java -cp out com.tolet.Main
