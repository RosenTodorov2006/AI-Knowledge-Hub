package org.example.utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;

public final class FileUtils {
    private FileUtils() {}
    public static String extractTextFromPdf(byte[] content) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(content)) {
            return new PDFTextStripper().getText(pdf);
        }
    }
}
