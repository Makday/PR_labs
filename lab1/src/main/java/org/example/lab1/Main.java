package org.example.lab1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Main {
  public static void main(String[] args) {
    String fileName = "numbers.txt";
    int count = 50_000_000;
    int min = -1_000_000;
    int max = 1_000_000;

    Random random = new Random();

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

      for (int i = 0; i < count; i++) {
        int number = random.nextInt(min, max);

        writer.write(Integer.toString(number));
        writer.newLine();
      }

      System.out.println("File generated successfully!");

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
