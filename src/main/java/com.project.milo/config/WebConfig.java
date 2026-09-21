package com.project.milo.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Controller
public class WebConfig implements WebMvcConfigurer {
    @RequestMapping("/milo")
    public String redirectLogin() {
        // 重定向到根路径的 /login
        return "redirect:/login";
    }
}
