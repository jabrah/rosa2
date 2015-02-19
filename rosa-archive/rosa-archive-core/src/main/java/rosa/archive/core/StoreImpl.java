package rosa.archive.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import rosa.archive.core.check.BookChecker;
import rosa.archive.core.check.BookCollectionChecker;
import rosa.archive.core.serialize.SerializerSet;
import rosa.archive.core.util.BookImageComparator;
import rosa.archive.core.util.ChecksumUtil;
import rosa.archive.core.util.CropRunnable;
import rosa.archive.model.ArchiveItemType;
import rosa.archive.model.Book;
import rosa.archive.model.BookCollection;
import rosa.archive.model.BookImage;
import rosa.archive.model.BookImageLocation;
import rosa.archive.model.BookImageRole;
import rosa.archive.model.BookMetadata;
import rosa.archive.model.BookReferenceSheet;
import rosa.archive.model.BookStructure;
import rosa.archive.model.CharacterNames;
import rosa.archive.model.CropData;
import rosa.archive.model.CropInfo;
import rosa.archive.model.FileMap;
import rosa.archive.model.HasId;
import rosa.archive.model.HashAlgorithm;
import rosa.archive.model.IllustrationTagging;
import rosa.archive.model.IllustrationTitles;
import rosa.archive.model.ImageList;
import rosa.archive.model.ReferenceSheet;
import rosa.archive.model.NarrativeSections;
import rosa.archive.model.NarrativeTagging;
import rosa.archive.model.Permission;
import rosa.archive.model.SHA1Checksum;
import rosa.archive.model.Transcription;
import rosa.archive.model.aor.AnnotatedPage;
import rosa.archive.model.meta.MultilangMetadata;

import com.google.inject.Inject;

/**
 *
 */
public class StoreImpl implements Store, ArchiveConstants {
    private static final ArchiveNameParser parser = new ArchiveNameParser();

    private final SerializerSet serializers;
    private final ByteStreamGroup base;
    private final BookCollectionChecker collectionChecker;
    private final BookChecker bookChecker;

    /**
     * @param serializers object containing all required serializers
     * @param bookChecker object that knows how to validate the contents of a book
     * @param collectionChecker knows how to validate contents of a book collection
     * @param base byte stream group representing the base of the archive
     */
    @Inject
    public StoreImpl(SerializerSet serializers, BookChecker bookChecker, BookCollectionChecker collectionChecker,
            ByteStreamGroup base) {
        this.serializers = serializers;
        this.base = base;
        this.collectionChecker = collectionChecker;
        this.bookChecker = bookChecker;
    }

    @Override
    public String[] listBookCollections() throws IOException {
        return base.listByteStreamGroupNames().toArray(new String[] {});
    }

    @Override
    public String[] listBooks(String collectionId) throws IOException {
        ByteStreamGroup collection = base.getByteStreamGroup(collectionId);
        return collection.listByteStreamGroupNames().toArray(new String[] {});
    }

    @Override
    public BookCollection loadBookCollection(String collectionId, List<String> errors) throws IOException {
        errors = nonNullList(errors);
        ByteStreamGroup collectionGroup = base.getByteStreamGroup(collectionId);
        BookCollection collection = new BookCollection();

        collection.setId(collectionId);
        collection.setBooks(listBooks(collectionId));
        collection.setCharacterNames(loadItem(CHARACTER_NAMES, collectionGroup, CharacterNames.class, errors));
        collection.setIllustrationTitles(loadItem(ILLUSTRATION_TITLES, collectionGroup, IllustrationTitles.class,
                errors));
        collection.setNarrativeSections(loadItem(NARRATIVE_SECTIONS, collectionGroup, NarrativeSections.class, errors));
        collection.setChecksum(loadItem(collectionId + SHA1SUM, collectionGroup, SHA1Checksum.class, errors));
        // Add AoR collection data, if applicable
        collection.setPeopleRef(loadItem(PEOPLE, collectionGroup, ReferenceSheet.class, errors));
        collection.setLocationsRef(loadItem(LOCATIONS, collectionGroup, ReferenceSheet.class, errors));
        collection.setBooksRef(loadItem(BOOKS, collectionGroup, BookReferenceSheet.class, errors));

        BookImage missing = new BookImage();
        missing.setId(MISSING_IMAGE);
        missing.setMissing(false);

        int[] missingDimensions = getMissingImageDimensions(collectionId);
        missing.setWidth(missingDimensions[0]);
        missing.setHeight(missingDimensions[1]);

        collection.setMissingImage(missing);

        // Languages from configuration.
        Properties props = new Properties();
        try (InputStream configIn = collectionGroup.getByteStream("config.properties")) {
            props.load(configIn);
        }
        String langs = props.getProperty("languages");
        if (langs != null) {
            collection.setLanguages(langs.split(","));
        }

        return collection;
    }

