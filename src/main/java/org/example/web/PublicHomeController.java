package org.example.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class PublicHomeController {
    @GetMapping("/")
    public String index(Model model){
        return "index";
    }
    @GetMapping("/work")
    public String work(Model model){
        return "work";
    }
    @GetMapping("/pricing")
    public String pricing(Model model){
        return "pricing";
    }
    @GetMapping("/features")
    public String features(Model model){
        return "features";
    }
}
