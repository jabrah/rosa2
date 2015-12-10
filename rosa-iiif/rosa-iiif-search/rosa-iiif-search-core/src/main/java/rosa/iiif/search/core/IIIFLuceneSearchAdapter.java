package rosa.iiif.search.core;

import rosa.archive.core.Store;
import rosa.archive.model.Book;
import rosa.archive.model.BookCollection;
import rosa.iiif.presentation.core.transform.impl.AnnotationTransformer;
import rosa.archive.core.util.Annotations;
import rosa.iiif.presentation.model.annotation.Annotation;
import rosa.iiif.search.model.IIIFSearchHit;
import rosa.iiif.search.model.IIIFSearchRequest;
import rosa.iiif.search.model.IIIFSearchResult;
import rosa.iiif.search.model.SearchCategory;
import rosa.search.core.SearchUtil;
import rosa.search.model.Query;
import rosa.search.model.QueryOperation;
import rosa.search.model.SearchFields;
import rosa.search.model.SearchMatch;
import rosa.search.model.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapt Lucene search results into JSON-LD that follows the IIIF Search API
 * (http://search.iiif.io/api/search/0.9/)
 *
 * Must be able to transform:
 *
 * - HTTP GET request formatted as specified in the IIIF Search API TO a Lucene
 * query that can be handed to the search service.
 *   - (http://search.iiif.io/api/search/0.9/#request)
 *
 * - Search results from the search service TO a format specified in the IIIF Search API
 *   - (http://search.iiif.io/api/search/0.9/#response)
 */
public class IIIFLuceneSearchAdapter {
    private static final int max_cache_size = 1000;

    private AnnotationTransformer annotationTransformer;
    private Store archiveStore;

    private final ConcurrentHashMap<String, Object> cache;

    public IIIFLuceneSearchAdapter(AnnotationTransformer annotationTransformer, Store archiveStore) {
        this.annotationTransformer = annotationTransformer;
        this.archiveStore = archiveStore;

        this.cache = new ConcurrentHashMap<>(max_cache_size);
    }

    /**
     * Transform a IIIF search request to a Lucene search query.
     *
     * @param iiifReq IIIF Search request
     * @return a Lucene query
     */
    public Query iiifToLuceneQuery(IIIFSearchRequest iiifReq) {
        // This will generate independent queries for each word...
        List<Query> top_query = new ArrayList<>();
        for (String queryTerm : iiifReq.queryTerms) {
            if (queryTerm == null || queryTerm.isEmpty()) {
                continue;
            }

            SearchCategory category = getSearchCategory(queryTerm);

            List<Query> children = new ArrayList<>();
            for (SearchFields luceneFields : category.luceneFields) {
                children.add(new Query(luceneFields, getSearchTerm(queryTerm)));
            }

            top_query.add(new Query(QueryOperation.OR, children.toArray(new Query[children.size()])));
        }

        /*
            Here, the rest of the parameters would be added to the query (motivation, date, user, box).

            In this specific case, motivation will be the same for all annotations that are being
            searched and will be omitted.

            The other parameters will not be searched for either, as no data is stored related
            to them. No user data, no time-stamp, location data is not precise enough for a box
            restriction.

            TODO to make this more general
            consider a generalized map: String (parameter) -> String[] (query terms)
            Parameters can be related (or the same as) Lucene fields. These can then be iterated
            over to generate the Lucene query.
         */




        return new Query(QueryOperation.AND, top_query.toArray(new Query[top_query.size()]));
    }

    /**
     * Transform a Lucene search result into a IIIF search result. The Lucene
     * search result is structured contains the offset, total # results, and
     * an array of SearchMatches
     *
     * Lucene result match will contain an ID of the format:
     *      collection_name;book_name;page_id;(annotation_id)
     *
     * From this ID, the annotation can be retrieved from the archive and
     * transformed into IIIF format.
     *
     * The context gives a small snippet of text surrounding the search hit.
     * This context is handled with {@link #getContextHits(List, String)}
     *
     * @param result lucene result
     * @return IIIF compatible result
     * @throws IOException possible exception if the archive is unavailable
     *
     * @see SearchResult
     * @see rosa.search.model.SearchMatch
     */
    public IIIFSearchResult luceneResultToIIIF(SearchResult result) throws IOException {
        IIIFSearchResult iiifResult = new IIIFSearchResult();

        iiifResult.setTotal(result.getTotal());
        iiifResult.setStartIndex(result.getOffset());

        // Create List<iiif annotation>
        List<Annotation> annotations = iiifResult.getAnnotations();
        List<IIIFSearchHit> hits = new ArrayList<>();
        for (SearchMatch match : result.getMatches()) {
            // Create the Annotation from Match
            //   Get archive annotation from match ID using Annotations util class
            String matchId = match.getId();

            BookCollection col = getCollection(matchId);
            Book book = getBook(matchId);
            rosa.archive.model.aor.Annotation archiveAnno = getArchiveAnnotation(matchId);

            //   Transform archive anno -> iiif anno using AnnotationTransformer
            annotations.add(annotationTransformer.transform(col, book, archiveAnno));
            hits.addAll(getContextHits(match.getContext(), matchId));
        }

        iiifResult.setHits(hits.toArray(new IIIFSearchHit[hits.size()]));

        return iiifResult;
    }

    /**
     * Translate the search results contexts into IIIF form.
     *
     * Search results from Lucene return with an ID and a set of strings that set the
     * context of the search match. In these context strings, those words that were
     * matched are bolded using HTML &lt;b&gt; tags. Surrounding text is present to give
     * the match some context. These strings must be transformed into a form used by IIIF
     * results.
     *
     * Search results in IIIF are formatted according to the (new/WIP) sc:Hit object. In
     * this form, the search context can use an oa:TextQuoteSelector, which hold the
     * matching words in a parameter (match|exact) and holds surrounding contextual text
     * in separate parameters (before|prefix) and (after|suffix).
     *
     * When translating, all text surrounded by HTML 'b' tags are put in the 'matching'
     * parameter, while preceding text is put in the 'before' parameter and proceeding
     * text is put in the 'after' parameter.
     *
     * EX:
     * {@code
     *   Lucene context: "This is a <b>matching string</b> with context"
     *   IIIF Hit: {
     *       match: "matching string",
     *       before: "This is a ",
     *       after: " with context"
     *   }
     * }
     *
     * One minor complication is the possibility that if sequential words are matched,
     * Lucene will often surround the individual words with HTML 'b' tags, instead of
     * the phrase: EX: {@code A string <b>with</b> <b>multiple</b> matches}. In this case,
     * consecutive bolded words should be detected and be put together in the same IIIF
     * 'match' parameter, instead of separate IIIF hits.
     *
     * EX:
     * {@code
     *   Lucene context: "This is a <b>matching</b> <b>string</b> with context"
     *   IIIF Hit: {
     *       match: "matching string",
     *       before: "This is a ",
     *       after: " with context"
     *   }
     * }
     *
     * @param contexts list of search contexts
     * @param matchId ID field from a search match
     * @return a list of IIIF Hits
     */
    protected List<IIIFSearchHit> getContextHits(List<String> contexts, String matchId) {
        List<IIIFSearchHit> hits = new ArrayList<>();
        String[] associatedAnnos = new String[] {getAnnotationId(matchId)};

        // Create Hit objects from the context
        for (String context : contexts) {
            String tmp = context.toLowerCase();     // In case <B> appears instead of <b>
            int start = tmp.indexOf("<b>");
            int end = 0;
            while (start >= 0 && start < tmp.length()) {
                String hit_before = context.substring((end == 0 ? end : end+4), start);
                end = tmp.indexOf("</b>", start);
                String hit_match = context.substring(start + 3, end);
                start = tmp.indexOf("<b>", end);

                String hit_after;
                if (start > context.length() || start < 0) {
                    hit_after = context.substring(end + 4);
                } else {
                    hit_after = context.substring(end + 4, start);
                }

                hits.add(new IIIFSearchHit(associatedAnnos, hit_match, hit_before, hit_after));
            }
        }

        int i = 0;
        while (i < hits.size() - 1) {
            IIIFSearchHit hit1 = hits.get(i);
            IIIFSearchHit hit2 = hits.get(i + 1);

            if (isEmpty(hit1.after) && isEmpty(hit2.before)) {
                // If (after hit1) == (before hit2) == (blank), then merge the 2 hits
                hits.remove(hit1);
                hits.remove(hit2);
                hits.add(i, new IIIFSearchHit(
                        associatedAnnos, (hit1.matching + " " + hit2.matching), hit1.before, hit2.after
                ));

                i--;
            }

            i++;
        }

        return hits;
    }

    private boolean isEmpty(String str) {
        return str == null || str.isEmpty() || str.matches("^\\s+$");
    }

    /**
     * TODO for now, assume ALL category. this will change when faceted search is implemented
     *
     * @param queryFragment query fragment in question
     * @return search category associated with the fragment
     */
    private SearchCategory getSearchCategory(String queryFragment) {
        return SearchCategory.ALL;
    }

    /**
     * TODO for now, just return the fragment. this may change when faceted search is implemented
     * @param queryFrag part of query term from request
     * @return keyword to search for
     */
    private String getSearchTerm(String queryFrag) {
        return queryFrag;
    }

    // ----- Name parsing -----

    private String getAnnotationCollection(String matchId) {
        return SearchUtil.getCollectionFromId(matchId);
    }

    private String getAnnotationBook(String matchId) {
        return SearchUtil.getBookFromId(matchId);
    }

    private String getAnnotationPage(String matchId) {
        return SearchUtil.getImageFromId(matchId);
    }

    private String getAnnotationId(String matchId) {
        return SearchUtil.getAnnotationFromId(matchId);
    }

    // ----- Caching -----

    private String cache_key(String id, Class<?> type) {
        return id + "," + type.getName();
    }

    private <T> T lookupCache(String id, Class<T> type) {
        return type.cast(cache.get(cache_key(id, type)));
    }

    private void updateCache(String id, Object value) {
        if (id == null || value == null) {
            return;
        }

        if (cache.size() > max_cache_size) {
            cache.clear();
        }

        cache.putIfAbsent(cache_key(id, value.getClass()), value);
    }

    private BookCollection getCollection(String matchId) throws IOException {
        String col_id = getAnnotationCollection(matchId);
        BookCollection result = lookupCache(col_id, BookCollection.class);

        if (result == null) {
            result = archiveStore.loadBookCollection(col_id, null);
            updateCache(col_id, result);
        }

        return result;
    }

    private Book getBook(String matchId) throws IOException {
        String col_id = getAnnotationCollection(matchId);
        String book_id = getAnnotationBook(matchId);

        Book result = lookupCache(book_id, Book.class);

        if (result == null) {
            BookCollection col = getCollection(col_id);

            if (col == null) {
                return null;
            }

            result = archiveStore.loadBook(col, book_id, null);
            updateCache(book_id, result);
        }

        return result;
    }

    private rosa.archive.model.aor.Annotation getArchiveAnnotation(String matchId) throws IOException {
        String annoId = getAnnotationId(matchId);

        rosa.archive.model.aor.Annotation anno = lookupCache(annoId, rosa.archive.model.aor.Annotation.class);

        if (anno == null) {
            Book book = getBook(matchId);

            if (book == null) {
                return null;
            }

            anno = Annotations.getArchiveAnnotation(book, annoId);
            updateCache(annoId, rosa.archive.model.aor.Annotation.class);
        }

        return anno;
    }
}
