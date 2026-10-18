package org.example.lab1.tasks;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Task1c implements Task {
  private static final int BATCH_SIZE = 10_000;
  private static final int NUMBER_BATCHES = 100;
  private static final int MAX_WORKERS = 64;
  private static final List<String> POISON = new ArrayList<>();

  private final Path file;

  public Task1c(Path file) {
    this.file = file;
  }

  public void run() {
    for (int i = 1; i <= MAX_WORKERS; i *= 2) {
      runWithWorkers(i);
    }
  }

  private void runWithWorkers(int workerCount) {
    BlockingQueue<List<String>> queue = new ArrayBlockingQueue<>(NUMBER_BATCHES);

    Thread reader = new Thread(new Reader(queue, workerCount));
    List<Worker> workers = new ArrayList<>();

    for (int i = 0; i < workerCount; i++) {
      workers.add(new Worker(queue));
    }

    long start = System.nanoTime();

    workers.forEach(Thread::start);
    reader.start();

    try {
      reader.join();
      for (Worker w : workers)
        w.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return;
    }

    int total = 0;
    for (Worker w : workers) {
      total += w.getCount();
    }

    long ms = (System.nanoTime() - start) / 1_000_000;
    System.out.println("workers=" + workerCount + " count=" + total + " time=" + ms + " ms");
  }

  private class Reader implements Runnable {
    private final BlockingQueue<List<String>> queue;
    private final int workerCount;

    Reader(BlockingQueue<List<String>> queue, int workerCount) {
      this.queue = queue;
      this.workerCount = workerCount;
    }

    public void run() {
      try (BufferedReader br = Files.newBufferedReader(file)) {
        List<String> batch = new ArrayList<>(BATCH_SIZE);
        String line;
        while ((line = br.readLine()) != null) {
          batch.add(line);
          if (batch.size() == BATCH_SIZE) {
            queue.put(batch);
            batch = new ArrayList<>(BATCH_SIZE);
          }
        }
        if (!batch.isEmpty())
          queue.put(batch);
        for (int i = 0; i < workerCount; i++) {
          queue.put(POISON);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private class Worker extends Thread {
    private final BlockingQueue<List<String>> queue;
    private int count;

    Worker(BlockingQueue<List<String>> queue) {
      this.queue = queue;
    }

    int getCount() {
      return count;
    }

    public void run() {
      int local = 0;
      try {
        while (true) {
          List<String> batch = queue.take();
          if (batch == POISON)
            break;
          for (String line : batch) {
            if (line.isBlank())
              continue;
            Integer.parseInt(line.trim());
            local++;
          }
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      count = local;
    }
  }
}
