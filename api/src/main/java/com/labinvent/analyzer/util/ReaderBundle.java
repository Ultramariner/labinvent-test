package com.labinvent.analyzer.util;

import java.io.*;

public record ReaderBundle(BufferedReader reader, CountingInputStream countingStream) implements AutoCloseable {
    public long getReadBytes() {
        return countingStream.getReadBytes();
    }

    @Override
    public void close() throws IOException {
        reader.close();
        countingStream.close();
    }
}
