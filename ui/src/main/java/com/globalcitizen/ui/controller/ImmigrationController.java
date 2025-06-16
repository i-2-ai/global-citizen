package com.globalcitizen.ui.controller;

import com.globalcitizen.ui.service.ApiClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/immigration")
@RequiredArgsConstructor
@Slf4j
public class ImmigrationController {
    
    private final ApiClientService apiClientService;
    
    @GetMapping
    public String dashboard(Model model) {
        // Load public key registry
        apiClientService.getPublicKeyRegistry()
                .subscribe(registry -> model.addAttribute("publicKeyRegistry", registry));
        
        return "immigration/dashboard";
    }
    
    @GetMapping("/verify/passport")
    public String verifyPassportForm() {
        return "immigration/verify-passport";
    }
    
    @PostMapping("/verify/passport")
    public String verifyPassport(@RequestParam String jwtToken, 
                                RedirectAttributes redirectAttributes) {
        log.info("Verifying passport with JWT token");
        
        apiClientService.verifyPassport(jwtToken)
                .subscribe(
                    result -> {
                        redirectAttributes.addFlashAttribute("message", "Passport verified successfully");
                        redirectAttributes.addFlashAttribute("passport", result);
                    },
                    error -> redirectAttributes.addFlashAttribute("error", "Failed to verify passport: " + error.getMessage())
                );
        
        return "redirect:/immigration/verify/passport";
    }
    
    @GetMapping("/verify/visa")
    public String verifyVisaForm() {
        return "immigration/verify-visa";
    }
    
    @PostMapping("/verify/visa")
    public String verifyVisa(@RequestParam String jwtToken, 
                            RedirectAttributes redirectAttributes) {
        log.info("Verifying visa with JWT token");
        
        apiClientService.verifyVisa(jwtToken)
                .subscribe(
                    result -> {
                        redirectAttributes.addFlashAttribute("message", "Visa verified successfully");
                        redirectAttributes.addFlashAttribute("visa", result);
                    },
                    error -> redirectAttributes.addFlashAttribute("error", "Failed to verify visa: " + error.getMessage())
                );
        
        return "redirect:/immigration/verify/visa";
    }
    
    @GetMapping("/verify/qr")
    public String verifyQrForm() {
        return "immigration/verify-qr";
    }
    
    @PostMapping("/verify/qr")
    public String verifyQrCode(@RequestParam String qrCodeData, 
                              RedirectAttributes redirectAttributes) {
        log.info("Verifying QR code data");
        
        apiClientService.verifyQrCode(qrCodeData)
                .subscribe(
                    result -> {
                        redirectAttributes.addFlashAttribute("message", "QR code verified successfully");
                        redirectAttributes.addFlashAttribute("verificationResult", result);
                    },
                    error -> redirectAttributes.addFlashAttribute("error", "Failed to verify QR code: " + error.getMessage())
                );
        
        return "redirect:/immigration/verify/qr";
    }
    
    @GetMapping("/public-keys")
    public String publicKeyRegistry(Model model) {
        apiClientService.getPublicKeyRegistry()
                .subscribe(registry -> model.addAttribute("publicKeyRegistry", registry));
        
        return "immigration/public-keys";
    }
    
    @GetMapping("/entry-log")
    public String entryLog(Model model) {
        // This would load entry/exit logs
        return "immigration/entry-log";
    }
    
    @GetMapping("/alerts")
    public String alerts(Model model) {
        // This would load security alerts and watchlist checks
        return "immigration/alerts";
    }
    
    @GetMapping("/statistics")
    public String statistics(Model model) {
        // This would load immigration statistics
        return "immigration/statistics";
    }
} 