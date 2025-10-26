package com.example.Insurance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }

    @RequestMapping(value = {"/index.html", "/userclaims.html", "/adminclaims.html"})
    public String staticPages() {
        return "forward:/index.html";
    }
}
