package org.example.lab1;

import org.example.lab1.tasks.SignCounts;
import org.example.lab1.tasks.task1a.ThreadedCounter;

import java.nio.file.Path;

public class Main {
  private static final Path OUTPUT_FILE = Path.of("numbers.txt");

  public static void main(String[] args) throws Exception {
    switch (args[0]) {
      case "generate" -> new NumberFileGenerator(50_000_000, -1_000_000, 1_000_000).generate(OUTPUT_FILE);
      case "1a" -> {
        int threads = Integer.parseInt(args[1]);
        long t0 = System.nanoTime();
        SignCounts c = ThreadedCounter.run(OUTPUT_FILE, threads);
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("threads=%d time=%dms neg=%d pos=%d zero=%d total=%d%n",
            threads, ms, c.negative(), c.positive(), c.zero(), c.total());
      }
    }
  }
}
