package com.labinvent.analyzer.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String saveTempFile(MultipartFile file);
    void deleteTempFile(String path);
}