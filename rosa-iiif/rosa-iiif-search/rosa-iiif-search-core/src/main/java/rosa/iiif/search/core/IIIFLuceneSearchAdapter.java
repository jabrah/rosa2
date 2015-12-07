package rosa.iiif.search.core;

import rosa.iiif.search.model.IIIFSearchRequest;
import rosa.iiif.search.model.SearchCategory;
import rosa.search.model.Query;
import rosa.search.model.QueryOperation;
import rosa.search.model.SearchFields;

import java.util.ArrayList;
import java.util.List;

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

    public IIIFLuceneSearchAdapter() {}

    /**
     * Transform a IIIF search request to a Lucene search query.
     *
     * @param iiifReq IIIF Search request
     * @return a Lucene query
     */
    public Query iiifToLuceneQuery(IIIFSearchRequest iiifReq) {
        // This will generate independent queries for each word...
        List<Query> top = new ArrayList<>();
        for (String queryTerm : iiifReq.queryTerms) {
            if (queryTerm == null || queryTerm.isEmpty()) {
                continue;
            }

            SearchCategory category = getSearchCategory(queryTerm);

            List<Query> children = new ArrayList<>();
            for (SearchFields luceneFields : category.luceneFields) {
                children.add(new Query(luceneFields, getSearchTerm(queryTerm)));
            }

            top.add(new Query(QueryOperation.OR, children.toArray(new Query[children.size()])));
        }

        return new Query(QueryOperation.AND, top.toArray(new Query[top.size()]));
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
}
