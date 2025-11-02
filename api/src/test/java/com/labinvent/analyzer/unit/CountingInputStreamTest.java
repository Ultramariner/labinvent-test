package com.labinvent.analyzer.unit;

import com.labinvent.analyzer.util.CountingInputStream;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import static org.junit.jupiter.api.Assertions.*;

class CountingInputStreamTest {

    @Test
    void testReadCountsBytes() throws Exception {
        byte[] data = "abc".getBytes();
        try (CountingInputStream in = new CountingInputStream(new ByteArrayInputStream(data))) {
            while (in.read() != -1) { }
            assertEquals(3, in.getReadBytes());
        }
    }

    @Test
    void testReadArrayCountsBytes() throws Exception {
        byte[] data = "abcdef".getBytes();
        try (CountingInputStream in = new CountingInputStream(new ByteArrayInputStream(data))) {
            byte[] buf = new byte[10];
            int read = in.read(buf, 0, buf.length);
            assertEquals(6, read);
            assertEquals(6, in.getReadBytes());
        }
    }
}