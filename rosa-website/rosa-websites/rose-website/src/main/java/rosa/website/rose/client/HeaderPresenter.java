package rosa.website.rose.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import rosa.website.core.client.ClientFactory;
import rosa.website.core.client.Labels;
import rosa.website.core.client.event.SidebarItemSelectedEvent;
import rosa.website.core.client.place.AdvancedSearchPlace;
import rosa.website.core.client.view.HeaderViewWithSearch;
import rosa.website.core.client.view.HeaderViewWithSearch.Presenter;

public class HeaderPresenter implements Presenter, IsWidget {

    private final HeaderViewWithSearch view;
    private final EventBus eventBus;

    /**
     * @param clientFactory .
     */
    public HeaderPresenter(final ClientFactory clientFactory) {
        this.eventBus = clientFactory.eventBus();
        final Labels labels = Labels.INSTANCE;

        this.view = clientFactory.headerViewWithSearch();
        view.setPresenter(this);

        view.addHeaderImage(GWT.getModuleBaseURL() + "banner_text.jpg", "Roman de la Rose Digital Library");
        view.addHeaderImage(GWT.getModuleBaseURL() + "banner_image1.gif", "");

        view.setSearchButtonText(labels.search());
        view.addAdvancedSearchLink(labels.advancedSearch(), "search;");
        view.addSearchClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String searchToken = view.getSearchToken();

                // Only go to search place if there is something in the search box.
                if (searchToken != null && !searchToken.trim().isEmpty()) {
                    clientFactory.placeController().goTo(new AdvancedSearchPlace(searchToken));
                }
            }
        });

        view.addSearchKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getUnicodeCharCode() != KeyCodes.KEY_ENTER) {
                    return;
                }

                String searchToken = view.getSearchToken();
                if (searchToken != null && !searchToken.trim().isEmpty()) {
                    clientFactory.placeController().goTo(new AdvancedSearchPlace(searchToken));
                }
            }
        });
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    @Override
    public void goHome() {
        eventBus.fireEvent(new SidebarItemSelectedEvent(null));
        History.newItem("home");
    }

}
