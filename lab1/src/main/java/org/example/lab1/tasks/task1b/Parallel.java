package org.example.lab1.tasks.task1b;

/** Splits [0, length) into equal index ranges and runs one thread per range. */
final class Parallel {
    @FunctionalInterface
    interface RangeWork {
        void run(int threadIndex, int from, int to);   // handles numbers[from] .. numbers[to - 1]
    }

    private Parallel() {
    }

    static void forRanges(int length, int threadCount, RangeWork work) throws InterruptedException {
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            final int from = (int) ((long) length * i / threadCount);
            final int to = (int) ((long) length * (i + 1) / threadCount);
            threads[i] = new Thread(() -> work.run(idx, from, to));
            threads[i].start();
        }

        for (Thread t : threads)
            t.join();   // wait for everyone; also guarantees we see what they wrote
    }
}