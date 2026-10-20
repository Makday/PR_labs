package org.example.lab1.profiling;

import java.time.Instant;

import org.example.lab1.tasks.SignCounts;
import org.example.lab1.tasks.Task;

public final class Runner {
  private Runner() {
  }

  public static RunResult run(String taskId, int threads, Task task) throws Exception {
    try {
      task.prepare();

      long start = System.nanoTime();
      SignCounts counts = task.run();
      long timeMs = (System.nanoTime() - start) / 1_000_000;

      long peakKb = MemoryStats.peakRssKb();

      return new RunResult(taskId, threads, timeMs, peakKb, counts, Instant.now());
    } finally {
      task.cleanup();
    }
  }
}
