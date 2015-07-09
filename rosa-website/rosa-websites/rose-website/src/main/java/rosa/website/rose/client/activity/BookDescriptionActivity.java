package rosa.website.rose.client.activity;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import rosa.archive.model.BookImage;
import rosa.website.core.client.ArchiveDataServiceAsync;
import rosa.website.core.client.ClientFactory;
import rosa.website.core.client.event.BookSelectEvent;
import rosa.website.core.client.place.BookDescriptionPlace;
import rosa.website.core.client.view.BookDescriptionView;
import rosa.website.core.client.widget.LoadingPanel;
import rosa.website.model.view.BookDescriptionViewModel;
import rosa.website.rose.client.WebsiteConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BookDescriptionActivity implements Activity, BookDescriptionView.Presenter {
    private static final Logger logger = Logger.getLogger(BookDescriptionActivity.class.toString());

    private final String bookName;
    private final String language;
    private final ArchiveDataServiceAsync service;
    private final BookDescriptionView view;
    // TODO need to keep this eventBus, or use the one on .start()?
    private com.google.web.bindery.event.shared.EventBus eventBus;

    private BookDescriptionViewModel model;

    /**
     * @param place initial state
     * @param clientFactory .
     */
    public BookDescriptionActivity(BookDescriptionPlace place, ClientFactory clientFactory) {
        this.bookName = place.getBook();
        this.language = LocaleInfo.getCurrentLocale().getLocaleName();
        this.service = clientFactory.archiveDataService();
        this.view = clientFactory.bookDescriptionView();
        this.eventBus = clientFactory.eventBus();
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {
        finishActivity();
    }

    @Override
    public void onStop() {
        finishActivity();
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        LoadingPanel.INSTANCE.show();
        this.eventBus.fireEvent(new BookSelectEvent(true, bookName));
        panel.setWidget(view);
        view.setPresenter(this);

        service.loadBookDescriptionModel(WebsiteConfig.INSTANCE.collection(), bookName, language,
                new AsyncCallback<BookDescriptionViewModel>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        logger.log(Level.SEVERE, "Failed to load book description. ["
                                + WebsiteConfig.INSTANCE.collection() + "," + bookName + "]");
                        LoadingPanel.INSTANCE.hide();
                    }

                    @Override
                    public void onSuccess(BookDescriptionViewModel result) {
                        model = result;

                        view.setMetadata(result.getMetadata());
                        view.setDescription(result.getProse());
                        LoadingPanel.INSTANCE.hide();
                    }
                });
    }

    @Override
    public String getPageUrlFragment(String page) {
        if (model == null || model.getImages() == null) {
            return null;
        }

        for (BookImage image : model.getImages()) {
            if (image.getName().equals(page)) {
                return "read;" + image.getId();
            }
        }

        return null;
    }

    @Override
    public String getPageUrlFragment(int page) {
        return getPageUrlFragment(page + "r");
    }

    private void finishActivity() {
        LoadingPanel.INSTANCE.hide();
        view.clear();
        eventBus.fireEvent(new BookSelectEvent(false, bookName));
    }
}