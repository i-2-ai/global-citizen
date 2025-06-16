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
@RequestMapping("/country-admin")
@RequiredArgsConstructor
@Slf4j
public class CountryAdminController {
    
    private final ApiClientService apiClientService;
    
    @GetMapping
    public String dashboard(Model model) {
        // Load country public key
        apiClientService.getCountryPublicKey()
                .subscribe(publicKey -> model.addAttribute("publicKey", publicKey));
        
        return "country-admin/dashboard";
    }
    
    @GetMapping("/passport/issue")
    public String issuePassportForm(Model model) {
        return "country-admin/issue-passport";
    }
    
    @PostMapping("/passport/issue")
    public String issuePassport(@RequestParam Map<String, String> passportData, 
                               RedirectAttributes redirectAttributes) {
        log.info("Issuing passport for citizen: {}", passportData.get("citizenId"));
        
        apiClientService.issuePassport(passportData)
                .subscribe(
                    result -> {
                        redirectAttributes.addFlashAttribute("message", "Passport issued successfully");
                        redirectAttributes.addFlashAttribute("passport", result);
                    },
                    error -> redirectAttributes.addFlashAttribute("error", "Failed to issue passport: " + error.getMessage())
                );
        
        return "redirect:/country-admin/passport/result";
    }
    
    @GetMapping("/passport/result")
    public String passportResult(Model model) {
        return "country-admin/passport-result";
    }
    
    @GetMapping("/passport/search")
    public String searchPassportForm() {
        return "country-admin/search-passport";
    }
    
    @PostMapping("/passport/search")
    public String searchPassport(@RequestParam String citizenId, 
                                RedirectAttributes redirectAttributes) {
        apiClientService.getPassportByCitizenId(citizenId)
                .subscribe(
                    passport -> redirectAttributes.addFlashAttribute("passport", passport),
                    error -> redirectAttributes.addFlashAttribute("error", "Passport not found")
                );
        
        return "redirect:/country-admin/passport/search";
    }
    
    @GetMapping("/public-key")
    public String publicKey(Model model) {
        apiClientService.getCountryPublicKey()
                .subscribe(publicKey -> model.addAttribute("publicKey", publicKey));
        
        return "country-admin/public-key";
    }
    
    @GetMapping("/passports")
    public String listPassports(Model model) {
        // This would load all passports from the country service
        return "country-admin/passports";
    }
} 