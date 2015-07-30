package rosa.archive.aor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import rosa.archive.core.ResourceUtil;
import rosa.archive.core.util.CSV;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GitStatsCollectorTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    protected Path basePath;

    private GitStatCollector collector;

    @Before
    public void setup() throws Exception {
        collector = new GitStatCollector();

        Path temp = tempFolder.newFolder().toPath();
        ResourceUtil.copyResource(getClass(), "/archive", temp);

        basePath = temp.resolve("archive");
    }

    @Test
    public void collectGitStatsTest() throws Exception {
        Path output = basePath.getParent();

        // Do stats collection on test repository
        collector.setOutputDirectory(output.toString());
        collector.collectGitStats("https://github.com/jabrah/test-transcriptions.git");

        // Check output files
        assertTrue("books.csv not found.", Files.exists(output.resolve("books.csv")));
        assertTrue("commits.csv not found.", Files.exists(output.resolve("commits.csv")));

        checkCommitsCsv(output.resolve("commits.csv"));
        checkBooksCsv(output.resolve("books.csv"));
    }

    private void checkCommitsCsv(Path pathToCsv) throws IOException {
        assertTrue("File not found.", Files.exists(pathToCsv));

        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(pathToCsv))) {
            String[][] table = CSV.parseTable(reader);
//print(table);
            assertNotNull(table);
            assertEquals("Unexpected number of rows.", 8, table.length);
            assertEquals("Unexpected number of columns", 11, table[0].length);
        }
    }

    private void checkBooksCsv(Path pathToCsv) throws IOException {
        assertTrue("File not found.", Files.exists(pathToCsv));

        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(pathToCsv))) {
            String[][] table = CSV.parseTable(reader);

            assertNotNull(table);
            assertEquals("Unexpected number of rows.", 15, table.length);
            assertEquals("Unexpected number of columns", 17, table[0].length);
        }
    }

    private void print(String[][] table) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            CSV.write(new OutputStreamWriter(out), table);
            System.out.println(out);
        } catch (IOException e) {
            System.err.println("Failed to write table.");
        }
    }

    /**
     * Gather stats for the AoR repository in an early state.
     *
     * In resources, there exists a copy of the AoR repository in an early state.
     * The stats gathering should show that there exist 3 books with a set number
     * of annotations in each. Since this repository in test/resources is not a
     * real git repository, no git commands can be done on it.
     *
     * @throws Exception
     */
    @Test
    public void collectBookStatsTest() throws Exception {
        BookStats stats = collector.collectBookStats(basePath);

        assertNotNull(stats);
        assertEquals("There should be exactly 3 books counted.", 3, stats.statsMap.size());

        assertNotNull("Book Domenichi not found.", stats.statsMap.get("Domenichi"));
        assertNotNull("Book Castiglione not found.", stats.statsMap.get("Castiglione"));
        assertNotNull("Book Livy not found.", stats.statsMap.get("Livy"));

        assertEquals("Unexpected number of annotations found for Domenichi.",
                1449, stats.statsMap.get("Domenichi").totalAnnotations());
        assertEquals("Unexpected number of annotions found for Castiglione.",
                17, stats.statsMap.get("Castiglione").totalAnnotations());
        assertEquals("Unexpected number of annotations found for Livy.",
                1587, stats.statsMap.get("Livy").totalAnnotations());
    }

}
