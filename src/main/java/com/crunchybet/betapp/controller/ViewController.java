package com.crunchybet.betapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";  // Remove .html extension since it's configured in WebConfig
    }

    @GetMapping("/home-page")
    public String homePage() {
        return "home-page";  // Remove .html extension since it's configured in WebConfig
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "profile";  // Remove .html extension since it's configured in WebConfig
    }
}
