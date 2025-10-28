package com.labinvent.analyzer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//todo удалить
@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("OK");
    }

}
