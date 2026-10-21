# Concurrency in practice

## How to build

This program requires a linux based operating system because of the nature of the peak memory usage measurement.

To run the entire pipeline:

```
bash script.sh
```

Or alternatively, each task could be ran individually using corresponding flags, e.g.
```
./mvnw compile # Build first

java -cp target/classes/ org.example.lab1.Main generate --output numbers.txt # generate file with numbers

java -cp target/classes/ org.example.lab1.Main run --task 1a --threads 2 --input numbers.txt --output results.csv
```

Both ways will yield a `results.csv` file containing profiling results.