package org.example.lab1.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class FileSplitter {
  private static final int PARTS = 8;
  private static final int LINES_PER_PART = 6_250_000;

  private FileSplitter() {
  }

  public static void split(Path input, Path outDir) throws IOException {
    Files.createDirectories(outDir);

    try (BufferedReader in = Files.newBufferedReader(input)) {
      for (int p = 0; p < PARTS; p++) {
        Path target = outDir.resolve(String.format("part-%02d.txt", p));
        long written = 0;

        try (BufferedWriter out = Files.newBufferedWriter(target, StandardOpenOption.CREATE_NEW)) {
          String line;
          while (written < LINES_PER_PART && (line = in.readLine()) != null) {
            if (written > 0)
              out.newLine();
            out.write(line);
            written++;
          }
        }

        if (written != LINES_PER_PART) {
          throw new IOException("Input ended early: " + target + " got " + written
              + " lines, expected " + LINES_PER_PART);
        }
      }

      if (in.readLine() != null) {
        throw new IOException("Input has more than " + PARTS * LINES_PER_PART + " lines");
      }
    }
  }
}