    @Override
    public Book loadBook(BookCollection collection, String bookId, List<String> errors) throws IOException {
        errors = nonNullList(errors);
        ByteStreamGroup byteStreams = base.getByteStreamGroup(collection.getId());

        if (!byteStreams.hasByteStreamGroup(bookId)) {
            errors.add("Unable to find book. [" + bookId + "]");
            return null;
        }

        ByteStreamGroup bookStreams = byteStreams.getByteStreamGroup(bookId);
        Book book = new Book();

        book.setId(bookId);
        book.setImages(loadItem(bookId + IMAGES, bookStreams, ImageList.class, errors));
        book.setCroppedImages(loadItem(bookId + IMAGES_CROP, bookStreams, ImageList.class, errors));
        book.setCropInfo(loadItem(bookId + CROP, bookStreams, CropInfo.class, errors));
        book.setBookStructure(loadItem(bookId + REDUCED_TAGGING, bookStreams, BookStructure.class, errors));
        book.setChecksum(loadItem(bookId + SHA1SUM, bookStreams, SHA1Checksum.class, errors));
        book.setIllustrationTagging(loadItem(bookId + IMAGE_TAGGING, bookStreams, IllustrationTagging.class, errors));
        book.setManualNarrativeTagging(loadItem(bookId + NARRATIVE_TAGGING_MAN, bookStreams, NarrativeTagging.class,
                errors));
        book.setAutomaticNarrativeTagging(loadItem(bookId + NARRATIVE_TAGGING, bookStreams, NarrativeTagging.class,
                errors));
        book.setTranscription(loadItem(bookId + TRANSCRIPTION + XML_EXT, bookStreams, Transcription.class, errors));
        book.setMultilangMetadata(loadItem(bookId + ".description.xml", bookStreams, MultilangMetadata.class, errors));

        List<String> content = bookStreams.listByteStreamNames();
        book.setContent(content.toArray(new String[]{}));

        // For all image lists, add in dimensions of missing images
        setMissingDimensions(book.getImages(), collection.getMissingImage());
        setMissingDimensions(book.getCroppedImages(), collection.getMissingImage());

        List<AnnotatedPage> pages = book.getAnnotatedPages();

        // Handle permission and description in all languages
        for (String lang : collection.getAllSupportedLanguages()) {
            String perm_name = bookId + PERMISSION + lang + HTML_EXT;
            book.addPermission(loadItem(perm_name, bookStreams, Permission.class, errors), lang);
            
            String descr_name = bookId + DESCRIPTION + lang + XML_EXT;
            book.addBookMetadata(loadItem(descr_name, bookStreams, BookMetadata.class, errors), lang);
        }

        // Handle AoR annotations
        for (String name : content) {
            if (parser.getArchiveItemType(name) == ArchiveItemType.TRANSCRIPTION_AOR) {
                pages.add(loadItem(name, bookStreams, AnnotatedPage.class, errors));
            }
        }

        return book;
    }

    private void setMissingDimensions(ImageList images, BookImage missingImage) {
        if (missingImage == null || images == null) {
            return;
        }

        for (BookImage image : images) {
            if (image.isMissing()) {
//                image.setId(missingImage.getId());
                image.setWidth(missingImage.getWidth());
                image.setHeight(missingImage.getHeight());
                image.setMissing(true);
            }
        }
    }

    @Override
    public boolean check(BookCollection collection, Book book, boolean checkBits, List<String> errors,
            List<String> warnings) {
        errors = nonNullList(errors);
        warnings = nonNullList(warnings);
        if (base.hasByteStreamGroup(collection.getId())) {
            ByteStreamGroup collectionGroup = base.getByteStreamGroup(collection.getId());
            if (collectionGroup.hasByteStreamGroup(book.getId())) {
                ByteStreamGroup bookGroup = collectionGroup.getByteStreamGroup(book.getId());
                return bookChecker.checkContent(collection, book, bookGroup, checkBits, errors, warnings);
            }
        }
        errors.add("Unable to find book. [" + book.getId() + "]");
        return false;
    }

    @Override
    public boolean check(BookCollection collection, boolean checkBits, List<String> errors, List<String> warnings) {
        errors = nonNullList(errors);
        warnings = nonNullList(warnings);
        return collectionChecker.checkContent(collection, base.getByteStreamGroup(collection.getId()), checkBits,
                errors, warnings);
    }

    @Override
    public boolean updateChecksum(String collection, boolean force, List<String> errors) throws IOException {
        errors = nonNullList(errors);
        BookCollection col = loadBookCollection(collection, errors);
        return col != null && updateChecksum(col, force, errors);
    }

    @Override
    public boolean updateChecksum(BookCollection collection, boolean force, List<String> errors) throws IOException {
        errors = nonNullList(errors);

        SHA1Checksum checksums = collection.getChecksum();
        // If SHA1SUM does not exist, create it!
        if (checksums == null) {
            checksums = new SHA1Checksum();
            checksums.setId(collection.getId() + SHA1SUM);
        }

        ByteStreamGroup collectionStreams = base.getByteStreamGroup(collection.getId());

        return updateChecksum(checksums, collectionStreams, force, errors);
    }

