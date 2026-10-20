package org.example.lab1.profiling;

import java.time.Instant;

import org.example.lab1.tasks.SignCounts;

public record RunResult(
    String taskId,
    int threads,
    long timeMs,
    long peakRssKb,
    SignCounts counts,
    Instant timestamp) {
}
