package com.labinvent.analyzer.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import com.labinvent.analyzer.util.StorageProperties;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceImplTest {

    @Test
    void testSaveAndDeleteTempFile() throws Exception {
        Path tempDir = Files.createTempDirectory("storage-test");
        StorageProperties props = new StorageProperties();
        props.setTempDir(tempDir.toString());

        FileStorageServiceImpl storage = new FileStorageServiceImpl(props);

        MockMultipartFile file = new MockMultipartFile("file", "test.txt",
                "text/plain", "hello".getBytes());

        String path = storage.saveTempFile(file);
        assertTrue(Files.exists(Path.of(path)));

        storage.deleteTempFile(path);
        assertFalse(Files.exists(Path.of(path)));
    }
}
