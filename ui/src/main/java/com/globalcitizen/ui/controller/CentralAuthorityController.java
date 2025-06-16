package com.globalcitizen.ui.controller;

import com.globalcitizen.ui.service.ApiClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

import java.util.Map;

@Controller
@RequestMapping("/central-authority")
@RequiredArgsConstructor
@Slf4j
public class CentralAuthorityController {
    
    private final ApiClientService apiClientService;
    
    @GetMapping
    public String dashboard(Model model) {
        // Load key statistics
        apiClientService.getKeyStatistics()
                .subscribe(stats -> model.addAttribute("keyStats", stats));
        
        return "central-authority/dashboard";
    }
    
    @GetMapping("/keys")
    public String keys(Model model) {
        // Load mother key and global keys
        apiClientService.getMotherKey()
                .subscribe(motherKey -> model.addAttribute("motherKey", motherKey));
        
        apiClientService.getGlobalKeys()
                .subscribe(globalKeys -> model.addAttribute("globalKeys", globalKeys));
        
        return "central-authority/keys";
    }
    
    @PostMapping("/keys/mother")
    public String generateMotherKey(RedirectAttributes redirectAttributes) {
        // This would call the API to generate mother key
        redirectAttributes.addFlashAttribute("message", "Mother key generation initiated");
        return "redirect:/central-authority/keys";
    }
    
    @PostMapping("/keys/global")
    public String generateGlobalKeys(RedirectAttributes redirectAttributes) {
        // This would call the API to generate global keys
        redirectAttributes.addFlashAttribute("message", "Global keys generation initiated");
        return "redirect:/central-authority/keys";
    }
    
    @GetMapping("/certificates")
    public String certificates(Model model) {
        return "central-authority/certificates";
    }
    
    @PostMapping("/certificates/request")
    public String requestCertificate(@RequestParam Map<String, String> request, 
                                   RedirectAttributes redirectAttributes) {
        apiClientService.requestCertificate(request)
                .subscribe(
                    result -> redirectAttributes.addFlashAttribute("message", "Certificate requested successfully"),
                    error -> redirectAttributes.addFlashAttribute("error", "Failed to request certificate: " + error.getMessage())
                );
        
        return "redirect:/central-authority/certificates";
    }
    
    @GetMapping("/statistics")
    public String statistics(Model model) {
        apiClientService.getKeyStatistics()
                .subscribe(stats -> model.addAttribute("statistics", stats));
        
        return "central-authority/statistics";
    }
} 