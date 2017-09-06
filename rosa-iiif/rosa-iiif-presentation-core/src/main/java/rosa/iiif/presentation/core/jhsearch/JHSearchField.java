package rosa.iiif.presentation.core.jhsearch;

import rosa.search.model.SearchField;
import rosa.search.model.SearchFieldType;

/**
 * Each search document represents a IIIF Presentation object.
 */

public enum JHSearchField implements SearchField, JHSearchFieldProperties {
    OBJECT_ID(false, false, SearchFieldType.STRING),
    OBJECT_TYPE(false, true, SearchFieldType.STRING),
    OBJECT_LABEL(false, true, SearchFieldType.STRING),
    COLLECTION_ID(false, false, SearchFieldType.STRING),    
    MANIFEST_ID(false, true, SearchFieldType.STRING),
    MANIFEST_LABEL(false, true, SearchFieldType.STRING),
    IMAGE_NAME(true, false, SearchFieldType.IMAGE_NAME),
    MARGINALIA(true, false, true, JHSearchFieldProperties.MARGINALIA, SearchFieldType.ENGLISH, SearchFieldType.FRENCH, SearchFieldType.ITALIAN, SearchFieldType.GREEK, SearchFieldType.SPANISH, SearchFieldType.LATIN),
    UNDERLINE(true, false, true, JHSearchFieldProperties.UNDERLINE, SearchFieldType.ENGLISH, SearchFieldType.FRENCH, SearchFieldType.ITALIAN, SearchFieldType.GREEK, SearchFieldType.SPANISH, SearchFieldType.LATIN),
    EMPHASIS(true, false, true, JHSearchFieldProperties.EMPHASIS, SearchFieldType.ENGLISH, SearchFieldType.FRENCH, SearchFieldType.ITALIAN, SearchFieldType.GREEK, SearchFieldType.SPANISH, SearchFieldType.LATIN),
    ERRATA(true, false,true, JHSearchFieldProperties.ERRATA, SearchFieldType.ENGLISH, SearchFieldType.FRENCH, SearchFieldType.ITALIAN, SearchFieldType.GREEK, SearchFieldType.SPANISH, SearchFieldType.LATIN),
    MARK(true, false, true, JHSearchFieldProperties.MARK, SearchFieldType.STRING, SearchFieldType.ENGLISH, SearchFieldType.FRENCH, SearchFieldType.ITALIAN, SearchFieldType.GREEK, SearchFieldType.SPANISH, SearchFieldType.LATIN),
    SYMBOL(true, false,true, JHSearchFieldProperties.SYMBOL, SearchFieldType.STRING, SearchFieldType.ENGLISH, SearchFieldType.FRENCH, SearchFieldType.ITALIAN, SearchFieldType.GREEK, SearchFieldType.SPANISH, SearchFieldType.LATIN),
    NUMERAL(true,false,true, JHSearchFieldProperties.NUMERAL, SearchFieldType.STRING, SearchFieldType.ENGLISH, SearchFieldType.FRENCH, SearchFieldType.ITALIAN, SearchFieldType.GREEK, SearchFieldType.SPANISH, SearchFieldType.LATIN),
    DRAWING(true,false,true, JHSearchFieldProperties.DRAWING, SearchFieldType.STRING, SearchFieldType.ENGLISH, SearchFieldType.FRENCH, SearchFieldType.ITALIAN, SearchFieldType.GREEK, SearchFieldType.SPANISH, SearchFieldType.LATIN),
    CROSS_REFERENCE(true, false,true, JHSearchFieldProperties.X_REF, SearchFieldType.ENGLISH, SearchFieldType.FRENCH, SearchFieldType.ITALIAN, SearchFieldType.GREEK, SearchFieldType.SPANISH, SearchFieldType.LATIN),
    TRANSCRIPTION(true, false, true, JHSearchFieldProperties.TRANSCRIPTION, SearchFieldType.OLD_FRENCH, SearchFieldType.ENGLISH),
    ILLUSTRATION(true, false, true, JHSearchFieldProperties.ILLUSTRATION, SearchFieldType.ENGLISH),
    LANGUAGE(false, false, true, JHSearchFieldProperties.LANGUAGE, SearchFieldType.STRING),
    MARGINALIA_LANGUAGE(false, false, true, JHSearchFieldProperties.MARG_LANG, SearchFieldType.STRING),
    BOOK(true, false,true, JHSearchFieldProperties.BOOK, SearchFieldType.ENGLISH),
    METHOD(false, false, true, JHSearchFieldProperties.METHOD, SearchFieldType.STRING),
    CHARACTER_NAME(true, false, true, JHSearchFieldProperties.CHAR_NAME, SearchFieldType.ENGLISH, SearchFieldType.OLD_FRENCH, SearchFieldType.FRENCH, SearchFieldType.LATIN, SearchFieldType.GREEK, SearchFieldType.ITALIAN, SearchFieldType.SPANISH),

    // Set of fields that will be shared among all collections
    TITLE(true, false, true, JHSearchFieldProperties.TITLE, SearchFieldType.ENGLISH, SearchFieldType.OLD_FRENCH, SearchFieldType.FRENCH, SearchFieldType.LATIN, SearchFieldType.GREEK, SearchFieldType.ITALIAN, SearchFieldType.SPANISH),
    PEOPLE(true, false, true, JHSearchFieldProperties.PEOPLE, SearchFieldType.ENGLISH),
    PLACE(true, false, true, JHSearchFieldProperties.PLACE, SearchFieldType.ENGLISH),
    // Include 'author'/'creator' ?
    REPO(true, false, true, JHSearchFieldProperties.REPOSITORY, SearchFieldType.ENGLISH),
    DESCRIPTION(true, false, true, JHSearchFieldProperties.DESCRIPTION, SearchFieldType.ENGLISH),
    TEXT(true, false, true, JHSearchFieldProperties.TEXT, SearchFieldType.ENGLISH, SearchFieldType.FRENCH, SearchFieldType.OLD_FRENCH, SearchFieldType.ITALIAN, SearchFieldType.GREEK, SearchFieldType.SPANISH, SearchFieldType.LATIN)
    ;
    
     // TODO Move some of this to SearchField?
    
    private final SearchFieldType[] types;
    private final boolean context;
    private final boolean include;
    private final boolean expose;
    private final String field;
    private final String propName;
    
    /**
     * 
     * @param context - Context in search result
     * @param include - Include value in search result
     * @param expose - Advertise to user
     * @param propName - Field name used in config to retrieve label, description, values
     * @param types .
     */
    JHSearchField(boolean context, boolean include, boolean expose, String propName, SearchFieldType... types) {
        this.types = types;
        this.context = context;
        this.include = include;
        this.expose = expose;
        this.field = name().toLowerCase();
        this.propName = propName;
    }
    
    JHSearchField(boolean context, boolean include, SearchFieldType... types) {
        this(context, include, false, null, types);
    }

    @Override
    public SearchFieldType[] getFieldTypes() {
        return types;
    }

    @Override
    public String getFieldName() {
        return field;
    }
    
    public String getLabel() {
        return propName + LABEL_SUFFIX;
    }
    
    public String getDescription() {
        return propName + DESC_SUFFIX;
    }
    
    @Override
    public boolean isContext() {
        return context;
    }

    @Override
    public boolean includeValue() {
        return include;
    }
    
    public String getValueLabelPairs() {
        return propName + VALUES_SUFFIX;
    }
    
    public boolean isExposed() {
        return expose;
    }
}
