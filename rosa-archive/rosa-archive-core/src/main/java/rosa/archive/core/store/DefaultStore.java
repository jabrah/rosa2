package rosa.archive.core.store;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.apache.commons.lang3.StringUtils;
import rosa.archive.core.ByteStreamGroup;
import rosa.archive.core.config.AppConfig;
import rosa.archive.core.serialize.Serializer;
import rosa.archive.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class DefaultStore implements Store {

    private final ByteStreamGroup base;
    private final AppConfig config;
    private Map<Class, Serializer> serializerMap;

    @Inject
    public DefaultStore(Map<Class, Serializer> serializerMap,
                        AppConfig config,
                        @Assisted ByteStreamGroup base) {
        this.base = base;
        this.config = config;
        this.serializerMap = serializerMap;
    }

    @Override
    public String[] listBookCollections() {
        return base.listByteStreamGroupNames()
                .toArray(new String[base.numberOfByteStreamGroups()]);
    }

    @Override
    public String[] listBooks(String collectionId) {
        ByteStreamGroup collection = base.getByteStreamGroup(collectionId);
        return collection.listByteStreamGroupNames()
                .toArray(new String[collection.numberOfByteStreamGroups()]);
    }

    @Override
    public BookCollection loadBookCollection(String collectionId) {
        ByteStreamGroup collectionGroup = base.getByteStreamGroup(collectionId);
        BookCollection collection = new BookCollection();

        collection.setBooks(listBooks(collectionId));
        collection.setCharacterNames(
                loadItem(config.CHARACTER_NAMES, collectionGroup, CharacterNames.class));
        collection.setIllustrationTitles(
                loadItem(config.ILLUSTRATION_TITLES, collectionGroup, IllustrationTitles.class));
        collection.setNarrativeSections(
                loadItem(config.NARRATIVE_SECTIONS, collectionGroup, NarrativeSections.class));
        collection.setMissing(loadItem(config.MISSING_PAGES, collectionGroup, MissingList.class));

        // Languages from configuration.
        collection.setLanguages(config.languages());

        return collection;
    }

    @Override
    public Book loadBook(String collectionId, String bookId) {
        ByteStreamGroup byteStreams = base.getByteStreamGroup(collectionId);
        if (!byteStreams.hasByteStreamGroup(bookId)) {
            // TODO report missing book
            return null;
        }

        ByteStreamGroup bookStreams = byteStreams.getByteStreamGroup(bookId);
        Book book = new Book();

        book.setId(bookId);
        book.setImages(
                loadItem(bookId + config.IMAGES, bookStreams, ImageList.class));
        book.setCroppedImages(
                loadItem(bookId + config.IMAGES_CROP, bookStreams, ImageList.class));
        book.setCropInfo(
                loadItem(bookId + config.CROP, bookStreams, CropInfo.class));
        book.setBookMetadata(
                loadItem(bookId + config.DESCRIPTION, bookStreams, BookMetadata.class));
        book.setBookStructure(
                loadItem(bookId + config.REDUCED_TAGGING, bookStreams, BookStructure.class));
        book.setChecksumInfo(
                loadItem(bookId + config.SHA1SUM, bookStreams, ChecksumInfo.class));
        book.setIllustrationTagging(
                loadItem(bookId + config.IMAGE_TAGGING, bookStreams, IllustrationTagging.class));
        book.setManualNarrativeTagging(
                loadItem(bookId + config.NARRATIVE_TAGGING_MAN, bookStreams, NarrativeTagging.class));
        book.setAutomaticNarrativeTagging(
                loadItem(bookId + config.NARRATIVE_TAGGING, bookStreams, NarrativeTagging.class));
        book.setTranscription(
                loadItem(bookId + config.TRANSCRIPTION, bookStreams, Transcription.class));

        List<String> content = bookStreams.listByteStreamNames();
        book.setContent(content.toArray(new String[bookStreams.numberOfByteStreams()]));

        // Look for permissions
        for (String name : content) {
            if (name.contains(config.PERMISSION)) {
                String lang = findLanguageCodeInName(name);
                if (StringUtils.isNotBlank(lang)) {
                    Permission perm = loadItem(
                            bookId + config.PERMISSION + lang + config.XML,
                            bookStreams,
                            Permission.class
                    );
                    book.addPermission(perm, lang);
                }
            }
        }

        return book;
    }

    @SuppressWarnings("unchecked")
    protected <T> T loadItem(String name, ByteStreamGroup bsg, Class<T> type) {
        List<String> errors = new ArrayList<>();

        try {
            InputStream in = bsg.getByteStream(name);
            Serializer serializer = serializerMap.get(type);

            return (T) serializer.read(in, errors);

        } catch (IOException e) {
            // TODO
            return null;
        }
    }

    protected String findLanguageCodeInName(String name) {

        String[] parts = name.split("_");
        for (String part : parts) {
            if (part.matches("(\\w){2,3}(?:(\\.[\\w]+)|$)")) {
                return part.split("\\.")[0];
            }
        }

        return "";
    }

    @Override
    public boolean checkBitIntegrity(BookCollection collection) {
        return false;
    }

    @Override
    public boolean checkBitIntegrity(Book book) {
        return false;
    }

    @Override
    public boolean checkContentConsistency(BookCollection collection) {
        return false;
    }

    @Override
    public boolean checkContentConsistency(Book book) {
        return false;
    }
}
