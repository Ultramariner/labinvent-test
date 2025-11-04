package com.labinvent.analyzer.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String saveTempFile(MultipartFile file);
    void deleteTempFile(String path);
}