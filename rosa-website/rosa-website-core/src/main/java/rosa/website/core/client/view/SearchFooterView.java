package rosa.website.core.client.view;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;

public interface SearchFooterView extends IsWidget {
    public interface Presenter {}

    void setSearchButtonText(String text);

    /**
     * Add a click handler to the "search" button in the header to define
     * its behavior.
     *
     * @param handler click handler
     * @return .
     */
    HandlerRegistration addSearchClickHandler(ClickHandler handler);

    HandlerRegistration addSearchKeyPressHandler(KeyPressHandler handler);

    /**
     * Add a link to "advanced search" in the header.
     *
     * @param displayText text to display to user
     * @param targetHistoryToken target history token
     */
    void addAdvancedSearchLink(String displayText, String targetHistoryToken);

    String getSearchToken();
}
