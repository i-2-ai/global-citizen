package com.globalcitizen.shared.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * QR Code service for generating and scanning QR codes
 * Used for passport and visa JWT tokens
 */
@Slf4j
@Service
public class QRCodeService {

    private static final int QR_CODE_SIZE = 400;
    private static final int MARGIN = 10;
    private static final String IMAGE_FORMAT = "PNG";

    /**
     * Generate QR code from JWT token
     * @param jwtToken JWT token string
     * @return QR code as byte array
     */
    public byte[] generateQRCode(String jwtToken) throws WriterException, IOException {
        log.debug("Generating QR code for JWT token");
        
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, MARGIN);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(jwtToken, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);

        BufferedImage qrImage = createQRImage(bitMatrix);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(qrImage, IMAGE_FORMAT, outputStream);
        
        byte[] qrCodeBytes = outputStream.toByteArray();
        log.debug("QR code generated successfully, size: {} bytes", qrCodeBytes.length);
        
        return qrCodeBytes;
    }

    /**
     * Generate QR code with custom styling
     * @param jwtToken JWT token string
     * @param title Title to display on QR code
     * @param subtitle Subtitle to display on QR code
     * @return QR code as byte array
     */
    public byte[] generateStyledQRCode(String jwtToken, String title, String subtitle) throws WriterException, IOException {
        log.debug("Generating styled QR code with title: {}", title);
        
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, MARGIN);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(jwtToken, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);

        BufferedImage qrImage = createStyledQRImage(bitMatrix, title, subtitle);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(qrImage, IMAGE_FORMAT, outputStream);
        
        byte[] qrCodeBytes = outputStream.toByteArray();
        log.debug("Styled QR code generated successfully, size: {} bytes", qrCodeBytes.length);
        
        return qrCodeBytes;
    }

    /**
     * Create basic QR code image
     * @param bitMatrix QR code bit matrix
     * @return BufferedImage
     */
    private BufferedImage createQRImage(BitMatrix bitMatrix) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        
        // Set background to white
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        
        // Set QR code color to black
        graphics.setColor(Color.BLACK);
        
        // Draw QR code
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (bitMatrix.get(x, y)) {
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        }
        
        graphics.dispose();
        return image;
    }

    /**
     * Create styled QR code image with title and subtitle
     * @param bitMatrix QR code bit matrix
     * @param title Title text
     * @param subtitle Subtitle text
     * @return BufferedImage
     */
    private BufferedImage createStyledQRImage(BitMatrix bitMatrix, String title, String subtitle) {
        int qrWidth = bitMatrix.getWidth();
        int qrHeight = bitMatrix.getHeight();
        int titleHeight = 60;
        int subtitleHeight = 40;
        int totalHeight = qrHeight + titleHeight + subtitleHeight;
        
        BufferedImage image = new BufferedImage(qrWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        
        // Enable anti-aliasing
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Set background to white
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, qrWidth, totalHeight);
        
        // Draw title
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics titleMetrics = graphics.getFontMetrics();
        int titleX = (qrWidth - titleMetrics.stringWidth(title)) / 2;
        graphics.drawString(title, titleX, 25);
        
        // Draw subtitle
        graphics.setFont(new Font("Arial", Font.PLAIN, 14));
        FontMetrics subtitleMetrics = graphics.getFontMetrics();
        int subtitleX = (qrWidth - subtitleMetrics.stringWidth(subtitle)) / 2;
        graphics.drawString(subtitle, subtitleX, titleHeight + 25);
        
        // Draw QR code
        graphics.setColor(Color.BLACK);
        for (int x = 0; x < qrWidth; x++) {
            for (int y = 0; y < qrHeight; y++) {
                if (bitMatrix.get(x, y)) {
                    graphics.fillRect(x, y + titleHeight + subtitleHeight, 1, 1);
                }
            }
        }
        
        graphics.dispose();
        return image;
    }

    /**
     * Generate passport QR code
     * @param jwtToken Passport JWT token
     * @return QR code as byte array
     */
    public byte[] generatePassportQRCode(String jwtToken) throws WriterException, IOException {
        return generateStyledQRCode(jwtToken, "GLOBAL PASSPORT", "Digital Identity Document");
    }

    /**
     * Generate visa QR code
     * @param jwtToken Visa JWT token
     * @return QR code as byte array
     */
    public byte[] generateVisaQRCode(String jwtToken) throws WriterException, IOException {
        return generateStyledQRCode(jwtToken, "GLOBAL VISA", "Travel Authorization");
    }

    /**
     * Convert QR code to base64 string
     * @param qrCodeBytes QR code byte array
     * @return Base64 encoded string
     */
    public String qrCodeToBase64(byte[] qrCodeBytes) {
        return java.util.Base64.getEncoder().encodeToString(qrCodeBytes);
    }

    /**
     * Convert base64 string to QR code bytes
     * @param base64String Base64 encoded QR code
     * @return QR code byte array
     */
    public byte[] base64ToQRCode(String base64String) {
        return java.util.Base64.getDecoder().decode(base64String);
    }

    /**
     * Create QR code response with metadata
     * @param jwtToken JWT token
     * @param type Type of document (passport/visa)
     * @return QRCodeResponse object
     */
    public QRCodeResponse createQRCodeResponse(String jwtToken, String type) throws WriterException, IOException {
        byte[] qrCodeBytes;
        String title;
        String subtitle;
        
        if ("passport".equals(type)) {
            qrCodeBytes = generatePassportQRCode(jwtToken);
            title = "GLOBAL PASSPORT";
            subtitle = "Digital Identity Document";
        } else if ("visa".equals(type)) {
            qrCodeBytes = generateVisaQRCode(jwtToken);
            title = "GLOBAL VISA";
            subtitle = "Travel Authorization";
        } else {
            qrCodeBytes = generateQRCode(jwtToken);
            title = "GLOBAL DOCUMENT";
            subtitle = "Digital Document";
        }
        
        return QRCodeResponse.builder()
                .qrCodeBase64(qrCodeToBase64(qrCodeBytes))
                .jwtToken(jwtToken)
                .type(type)
                .title(title)
                .subtitle(subtitle)
                .generatedAt(java.time.Instant.now())
                .build();
    }
} 