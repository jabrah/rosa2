package rosa.website.search.client;

import rosa.search.model.SearchFields;

/**
 * Represents a category by which a search can be restricted.
 * These categories map directly to {@link rosa.search.model.SearchFields} which
 * are used in the search service.
 *
 * Enum names come from the old rosa1 UserFields used in its Lucene search widget.
 */
public enum SearchCategory {
    POETRY(SearchFields.TRANSCRIPTION_TEXT),
    RUBRIC(SearchFields.TRANSCRIPTION_RUBRIC),
    ILLUSTRATION_TITLE(SearchFields.ILLUSTRATION_TITLE),
    LECOY(SearchFields.TRANSCRIPTION_LECOY),
    NOTE(SearchFields.TRANSCRIPTION_NOTE),
    ILLUSTRATION_CHAR(SearchFields.ILLUSTRATION_CHAR),
    ILLUSTRATION_KEYWORDS(SearchFields.ILLUSTRATION_KEYWORD),
    DESCRIPTION(SearchFields.DESCRIPTION_TEXT),
    IMAGE(SearchFields.IMAGE_NAME),
    NARRATIVE_SECTION(SearchFields.NARRATIVE_SECTION_ID, SearchFields.NARRATIVE_SECTION_DESCRIPTION),
    ALL(SearchFields.values());

    private SearchFields[] fields;

    SearchCategory(SearchFields... fields) {
        this.fields = fields;
    }

    /**
     * Get all related Lucene search fields.
     *
     * @return array of search fields
     */
    public SearchFields[] getFields() {
        return fields;
    }

    public static SearchCategory category(String category) {
        for (SearchCategory cat : SearchCategory.values()) {
            if (cat.toString().equals(category)) {
                return cat;
            }
        }
        return null;
    }
}
