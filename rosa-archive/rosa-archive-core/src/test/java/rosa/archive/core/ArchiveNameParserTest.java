package rosa.archive.core;

import org.junit.Before;
import org.junit.Test;
import rosa.archive.model.ArchiveItemType;
import rosa.archive.model.BookImageLocation;
import rosa.archive.model.BookImageRole;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ArchiveNameParserTest {
    private final static String[] TEST_NAMES = {
            "*LudwigXV7.binding.frontcover.tif", "*LudwigXV7.frontmatter.flyleaf.002r.tif",
            "LudwigXV7.frontmatter.flyleaf.002v.tif", "LudwigXV7.020r.tif", "LudwigXV7.020v.tif",
            "LudwigXV7.endmatter.001v.tif", "LudwigXV7.misc.greyscale.tif", "filemap.csv"
    };

    private ArchiveNameParser parser;

    @Before
    public void setup() {
        parser = new ArchiveNameParser();
    }

    @Test
    public void locationTest() {
        BookImageLocation[] expected = {
                BookImageLocation.BINDING, BookImageLocation.FRONT_MATTER,
                BookImageLocation.FRONT_MATTER, BookImageLocation.BODY_MATTER, BookImageLocation.BODY_MATTER,
                BookImageLocation.END_MATTER, BookImageLocation.MISC, null
        };

        for (int i = 0; i < TEST_NAMES.length; i++) {
            assertEquals("Unexpected location found.", expected[i], parser.location(TEST_NAMES[i]));
        }
    }

    @Test
    public void roleTest() {
        BookImageRole[] expected = {
                BookImageRole.FRONT_COVER, null,
                null, null, null,
                null, null, null
        };

        for (int i = 0; i < TEST_NAMES.length; i++) {
            assertEquals("Unexpected role found.", expected[i], parser.role(TEST_NAMES[i]));
        }
    }

    @Test
    public void otherPageNumberingTest() {
        String[] names = {
                "LudwigXV7.frontmatter.flyleaf.002r.tif", "LudwigXV7.frontmatter.flyleaf.002v.tif",
                "LudwigXV7.020r.tif", "LudwigXV7.020v.tif", "LudwigXV7.A1r.tif", "LudwigXV7.A2v.tif"
        };
        String[] expected = {
                "front matter 2r", "front matter 2v", "20r", "20v", "A1r", "A2v"
        };

        for (int i = 0; i < names.length; i++) {
            assertEquals("Unexpected short name found.", expected[i], parser.shortName(names[i]));
        }
    }

    @Test
    public void checkIsMissingTest() {
        boolean[] expected = {
                true, true, false, false, false, false, false, false
        };

        for (int i = 0; i < TEST_NAMES.length; i++) {
            if (expected[i]) {
                assertTrue("Name should be marked as missing.", parser.isMissing(TEST_NAMES[i]));
            } else {
                assertFalse("Name should not be marked as missing.", parser.isMissing(TEST_NAMES[i]));
            }
        }
    }

    @Test
    public void archiveItemTypeTest() {
        String[] names = {
                "LudwigXV7.130r.tif", "LudwigXV7.001v.tif", "LudwigXV7.crop.txt", "LudwigXV7.description_en.xml",
                "LudwigXV7.description.xml", "LudwigXV7.endmatter.flyleaf.01v.tif",
                "LudwigXV7.images.crop.csv", "LudwigXV7.images.csv", "LudwigXV7.nartag.csv",
                "LudwigXV7.permission_en.html", "LudwigXV7.redtag.txt", "LudwigXV7.SHA1SUM",
                "LudwigXV7.transcription.xml", "LudwigXV7.aor.130r.xml", "LudwigXV7.aor.001v.xml"
        };
        ArchiveItemType[] expected = {
                ArchiveItemType.IMAGE, ArchiveItemType.IMAGE, ArchiveItemType.CROPPING, ArchiveItemType.DESCRIPTION,
                ArchiveItemType.DESCRIPTION_MULTILANG, ArchiveItemType.IMAGE,
                ArchiveItemType.CROPPED_IMAGE_LIST, ArchiveItemType.IMAGE_LIST, ArchiveItemType.NARRATIVE_TAGGING,
                ArchiveItemType.PERMISSION, ArchiveItemType.REDUCED_TAGGING_TXT, ArchiveItemType.SHA1SUM,
                ArchiveItemType.TRANSCRIPTION_ROSE, ArchiveItemType.TRANSCRIPTION_AOR, ArchiveItemType.TRANSCRIPTION_AOR
        };

        for (int i = 0; i < names.length; i++) {
            assertEquals("Unexpected type found.", expected[i], parser.getArchiveItemType(names[i]));
        }
    }

}
