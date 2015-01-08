package rosa.iiif.presentation.endpoint;

import rosa.archive.core.ArchiveCoreModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class IIIFServletConfig extends GuiceServletContextListener {
    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new ArchiveCoreModule(), new IIIFServletModule());
    }
}
