package com.labinvent.analyzer.util;


import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Подсчёт количества прочитанных байт.
 */
public class CountingInputStream extends FilterInputStream {
    private long readBytes = 0;

    public CountingInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        int r = super.read();
        if (r != -1) readBytes++;
        return r;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int r = super.read(b, off, len);
        if (r > 0) readBytes += r;
        return r;
    }

    public long getReadBytes() {
        return readBytes;
    }
}
