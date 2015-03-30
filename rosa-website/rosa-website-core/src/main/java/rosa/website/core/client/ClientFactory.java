package rosa.website.core.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import rosa.website.core.client.view.CSVDataView;
import rosa.website.core.client.view.HTMLView;
import rosa.website.core.client.view.impl.CSVDataViewImpl;
import rosa.website.core.client.view.impl.HTMLViewImpl;

public class ClientFactory {
    private static EventBus event_bus = new SimpleEventBus();
    private static PlaceController place_controller = new PlaceController(event_bus);
    private final ArchiveDataServiceAsync archiveDataService = GWT.create(ArchiveDataService.class);

    private static HTMLView htmlView;
    private static CSVDataView csvDataView;

    public EventBus eventBus() {
        return event_bus;
    }

    public PlaceController placeController() {
        return place_controller;
    }

    public ArchiveDataServiceAsync archiveDataService() {
        return archiveDataService;
    }

    public HTMLView htmlView() {
        if (htmlView == null) {
            htmlView = new HTMLViewImpl();
        }
        return htmlView;
    }

    public CSVDataView csvDataView() {
        if (csvDataView == null) {
            csvDataView = new CSVDataViewImpl();
        }
        return csvDataView;
    }
}
