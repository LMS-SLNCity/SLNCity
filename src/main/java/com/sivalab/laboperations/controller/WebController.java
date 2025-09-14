package com.sivalab.laboperations.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web Controller for handling role-based page routing
 */
@Controller
public class WebController {

    /**
     * Root path - redirect to appropriate dashboard based on role
     */
    @GetMapping("/")
    public String index(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    /**
     * Login page
     */
    @GetMapping("/login")
    public String login() {
        return "redirect:/login.html";
    }

    /**
     * Dashboard - redirect to role-specific dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // Check user role and redirect to appropriate dashboard
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard.html";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RECEPTION"))) {
            return "redirect:/reception/dashboard.html";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PHLEBOTOMIST"))) {
            return "redirect:/phlebotomy/dashboard.html";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TECHNICIAN"))) {
            return "redirect:/technician/dashboard.html";
        }

        // Default fallback
        return "redirect:/login?error=unauthorized";
    }


}
