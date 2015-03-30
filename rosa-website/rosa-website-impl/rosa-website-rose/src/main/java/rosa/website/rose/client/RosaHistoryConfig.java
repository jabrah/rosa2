package rosa.website.rose.client;

import com.google.gwt.resources.client.ExternalTextResource;
import com.google.gwt.resources.client.TextResource;
import rosa.website.model.csv.CsvType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains mappings to get dynamic resources from history tokens.
 */
public class RosaHistoryConfig {
    private static final WebsiteConfig CONFIG = WebsiteConfig.INSTANCE;
    private static final RosaHtml HTMLS = RosaHtml.INSTANCE;

    private static Set<String> htmlPages;
    private static Set<String> csvPages;

    /**
     * @param history history token
     * @return does the history token belong to a static HTML page?
     */
    public static boolean isValidHtmlPage(String history) {
        if (htmlPages == null) {
            htmlPages = new HashSet<>(Arrays.asList(CONFIG.htmlHistory().split(",")));
        }

        return htmlPages.contains(history);
    }

    /**
     * @param history history token
     * @return does the history token belong to a CSV data page?
     */
    public static boolean isValidCsvPage(String history) {
        if (csvPages == null) {
            csvPages = new HashSet<>(Arrays.asList(CONFIG.csvHistory().split(",")));
        }

        return csvPages.contains(history);
    }

    /**
     * Get an external HTML resource according to its name. If no resource
     * is found associated with a given name, NULL is returned.
     *
     * @param history name associated with the HTML resource
     * @return external text resource, or NULL if name not found
     */
    public static ExternalTextResource getHtml(String history) {
        // TODO make more dynamic or interact with config? Cache these resources (here or in the Activity)?
        switch (history) {
            case "contact":
                return HTMLS.contactHtml();
            case "donation":
                return HTMLS.donationHtml();
            case "partners":
                return HTMLS.partnersHtml();
            case "project":
                return HTMLS.projectHistoryHtml();
            case "rose":
                return HTMLS.roseHistoryHtml();
            case "terms":
                return HTMLS.termsHtml();
            default:
                return null;
        }
    }

    /**
     * Get an external HTML resource containing prose description of a
     * CSV data set.
     *
     * @param history name associated with a particular CSV
     * @return HTML description, or NULL if not found
     */
    public static ExternalTextResource getCsvDescription(String history) {
        switch (history) {
            case "chars":
                return HTMLS.characterNamesHtml();
            case "corpus":
                return HTMLS.collectionDataHtml();
            case "illustrations":
                return HTMLS.illustrationTitlesHtml();
            case "sections":
                return HTMLS.narrativeSectionsHtml();
            default:
                return null;
        }
    }

    public static TextResource getHomeHtml() {
        return HTMLS.homeHtml();
    }

    /**
     * Get the CsvType associated with a history token.
     *
     * @param history history token
     * @return the csv type
     */
    public static CsvType getCsvType(String history) {
        switch (history) {
            case "corpus":
                return CsvType.COLLECTION_DATA;
            case "data":
                return CsvType.COLLECTION_BOOKS;
            case "illustrations":
                return CsvType.ILLUSTRATIONS;
            case "chars":
                return CsvType.CHARACTERS;
            case "sections":
                return CsvType.NARRATIVE_SECTIONS;
            default:
                return null;
        }
    }

}
