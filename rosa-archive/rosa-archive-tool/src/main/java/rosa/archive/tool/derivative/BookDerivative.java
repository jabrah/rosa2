package rosa.archive.tool.derivative;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import rosa.archive.core.Store;
import rosa.archive.model.Book;
import rosa.archive.model.BookCollection;

/**
 *
 */
public class BookDerivative extends AbstractDerivative {

    protected String collection;
    protected String book;

    private boolean collectionExists;
    private boolean bookExists;

    public BookDerivative(String collection, String book, PrintStream report, Store store) {
        super(report, store);
        this.collection = collection;
        this.book = book;

        try {
            collectionExists = Arrays.asList(store.listBookCollections()).contains(collection);
            bookExists = collectionExists && Arrays.asList(store.listBooks(collection)).contains(book);
        } catch (IOException e) {
            report.println("Failed to find archive.");
        }
    }

    @Override
    public void list() {
        if (!collectionExists) {
            report.println("Collection not found in archive. (" + collection + ")");
            return;
        } else if (!bookExists) {
            report.println("Book not found in archive. (" + collection + ":" + book + ")");
            return;
        }

        try {
            BookCollection col = store.loadBookCollection(collection, null);
            Book book = store.loadBook(col, this.book, null);

            report.println(collection + ":" + this.book);
            for (String item : book.getContent()) {
                report.println("  " + item);
            }
        } catch (IOException e) {
            reportError("Failed to list items in book. [" + collection + ":" + book + "]");
        }
    }

    @Override
    public void updateChecksum(boolean force) throws IOException {
        if (!collectionExists) {
            report.println("Collection not found in archive. (" + collection + ")");
            return;
        } else if (!bookExists) {
            report.println("Book not found in archive. (" + collection + ":" + book + ")");
            return;
        }

        List<String> errors = new ArrayList<>();

        report.println("Updating checksum [" + collection + ":" + book + "]");
        store.updateChecksum(collection, book, force, errors);

        if (errors.size() > 0) {
            reportError(errors.toArray(new String[errors.size()]));
        }
    }

    @Override
    public void check(boolean checkBits) throws IOException {
        if (!collectionExists) {
            report.println("Collection not found in archive. (" + collection + ")");
            return;
        } else if (!bookExists) {
            report.println("Book not found in archive. (" + collection + ":" + book + ")");
            return;
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> loadingErrors = new ArrayList<>();

        BookCollection col = store.loadBookCollection(collection, loadingErrors);
        Book b = col == null ? null : store.loadBook(col, book, loadingErrors);
        if (b != null) {
            report.println("Checking book. [" + collection + ":" + book + "]");
            store.check(col, b, checkBits, errors, warnings);
        } else {
            report.println("Failed to read book. [" + collection + ":" + book + "]");
        }

        if (!errors.isEmpty()) {
            reportError("Errors: ", errors);
        }
        if (!warnings.isEmpty()) {
            reportError("Warnings: ", warnings);
        }
    }

    @Override
    public void generateAndWriteImageList(boolean force) throws IOException {
        if (!collectionExists) {
            report.println("Collection not found in archive. (" + collection + ")");
            return;
        } else if (!bookExists) {
            report.println("Book not found in archive. (" + collection + ":" + book + ")");
            return;
        }

        List<String> errors = new ArrayList<>();
        report.println("Generating and writing image list. [" + collection + ":" + book + "]");
        store.generateAndWriteImageList(collection, book, force, errors);

        if (!errors.isEmpty()) {
            reportError("Errors:", errors);
        }
    }

    @Override
    public void validateXml() throws IOException {
        if (!collectionExists) {
            report.println("Collection not found in archive. (" + collection + ")");
            return;
        } else if (!bookExists) {
            report.println("Book not found in archive. (" + collection + ":" + book + ")");
            return;
        }

        List<String> errors = new ArrayList<>();
        store.validateXml(collection, book, errors, errors);

        if (!errors.isEmpty()) {
            reportError("Errors:", errors);
        }
    }

    @Override
    public void renameImages(boolean changeId, boolean reverse) throws IOException {
        if (!collectionExists) {
            report.println("Collection not found in archive. (" + collection + ")");
            return;
        } else if (!bookExists) {
            report.println("Book not found in archive. (" + collection + ":" + book + ")");
            return;
        }

        List<String> errors = new ArrayList<>();
        report.println("Renaming images. [" + collection + ":" + book + "]");
        store.renameImages(collection, book, changeId, reverse, errors);

        if (!errors.isEmpty()) {
            reportError("Errors:", errors);
        }
    }

    @Override
    public void renameTranscriptions(boolean reverse) throws IOException {
        if (!collectionExists) {
            report.println("Collection not found in archive. (" + collection + ")");
            return;
        } else if (!bookExists) {
            report.println("Book not found in archive. (" + collection + ":" + book + ")");
            return;
        }

        List<String> errors = new ArrayList<>();
        report.println("Renaming AoR transcriptions. [" + collection + ":" + book + "]");
        store.renameTranscriptions(collection, book, reverse, errors);

        if (!errors.isEmpty()) {
            reportError("Errors:", errors);
        }
    }

    public void convertTranscriptionTexts() throws IOException {
        if (!collectionExists) {
            report.println("Collection not found in archive. (" + collection + ")");
            return;
        } else if (!bookExists) {
            report.println("Book not found in archive. (" + collection + ":" + book + ")");
            return;
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        store.generateTEITranscriptions(collection, book, errors, warnings);

        if (!warnings.isEmpty()) {
            reportError("Warning:", warnings);
        }
        if (!errors.isEmpty()) {
            reportError("Errors:", errors);
        }

    }



    public void generateFileMap() throws IOException {
        if (!collectionExists) {
            report.println("Collection not found in archive. (" + collection + ")");
            return;
        } else if (!bookExists) {
            report.println("Book not found in archive. (" + collection + ":" + book + ")");
            return;
        }

        try (Scanner in = new Scanner(System.in)) {
            System.out.print("Has a frontcover + front pastedown image? (true|false) ");
            boolean hasFrontCover = in.nextBoolean();
            System.out.print("Has a back cover and back pastedown image? (true|false) ");
            boolean hasBackCover = in.nextBoolean();
    
            // Get number of front/end matter flyleaves
            System.out.print("Number of frontmatter flyleaves: ");
            int frontmatter = in.nextInt();
    
            System.out.print("Number of endmatter flyleaves: ");
            int endmatter = in.nextInt();
    
            // Get number of misc images
            System.out.print("Number of misc images: ");
            int misc = in.nextInt();
    
            System.out.print("Book ID: ");
            String id = in.next();
    
            List<String> errors = new ArrayList<>();
            store.generateFileMap(collection, book, id, hasFrontCover, hasBackCover, frontmatter, endmatter, misc, errors);
    
            if (!errors.isEmpty()) {
                reportError("Errors: ", errors);
            }
        }
    }
}
