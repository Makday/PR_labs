package org.example.lab1.tasks.task1b;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** Reads numbers.txt into an int[]. Not measured, so simplicity beats speed. */
public final class NumberLoader {
    private static final int BUFFER_SIZE = 1 << 20;

    private NumberLoader() {
    }

    public static int[] load(Path file) throws IOException {
        int count = parse(file, null);           // pass 1: just count the numbers
        int[] numbers = new int[count];
        parse(file, numbers);                    // pass 2: fill the array
        return numbers;
    }

    /**
     * Parses every number in the file. If target is null it only counts;
     * otherwise it also stores each number. One method for both passes means
     * the two passes can never disagree about what counts as a number.
     */
    private static int parse(Path file, int[] target) throws IOException {
        int count = 0;
        boolean negative = false, hasDigits = false;
        int value = 0;

        try (InputStream in = Files.newInputStream(file)) {
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = in.read(buf)) > 0) {
                for (int i = 0; i < n; i++) {
                    byte b = buf[i];
                    if (b >= '0' && b <= '9') {
                        value = value * 10 + (b - '0');
                        hasDigits = true;
                    } else if (b == '-') {
                        negative = true;
                    } else if (b == '\n') {
                        if (hasDigits) {
                            if (target != null)
                                target[count] = negative ? -value : value;
                            count++;
                        }
                        negative = false;
                        hasDigits = false;
                        value = 0;
                    }
                    // anything else ('\r') is ignored
                }
            }
        }

        // last line has no trailing newline
        if (hasDigits) {
            if (target != null)
                target[count] = negative ? -value : value;
            count++;
        }
        return count;
    }
}