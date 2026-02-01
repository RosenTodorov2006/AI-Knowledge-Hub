package unit.utils;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.apache.pdfbox.Loader;
import org.mockito.MockedConstruction;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.example.utils.FileUtils;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.Assertions;
import java.io.IOException;
public class FileUtilsTest {
    private static final byte[] MOCK_CONTENT = "fake pdf content".getBytes();
    private static final String EXTRACTED_TEXT = "This is the text from PDF.";
    @Test
    public void testExtractTextFromPdfShouldReturnTextOnSuccess() throws IOException {
        try (MockedStatic<Loader> loaderMock = Mockito.mockStatic(Loader.class);
             MockedConstruction<PDFTextStripper> stripperMock = Mockito.mockConstruction(PDFTextStripper.class,
                     (mock, context) -> {
                         // Използваме стандартния any(Class) от Mockito
                         Mockito.when(mock.getText(any(PDDocument.class))).thenReturn(EXTRACTED_TEXT);
                     })) {

            PDDocument mockDoc = Mockito.mock(PDDocument.class);
            loaderMock.when(() -> Loader.loadPDF(MOCK_CONTENT)).thenReturn(mockDoc);

            String result = FileUtils.extractTextFromPdf(MOCK_CONTENT);

            Assertions.assertEquals(EXTRACTED_TEXT, result);
            Mockito.verify(mockDoc).close();
        }
    }
    @Test
    public void testExtractTextFromPdfShouldThrowExceptionWhenLoaderFails() {
        try (MockedStatic<Loader> loaderMock = Mockito.mockStatic(Loader.class)) {
            loaderMock.when(() -> Loader.loadPDF(any(byte[].class)))
                    .thenThrow(new IOException("Invalid PDF"));

            Assertions.assertThrows(IOException.class, () ->
                    FileUtils.extractTextFromPdf(MOCK_CONTENT));
        }
    }
}
