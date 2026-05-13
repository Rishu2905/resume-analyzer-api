package com.axis.hraiportal.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@Slf4j
public class PdfExtractorUtil {

    // Extracts raw text from uploaded PDF file
    public String extractText(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                log.debug("Extracted {} characters from PDF: {}",                   // debug log
                        text.length(), file.getOriginalFilename());
                return text;
            }
        } catch (IOException e) {
            log.error("Failed to extract text from PDF: {}",
                    e.getMessage());
            throw new RuntimeException(
                    "Could not read PDF file: " + e.getMessage());
        }
    }

    // Generates a simple hash for duplicate detection
    public String generateHash(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            java.security.MessageDigest md =
                    java.security.MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Failed to generate hash: {}",
                    e.getMessage());
            return String.valueOf(file.getSize());
        }
    }
}