    @Override
    public boolean updateChecksum(String collection, String book, boolean force, List<String> errors)
            throws IOException {
        errors = nonNullList(errors);
        BookCollection col = loadBookCollection(collection, errors);
        Book b = loadBook(col, book, errors);
        return col != null && b != null && updateChecksum(col, b, force, errors);
    }

    @Override
    public void generateAndWriteImageList(String collection, String book, boolean force, List<String> errors)
            throws IOException {
        errors = nonNullList(errors);

        if (!base.hasByteStreamGroup(collection)) {
            errors.add("Collection not found in directory. [" + base.id() + "]");
            return;
        } else if (!base.getByteStreamGroup(collection).hasByteStreamGroup(book)) {
            errors.add("Book not found in collection. [" + collection + "]");
            return;
        }

        ByteStreamGroup bookStreams = base.getByteStreamGroup(collection).getByteStreamGroup(book);

        if (!force && bookStreams.hasByteStream(book + IMAGES)) {
            errors.add("[" + book + IMAGES + "] already exists. You can force this operation"
                    + " to update the existing image list.");
            return;
        }

        ImageList list = new ImageList();
        list.setId(book + IMAGES);
        list.setImages(buildImageList(collection, book, true, bookStreams));

        writeItem(list, bookStreams, ImageList.class, errors);
    }

    @Override
    public boolean updateChecksum(BookCollection collection, Book book, boolean force, List<String> errors)
            throws IOException {
        errors = nonNullList(errors);

        SHA1Checksum checksums = book.getChecksum();
        if (checksums == null) {
            checksums = new SHA1Checksum();
            checksums.setId(book.getId() + SHA1SUM);
        }

        ByteStreamGroup colStreams = base.getByteStreamGroup(collection.getId());
        if (colStreams == null || !colStreams.hasByteStreamGroup(book.getId())) {
            return false;
        }
        ByteStreamGroup bookStreams = colStreams.getByteStreamGroup(book.getId());

        return updateChecksum(checksums, bookStreams, force, errors);
    }

    @Override
    public void generateAndWriteCropList(String collection, String book, boolean force, List<String> errors)
            throws IOException {
        errors = nonNullList(errors);

        if (!base.hasByteStreamGroup(collection)) {
            errors.add("Collection not found in directory. [" + base.id() + "]");
            return;
        } else if (!base.getByteStreamGroup(collection).hasByteStreamGroup(book)) {
            errors.add("Book not found in collection. [" + collection + "]");
            return;
        }

        ByteStreamGroup bookStreams = base.getByteStreamGroup(collection).getByteStreamGroup(book);

        if (!bookStreams.hasByteStreamGroup(CROPPED_DIR)) {
            errors.add("No cropped images found. [" + collection + ":" + book + "]");
            return;
        } else if (!force && bookStreams.hasByteStream(book + IMAGES_CROP)) {
            errors.add("[" + book + IMAGES_CROP + "] already exists. You can force this operation"
                    + " to update the existing image list.");
            return;
        }

        ImageList list = new ImageList();
        list.setId(book + IMAGES_CROP);
        list.setImages(buildImageList(collection, book, false, bookStreams.getByteStreamGroup(CROPPED_DIR)));

        writeItem(list, bookStreams, ImageList.class, errors);
    }

    @Override
    public void cropImages(String collection, String book, boolean force, List<String> errors) throws IOException {
        errors = nonNullList(errors);

        if (!base.hasByteStreamGroup(collection)) {
            errors.add("Collection not found in directory. [" + base.id() + "]");
            return;
        } else if (!base.getByteStreamGroup(collection).hasByteStreamGroup(book)) {
            errors.add("Book not found in collection. [" + collection + "]");
            return;
        }

        // Load the book
        ByteStreamGroup bookStreams = base.getByteStreamGroup(collection).getByteStreamGroup(book);
        BookCollection col = loadBookCollection(collection, errors);       
        Book b = loadBook(col, book, errors);

        if (!force && b.getCroppedImages() != null && bookStreams.hasByteStreamGroup(CROPPED_DIR)) {
            errors.add("Cropped images already exist for this book. [" + collection + ":" + book
                    + "]. Force overwrite with '-force'");
            return;
        }

        // Create the cropped/ directory
        ByteStreamGroup cropGroup = bookStreams.newByteStreamGroup(CROPPED_DIR);

        CropInfo cropInfo = b.getCropInfo();
        ImageList images = b.getImages();

        // Crop images using ImageMagick, 4 at a time
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        for (BookImage image : images) {
            if (image.isMissing()) {
                continue;
            }

            CropData cropping = cropInfo.getCropDataForPage(image.getId());
            if (cropping == null) {
                errors.add("Image missing from cropping information, copying old file. [" + image.getId() + "]");
                bookStreams.copyByteStream(image.getId(), cropGroup);
                continue;
            }

            Runnable cropper = new CropRunnable(bookStreams.id(), image, cropping, CROPPED_DIR, errors);
            executorService.execute(cropper);
        }
        executorService.shutdown();

        try {
            executorService.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            errors.add("Cropping was interrupted!\n" + stacktrace(e));
        }
    }

