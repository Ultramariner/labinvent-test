package com.labinvent.analyzer.util.impl;

import com.labinvent.analyzer.util.CountingInputStream;
import com.labinvent.analyzer.util.FileProcessor;
import com.labinvent.analyzer.util.ReaderBundle;
import com.labinvent.analyzer.util.StatsAccumulator;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class CsvFileProcessorImpl implements FileProcessor {

    @Override
    public ReaderBundle openReader(Path filePath) throws IOException {
        InputStream is = Files.newInputStream(filePath);
        BufferedInputStream bis = new BufferedInputStream(is, 256 * 1024);
        CountingInputStream counting = new CountingInputStream(bis);
        Reader reader = new InputStreamReader(counting, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(reader, 256 * 1024);
        return new ReaderBundle(br, counting);
    }

    @Override
    public void processLine(String line, long lineNo, StatsAccumulator acc) {
        //todo проверка первой строки
        if (lineNo == 1) return;
        if (line.isBlank()) { acc.addInvalid(); return; }

        int commaIdx = line.indexOf(',');
        if (commaIdx < 0) { acc.addInvalid(); return; }

        String valPart = line.substring(commaIdx + 1).trim();
        if (valPart.isEmpty()) { acc.addInvalid(); return; }

        try {
            double value = Double.parseDouble(valPart);
            if (!Double.isFinite(value)) { acc.addInvalid(); return; }
            acc.addValid(value);
        } catch (NumberFormatException e) {
            acc.addInvalid();
        }
    }
}