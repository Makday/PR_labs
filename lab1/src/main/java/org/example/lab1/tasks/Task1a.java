package org.example.lab1.tasks;

import java.nio.file.Path;

import org.example.lab1.tasks.task1a.ThreadedCounter;

public class Task1a implements Task {
    private final Path file;
    private final int threads;

    public Task1a(Path file, int threads) {
        this.file = file;
        this.threads = threads;
    }

    @Override
    public SignCounts run() throws Exception {
        return ThreadedCounter.run(file, threads);
    }
}