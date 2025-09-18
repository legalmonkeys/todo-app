package com.todoapp.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Home controller that redirects to the main todo app and provides API information.
 */
@Controller
public class HomeController {

    /**
     * Redirects the root URL to the main todo lists page.
     * This provides a better user experience than showing JSON.
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/lists";
    }

    /**
     * Provides API information for developers at /api endpoint.
     * This maintains the JSON API info but moves it to a more appropriate endpoint.
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiInfo() {
        return ResponseEntity.ok(Map.of(
                "message", "TODO Challenge API",
                "version", "1.0.0",
                "description", "Multi-list TODO application REST API",
                "endpoints", Map.of(
                        "lists", "/api/lists",
                        "items", "/api/items",
                        "h2-console", "/h2-console"
                ),
                "views", Map.of(
                        "home", "/",
                        "lists", "/lists",
                        "items", "/lists/{listId}/items"
                ),
                "documentation", "See OpenAPI spec at /specs/001-title-multi-list/contracts/openapi.yaml"
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "message", "TODO Challenge API is running"
        ));
    }
}
