package rosa.archive.core.serialize;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import rosa.archive.model.BookReferenceSheet;

public class BookReferenceSerializerTest extends BaseSerializerTest<BookReferenceSheet> {
    @Before
    public void setup() {
        serializer = new BookReferenceSheetSerializer();
    }

    @Override
    public void readTest() throws IOException {
        final String file = "books.csv";
        List<String> errors = new ArrayList<>();

        BookReferenceSheet sheet = loadResource(COLLECTION_NAME, null, file, errors);
        assertNotNull("No book reference sheet found.", sheet);
        assertTrue("Errors found while loading book sheet.", errors.isEmpty());

        assertFalse("Book reference sheet has no books.", sheet.getKeys().isEmpty());
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void writeTest() throws IOException {
        serializer.write(null, null);
    }

    @Ignore
    @Override
    public void roundTripTest() throws IOException {

    }

    @Override
    public BookReferenceSheet createObject() {
        return null;
    }
}
