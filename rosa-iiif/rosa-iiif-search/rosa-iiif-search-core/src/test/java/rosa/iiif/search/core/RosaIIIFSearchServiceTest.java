package rosa.iiif.search.core;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import rosa.archive.core.BaseArchiveTest;
import rosa.iiif.presentation.core.IIIFRequestFormatter;
import rosa.iiif.presentation.core.transform.impl.AnnotationTransformer;
import rosa.iiif.search.model.IIIFSearchHit;
import rosa.iiif.search.model.IIIFSearchRequest;
import rosa.iiif.search.model.IIIFSearchResult;
import rosa.search.core.LuceneSearchService;
import rosa.search.core.SearchService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RosaIIIFSearchServiceTest extends BaseArchiveTest {

    private RosaIIIFSearchService service;

    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();

    @Before
    public void setup() throws Exception {
        setupArchiveStore();

        SearchService luceneSearchService = new LuceneSearchService(tmpfolder.newFolder().toPath());
        luceneSearchService.update(store, VALID_COLLECTION);

        IIIFLuceneSearchAdapter adapter = new IIIFLuceneSearchAdapter(
                new AnnotationTransformer(new IIIFRequestFormatter("SCHEME", "HOST", "PREFIX", 80)),
                store
        );

        this.service = new RosaIIIFSearchService(luceneSearchService, adapter);
    }

    /**
     * Search for the term "Sun" in the test data. There should be 44 instances found,
     * 40 of them from Symbols, 4 from Marginalia.
     *
     * @throws Exception .
     */
    @Test
    public void validSearchTest() throws Exception {
        IIIFSearchResult result = service.search(new IIIFSearchRequest("Sun"));

        assertNotNull("Result is NULL.", result);
        assertEquals("Unexpected number of results found.", 44, result.getTotal());
        assertEquals("Unexpected number of Hits found.", 44, result.getHits().length);

        int marg_count = 0;
        int symbol_count = 0;
        for (IIIFSearchHit hit : result.getHits()) {
            if (hit.annotations[0].contains("marginalia")) {
                marg_count++;
            } else if (hit.annotations[0].contains("symbol")) {
                symbol_count++;
            }
        }
        assertEquals("Unexpected number of symbols found.", 40, symbol_count);
        assertEquals("Unexpected number of marginalia found.", 4, marg_count);
    }

}
