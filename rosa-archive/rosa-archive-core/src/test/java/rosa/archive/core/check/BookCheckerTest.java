package rosa.archive.core.check;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import rosa.archive.core.BaseGuiceTest;
import rosa.archive.core.ByteStreamGroup;
import rosa.archive.model.Book;
import rosa.archive.model.BookCollection;
import rosa.archive.model.BookImage;
import rosa.archive.model.BookMetadata;
import rosa.archive.model.BookScene;
import rosa.archive.model.BookStructure;
import rosa.archive.model.BookText;
import rosa.archive.model.CharacterName;
import rosa.archive.model.CharacterNames;
import rosa.archive.model.CropData;
import rosa.archive.model.CropInfo;
import rosa.archive.model.Illustration;
import rosa.archive.model.IllustrationTagging;
import rosa.archive.model.IllustrationTitles;
import rosa.archive.model.ImageList;
import rosa.archive.model.NarrativeScene;
import rosa.archive.model.NarrativeSections;
import rosa.archive.model.NarrativeTagging;
import rosa.archive.model.Permission;
import rosa.archive.model.SHA1Checksum;
import rosa.archive.model.aor.AnnotatedPage;
import rosa.archive.model.aor.Symbol;
import rosa.archive.model.aor.Underline;
import rosa.archive.model.redtag.StructurePage;
import rosa.archive.model.redtag.StructurePageSide;

/**
* @see rosa.archive.core.check.BookChecker
*/
public class BookCheckerTest extends BaseGuiceTest {

