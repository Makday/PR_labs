package org.example.lab1.tasks.task1b;

import java.nio.file.Path;

import org.example.lab1.tasks.SignCounts;
import org.example.lab1.tasks.Task;

/** Version 3: each thread counts into its own locals; results are added after join(). Correct and fast. */
public class Task1bLocal implements Task {
    private final Path file;
    private final int threads;
    private int[] numbers;

    public Task1bLocal(Path file, int threads) {
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
        final SignCounts[] results = new SignCounts[threads];   // one slot per thread

        Parallel.forRanges(data.length, threads, (idx, from, to) -> {
            long neg = 0, pos = 0, zero = 0;       // private to this thread
            for (int i = from; i < to; i++) {
                int v = data[i];
                if (v < 0)
                    neg++;
                else if (v > 0)
                    pos++;
                else
                    zero++;
            }
            results[idx] = new SignCounts(neg, pos, zero);   // the only write to shared memory
        });

        SignCounts total = new SignCounts(0, 0, 0);
        for (SignCounts r : results)
            total = total.plus(r);
        return total;
    }

    @Override
    public void cleanup() {
        numbers = null;
    }
}