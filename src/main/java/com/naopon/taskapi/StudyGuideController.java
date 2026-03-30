package com.naopon.taskapi;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Redirects to the static study guide page for this project.
@Controller
public class StudyGuideController {

    @GetMapping("/study-guide")
    public String studyGuide() {
        return "redirect:/study-guide.html";
    }

    @GetMapping("/project-setup-guide")
    public String projectSetupGuide() {
        return "redirect:/project-setup-guide.html";
    }

    @GetMapping("/w03-exception-test-guide")
    public String w03ExceptionTestGuide() {
        return "redirect:/w03-exception-test-guide.html";
    }

    @GetMapping("/w04-jwt-auth-guide")
    public String w04JwtAuthGuide() {
        return "redirect:/w04-jwt-auth-guide.html";
    }

    @GetMapping("/w05-docker-compose-guide")
    public String w05DockerComposeGuide() {
        return "redirect:/w05-docker-compose-guide.html";
    }
}
