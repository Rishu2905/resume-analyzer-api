package com.axis.hraiportal.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import java.security.MessageDigest;

import java.io.IOException;

@Component
@Slf4j
public class PdfExtractorUtil {

    // Extracts raw text from uploaded PDF file
    public String extractText(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)){
                PDFTextStripper stripper = new PDFTextStripper();
                 return stripper.getText(document);

        } catch (IOException e) {
            log.error("Failed to extract text from PDF: {}",
                    e.getMessage());
            throw new RuntimeException(
                    "Could not read PDF file: " + e.getMessage());
        }
    }

    // Generates a simple hash for duplicate detection
    public String generateHash(byte[] pdfBytes) {

        try {

            MessageDigest md =
                    MessageDigest.getInstance("MD5");

            byte[] hash = md.digest(pdfBytes);

            StringBuilder sb =
                    new StringBuilder();

            for (byte b : hash) {

                sb.append(
                        String.format(
                                "%02x",
                                b));
            }

            return sb.toString();

        } catch (Exception e) {

            log.error(
                    "Failed to generate hash: {}",
                    e.getMessage());

            return String.valueOf(
                    pdfBytes.length);
        }
    }
}
