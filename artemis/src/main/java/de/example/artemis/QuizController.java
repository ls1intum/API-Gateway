package de.example.artemis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("quiz")
@RequestMapping("api/")
public class QuizController {

    @Value("${artemis.instance-name}")
    private String instanceName;

    @GetMapping("/quiz")
    public String getText() {
        return "Quiz enabled for instanceName " + instanceName;
    }
}
