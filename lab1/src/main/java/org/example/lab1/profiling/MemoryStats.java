package org.example.lab1.profiling;

import java.nio.file.Files;
import java.nio.file.Path;

public class MemoryStats {
  private MemoryStats() {
  }

  public static long peakRssKb() {
    try {
      for (String l : Files.readAllLines(Path.of("/proc/self/status"))) {
        if (l.startsWith("VmHWM:"))
          return Long.parseLong(l.replaceAll("\\D+", ""));
      }
    } catch (Exception ignored) {
    }
    return -1; // not available (e.g. macOS)
  }
}
