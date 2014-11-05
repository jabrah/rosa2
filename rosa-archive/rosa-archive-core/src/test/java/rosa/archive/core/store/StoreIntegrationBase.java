package rosa.archive.core.store;

import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import rosa.archive.core.ArchiveCoreModule;
import rosa.archive.core.ByteStreamGroup;
import rosa.archive.core.FSByteStreamGroup;
import rosa.archive.core.GuiceJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ArchiveCoreModule.class})
public abstract class StoreIntegrationBase {
    public final String COLLECTION = "collection";
    public final String BOOK = "LudwigXV7";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Inject
    private StoreFactory storeFactory;
    protected Store store;

    protected Path defaultPath;
    protected File folder;

    public Path testCollection;
    public Path testBook;

    @Before
    public void setup() throws URISyntaxException, IOException {
        URL url = getClass().getClassLoader().getResource("data/LudwigXV7");
        assertNotNull(url);
        defaultPath = Paths.get(url.toURI());
        assertNotNull(defaultPath);

        folder = tempFolder.newFolder();

        ByteStreamGroup tGroup = new FSByteStreamGroup(folder.toString());

        store = storeFactory.create(tGroup);
        assertNotNull(store);

        testCollection = Files.createDirectory(folder.toPath().resolve(COLLECTION));
        testBook = Files.createDirectory(testCollection.resolve(BOOK));
    }

    protected void copyMissingImage(Path originalPath, Path collectionPath) throws IOException {
        if (Files.exists(originalPath.resolve("missing_image.tif"))) {
            Files.copy(
                    originalPath.resolve("missing_image.tif"),
                    collectionPath.resolve("missing_image.tif")
            );
        }
    }

    /**
     * Copy all files from the original path (data/LudwigXV7) to a new book in the temporary folder,
     * except for images.csv and images.crop.csv. Directories in {@param originalPath} are not copied.
     *
     * @throws IOException
     */
    protected void copyTestFiles(Path originalPath, Path bookPath) throws IOException {
        // Copy test files to tmp directory
        copyMissingImage(originalPath.getParent(), bookPath.getParent());

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(originalPath, new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return Files.isRegularFile(entry);
            }
        })) {
            for (Path path : ds) {
                try (InputStream in = Files.newInputStream(path)) {
                    String name = path.getFileName().toString();
                    Path filePath = bookPath.resolve(name);

                    if (name.endsWith("images.csv")) {
                        System.out.println("Readable? " + Files.isReadable(path));
                        System.out.println("---------- ORIGINAL : " + name + " ----------");
//                        List<String> lines = Files.readAllLines(path, Charset.forName("UTF-8"));
//                        List<String> lines = IOUtils.readLines(in, Charset.forName("UTF-8"));
                        byte[] bytes = Files.readAllBytes(path);
                        System.out.println(new String(bytes, "UTF-8"));
//                        for (String l : lines) {
//                            System.out.println(l);
//                        }
                        System.out.println("---------- END ----------");
                    }

                    Files.copy(in, filePath);
                    assertTrue(Files.exists(filePath));
                    assertTrue(Files.isRegularFile(filePath));
                }
            }
        }
    }

    protected void remove(Path basePath, String toBeRemoved) throws IOException {
        remove(basePath.resolve(toBeRemoved));
    }

    protected void remove(Path toBeRemoved) throws IOException {
        Files.deleteIfExists(toBeRemoved);
    }

}
