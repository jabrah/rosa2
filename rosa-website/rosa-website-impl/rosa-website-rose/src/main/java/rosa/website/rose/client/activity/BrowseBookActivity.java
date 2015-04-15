package rosa.website.rose.client.activity;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import rosa.website.core.client.ArchiveDataServiceAsync;
import rosa.website.core.client.ClientFactory;
import rosa.website.core.client.place.BrowseBookPlace;
import rosa.website.core.client.view.BrowseBookView;
import rosa.website.core.client.widget.FsiViewerHTMLBuilder;
import rosa.website.core.client.widget.FsiViewerType;
import rosa.website.rose.client.WebsiteConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BrowseBookActivity implements Activity {
    private static final Logger logger = Logger.getLogger(BrowseBookActivity.class.toString());

    private boolean useFlash;
    private String language;

    private String book;
    private FsiViewerType type;

    private BrowseBookView view;
    private ArchiveDataServiceAsync service;

    private int current_selected_image = 1;

    public BrowseBookActivity(BrowseBookPlace place, ClientFactory clientFactory) {
        this.useFlash = clientFactory.context().useFlash();
        this.language = clientFactory.context().getLanguage();
        this.service = clientFactory.archiveDataService();
        this.view = clientFactory.browseBookView();

        this.book = place.getBook();
        this.type = getViewerType(place.getType());
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(view);

        String collection = WebsiteConfig.INSTANCE.collection();
        String fsi_xml_url = GWT.getModuleBaseURL() + "fsi/"
                + collection
                + "/" + book
                + "/" + type.getXmlId();

        String fsiHtml = new FsiViewerHTMLBuilder()
                .book(collection, book, language)
                .type(type)
                .fsiBookData(URL.encode(fsi_xml_url))
                .build();

        view.setFlashViewer(fsiHtml);

        service.loadPermissionStatement(collection, book, language, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Failed to load permission statement.", caught);
            }

            @Override
            public void onSuccess(String result) {
                view.setPermissionStatement(result);
            }
        });

        view.useFlash(useFlash);
    }

    private FsiViewerType getViewerType(String type) {
        // TODO need a map for relationship: history token -> viewer type   instead of hard coding...
        switch (type) {
            case "browse":
                return FsiViewerType.SHOWCASE;
            case "read":
                return FsiViewerType.PAGES;
            case "pages":
                return FsiViewerType.PAGES;
            case "showcase":
                return FsiViewerType.SHOWCASE;
            default:
                return null;
        }
    }
}
