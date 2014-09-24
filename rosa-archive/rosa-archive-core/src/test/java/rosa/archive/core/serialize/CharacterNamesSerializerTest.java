package rosa.archive.core.serialize;

import org.junit.Before;
import org.junit.Test;
import rosa.archive.model.CharacterNames;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @see rosa.archive.core.serialize.CharacterNamesSerializer
 */
public class CharacterNamesSerializerTest extends BaseSerializerTest {
    private static final String testFile = "data/character_names.csv";

    private Serializer<CharacterNames> serializer;

    @Before
    public void setup() {
        super.setup();
        serializer = new CharacterNamesSerializer(config);
    }

    @Test
    public void readsFromFile() throws IOException {

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(testFile)) {

            CharacterNames names = serializer.read(in, errors);
            assertNotNull(names);

            assertEquals(117, names.getAllCharacterIds().size());
            assertEquals(116, names.getAllNamesInLanguage("en").size());
            assertEquals(116, names.getAllNamesInLanguage("Site name").size());
            assertTrue(names.getAllNamesInLanguage("fr").size() > 0);
            assertTrue(names.getAllNamesInLanguage("fr").size() < 117);

            assertEquals("Diogenes", names.getNameInLanguage("36", "Site name"));
            assertEquals("Dyogenes, Dyogenés", names.getNameInLanguage("36", "fr"));
            assertEquals("Diogenes", names.getNameInLanguage("36", "en"));

        }

    }

    @Test (expected = UnsupportedOperationException.class)
    public void writeTest() throws IOException {
        OutputStream out = mock(OutputStream.class);
        serializer.write(new CharacterNames(), out);
    }

}
