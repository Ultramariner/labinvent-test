package com.labinvent.analyzer.util;

import java.io.IOException;
import java.nio.file.Path;

public interface FileProcessor {
    ReaderBundle openReader(Path filePath) throws IOException;

    void processLine(String line, long lineNo, StatsAccumulator acc);
}
