package org.example.lab1;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
  private static final Path OUTPUT_FILE = Path.of("numbers.txt");

  public static void main(String[] args) throws IOException {
    new NumberFileGenerator(50_000_000, -1_000_000, 1_000_000).generate(OUTPUT_FILE);
  }
}
