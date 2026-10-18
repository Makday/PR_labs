package org.example.lab1.tasks;

import org.example.lab1.SignCounts;

public interface Task {
  default void prepare() throws Exception {
  }

  SignCounts run() throws Exception;

  default void cleanup() {
  }
}
