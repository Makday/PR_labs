#!/usr/bin/env bash
set -euo pipefail

JAVA_FLAGS="-Xmx512m -Xmx512m"
CP="target/classes/"
RUNS="3"
THREADS="1 2 4 8 16 32 64"
MAX_PROCESSORS="8"
OUT="results.csv"

run() { java $JAVA_FLAGS -cp "$CP" org.example.lab1.Main "$@"; }

./mvnw -q clean compile

run generate && echo "generated"
run split && echo "divided"

for task in 1a 1c; do
  for t in $THREADS; do
    run run --task "$task" --threads "$t" --output /dev/null # warm-up, discarded
    for ((i = 1; i <= RUNS; i++)); do
      run run --task "$task" --threads "$t" --output "$OUT"
    done
  done
done