    @Override
    public void generateFileMap(String collection, String book, String newId, boolean hasFrontCover, boolean hasBackCover,
                                int numFrontmatter, int numEndmatter, int numMisc, List<String> errors) throws IOException {
        errors = nonNullList(errors);

        if (!base.hasByteStreamGroup(collection)) {
            errors.add("Collection not found in directory. [" + base.id() + "]");
            return;
        } else if (!base.getByteStreamGroup(collection).hasByteStreamGroup(book)) {
            errors.add("Book not found in collection. [" + collection + "]");
            return;
        }

        ByteStreamGroup bookStreams = base.getByteStreamGroup(collection).getByteStreamGroup(book);

        FileMap fileMap = generateFileMap(getImageNames(bookStreams), newId, numFrontmatter,
                numEndmatter, numMisc, hasFrontCover, hasBackCover);
        fileMap.setId(FILE_MAP);
        
        writeItem(fileMap, bookStreams, FileMap.class, errors);
    }

    @Override
    public void validateXml(String collection, String book, List<String> errors, List<String> warnings) throws IOException {
        errors = nonNullList(errors);
        warnings = nonNullList(warnings);

        if (!base.hasByteStreamGroup(collection)) {
            errors.add("Collection not found in directory. [" + base.id() + "]");
            return;
        } else if (!base.getByteStreamGroup(collection).hasByteStreamGroup(book)) {
            errors.add("Book not found in collection. [" + collection + "]");
            return;
        }

        ByteStreamGroup bookStreams = base.getByteStreamGroup(collection).getByteStreamGroup(book);
        for (String file : bookStreams.listByteStreamNames()) {
            if (parser.getArchiveItemType(file) == ArchiveItemType.TRANSCRIPTION_AOR) {
                bookChecker.validateAgainstSchema(file, bookStreams, errors, warnings);
            }
        }
    }

    @Override
    public void renameImages(String collection, String book, boolean changeId, boolean reverse, List<String> errors)
            throws IOException {
        errors = nonNullList(errors);

        if (!base.hasByteStreamGroup(collection)) {
            errors.add("Collection not found in directory. [" + base.id() + "]");
            return;
        } else if (!base.getByteStreamGroup(collection).hasByteStreamGroup(book)) {
            errors.add("Book not found in collection. [" + collection + "]");
            return;
        }

        ByteStreamGroup bookStreams = base.getByteStreamGroup(collection).getByteStreamGroup(book);

        if (!bookStreams.hasByteStream(FILE_MAP) && !changeId) {
            errors.add("No file map found. Cannot change image names.");
            return;
        } else if (changeId) {
            for (String image : getImageNames(bookStreams)) {
                String target = bookStreams.name() + image.substring(image.indexOf('.'));

                // Skip if the names are the same somehow
                if (target.equals(image)) {
                    continue;
                }

                bookStreams.renameByteStream(image, target);
            }
            return;
        }

        // TODO checking file map should happen in the BookChecker!
        // This would involve including the file map in the Book model object
        FileMap fileMap = loadItem(FILE_MAP, bookStreams, FileMap.class, errors);

        List<String> dups = new ArrayList<>();
        if (containsDuplicateValues(fileMap.getMap(), dups)) {
            StringBuilder sb = new StringBuilder("Duplicate target names found.\n");
            for (String s : dups) {
                sb.append("  ");
                sb.append(s);
                sb.append("\n");
            }

            errors.add(sb.toString());
            return;
        }
        for (String from : getImageNames(bookStreams)) {
            String to = fileMap.getMap().get(from);
            if (to == null || to.isEmpty()) {
                continue;
            }

            if (reverse) {
                bookStreams.renameByteStream(to, from);
            } else {
                bookStreams.renameByteStream(from, to);
            }
        }
    }

    private boolean containsDuplicateValues(Map<String, String> map, List<String> duplicates) {
        duplicates = nonNullList(duplicates);
        Set<String> valueSet = new HashSet<>();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (valueSet.contains(entry.getValue())) {
                duplicates.add(entry.getKey() + "," + entry.getValue());
                continue;
            }

            valueSet.add(entry.getValue());
        }

