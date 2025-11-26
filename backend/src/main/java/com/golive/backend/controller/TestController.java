package com.golive.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String home() {
        return "✅ Backend de GoLive está funcionando correctamente.";
    }

    @GetMapping("/api/hello")
    public String hello() {
        return "👋 Hola Gorka, tu API está lista para usarse.";
    }
}
