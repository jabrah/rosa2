package rosa.archive.tool;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import rosa.archive.core.ArchiveConstants;
import rosa.archive.core.BaseArchiveTest;
import rosa.archive.core.ByteStreamGroup;
import rosa.archive.core.util.AnnotationLocationMapUtil;
import rosa.archive.model.aor.AorLocation;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * The AORIdMapper acts directly on the byte streams in an archive. We need to verify its behavior
 * of reading the image list and annotation transcription files and writing a single file that
 * contains possible IDs in a collection. This file should include:
 *
 *      - all image IDs
 *      - all 'original' or unmapped image IDs
 *      - all annotation IDs
 *
 * ### Test data as of writing this test is AOR1 era data, so the annotations shouldn't have IDs ###
 * Sadly, current test data has no 'filemap' :(
 */
public class AORIdMapperTest extends BaseArchiveTest {

    private AORIdMapper mapper;

    private ByteArrayOutputStream out;

    @Before
    public void setup() {
        out = new ByteArrayOutputStream();
        mapper = new AORIdMapper(base, new PrintStream(out));
    }

    @Ignore
    @Test
    public void lookForImages() throws Exception {
        mapper.run(VALID_COLLECTION);

        ByteStreamGroup colGroup = base.getByteStreamGroup(VALID_COLLECTION);

        assertTrue("Collection should include the ID map", colGroup.hasByteStream(ArchiveConstants.ID_LOCATION_MAP));

        Map<String, AorLocation> map = AnnotationLocationMapUtil.annotationIdMap(colGroup);

        assertNotNull("Failed to load location map", map);
        assertFalse(map.isEmpty());

        map.entrySet().forEach(entry -> System.out.println(entry.getKey() + " ::: " + entry.getValue()));

        assertEquals((numImages(colGroup) * 2), map.size());
    }

    private long numImages(ByteStreamGroup group) throws Exception {
        long sum = 0;

        for (ByteStreamGroup child : group.listByteStreamGroups()) {
            sum += child.listByteStreamNames().stream().filter(name -> name.endsWith(".tif")).count();
        }

        return sum;
    }

}
