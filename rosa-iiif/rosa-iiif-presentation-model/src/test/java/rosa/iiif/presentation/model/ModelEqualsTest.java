package rosa.iiif.presentation.model;

import org.junit.Test;

import rosa.iiif.presentation.model.annotation.Annotation;
import rosa.iiif.presentation.model.annotation.AnnotationSource;
import rosa.iiif.presentation.model.annotation.AnnotationTarget;
import rosa.iiif.presentation.model.search.HitSelector;
import rosa.iiif.presentation.model.search.IIIFSearchHit;
import rosa.iiif.presentation.model.search.IIIFSearchRequest;
import rosa.iiif.presentation.model.search.IIIFSearchResult;
import rosa.iiif.presentation.model.search.Rectangle;
import rosa.iiif.presentation.model.selector.FragmentSelector;
import rosa.iiif.presentation.model.selector.SvgSelector;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class ModelEqualsTest {
    @Test
    public void testAnnotationList() {
        EqualsVerifier.forClass(AnnotationList.class).allFieldsShouldBeUsed().withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }
    
    @Test
    public void testCanvas() {
        EqualsVerifier.forClass(Canvas.class).allFieldsShouldBeUsed().withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }
    
    @Test
    public void testCollection() {
        EqualsVerifier.forClass(Collection.class).allFieldsShouldBeUsed().withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }
    
    @Test
    public void testHtmlValue() {
        EqualsVerifier.forClass(HtmlValue.class).allFieldsShouldBeUsed()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }
    
    @Test
    public void testIIIFImageService() {
        EqualsVerifier.forClass(IIIFImageService.class).allFieldsShouldBeUsed().withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void testLayer() {
        EqualsVerifier.forClass(Layer.class).allFieldsShouldBeUsed().withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }
    
    @Test
    public void testManifest() {
        EqualsVerifier.forClass(Manifest.class).allFieldsShouldBeUsed().withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }
    
    @Test
    public void testPresentationRequest() {
        EqualsVerifier.forClass(PresentationRequest.class).allFieldsShouldBeUsed()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }
    
    @Test
    public void testRange() {
        EqualsVerifier.forClass(Range.class).allFieldsShouldBeUsed().withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }
    
    @Test
    public void testReference() {
        EqualsVerifier.forClass(Reference.class).allFieldsShouldBeUsed()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }
    
    @Test
    public void testPresentationBase() {
        EqualsVerifier.forClass(PresentationBase.class).allFieldsShouldBeUsed()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void testSequence() {
        EqualsVerifier.forClass(Sequence.class).allFieldsShouldBeUsed().withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }
    
    @Test
    public void testService() {
        EqualsVerifier.forClass(Service.class).allFieldsShouldBeUsed()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }
    
    @Test
    public void testTextValue() {
        EqualsVerifier.forClass(TextValue.class).allFieldsShouldBeUsed()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }
    
    @Test
    public void testAnnotation() {
        EqualsVerifier.forClass(Annotation.class).allFieldsShouldBeUsed().withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }
    
    @Test
    public void testAnnotationSource() {
        EqualsVerifier.forClass(AnnotationSource.class).allFieldsShouldBeUsed()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void testAnnotationTarget() {
        EqualsVerifier.forClass(AnnotationTarget.class).allFieldsShouldBeUsed()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }
    
    @Test
    public void testFragmentSelector() {
        EqualsVerifier.forClass(FragmentSelector.class).allFieldsShouldBeUsed()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }
    
    @Test
    public void testSvgSelector() {
        EqualsVerifier.forClass(SvgSelector.class).allFieldsShouldBeUsed()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void testIIIFSearchRequest() {
        EqualsVerifier.forClass(IIIFSearchRequest.class).allFieldsShouldBeUsed()
                .usingGetClass()
//                .suppress(Warning.NONFINAL_FIELDS, Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void testRectangle() {
        EqualsVerifier.forClass(Rectangle.class).allFieldsShouldBeUsed()
                .usingGetClass()
                .verify();
    }

    @Test
    public void testIIIFSearchHit() {
        EqualsVerifier.forClass(IIIFSearchHit.class).allFieldsShouldBeUsed()
                .usingGetClass()
                .verify();
    }

    @Test
    public void testIIIFSearchResult() {
        EqualsVerifier.forClass(IIIFSearchResult.class).allFieldsShouldBeUsed()
                .suppress(Warning.NONFINAL_FIELDS)
                .usingGetClass()
                .verify();
    }

    @Test
    public void testHitSelector() {
        EqualsVerifier.forClass(HitSelector.class).allFieldsShouldBeUsed()
                .suppress(Warning.NONFINAL_FIELDS)
                .usingGetClass()
                .verify();
    }

    @Test
    public void testRights() {
        EqualsVerifier.forClass(Rights.class).allFieldsShouldBeUsed()
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
    }

}
