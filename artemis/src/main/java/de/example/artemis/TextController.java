package de.example.artemis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("text")
@RequestMapping("api/")
public class TextController {

    @Value("${artemis.instance-name}")
    private String instanceName;

    @GetMapping("/text")
    public String getText() {
        return "Text enabled for instanceName " + instanceName;
    }
}
