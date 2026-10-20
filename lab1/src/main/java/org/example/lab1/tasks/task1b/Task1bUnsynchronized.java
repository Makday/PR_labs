package org.example.lab1.tasks.task1b;

import java.nio.file.Path;

import org.example.lab1.tasks.SignCounts;
import org.example.lab1.tasks.Task;

/** Version 1: all threads increment the same counters with no protection. INCORRECT on purpose. */
public class Task1bUnsynchronized implements Task {
    /** One mutable box shared by every thread. Deliberately not thread-safe. */
    private static final class Counters {
        long negative;
        long positive;
        long zero;
    }

    private final Path file;
    private final int threads;
    private int[] numbers;

    public Task1bUnsynchronized(Path file, int threads) {
        this.file = file;
        this.threads = threads;
    }

    @Override
    public void prepare() throws Exception {
        numbers = NumberLoader.load(file);   // Runner calls this before starting the clock
    }

    @Override
    public SignCounts run() throws Exception {
        final int[] data = numbers;
        final Counters shared = new Counters();

        Parallel.forRanges(data.length, threads, (idx, from, to) -> {
            for (int i = from; i < to; i++) {
                int v = data[i];
                if (v < 0)
                    shared.negative++;       // read, add 1, write: three steps, not atomic
                else if (v > 0)
                    shared.positive++;
                else
                    shared.zero++;
            }
        });

        return new SignCounts(shared.negative, shared.positive, shared.zero);
    }

    @Override
    public void cleanup() {
        numbers = null;
    }
}