package org.example.lab1;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class NumberFileGenerator {
  private final int count;
  private final int min;
  private final int max;
  private final Random random = new Random();

  public NumberFileGenerator(int count, int min, int max) {
    this.count = count;
    this.min = min;
    this.max = max;
  }

  public void generate(Path file) throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(file)) {
      for (int i = 0; i < count; i++) {
        int number = random.nextInt(min, max + 1);
        writer.write(Integer.toString(number));
        if (i != count - 1)
          writer.newLine();
      }
    }
  }
}
