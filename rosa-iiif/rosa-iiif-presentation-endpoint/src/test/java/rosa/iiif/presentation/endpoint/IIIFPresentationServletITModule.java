package rosa.iiif.presentation.endpoint;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import rosa.archive.core.FSByteStreamGroup;
import rosa.archive.core.Store;
import rosa.archive.core.StoreImpl;
import rosa.archive.core.check.BookChecker;
import rosa.archive.core.check.BookCollectionChecker;
import rosa.archive.core.serialize.SerializerSet;
import rosa.iiif.presentation.core.IIIFPresentationRequestFormatter;


/**
 * Guice module for IIIF Presentation API IT 
 */

public class IIIFPresentationServletITModule extends AbstractModule {
    @Override
    protected void configure() {
        Names.bindProperties(binder(), System.getProperties());
    }

    @Provides
    public Store store(SerializerSet serializerSet, BookChecker bookChecker, BookCollectionChecker collectionChecker,
            @Named("archive.path") String archive_path) {
        return new StoreImpl(serializerSet, bookChecker, collectionChecker, new FSByteStreamGroup(archive_path));
    }

    @Provides
    IIIFPresentationRequestFormatter provideIIIFPresentationRequestFormatter() {
        return new IIIFPresentationRequestFormatter("http", "localhost", "/iiif-pres", 9090);
    }
}
