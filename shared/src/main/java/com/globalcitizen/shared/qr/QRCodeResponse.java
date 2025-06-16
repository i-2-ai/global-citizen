package com.globalcitizen.shared.qr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response model for QR code generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeResponse {
    
    /**
     * Base64 encoded QR code image
     */
    private String qrCodeBase64;
    
    /**
     * JWT token that was encoded in the QR code
     */
    private String jwtToken;
    
    /**
     * Type of document (passport/visa)
     */
    private String type;
    
    /**
     * Title displayed on the QR code
     */
    private String title;
    
    /**
     * Subtitle displayed on the QR code
     */
    private String subtitle;
    
    /**
     * Timestamp when QR code was generated
     */
    private Instant generatedAt;
    
    /**
     * MIME type of the QR code image
     */
    @Builder.Default
    private String mimeType = "image/png";
    
    /**
     * Size of the QR code in pixels
     */
    @Builder.Default
    private int size = 400;
} 