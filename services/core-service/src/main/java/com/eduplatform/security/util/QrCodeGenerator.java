// FILE 27: QrCodeGenerator.java
package com.eduplatform.security.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class QrCodeGenerator {

    /**
     * GENERATE QR CODE FOR TOTP
     */
    public static String generateQrCode(String userId, String secret) {
        try {
            String otpauthUrl = "otpauth://totp/EduPlatform:" + userId +
                    "?secret=" + secret + "&issuer=EduPlatform";

            BitMatrix matrix = new MultiFormatWriter().encode(
                    otpauthUrl, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);

            byte[] qrCodeImage = outputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(qrCodeImage);

        } catch (Exception e) {
            throw new RuntimeException("QR code generation failed", e);
        }
    }
}