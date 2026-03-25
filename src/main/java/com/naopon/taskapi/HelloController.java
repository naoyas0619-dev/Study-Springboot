package com.naopon.taskapi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Simple sample endpoint used to confirm that the API is reachable.
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        // Returns plain text instead of JSON.
        return "hello";
    }
}
