package com.globalcitizen.centralauthority.controller;

import com.globalcitizen.centralauthority.model.GlobalKey;
import com.globalcitizen.centralauthority.model.MotherKey;
import com.globalcitizen.centralauthority.service.KeyManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/keys")
@RequiredArgsConstructor
public class KeyManagementController {

    private final KeyManagementService keyManagementService;

    @PostMapping("/mother")
    public ResponseEntity<MotherKey> createMotherKey() throws Exception {
        MotherKey motherKey = keyManagementService.generateMotherKey();
        return ResponseEntity.ok(motherKey);
    }

    @PostMapping("/global")
    public ResponseEntity<List<GlobalKey>> createGlobalKeys() throws Exception {
        List<GlobalKey> globalKeys = keyManagementService.generateGlobalKeys();
        return ResponseEntity.ok(globalKeys);
    }

    @GetMapping("/mother")
    public ResponseEntity<MotherKey> getMotherKey() {
        return ResponseEntity.ok(keyManagementService.getActiveMotherKey());
    }

    @GetMapping("/global")
    public ResponseEntity<List<GlobalKey>> getGlobalKeys() {
        return ResponseEntity.ok(keyManagementService.getActiveGlobalKeys());
    }

    @GetMapping("/global/{number}")
    public ResponseEntity<GlobalKey> getGlobalKeyByNumber(@PathVariable int number) {
        return ResponseEntity.ok(keyManagementService.getGlobalKeyByNumber(number));
    }

    @PostMapping("/global/{number}/rotate")
    public ResponseEntity<GlobalKey> rotateGlobalKey(@PathVariable int number) throws Exception {
        return ResponseEntity.ok(keyManagementService.rotateGlobalKey(number));
    }

    @PostMapping("/global/{number}/revoke")
    public ResponseEntity<Void> revokeGlobalKey(@PathVariable int number) {
        keyManagementService.revokeGlobalKey(number);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<KeyManagementService.KeyStatistics> getKeyStatistics() {
        return ResponseEntity.ok(keyManagementService.getKeyStatistics());
    }
} 