package rosa.iiif.presentation.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import rosa.archive.core.BaseSearchTest;
import rosa.iiif.presentation.core.transform.PresentationTransformer;
import rosa.iiif.presentation.core.transform.impl.CollectionTransformer;
import rosa.iiif.presentation.core.transform.impl.JsonldSerializer;
import rosa.iiif.presentation.core.transform.impl.PresentationTransformerImpl;
import rosa.iiif.presentation.model.PresentationRequest;
import rosa.iiif.presentation.model.PresentationRequestType;

/**
 * Evaluate service against test data from rosa-archive-core.
 */
public class ArchiveIIIFPresentationServiceTest extends BaseSearchTest {
    private static ArchiveIIIFPresentationService service;

    @BeforeClass
    public static void setup() throws Exception {
        JsonldSerializer serializer = new JsonldSerializer();

        String scheme = "http";
        String host = "serenity.dkc.jhu.edu";
        int port = 80;
        String pres_prefix = "/pres";
        String image_prefix = "/image";
//        String search_prefix = "/search";

        IIIFPresentationRequestFormatter requestFormatter = new IIIFPresentationRequestFormatter(scheme, host, pres_prefix, port);

        rosa.iiif.image.core.IIIFRequestFormatter imageFormatter = new rosa.iiif.image.core.IIIFRequestFormatter(
                scheme, host, port, image_prefix);
        ImageIdMapper imageIdMapper = new JhuImageIdMapper(new HashMap<String, String>());

        CollectionTransformer collectionTransformer = new CollectionTransformer(requestFormatter, simpleStore, imageFormatter, imageIdMapper);

        PresentationTransformer transformer = new PresentationTransformerImpl(requestFormatter,
                PresentationTestUtils.transformerSet(requestFormatter, imageFormatter, imageIdMapper),
                collectionTransformer);

        service = new ArchiveIIIFPresentationService(simpleStore, serializer, transformer, 1000);
    }
    
    // TODO More extensive testing
    
    @Test
    public void testLudwigXV7ManifestRequest() throws IOException {
        String id = VALID_COLLECTION + "." + VALID_BOOK_LUDWIGXV7;
        PresentationRequest req = new PresentationRequest(id, null, PresentationRequestType.MANIFEST);
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertTrue(service.handle_request(req, os));
     
        String result = new String(os.toByteArray(), "UTF-8");
        
        JSONObject json = new JSONObject(result);
        
        assertEquals("http://iiif.io/api/presentation/2/context.json", json.get("@context"));
        assertTrue(json.has("sequences"));
        assertTrue(result.contains("otherContent"));
//        assertTrue(json.has("structures"));
    }
    
    @Test
    public void testFolgersHa2ManifestRequest() throws IOException {
        String id = VALID_COLLECTION + "." + VALID_BOOK_FOLGERSHA2;
        PresentationRequest req = new PresentationRequest(id, null, PresentationRequestType.MANIFEST);
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertTrue(service.handle_request(req, os));
     
        String result = new String(os.toByteArray(), "UTF-8");
        
        JSONObject json = new JSONObject(result);
        
        assertEquals("http://iiif.io/api/presentation/2/context.json", json.get("@context"));        
        assertTrue(json.has("sequences"));
//        assertTrue(json.has("structures"));
    }
}