    @Before
    public void setup() throws URISyntaxException, IOException {
        super.setup();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void checkContentTest() throws IOException {
        BookChecker bChecker = new BookChecker(serializers);

        ByteStreamGroup bsg = mock(ByteStreamGroup.class);
        when(bsg.getByteStream(anyString())).thenReturn(
                getClass().getClassLoader().getResourceAsStream("rosedata/LudwigXV7/LudwigXV7.crop.txt")
        );

        assertFalse(bChecker.checkContent(mockCollection(), createBook(), bsg, true, new ArrayList<String>(), new ArrayList<String>()));
        assertFalse(bChecker.checkContent(mockCollection(), createBadBook(), bsg, true, new ArrayList<String>(), new ArrayList<String>()));
        assertFalse(bChecker.checkContent(mockCollection(), createBook(), bsg, false, new ArrayList<String>(), new ArrayList<String>()));
        assertFalse(bChecker.checkContent(mockCollection(), createBadBook(), bsg, false, new ArrayList<String>(), new ArrayList<String>()));
    }

    private Book createBook() {
        Book book = new Book();
        book.setId("LudwigXV7");
        List<String> content = new ArrayList<>();

        // Metadata
        BookMetadata metadata = new BookMetadata();
        metadata.setId("LudwigXV7.description_en.csv");
        metadata.setCommonName("Common Name");
        metadata.setShelfmark("Shelfmark");
        metadata.setYearStart(0);
        metadata.setYearEnd(1);
        metadata.setCurrentLocation("Baltimore, MD");
        metadata.setDate("Date");
        metadata.setDimensions("2x2");
        metadata.setHeight(2);
        metadata.setWidth(2);
        metadata.setMaterial("material");
        metadata.setNumberOfIllustrations(111);
        metadata.setNumberOfPages(10);
        metadata.setOrigin("Origin");
        metadata.setRepository("Repository");
        metadata.setType("Type");

        BookText text = new BookText();
        text.setId("BookTextId");
        text.setFirstPage("001r");
        text.setLastPage("010v");
        text.setNumberOfIllustrations(101);
        text.setNumberOfPages(11);
        text.setColumnsPerPage(2);
        text.setLeavesPerGathering(4);
        text.setLinesPerColumn(50);
        text.setTitle("BookTextTitle");

        metadata.setTexts(new BookText[] {text});

        content.add(metadata.getId());
        book.addBookMetadata(metadata, "en");
        book.addBookMetadata(metadata, "fr");

        // Images
        ImageList images = new ImageList();
        ImageList cropped = new ImageList();
        images.setId("LudwigXV7.images.csv");
        cropped.setId("LudwigXV7.images.crop.csv");
        for (int i = 0; i < 10; i++) {
            BookImage image = new BookImage();
            image.setId("LudwigXV7.00" + (i) + "v.tif");
            image.setMissing(false);
            image.setWidth(100);
            image.setHeight(100);
            images.getImages().add(image);
            BookImage image1 = new BookImage();
            image1.setId("LudwigXV7.00" + (i) + "r.tif");
            image1.setMissing(false);
            image1.setWidth(100);
            image1.setHeight(100);
            images.getImages().add(image1);

            // cropped images marked as missing
            BookImage crop = new BookImage();
            crop.setId("CroppedImage.00" + (i) + "r.tif");
            crop.setMissing(true);
            crop.setHeight(95);
            crop.setWidth(95);
            cropped.getImages().add(crop);

            content.add(image.getId());
            content.add(image1.getId());
        }
        content.add(images.getId());
        content.add(cropped.getId());
        book.setImages(images);
        book.setCroppedImages(cropped);

        // Crop info
        CropInfo cropInfo = new CropInfo();
        cropInfo.setId("LudwigXV7.crop.txt");
        for (int i = 0; i < 10; i++) {
            CropData data = new CropData();
            data.setId("LudwigXV7.00" + (i) + "v.tif");
            data.setLeft(.01);
            data.setRight(.02);
            data.setTop(0.03);
            data.setBottom(.04);

            cropInfo.addCropData(data);
        }
        content.add(cropInfo.getId());
        book.setCropInfo(cropInfo);

        // reduced tagging
        BookStructure structure = new BookStructure();
        structure.setId("LudwigXV7.redtag.csv");
        List<StructurePage> pages = structure.pages();
        for (int i = 1; i < 10; i++) {
            StructurePage page = new StructurePage();
            page.setId(String.valueOf(i));
            page.setName(String.valueOf(i));
            page.setRecto(new StructurePageSide(page.getId() + "r", 10));
            page.setVerso(new StructurePageSide(page.getId() + "v", 10));

            pages.add(page);
        }
        content.add(structure.getId());
        book.setBookStructure(structure);

        // Annotated pages
        List<AnnotatedPage> annotatedPages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            AnnotatedPage page = new AnnotatedPage();
            page.setId("LudwigXV7.AnnoPage" + i + ".xml");
            page.setPage("LudwigXV7.00" + i + "r.tif");
            page.setReader("Harvey Birdman, attorney at law");
            page.setPagination("I don't know what this field will do");

            Underline u1 = new Underline();
            u1.setMethod("UnderlineMethod");
            u1.setReferringText("This is some 'underlined' text.");
            u1.setType("Type");
            page.getUnderlines().add(u1);

            Symbol s1 = new Symbol();
            s1.setName("SymbolName");
            s1.setReferringText("This is some symbol text.");
            page.getSymbols().add(s1);

            annotatedPages.add(page);
            content.add(page.getId());
        }
        book.setAnnotatedPages(annotatedPages);

        // Illustration tagging
        IllustrationTagging ilTag = new IllustrationTagging();
        ilTag.setId("LudwigXV7.imagetag.csv");
        for (int i = 0; i < 15; i++) {
            Illustration ill = new Illustration();
            ill.setId(String.valueOf(i));
            ill.setPage("1v");
            ill.setInitials("Initials");
            ill.setCharacters(new String[] {"1", "2"});
            ill.setTitles(new String[] {"1", "2"});

            ilTag.addIllustrationData(ill);
        }
        content.add(ilTag.getId());
        book.setIllustrationTagging(ilTag);

        // Manual narrative tagging
        NarrativeTagging manNarTag = new NarrativeTagging();
        NarrativeTagging autNarTag = new NarrativeTagging();
        manNarTag.setId("LudwigXV7.nartag.txt");
        autNarTag.setId("LudwigXV7.nartag.csv");
        List<BookScene> manScenes = manNarTag.getScenes();
        List<BookScene> autScenes = autNarTag.getScenes();
        for (int i = 0; i < 10; i++) {
            BookScene scene = new BookScene();
            scene.setId("scene" + i);
            scene.setStartPage("1r");
            scene.setEndPage("3r");
            manScenes.add(scene);

            BookScene auto = new BookScene();
            auto.setId("auto-scene" + i);
            auto.setStartPage("2v");
            auto.setEndPage("4v");
            autScenes.add(auto);
        }
        content.add(manNarTag.getId());
        content.add(autNarTag.getId());
        book.setManualNarrativeTagging(manNarTag);
        book.setAutomaticNarrativeTagging(autNarTag);

        // Permissions
        Permission perm_en = new Permission();
        perm_en.setId("LudwigXV7.permission_en.html");
        perm_en.setPermission("This is an english permission statement.");

        Permission perm_fr = new Permission();
        perm_fr.setId("LudwigXV7.permission_fr.html");
        perm_fr.setPermission("This is a french permission statement");

        content.add(perm_en.getId());
        content.add(perm_fr.getId());
        book.addPermission(perm_en, "en");
        book.addPermission(perm_fr, "fr");

        // Checksum info
        boolean first = true;
        Collections.sort(content);

        SHA1Checksum checksums = new SHA1Checksum();
        checksums.setId("LudwigXV7.SHA1SUM");
        for (String filename : content) {
            String hash;
            if (first) {
                hash = "1ae91bed8acf6df12710743b39b143905abbc8aa";
                first = false;
            } else
                hash = "da39a3ee5e6b4b0d3255bfef95601890afd80709";

            checksums.checksums().put(filename, hash);
        }
        content.add(checksums.getId());
        book.setSHA1Checksum(checksums);

        book.setContent(content.toArray(new String[content.size()]));
        return book;
    }

