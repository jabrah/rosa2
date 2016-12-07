package rosa.iiif.presentation.core.extres;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;

import org.junit.BeforeClass;
import org.junit.Test;

public class PerseusDictionaryTest {
    private static PerseusDictionary pd;

    @BeforeClass
    public static void setup() throws IOException {
        pd = new PerseusDictionary();
    }

    @Test
    public void testLookup() throws IOException {
        assertEquals(URI.create("http://cts.perseids.org/read/pdlrefwk/viaf88890045/003/perseus-eng1/A.apollo_1"), pd.lookup("Apollo"));
    }

    @Test
    public void testDecoration() throws IOException {
        String text = "Apollo rides a cow. ";

        String html = new HtmlDecorator().decorate(text, pd);

        assertTrue(html.contains("http://cts.perseids.org/read/pdlrefwk/viaf88890045/003/perseus-eng1/A.apollo_1"));
    }
}