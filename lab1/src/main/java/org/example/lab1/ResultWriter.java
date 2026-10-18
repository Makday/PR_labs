package org.example.lab1;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class ResultWriter {
  private static final String HEADER = "task,threads,time_ms,peak_rss_kb,negative,positive,zero,total,timestamp";

  private ResultWriter() {
  }

  public static String toRow(RunResult r) {
    SignCounts c = r.counts();
    return String.join(",",
        r.taskId(),
        String.valueOf(r.threads()),
        String.valueOf(r.timeMs()),
        String.valueOf(r.peakRssKb()),
        String.valueOf(c.negative()),
        String.valueOf(c.positive()),
        String.valueOf(c.zero()),
        String.valueOf(c.total()),
        r.timestamp().toString());
  }

  public static void append(Path file, RunResult result) throws IOException {
    boolean newFile = !Files.exists(file) || Files.size(file) == 0;
    try (BufferedWriter w = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
      if (newFile) {
        w.write(HEADER);
        w.newLine();
      }
      w.write(toRow(result));
      w.newLine();
    }
  }
}
