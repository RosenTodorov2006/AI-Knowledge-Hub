package unit.utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.example.utils.TextUtils;
import java.util.List;
import org.example.repositories.ChunkSearchResult;
import org.mockito.Mockito;
public class TextUtilsTest {
    private static final String SAMPLE_TEXT = "Java е програмен език. Той е обектно-ориентиран. Използва се за големи системи.";
    private static final int SMALL_LIMIT = 30;
    @Test
    public void testCalculateTokenCountShouldReturnZeroForEmptyOrNull() {
        Assertions.assertEquals(0, TextUtils.calculateTokenCount(null));
        Assertions.assertEquals(0, TextUtils.calculateTokenCount("   "));
    }
    @Test
    public void testCalculateTokenCountShouldReturnCorrectCount() {
        String content = "  Това са точно пет думи.  ";
        int result = TextUtils.calculateTokenCount(content);
        Assertions.assertEquals(5, result);
    }
    @Test
    public void testPrepareSemanticChunksShouldSplitAndMaintainContext() {
        List<String> chunks = TextUtils.prepareSemanticChunks(SAMPLE_TEXT, SMALL_LIMIT);

        Assertions.assertTrue(chunks.size() > 1);
        Assertions.assertTrue(chunks.get(0).contains("Java е програмен език."));

        Assertions.assertTrue(chunks.get(1).startsWith("Той е обектно-ориентиран."));
    }
    @Test
    public void testPrepareSemanticChunksShouldNotSplitIfUnderLimit() {
        List<String> chunks = TextUtils.prepareSemanticChunks("Кратък текст.", 100);

        Assertions.assertEquals(1, chunks.size());
        Assertions.assertEquals("Кратък текст.", chunks.get(0));
    }
    @Test
    public void testJoinChunkContentsShouldReturnJoinedString() {
        ChunkSearchResult res1 = Mockito.mock(ChunkSearchResult.class);
        ChunkSearchResult res2 = Mockito.mock(ChunkSearchResult.class);

        Mockito.when(res1.getContent()).thenReturn("Част 1");
        Mockito.when(res2.getContent()).thenReturn("Част 2");

        List<ChunkSearchResult> results = List.of(res1, res2);
        String separator = " | ";

        String joined = TextUtils.joinChunkContents(results, separator);

        Assertions.assertEquals("Част 1 | Част 2", joined);
    }
}
