package rosa.archive.core;

import java.io.IOException;
import java.util.List;

import rosa.archive.model.Book;
import rosa.archive.model.BookCollection;

/**
 *
 */
public interface Store {

    String[] listBookCollections() throws IOException;

    /**
     *
     * @return
     *          array of items that exists in the collection
     */
    String[] listBooks(String collectionId) throws IOException;

    /**
     * From the collection id, load the collection from the archive and get a BookCollection
     * object.
     *
     * @param collectionId
     *          id of the collection
     * @return
     *          BookCollection object
     */
    BookCollection loadBookCollection(String collectionId, List<String> errors) throws IOException;

    /**
     * Get a single book from the archive that exists in the specified collection.
     *
     * @param collection
     * @param bookId
     *          id of the book to get
     * @return
     *          Book object
     */
    Book loadBook(BookCollection collection, String bookId, List<String> errors) throws IOException;

    /**
     * Check the internal data consistency and bit integrity of an archive within this Store.
     *
     * <p>
     *     This method checks an archive data model object for the correct data structure.
     *     Certain necessary objects within a data model object are checked to exist, and
     *     it is ensured that the underlying data exists in the archive. If the object
     *     holds references to other items in the archive, those references are followed
     *     to make sure the items are readable. The bit level content of these items are not
     *     checked, instead this method only checks to see if all necessary items exist and
     *     are readable.
     * </p>
     * <p>
     *     If {@code checkBits} is TRUE, the bit values of each item in the archive is checked
     *     against known values to ensure that the bits that you read are the bits that you
     *     want, and that all of the data is valid.
     * </p>
     *
     * @param book book to check
     * @param checkBits check bit integrity?
     * @return TRUE if data checks complete with no errors, FALSE otherwise
     */
    boolean check(BookCollection collection, Book book, boolean checkBits, List<String> errors, List<String> warnings);

    /**
     * See {@link #check(rosa.archive.model.BookCollection, rosa.archive.model.Book,
     * boolean, java.util.List, java.util.List)}
     *
     * @param collection collection to check
     * @param checkBits check bit integrity?
     * @return TRUE if data checks complete with no errors, FALSE otherwise
     */
    boolean check(BookCollection collection, boolean checkBits, List<String> errors, List<String> warnings);

    boolean updateChecksum(String collection, boolean force, List<String> errors) throws IOException;

    boolean updateChecksum(BookCollection collection, boolean force, List<String> errors) throws IOException;

    boolean updateChecksum(String collection, String book, boolean force, List<String> errors) throws IOException;

    boolean updateChecksum(BookCollection collection, Book book, boolean force, List<String> errors) throws IOException;

    void generateAndWriteImageList(String collection, String book, boolean force, List<String> errors) throws IOException;

    void generateAndWriteCropList(String collection, String book, boolean force, List<String> errors) throws IOException;

    void cropImages(String collection, String book, boolean force, List<String> errors) throws IOException;

    void generateFileMap(String collection, String book, String newId, boolean hasFrontCover, boolean hasBackCover,
                         int numFrontmatter, int numEndmatter, int numMisc, List<String> errors) throws IOException;
}
