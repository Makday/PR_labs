package org.example.lab1.tasks.task1a;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.example.lab1.tasks.SignCounts;

public final class ThreadedCounter {
  public static SignCounts run(Path file, int threadCount) throws IOException, InterruptedException {
    long size = Files.size(file);
    Thread[] threads = new Thread[threadCount];
    SignCounts[] results = new SignCounts[threadCount];

    for (int i = 0; i < threadCount; i++) {
      final int idx = i;
      long start = size * i / threadCount;
      long end = size * (i + 1) / threadCount;
      threads[i] = new Thread(() -> {
        try {
          results[idx] = RangeCounter.count(file, start, end);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
      threads[i].start();
    }

    SignCounts total = new SignCounts(0, 0, 0);
    for (int i = 0; i < threadCount; i++) {
      threads[i].join();
      total = total.plus(results[i]);
    }
    return total;
  }
}
