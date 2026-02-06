package org.example.utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public final class FileUtils {

    private FileUtils() {}

    public static String extractTextFromPdf(byte[] content) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(content)) {
            return new PDFTextStripper().getText(pdf);
        }
    }

    public static String extractTextFromDocx(byte[] content) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(content))) {
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            return paragraphs.stream()
                    .map(XWPFParagraph::getText)
                    .collect(Collectors.joining("\n"));
        }
    }

    public static String extractTextFromPptx(byte[] content) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow(new ByteArrayInputStream(content))) {
            StringBuilder sb = new StringBuilder();
            for (XSLFSlide slide : ppt.getSlides()) {
                slide.getShapes().forEach(shape -> {
                    if (shape instanceof org.apache.poi.sl.usermodel.TextShape<?, ?> ts) {
                        sb.append(ts.getText()).append("\n");
                    }
                });
            }
            return sb.toString();
        }
    }
    public static String extractTextFromPptLegacy(byte[] content) throws IOException {
        try (HSLFSlideShow ppt = new HSLFSlideShow(new ByteArrayInputStream(content))) {
            StringBuilder sb = new StringBuilder();
            for (HSLFSlide slide : ppt.getSlides()) {
                slide.getShapes().forEach(shape -> {
                    if (shape instanceof HSLFTextShape ts) {
                        sb.append(ts.getText()).append("\n");
                    }
                });
            }
            return sb.toString();
        }
    }
    public static String extractTextFromTxt(byte[] content) {
        return new String(content, StandardCharsets.UTF_8);
    }

    public static String extractTextFromDocLegacy(byte[] content) throws IOException {
        try (HWPFDocument doc = new HWPFDocument(new ByteArrayInputStream(content))) {
            WordExtractor extractor = new WordExtractor(doc);
            return extractor.getText();
        }
    }
}