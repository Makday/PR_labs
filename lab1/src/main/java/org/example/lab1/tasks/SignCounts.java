package org.example.lab1.tasks;

public record SignCounts(long negative, long positive, long zero) {
  public SignCounts plus(SignCounts o) {
    return new SignCounts(negative + o.negative, positive + o.positive, zero + o.zero);
  }

  public long total() {
    return negative + positive + zero;
  }
}
