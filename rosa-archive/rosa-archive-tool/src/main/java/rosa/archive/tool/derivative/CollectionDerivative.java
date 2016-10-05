package rosa.archive.tool.derivative;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rosa.archive.core.Store;
import rosa.archive.model.Book;
import rosa.archive.model.BookCollection;

/**
 *
 */
public class CollectionDerivative extends AbstractDerivative {

    private String collection;
    private boolean collectionExists;

    /**
     * @param collection the collection name
     * @param report report to write to
     * @param store store
     */
    public CollectionDerivative(String collection, PrintStream report, Store store) {
        super(report, store);
        this.collection = collection;

        try {
            this.collectionExists = Arrays.asList(store.listBookCollections()).contains(collection);
        } catch (IOException e) {
            this.collectionExists = false;
            report.println("Could not find archive.");
        }
    }

    @Override
    public void list() {
        try {
            report.println(collection);
            for (String book : store.listBooks(collection)) {
                report.println("  " + book);
            }
        } catch (IOException e) {
            reportError("Failed to list books in collection. [" + collection + "]");
        }
    }

    /**
     * Update checksum values of the collection plus all books contained within
     * the collection.
     *
     * Checksum values will be updated if
     * <ul>
     *     <li>the last modified date of the file falls after the last modified
     *          date of the checksum file</li>
     *     <li>an item does not have a checksum value in the checksum file</li>
     *     <li>the force flag is present</li>
     * </ul>
     *
     * @param force force update of all checksum values
     * @throws IOException if collection is inaccessible
     */
    public void updateChecksum(boolean force) throws IOException {
        if (!collectionExists) {
            // Collection not present
            report.println("Failed to update, collection not found in archive. (" + collection + ")");
            return;
        }

        List<String> errors = new ArrayList<>();

        report.print("  \nUpdating SHA1SUM for collection. [" + collection + "]");
        store.updateChecksum(collection, force, errors);
        report.println(" ... complete!");

        if (!errors.isEmpty()) {
            reportError("Errors", errors);
            errors.clear();
        }

        for (String bookName : store.listBooks(collection)) {
            report.print("  Updating SHA1SUM for book. [" + collection + ":" + bookName + "]");
            store.updateChecksum(collection, bookName, force, errors);
            report.println(" ... complete!");

            if (!errors.isEmpty()) {
                reportError("Errors", errors);
                errors.clear();
            }
        }
    }

    @Override
    public void check(boolean checkBits) throws IOException {
        if (!collectionExists) {
            report.println("Failed to check collection, not found in archive. (" + collection + ")");
            return;
        }
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> loadingErrors = new ArrayList<>();

        BookCollection col = store.loadBookCollection(collection, loadingErrors);

        if (col == null) {
            return;
        }

        report.println(collection);
        store.check(col, checkBits, errors, warnings);

        if (!errors.isEmpty()) {
            reportError("Errors:", errors);
            errors.clear();
        }
        if (!warnings.isEmpty()) {
            reportError("Warnings:", warnings);
            warnings.clear();
        }

        for (String bookName : store.listBooks(collection)) {
            if (bookName.endsWith(".ignore")) {
                continue;
            }

            Book book = store.loadBook(col, bookName, loadingErrors);
            if (book == null) {
                report.println("Failed to read book. [" + collection + ":" + bookName + "]");
                continue;
            }
            report.println("\n" + bookName);
            store.check(col, book, checkBits, errors, warnings);

            if (!errors.isEmpty()) {
                reportError("Errors: ", errors);
                errors.clear();
            }
            if (!warnings.isEmpty()) {
                reportError("Warnings: ", warnings);
                warnings.clear();
            }
        }
    }

    @Override
    public void generateAndWriteImageList(boolean force) throws IOException {
        if (!collectionExists) {
            report.println("Failed to create image lists, collection not found in archive. (" + collection + ")");
            return;
        }
        List<String> errors = new ArrayList<>();
        for (String book : store.listBooks(collection)) {
            report.println("Generating image list for " + collection + ":" + book);
            store.generateAndWriteImageList(collection, book, force, errors);

            if (!errors.isEmpty()) {
                reportError("Errors:", errors);
            }
        }
    }

    @Override
    public void validateXml() throws IOException {
        if (!collectionExists) {
            report.println("Cannot validate XML, collection not found in archive. (" + collection + ")");
            return;
        }
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (String book : store.listBooks(collection)) {
            report.println("Validating XML files for " + collection + ":" + book);
            store.validateXml(collection, book, errors, warnings);

            if (!errors.isEmpty()) {
                reportError("Errors:", errors);
            }
            if (!warnings.isEmpty()) {
                reportError("Warnings:", warnings);
            }
        }
    }

    @Override
    public void renameImages(boolean changeId, boolean reverse) throws IOException {
        if (!collectionExists) {
            report.println("Cannot rename images, collection not found in archive. (" + collection + ")");
            return;
        }
        List<String> errors = new ArrayList<>();

        for (String book : store.listBooks(collection)) {
            report.println("Renaming images for " + collection + ":" + book);
            store.renameImages(collection, book, changeId, reverse, errors);

            if (!errors.isEmpty()) {
                reportError("Errors:", errors);
            }
        }
    }

    @Override
    public void renameTranscriptions(boolean reverse) throws IOException {
        if (!collectionExists) {
            report.println("Cannot rename transcriptions, collection not found in archive. (" + collection + ")");
            return;
        }
        List<String> errors = new ArrayList<>();

        for (String book : store.listBooks(collection)) {
            report.println("Validating XML files for " + collection + ":" + book);
            store.renameTranscriptions(collection, book, reverse, errors);

            if (!errors.isEmpty()) {
                reportError("Errors:", errors);
            }
        }
    }
}
