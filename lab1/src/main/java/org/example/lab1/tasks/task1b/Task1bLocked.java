package org.example.lab1.tasks.task1b;

import java.nio.file.Path;

import org.example.lab1.tasks.SignCounts;
import org.example.lab1.tasks.Task;

/** Version 2: same shared counters, but every increment happens inside a lock. Correct but slow. */
public class Task1bLocked implements Task {
    private static final class Counters {
        long negative;
        long positive;
        long zero;
    }

    private final Path file;
    private final int threads;
    private int[] numbers;

    public Task1bLocked(Path file, int threads) {
        this.file = file;
        this.threads = threads;
    }

    @Override
    public void prepare() throws Exception {
        numbers = NumberLoader.load(file);
    }

    @Override
    public SignCounts run() throws Exception {
        final int[] data = numbers;
        final Counters shared = new Counters();
        final Object lock = new Object();

        Parallel.forRanges(data.length, threads, (idx, from, to) -> {
            for (int i = from; i < to; i++) {
                int v = data[i];
                synchronized (lock) {          // one thread at a time gets past this line
                    if (v < 0)
                        shared.negative++;
                    else if (v > 0)
                        shared.positive++;
                    else
                        shared.zero++;
                }
            }
        });

        return new SignCounts(shared.negative, shared.positive, shared.zero);
    }

    @Override
    public void cleanup() {
        numbers = null;
    }
}