    private Book createBadBook() {
        Book badBook = new Book();
        List<String> content = new ArrayList<>();

        // Metadata
        BookMetadata metadata = new BookMetadata();
        metadata.setCommonName("Common Name");

        BookText text = new BookText();
        text.setTitle("BookTextTitle");
        metadata.setTexts(new BookText[] {text});

        badBook.addBookMetadata(metadata, "asdf");

        // Images
        ImageList images = new ImageList();
        ImageList cropped = new ImageList();
        for (int i = 0; i < 10; i++) {
            BookImage image = new BookImage();
            image.setId("BookImage" + i);
            images.getImages().add(image);

            BookImage crop = new BookImage();
            crop.setId("CroppedImage" + i);
            crop.setMissing(true);
            cropped.getImages().add(crop);

            content.add(image.getId());
            // Source of errors (10)
            content.add(crop.getId());
        }
        badBook.setImages(images);
        badBook.setCroppedImages(cropped);

        // Checksum info
        boolean first = true;
        Collections.sort(content);

        SHA1Checksum checksums = new SHA1Checksum();
        checksums.setId("LudwigXV7.SHA1SUM");
        for (String filename : content) {
            String hash;
            if (first) {
                hash = "1ae91bed8acf6df12710743b39b143905abbc8aa";
                first = false;
            } else
                hash = "da39a3ee5e6b4b0d3255bfef95601890afd80709";

            checksums.checksums().put(filename, hash);
        }
        content.add(checksums.getId());
        badBook.setSHA1Checksum(checksums);

        badBook.setContent(content.toArray(new String[content.size()]));
        return badBook;
    }

    private BookCollection mockCollection() {
        BookCollection collection = new BookCollection();

        collection.setId("rosedata");
        collection.setLanguages(new String[] {"en", "fr"});

        NarrativeSections sections = new NarrativeSections();
        List<NarrativeScene> scenes = sections.asScenes();
        for (int i = 0; i < 10; i++) {
            NarrativeScene scene = new NarrativeScene();
            scene.setId("scene" + i);

            NarrativeScene scene1 = new NarrativeScene();
            scene1.setId("auto-scene" + i);

            scenes.add(scene);
            scenes.add(scene1);
        }
        collection.setNarrativeSections(sections);

        IllustrationTitles titles = new IllustrationTitles();
        Map<String, String> titleData = new HashMap<>();
        titleData.put("1", "1");
        titleData.put("2", "2");
        titles.setData(titleData);
        collection.setIllustrationTitles(titles);

        CharacterNames names = new CharacterNames();
        CharacterName name1 = new CharacterName();
        name1.addName("1", "en");
        CharacterName name2 = new CharacterName();
        name2.addName("2", "en");

        names.addCharacterName(name1);
        names.addCharacterName(name2);

        return collection;
    }

}