        return !duplicates.isEmpty();
    }

    // TODO clean up
    @Override
    public void renameTranscriptions(String collection, String book, List<String> errors) throws IOException {
        errors = nonNullList(errors);

        if (!base.hasByteStreamGroup(collection)) {
            errors.add("Collection not found in directory. [" + base.id() + "]");
            return;
        } else if (!base.getByteStreamGroup(collection).hasByteStreamGroup(book)) {
            errors.add("Book not found in collection. [" + collection + "]");
            return;
        }

        ByteStreamGroup bookStreams = base.getByteStreamGroup(collection).getByteStreamGroup(book);
        if (!bookStreams.hasByteStream(FILE_MAP)) {
            errors.add("No file map found.");
            return;
        }

        FileMap fileMap = loadItem(FILE_MAP, bookStreams, FileMap.class, errors);
        for (String name : getTranscriptionsNames(bookStreams)) {
            AnnotatedPage aPage = loadItem(name, bookStreams, AnnotatedPage.class, errors);
            String referencePage = aPage.getPage();

            String imageName = fileMap.getMap().get(referencePage);
            if (imageName == null || imageName.isEmpty()) {
                continue;
            }

            List<String> parts = new ArrayList<>(Arrays.asList(imageName.split("\\.")));
            parts.add(1, ArchiveItemType.TRANSCRIPTION_AOR.getIdentifier());

            StringBuilder sb = new StringBuilder();
            boolean isFirst = true;
            for (String str : parts) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append('.');
                }
                sb.append(str);
            }
            String command = "sed -i s/" + referencePage + "/" + imageName + "/ " + bookStreams.id() + "/" + name;

            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec(command);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtils.copy(proc.getErrorStream(), out);

            String err = out.toString(UTF_8.name());
            if (err != null && !err.isEmpty()) {
                errors.add(err);
            } else {
                // If no errors, rename the transcription file
                bookStreams.renameByteStream(name, sb.toString().replace(TIF_EXT, XML_EXT));
            }
        }
    }

    private List<String> getTranscriptionsNames(ByteStreamGroup bookStreams) throws IOException {
        List<String> names = new ArrayList<>();

        for (String name : bookStreams.listByteStreamNames()) {
            ArchiveItemType type = parser.getArchiveItemType(name);
            if (name.endsWith(XML_EXT) && type != ArchiveItemType.TRANSCRIPTION_ROSE
                    && type != ArchiveItemType.DESCRIPTION && type != ArchiveItemType.DESCRIPTION_MULTILANG) {
                names.add(name);
            }
        }

        Collections.sort(names);
        return names;
    }

    /**
     * @param bookStreams byte stream group for a book
     * @return list of names of all images in the book
     * @throws IOException
     */
    private List<String> getImageNames(ByteStreamGroup bookStreams) throws IOException {
        List<String> filenames = new ArrayList<>();
        for (String name : bookStreams.listByteStreamNames()) {
            if (parser.getArchiveItemType(name) == ArchiveItemType.IMAGE) {
                filenames.add(name);
            }
        }

        Collections.sort(filenames);
        return filenames;
    }

    /**
     * @param fileNames list of name of relevant files
     * @param id files will be renamed using this ID
     * @param frontmatter number of frontmatter pages
     * @param endmatter number of endmatter pages
     * @param misc number of misc pages
     * @param hasFront does the book have a front cover?
     * @param hasBack does the book have a back cover?
     * @return list of CSV lines that can be written to a file
     */
    private FileMap generateFileMap(List<String> fileNames, String id, int frontmatter, int endmatter,
                                    int misc, boolean hasFront, boolean hasBack) {
        // assume front/back cover + pastedown!
        Map<String, String> map = new HashMap<>();

        if (hasFront) {
            map.put(fileNames.get(0), id + IMG_FRONTCOVER);
            map.put(fileNames.get(1), id + IMG_FRONTPASTEDOWN);
        }

        int total = fileNames.size();
        int front_flyleaf_end = hasFront ? 2 + frontmatter : frontmatter;
        int end_flyleaf_end = hasBack ? total - misc - 2 : total - misc;
        int end_flyleaf_start = end_flyleaf_end - endmatter;

        int nextseq = 1;
        char nextrv = 'r';
        int i;
        for (i = hasFront ? 2 : 0; i < front_flyleaf_end; i++) {
            map.put(fileNames.get(i), id + IMG_FRONT_FLYLEAF + String.format("%03d", nextseq) + nextrv + TIF_EXT);

            if (nextrv == 'v') {
                nextseq++;
                nextrv = 'r';
            } else {
                nextrv = 'v';
            }
        }

        nextseq = 1;
        nextrv = 'r';
        for (i = front_flyleaf_end; i < end_flyleaf_start; i++) {
            map.put(fileNames.get(i), id + "." + String.format("%03d", nextseq) + nextrv + TIF_EXT);

            if (nextrv == 'v') {
                nextseq++;
                nextrv = 'r';
            } else {
                nextrv = 'v';
            }
        }

        nextseq = 1;
        nextrv = 'r';
        for (i = end_flyleaf_start; i < end_flyleaf_end; i++) {
            map.put(fileNames.get(i), id + IMG_END_FLYLEAF + String.format("%03d", nextseq) + nextrv + TIF_EXT);

            if (nextrv == 'v') {
                nextseq++;
                nextrv = 'r';
            } else {
                nextrv = 'v';
            }
        }

        if (hasBack) {
            map.put(fileNames.get(i++), id + IMG_ENDPASTEDOWN);
            map.put(fileNames.get(i), id + IMG_BACKCOVER);
        }

        for (i = end_flyleaf_end + 2; i < total; i++) {
            map.put(fileNames.get(i), id + ".misc.LABEL.tif");
        }

        FileMap fileMap = new FileMap();
        fileMap.setMap(map);

        return fileMap;
    }

    /**
     * Inspect a list of images and add images that are missing.
     * 
     * Some images seem to be required, but can sometimes be missing: front/back
     * cover, front/back pastedown
     * 
     * @param book
     *            book ID
     * @param images
     *            list of images in the book
     */
    private void addMissingImages(String book, List<BookImage> images, int[] missingDimensions) {
        if (images == null || missingDimensions == null || missingDimensions.length != 2) {
            return;
        }

        if (images.size() < 2) {
            String[] requiredPages = { book + IMG_FRONTCOVER, book + IMG_FRONTPASTEDOWN,
                    book + IMG_FRONT_FLYLEAF + "001r.tif", book + IMG_FRONT_FLYLEAF + "001v.tif",
                    book + IMG_ENDPASTEDOWN, book + IMG_BACKCOVER };

            for (String name : requiredPages) {
                BookImage image = new BookImage();
                image.setId(name);
                image.setWidth(missingDimensions[0]);
                image.setHeight(missingDimensions[1]);
                image.setMissing(true);

                if (!images.contains(image)) {
                    images.add(image);
                }
            }
            return;
        }
        // front/back covers can be missing
        BookImage img = images.get(0);
        if (img.getLocation() == BookImageLocation.BINDING && img.getRole() == BookImageRole.FRONT_COVER) {
            images.add(0, new BookImage(book + IMG_FRONTCOVER, missingDimensions[0], missingDimensions[1], true));
        }
        img = images.get(1);
        if (img.getLocation() == BookImageLocation.FRONT_MATTER && img.getRole() == BookImageRole.PASTEDOWN) {
            images.add(1, new BookImage(book + IMG_FRONTPASTEDOWN, missingDimensions[0], missingDimensions[1], true));
        }
        // front/back pastedown can be missing
        img = images.get(images.size() - 2);
        if (img.getLocation() == BookImageLocation.END_MATTER && img.getRole() == BookImageRole.PASTEDOWN) {
            images.add(images.size(), new BookImage(book + IMG_ENDPASTEDOWN, missingDimensions[0],
                    missingDimensions[1], true));
        }
        img = images.get(images.size() - 1);
        if (img.getLocation() == BookImageLocation.BINDING && img.getRole() == BookImageRole.BACK_COVER) {
            images.add(images.size(), new BookImage(book + IMG_BACKCOVER, missingDimensions[0], missingDimensions[1],
                    true));
        }

        // If images were skipped over, add those
        addSkippedImages(images, missingDimensions);

        // Flyleaves, if present, must end in 'v' and have at minimum 1r & 1v
        int frontFlyleafIndex = lastIndexOfPrefix(book + IMG_FRONT_FLYLEAF, images);
        if (frontFlyleafIndex != -1) {
            img = images.get(frontFlyleafIndex);
            if (img.getId().endsWith("r.tif")) {
                images.add(frontFlyleafIndex + 1, new BookImage(img.getId().substring(0, img.getId().length() - 5)
                        + "v.tif", missingDimensions[0], missingDimensions[1], true));
            }
        }
        int endFlyleafIndex = lastIndexOfPrefix(book + IMG_END_FLYLEAF, images);
        if (endFlyleafIndex != -1) {
            img = images.get(endFlyleafIndex);
            if (img.getId().endsWith("r.tif")) {
                images.add(endFlyleafIndex + 1, new BookImage(img.getId().substring(0, img.getId().length() - 5)
                        + "v.tif", missingDimensions[0], missingDimensions[1], true));
            }
        }
    }

    /**
     * Add any images that were skipped. The manuscripts contain several
     * separate sequences of images (frontmatter, endmatter, and the main
     * sequence). In each of these sequences, it is possible for images to be
     * missing. This method adds a placeholder to the image list with the
     * correct name for those missing images.
     * 
     * @param images
     *            list of all BookImages
     * @param missingDimensions
     *            dimensions of the missing image
     */
    private void addSkippedImages(List<BookImage> images, int[] missingDimensions) {
        Collections.sort(images, BookImageComparator.instance());

        List<BookImage> missing = new ArrayList<>();
        for (int i = 1; i < images.size(); i++) {
            BookImage i1 = images.get(i - 1);
            BookImage i2 = images.get(i);

            String[] p1 = i1.getId().split("\\.");
            String[] p2 = i2.getId().split("\\.");

            if (p1.length != p2.length) {
                // assuming prefixes with folios change length
                continue;
            }

            String f1 = parser.page(i1.getId());
            String f2 = parser.page(i2.getId());

            if (StringUtils.isNotBlank(f1) && StringUtils.isNotBlank(f2)) {
                String prefix1 = i1.getId().substring(0, i1.getId().length() - (f1 + TIF_EXT).length());
                String prefix2 = i2.getId().substring(0, i2.getId().length() - (f2 + TIF_EXT).length());

                if (!prefix1.equals(prefix2)) {
                    continue;
                }

                int seq1 = Integer.parseInt(f1.substring(0, f1.length() - 1));
                int seq2 = Integer.parseInt(f2.substring(0, f2.length() - 1));

                char rv1 = f1.charAt(f1.length() - 1);
                char rv2 = f2.charAt(f2.length() - 1);

                // if seq1 == seq2 AND rv1 == 'r' AND rv2 == 'v'
                // nothing missing.
                // if seq1 == seq2 AND (rv1 == 'v' OR rv2 == 'r')
                // something is wrong!
                // if seq1 > seq2
                // list not sorted correctly
                if (seq1 < seq2) {
                    // Number of missing pages between two pages in the list, f1 and f2
                    int numMissing = (seq2 - seq1 - 1) * 2;

                    if (rv1 == 'r') {
                        numMissing++;
                    }
                    if (rv2 == 'v') {
                        numMissing++;
                    }

                    char next_rv = rv1;
                    int next_seq = seq1;
                    for (int j = 0; j < numMissing; j++) {
                        if (next_rv == 'v') {
                            next_rv = 'r';
                            next_seq++;
                        } else {
                            next_rv = 'v';
                        }

                        BookImage missingImage = new BookImage(prefix1 + String.format("%03d", next_seq) + next_rv
                                + TIF_EXT, missingDimensions[0], missingDimensions[1], true);
                        missing.add(missingImage);
                    }
                }
            }
        }

        images.addAll(missing);
        Collections.sort(images, BookImageComparator.instance());
    }

    /**
     * Take all images from
     * 
     * @param bookStreams
     *            and build a list of {@link rosa.archive.model.BookImage}s.
     * 
     * @param collection
     *            collection name
     * @param book
     *            book name
     * @return list of BookImages
     * @throws IOException
     */
    private List<BookImage> buildImageList(String collection, String book, boolean addMissing,
            ByteStreamGroup bookStreams) throws IOException {
        List<BookImage> images = new ArrayList<>();
        for (String file : bookStreams.listByteStreamNames()) {
            if (parser.getArchiveItemType(file) == ArchiveItemType.IMAGE) {

                String filepath = Paths.get(bookStreams.id()).resolve(file).toString();
                int[] dimensions = getImageDimensionsHack(filepath);

                BookImage img = new BookImage();
                img.setId(file);
                img.setWidth(dimensions[0]);
                img.setHeight(dimensions[1]);
                img.setMissing(false);

                images.add(img);
            }
        }
        Collections.sort(images, BookImageComparator.instance());
        // images.sort(BookImageComparator.instance());

        if (addMissing) {
            int[] missingDimensions = getMissingImageDimensions(collection);
            addMissingImages(book, images, missingDimensions);
        }

        return images;
    }

    private int[] getMissingImageDimensions(String collection) throws IOException {
        return base.getByteStreamGroup(collection).hasByteStream(MISSING_IMAGE) ? getImageDimensionsHack(Paths
                .get(base.getByteStreamGroup(collection).id()).resolve(MISSING_IMAGE).toString())
                : new int[] { 0, 0 };
    }

    /**
     * @param prefix
     *            defines the sequence
     * @param images
     *            list of all images
     * @return index of last item or -1 if prefix does not appear in the list
     */
    private int lastIndexOfPrefix(String prefix, List<BookImage> images) {
        int last = -1;

        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getId().startsWith(prefix)) {
                last = i;
            }
        }
        return last;
    }

    /**
     * Code adapted from original rosa archive tool, now uses Apache Commons IO
     * instead of custom byte array wrapper class.
     * 
     * @param path
     *            file path of image
     * @return array: [width, height]
     * @throws IOException
     */
    private static int[] getImageDimensionsHack(String path) throws IOException {

        String[] cmd = new String[] { "identify", "-ping", "-format", "%w %h ", path + "[0]" };
        Process p = Runtime.getRuntime().exec(cmd);

        try {
            if (p.waitFor() != 0) {
                byte[] byteArray = IOUtils.toByteArray(p.getErrorStream());
                String err = new String(byteArray, "UTF-8");

                throw new IOException("Failed to run on " + path + ": " + err);
            }

            byte[] buff = IOUtils.toByteArray(p.getInputStream());
            String result = new String(buff, "UTF-8");

            String[] s = result.trim().split("\\s+");
            if (s.length != 2) {
                throw new IOException("Invalid result " + result + " on " + path);
            }

            return new int[] { Integer.parseInt(s[0]), Integer.parseInt(s[1].trim()) };
        } catch (NumberFormatException e) {
            throw new IOException("Invalid result.");
        } catch (InterruptedException e) {
            throw new IOException(e);
        } finally {
            p.destroy();
        }
    }

    /**
     * Update all checksum values for an archive.
     * 
     * <p>
     * A {@link rosa.archive.core.ByteStreamGroup} with top level byte streams
     * representing the level in the archive to be checked is used. Each byte
     * stream is checked for the last time its contents were modified. If this
     * date falls after (more recent) the last modified date of the checksum
     * file, then the checksum value for that byte stream is updated and saved.
     * A new checksum entry is calculated and saved for those streams that are
     * not already present in the checksum data.
     * </p>
     * <p>
     * All entries can be forced to update, regardless of last modified dates by
     * using the
     * 
     * @param force
     *            flag.
     *            </p>
     *            <p>
     *            If a checksum entry is initially present for a byte stream
     *            that no is not present in this
     * @param bsg
     *            , it is assumed to no longer exist in the archive. In this
     *            case, the entry is removed from the checksum data.
     *            </p>
     * 
     * @param checksums
     *            container holding checksum information
     * @param errors
     *            list of errors found while calculating checksums
     * @return if checksums were updated and written successfully
     * @throws IOException if a byte stream or byte stream group does not exist as expected
     */
    protected boolean updateChecksum(SHA1Checksum checksums, ByteStreamGroup bsg, boolean force, List<String> errors)
            throws IOException {
        boolean success = true;

        long checksumLastMod = bsg.getLastModified(checksums.getId());
        Map<String, String> checksumMap = new HashMap<>();

        for (String streamName : bsg.listByteStreamNames()) {
            // Do not record checksum for the checksum file!
            if (streamName.equals(checksums.getId())) {
                continue;
            }

            long lastMod = bsg.getLastModified(streamName);
            String checksum = checksums.checksums().get(streamName);

            if (force || lastMod >= checksumLastMod || checksum == null) {
                // Write checksum if it is out of date or doesn't exist or it is
                // forced.
                try (InputStream in = bsg.getByteStream(streamName)) {

                    String checksumValue = ChecksumUtil.calculateChecksum(in, HashAlgorithm.SHA1);
                    checksumMap.put(streamName, checksumValue);

                } catch (NoSuchAlgorithmException e) {
                    errors.add("Failed to generate checksum. [" + bsg.name() + ":" + streamName + "]");
                    success = false;
                }
            } else if (lastMod < checksumLastMod) {
                // Keep if the item already has a checksum value that is
                // up-to-date AND it still exists in the archive.
                checksumMap.put(streamName, checksum);
            }
        }
        // Replace old checksum map. This serves to remove all checksum entries
        // that exist for
        // files that are no longer in the archive.
        checksums.checksums().clear();
        checksums.checksums().putAll(checksumMap);

        // Write out checksums only if nothing has failed yet.
        return success && writeItem(checksums, bsg, SHA1Checksum.class, errors);
    }

    /**
     * Write an archive model object to the archive, through the ByteStreamGroup that contains it.
     * Items that are NULL will not be written. If an object uses a byte stream ID that already
     * exists in the archive, the new object will overwrite it.
     *
     * @param item item to write to archive
     * @param bsg the byte stream group that will hold the item
     * @param type the archive model object type
     * @param errors list of errors encountered while writing object
     * @param <T> type
     * @return true if write succeeded, false otherwise
     */
    protected <T extends HasId> boolean writeItem(T item, ByteStreamGroup bsg, Class<T> type, List<String> errors) {
        // No item to write
        if (item == null) {
            errors.add("Cannot write an object that does not exist! [type=" + type.toString() + "]");
            return false;
        }

        try (OutputStream out = bsg.getOutputStream(item.getId())) {
            serializers.getSerializer(type).write(item, out);
            return true;
        } catch (IOException e) {
            errors.add("Failed to write [" + item.getId() + "]\n" + stacktrace(e));
            return false;
        }
    }

    /**
     *
     *
     * @param name name of the item to load
     * @param bsg byte stream group containing this item
     * @param type archive model object type
     * @param errors list of errors encountered while loading
     * @param <T> type
     * @return the item as an archive model object
     */
    protected <T extends HasId> T loadItem(String name, ByteStreamGroup bsg, Class<T> type, List<String> errors) {
        // The file does not exist
        if (!bsg.hasByteStream(name)) {
            return null;
        }

        try (InputStream in = bsg.getByteStream(name)) {
            T obj = serializers.getSerializer(type).read(in, errors);
            obj.setId(name);

            return obj;

        } catch (IOException e) {
            errors.add("Failed to read item in archive. [" + name + "]\n" + stacktrace(e));
            return null;
        }
    }

    private String stacktrace(Exception e) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(out));

        return out.toString();
    }

    /**
     * @param in input list
     * @return a new list if input is NULL, the input list otherwise
     */
    private List<String> nonNullList(List<String> in) {
        if (in == null) {
            return new ArrayList<>();
        }
        return in;
    }
}
