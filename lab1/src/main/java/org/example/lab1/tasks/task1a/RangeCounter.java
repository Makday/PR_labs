package org.example.lab1.tasks.task1a;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

public final class RangeCounter {
    private static final int BUFFER_SIZE = 1 << 20; // 1 MB

    /** Counts signs of every line that STARTS in [start, end). */
    public static SignCounts count(Path file, long start, long end) throws IOException {
        long neg = 0, pos = 0, zero = 0;

        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            long readFrom = Math.max(start - 1, 0);
            raf.seek(readFrom);

            boolean skipping = start > 0; // discard the partial line we landed in
            boolean negative = false, hasDigits = false;
            int value = 0;

            byte[] buf = new byte[BUFFER_SIZE];
            long bufOffset = readFrom; // absolute file offset of buf[0]
            int n;

            while ((n = raf.read(buf)) > 0) {
                for (int i = 0; i < n; i++) {
                    byte b = buf[i];

                    if (skipping) {
                        if (b == '\n') {
                            skipping = false;
                            if (bufOffset + i + 1 >= end) return new SignCounts(neg, pos, zero);
                        }
                        continue;
                    }

                    if (b >= '0' && b <= '9') {
                        value = value * 10 + (b - '0');
                        hasDigits = true;
                    } else if (b == '-') {
                        negative = true;
                    } else if (b == '\n') {
                        if (hasDigits) {
                            if (value == 0) zero++;
                            else if (negative) neg++;
                            else pos++;
                        }
                        negative = false; hasDigits = false; value = 0;
                        // the next line starts at the next byte; is it still ours?
                        if (bufOffset + i + 1 >= end) return new SignCounts(neg, pos, zero);
                    }
                    // anything else ('\r', etc.) is ignored
                }
                bufOffset += n;
            }

            // EOF without a trailing newline: your generator does this, so handle the last line
            if (!skipping && hasDigits) {
                if (value == 0) zero++;
                else if (negative) neg++;
                else pos++;
            }
        }
        return new SignCounts(neg, pos, zero);
    }
}
