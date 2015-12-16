package rosa.iiif.presentation.core.transform.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import rosa.archive.model.Book;
import rosa.archive.model.BookCollection;
import rosa.archive.model.BookImage;
import rosa.iiif.presentation.core.IIIFRequestFormatter;
import rosa.iiif.presentation.core.transform.Transformer;
import rosa.iiif.presentation.model.AnnotationListType;
import rosa.iiif.presentation.model.IIIFNames;
import rosa.iiif.presentation.model.Layer;
import rosa.iiif.presentation.model.PresentationRequestType;

import java.util.ArrayList;
import java.util.List;

// TODO finish implementing
public class LayerTransformer extends BasePresentationTransformer implements Transformer<Layer>, IIIFNames {

    @Inject
    public LayerTransformer(@Named("formatter.presentation") IIIFRequestFormatter presRequestFormatter) {
        super(presRequestFormatter);
    }

    @Override
    public Layer transform(BookCollection collection, Book book, String name) {
        return layer(collection, book, name);
    }

    @Override
    public Class<Layer> getType() {
        return Layer.class;
    }

    /**
     * Build a layer specified by its name from data in a book and the collection
     * that contains it.
     *
     * @param collection book collection
     * @param book book
     * @param name name of the layer
     * @return a IIIF layer
     */
    private Layer layer(BookCollection collection, Book book, String name) {
        Layer layer = new Layer();

        layer.setId(urlId(collection.getId(), book.getId(), name, PresentationRequestType.LAYER));
        layer.setType(SC_LAYER);
        layer.setLabel(name, "en");

        if (AnnotationListType.getType(name) != null) {
            List<String> otherContent = new ArrayList<>();
            for (BookImage image : book.getImages()) {
                String id = image.getId();

                otherContent.add(urlId(collection.getId(), book.getId(), annotationListName(id, name),
                        PresentationRequestType.LAYER));
            }
            layer.setOtherContent(otherContent);
        }

        return layer;
    }

    private String annotationListName(String page, String listType) {
        return page + (listType == null ? "" : "." + listType);
    }
}
