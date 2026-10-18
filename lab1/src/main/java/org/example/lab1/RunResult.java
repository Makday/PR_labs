package org.example.lab1;

import java.time.Instant;

public record RunResult(
    String taskId,
    int threads,
    long timeMs,
    long peakRssKb,
    SignCounts counts,
    Instant timestamp) {
}
