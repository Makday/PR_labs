package org.example.lab1.tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Task2 implements Task {
  private final Path directory;
  private final int workerCount;
  private final List<List<Path>> assignment = new ArrayList<>();

  public Task2(Path directory, int workerCount) {
    this.directory = directory;
    this.workerCount = workerCount;
  }

  @Override
  public void prepare() throws IOException {
    if (!Files.isDirectory(directory)) {
      throw new IOException("Not a directory: " + directory);
    }
    List<Path> files;
    try (Stream<Path> s = Files.list(directory)) {
      files = s.filter(Files::isRegularFile).sorted().toList();
    }
    if (files.isEmpty()) {
      throw new IOException("No files in " + directory);
    }

    for (int i = 0; i < workerCount; i++) {
      assignment.add(new ArrayList<>());
    }
    for (int i = 0; i < files.size(); i++) {
      assignment.get(i % workerCount).add(files.get(i));
    }
  }

  @Override
  public SignCounts run() throws Exception {
    List<Worker> workers = new ArrayList<>();
    for (List<Path> part : assignment) {
      workers.add(new Worker(part));
    }
    workers.forEach(Thread::start);

    SignCounts total = new SignCounts(0, 0, 0);
    for (Worker w : workers) {
      w.join();
      if (w.error != null)
        throw w.error;
      total = total.plus(w.result);
    }
    return total;
  }

  private static class Worker extends Thread {
    private final List<Path> files;
    private SignCounts result = new SignCounts(0, 0, 0);
    private IOException error;

    Worker(List<Path> files) {
      this.files = files;
    }

    @Override
    public void run() {
      try {
        for (Path f : files) {
          result = result.plus(countFile(f));
        }
      } catch (IOException e) {
        error = e;
      }
    }

    private static SignCounts countFile(Path file) throws IOException {
      byte[] data = Files.readAllBytes(file);

      long negative = 0, positive = 0, zero = 0;
      int value = 0;
      boolean inNumber = false, isNegative = false;

      for (int i = 0; i <= data.length; i++) {
        int b = i < data.length ? data[i] : '\n';
        if (b >= '0' && b <= '9') {
          value = value * 10 + (b - '0');
          inNumber = true;
        } else if (b == '-') {
          isNegative = true;
        } else {
          if (inNumber) {
            if (value == 0)
              zero++;
            else if (isNegative)
              negative++;
            else
              positive++;
          }
          value = 0;
          inNumber = false;
          isNegative = false;
        }
      }
      return new SignCounts(negative, positive, zero);
    }

  }
}
