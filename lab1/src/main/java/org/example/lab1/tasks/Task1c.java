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
  private static final List<String> POISON = new ArrayList<>();

  private final Path file;
  private final int workerCount;

  public Task1c(Path file, int workerCount) {
    this.file = file;
    this.workerCount = workerCount;
  }

  @Override
  public SignCounts run() throws Exception {
    BlockingQueue<List<String>> queue = new ArrayBlockingQueue<>(NUMBER_BATCHES);

    Thread reader = new Thread(new Reader(queue, workerCount));
    List<Worker> workers = new ArrayList<>();

    for (int i = 0; i < workerCount; i++) {
      workers.add(new Worker(queue));
    }

    workers.forEach(Thread::start);
    reader.start();

    try {
      reader.join();
      for (Worker w : workers)
        w.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw e;
    }

    SignCounts total = new SignCounts(0, 0, 0);
    for (Worker w : workers) {
      total = total.plus(w.getCount());
    }

    return total;
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
    private SignCounts signcounts;

    Worker(BlockingQueue<List<String>> queue) {
      this.queue = queue;
      this.signcounts = new SignCounts(0, 0, 0);
    }

    public SignCounts getCount() {
      return signcounts;
    }

    public void run() {
      long negative = 0;
      long positive = 0;
      long zero = 0;
      try {
        while (true) {
          List<String> batch = queue.take();
          if (batch == POISON)
            break;
          for (String line : batch) {
            if (line.isBlank())
              continue;
            int value = Integer.parseInt(line.trim());
            if (value < 0)
              negative++;
            else if (value > 0)
              positive++;
            else
              zero++;
          }
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      signcounts = new SignCounts(negative, positive, zero);
    }
  }
}
