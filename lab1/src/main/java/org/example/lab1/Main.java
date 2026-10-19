package org.example.lab1;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;

import org.example.lab1.data.NumberFileGenerator;
import org.example.lab1.profiling.ResultWriter;
import org.example.lab1.profiling.RunResult;
import org.example.lab1.profiling.Runner;
import org.example.lab1.tasks.Task;
import org.example.lab1.tasks.Task1c;

public class Main {
  private static final String DEFAULT_NUMBER_FILE = "numbers.txt";
  private static final String DEFAULT_RESULT_FILE = "results.csv";

  private static final Map<String, BiFunction<Path, Integer, Task>> REGISTRY = Map.of("1c", Task1c::new);
  private static final Set<String> RUN_FLAGS = Set.of("task", "threads", "input", "output");
  private static final Set<String> GENERATE_FLAGS = Set.of("output", "count");

  public static void main(String[] args) {
    try {
      if (args.length == 0)
        throw new IllegalArgumentException("Missing mode: run | generate | split");

      String mode = args[0];
      String[] rest = Arrays.copyOfRange(args, 1, args.length);

      switch (mode) {
        case "run" -> runTask(parseFlags(rest, RUN_FLAGS));
        case "generate" -> generate(parseFlags(rest, GENERATE_FLAGS));
        default -> throw new IllegalArgumentException("Unknown mode '" + mode + "'");
      }

    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      System.exit(1);
    }
  }

  private static Map<String, String> parseFlags(String[] args, Set<String> allowed) {
    Map<String, String> flags = new HashMap<>();
    for (int i = 0; i < args.length; i += 2) {
      String arg = args[i];

      if (!arg.startsWith("--")) {
        throw new IllegalArgumentException("Expected a flag starting with '--', got: " + arg);
      }

      String key = arg.substring(2);
      if (!allowed.contains(key)) {
        throw new IllegalArgumentException("Unknown flag --" + key + ". Allowed: " + allowed);
      }
      if (i + 1 >= args.length) {
        throw new IllegalArgumentException("Missing value for --" + key);
      }
      if (flags.put(key, args[i + 1]) != null) {
        throw new IllegalArgumentException("Flag --" + key + " given more than once");
      }
    }
    return flags;
  }

  private static void runTask(Map<String, String> flags) throws Exception {
    String taskId = require(flags, "task");
    int threads = parsePositiveInt(require(flags, "threads"), "threads");
    Path input = Path.of(flags.getOrDefault("input", DEFAULT_NUMBER_FILE));
    Path out = Path.of(flags.getOrDefault("output", DEFAULT_RESULT_FILE));

    BiFunction<Path, Integer, Task> factory = REGISTRY.get(taskId);
    if (factory == null) {
      throw new IllegalArgumentException("Unknown task '" + taskId + "'. Valid: " + new TreeSet<>(REGISTRY.keySet()));
    }
    Task task = factory.apply(input, threads);
    RunResult result = Runner.run(taskId, threads, task);

    ResultWriter.append(out, result);
    System.out.println(ResultWriter.toRow(result));
  }

  private static int parsePositiveInt(String text, String name) {
    int value;
    try {
      value = Integer.parseInt(text);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("--" + name + " must be an integer, got: " + text);
    }
    if (value < 1) {
      throw new IllegalArgumentException("--" + name + " must be >= 1, got: " + value);
    }
    return value;
  }

  private static void generate(Map<String, String> flags) throws IOException {
    Path out = Path.of(flags.getOrDefault("output", DEFAULT_NUMBER_FILE));
    int count = Integer.parseInt(flags.getOrDefault("count", "50000000"));
    new NumberFileGenerator(count, -1_000_000, 1_000_000).generate(out);
  }

  private static String require(Map<String, String> flags, String key) {
    String v = flags.get(key);
    if (v == null)
      throw new IllegalArgumentException("Missing --" + key);
    return v;
  }
}
