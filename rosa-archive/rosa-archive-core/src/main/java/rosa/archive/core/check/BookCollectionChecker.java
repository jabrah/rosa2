package rosa.archive.core.check;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import rosa.archive.core.ByteStreamGroup;
import rosa.archive.core.ArchiveConfig;
import rosa.archive.core.serialize.SerializerSet;
import rosa.archive.model.BookCollection;
import rosa.archive.model.CharacterNames;
import rosa.archive.model.IllustrationTitles;
import rosa.archive.model.NarrativeSections;

import com.google.inject.Inject;

/**
 *
 */
public class BookCollectionChecker extends AbstractArchiveChecker {

    @Inject
    public BookCollectionChecker(ArchiveConfig config, SerializerSet serializers) {
        super(config, serializers);
    }

    public boolean checkContent(BookCollection collection, ByteStreamGroup bsg, boolean checkBits,
                                List<String> errors, List<String> warnings) {

        if (collection == null) {
            errors.add("Book collection missing.");
            return false;
        }

        // Check collection items:
        //   id
        if (StringUtils.isBlank(collection.getId())) {
            errors.add("Collection ID missing from collection. [" + collection.getId() + "]");
        }

        //   books[]
        for (String id : collection.books()) {
            if (!bsg.hasByteStreamGroup(id)) {
                errors.add("Book ID in collection not found in archive. [" + id + "]");
            }
        }

        //   languages
        for (String lang : config.getLanguages()) {
            if (!collection.isLanguageSupported(lang)) {
                errors.add("Language should be supported but is not. [" + lang + "]");
            }
        }

        if (!bsg.hasByteStream(MISSING_IMAGE)) {
            errors.add("[" + MISSING_IMAGE + "] missing.");
        }

        //   character_names and illustration_titles and narrative_sections
        check(collection, bsg, errors, warnings);

        // Check bit integrity (there is no stored checksum values for these files)
        if (checkBits) {
            checkBits(bsg, false, false, errors, warnings);
        }

        return errors.isEmpty();
    }

    /**
     *
     *
     * @param bsg byte stream group
     * @param collection parent collection
     * @param errors list of errors
     * @param warnings list of warnings
     */
    private void check(BookCollection collection, ByteStreamGroup bsg, List<String> errors, List<String> warnings) {

        CharacterNames names = collection.getCharacterNames();
        IllustrationTitles titles = collection.getIllustrationTitles();
        NarrativeSections sections = collection.getNarrativeSections();

        // Make sure the things can be read
        attemptToRead(names, bsg, errors, warnings);
        attemptToRead(titles, bsg, errors, warnings);
        attemptToRead(sections, bsg, errors, warnings);
    }
}
