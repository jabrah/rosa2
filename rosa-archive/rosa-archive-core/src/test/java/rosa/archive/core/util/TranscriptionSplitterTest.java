package rosa.archive.core.util;

import org.junit.Test;
import rosa.archive.core.BaseArchiveTest;
import rosa.archive.model.Book;
import rosa.archive.model.Transcription;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class TranscriptionSplitterTest extends BaseArchiveTest {
    @Test
    public void test() throws IOException {
        String transcription = loadLudwigTranscription();
        Map<String, String> map = TranscriptionSplitter.split(transcription);

        assertNotNull("Results map is empty.", map);
//        System.out.println();
//        System.out.println(map.size());
//        System.out.println(numPagesLudwig());
//        System.out.println("024r -> " + map.get("024r"));
//        System.out.println();

        for (Entry<String, String> entry : map.entrySet()) {
            assertNotNull("NULL key", entry.getKey());
            assertFalse("Empty key", entry.getKey().isEmpty());
            assertNotNull("NULL value", entry.getValue());
            assertFalse("Empty value", entry.getValue().isEmpty());
        }
    }

    private String loadLudwigTranscription() throws IOException {
        Book book = loadValidLudwigXV7();
        Transcription transcription = book.getTranscription();

        assertNotNull("Transcription missing.", transcription);
        assertFalse("Transcription string missing.", transcription.getXML().isEmpty());

        return transcription.getXML();
    }

    private int numPagesLudwig() throws IOException {
        Book book = loadValidLudwigXV7();
        return book.getImages().getImages().size();
    }
}
