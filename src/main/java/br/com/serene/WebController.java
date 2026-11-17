package br.com.serene;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @GetMapping({"/api", "/api/"})
    public String home() { return "redirect:/"; }
}
