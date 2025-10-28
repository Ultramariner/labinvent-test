package com.labinvent.analyzer.service.storage.impl;

import com.labinvent.analyzer.service.storage.StorageProperties;
import com.labinvent.analyzer.service.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Slf4j
@Service
public class StorageServiceImpl implements StorageService {

    private final StorageProperties properties;

    public StorageServiceImpl(StorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public String saveTempFile(MultipartFile file) {
        try {
            Path tempDir = Paths.get(properties.getTempDir());
            Files.createDirectories(tempDir);

            Path tempFile = Files.createTempFile(tempDir, "upload-", "-" + file.getOriginalFilename());
            file.transferTo(tempFile);

            log.info("Файл [{}] сохранён во временное хранилище: {}", file.getOriginalFilename(), tempFile);
            return tempFile.toString();
        } catch (IOException e) {
            log.error("Ошибка при сохранении файла [{}]", file.getOriginalFilename(), e);
            throw new RuntimeException("Ошибка при сохранении файла", e);
        }
    }

    @Override
    public void deleteTempFile(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
            log.info("Временный файл удалён: {}", path);
        } catch (IOException e) {
            log.warn("Не удалось удалить временный файл: {}", path, e);
        }
    }
}
