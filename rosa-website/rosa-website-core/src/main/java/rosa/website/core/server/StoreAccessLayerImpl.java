package rosa.website.core.server;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import rosa.archive.core.Store;
import rosa.archive.model.Book;
import rosa.archive.model.BookCollection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StoreAccessLayerImpl implements StoreAccessLayer {
    private static final Logger LOG = Logger.getLogger(StoreAccessLayerImpl.class.toString());

    private static final int MAX_CACHE_SIZE = 2000;
    private static final ConcurrentMap<String, Book> BOOK_CACHE = new ConcurrentHashMap<>(MAX_CACHE_SIZE);
    private static final ConcurrentMap<String, BookCollection> COLLECTION_CACHE = new ConcurrentHashMap<>(MAX_CACHE_SIZE);

    private Store store;

    @Inject
    public StoreAccessLayerImpl(StoreProvider storeProvider, @Named("archive.path") String archivePath) {
        this.store = storeProvider.getStore(archivePath);
    }

    @Override
    public boolean hasCollection(String name) {
        try {
            return Arrays.asList(store.listBookCollections()).contains(name);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to get collection list.");
            return false;
        }
    }

    @Override
    public boolean hasBook(String collection, String book) {
        try {
            return hasCollection(collection) && Arrays.asList(store.listBooks(collection)).contains(book);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to get book list for collection (" + collection + ")");
            return false;
        }
    }

    @Override
    public BookCollection collection(String collection) throws IOException {
        BookCollection col = COLLECTION_CACHE.get(collection);
        if (col != null) {
            return col;
        }

        if (!hasCollection(collection)) {
            LOG.log(Level.SEVERE, "Could not find collection (" + collection + ")");
            return null;
        }

        List<String> errors = new ArrayList<>();

        try {
            col = store.loadBookCollection(collection, errors);
            checkErrors(errors);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "An error has occurred while loading a collection. [" + collection + "]", e);
        }

        if (COLLECTION_CACHE.size() >= MAX_CACHE_SIZE) {
            COLLECTION_CACHE.clear();
        }
        if (collection != null && col != null) {
            COLLECTION_CACHE.putIfAbsent(collection, col);
        }

        return col;
    }

    @Override
    public Book book(String collection, String book) throws IOException {
        String key = collection + "." + book;

        Book b = BOOK_CACHE.get(key);
        if (b != null) {
            return b;
        }

        if (!hasBook(collection, book)) {
            LOG.log(Level.SEVERE, "Could not find book (" + book + ") in collection (" + collection + ")");
            return null;
        }

        b = loadBook(collection(collection), book);
        if (BOOK_CACHE.size() >= MAX_CACHE_SIZE) {
            BOOK_CACHE.clear();
        }

        if (b != null) {
            BOOK_CACHE.putIfAbsent(key, b);
        }

        return b;
    }

    @Override
    public Store store() {
        return store;
    }

    private Book loadBook(BookCollection collection, String book) throws IOException {
        List<String> errors = new ArrayList<>();
        Book b = null;

        try {
            b = store.loadBook(collection, book, errors);
            checkErrors(errors);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "An error has occurred while loading a book. [" + collection + ":" + book + "]", e);
        }

        return b;
    }

    private void checkErrors(List<String> errors) {
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder("Error loading book collection.\n");
            for (String s : errors) {
                sb.append(s);
                sb.append('\n');
            }
            LOG.warning(sb.toString());
        }
    }
}
