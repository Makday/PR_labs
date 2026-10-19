package org.example.lab1.tasks;

public interface Task {
  default void prepare() throws Exception {
  }

  SignCounts run() throws Exception;

  default void cleanup() {
  }
}
