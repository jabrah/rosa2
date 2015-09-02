package rosa.website.rose.client.activity;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import rosa.website.core.client.ArchiveDataServiceAsync;
import rosa.website.core.client.ClientFactory;
import rosa.website.core.client.Labels;
import rosa.website.core.client.place.BookSelectPlace;
import rosa.website.core.client.view.BookSelectView;
import rosa.website.core.client.widget.LoadingPanel;
import rosa.website.model.select.BookSelectList;
import rosa.website.model.select.SelectCategory;
import rosa.website.rose.client.WebsiteConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Activity for selecting books by sorting through criteria.
 */
public class BookSelectActivity implements Activity {
    private static final Logger logger = Logger.getLogger(BookSelectActivity.class.toString());

    private final BookSelectView view;
    private final SelectCategory category;

    private final ArchiveDataServiceAsync service;

    /**
     * Create a new BookSelectActivity
     *
     * @param place initial state of activity
     * @param clientFactory .
     */
    public BookSelectActivity(BookSelectPlace place, ClientFactory clientFactory) {
        this.view = clientFactory.bookSelectView();
        this.category = place.getCategory();
        this.service = clientFactory.archiveDataService();
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {
        LoadingPanel.INSTANCE.hide();
    }

    @Override
    public void onStop() {
        LoadingPanel.INSTANCE.hide();
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(view);
        LoadingPanel.INSTANCE.show();

        view.setHeaderText(getHeader(category));
        service.loadBookSelectionData(
                WebsiteConfig.INSTANCE.collection(),
                category,
                LocaleInfo.getCurrentLocale().getLocaleName(),
                new AsyncCallback<BookSelectList>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        logger.log(Level.SEVERE, "Failed to load book selection data.", caught);
                        LoadingPanel.INSTANCE.hide();
                    }

                    @Override
                    public void onSuccess(BookSelectList result) {
                        LoadingPanel.INSTANCE.hide();
                        result.setCategory(category);
                        view.setData(result);
                    }
                }
        );

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                view.onResize();
            }
        });
    }

    private String getHeader(SelectCategory category) {
        Labels labels = Labels.INSTANCE;

        String header = labels.selectBook();
        switch (category) {
            case REPOSITORY:
                header += ": " + labels.repository();
                break;
            case SHELFMARK:
                header += ": " + labels.shelfmark();
                break;
            case COMMON_NAME:
                header += ": " + labels.commonName();
                break;
            case LOCATION:
                header += ": " + labels.currentLocation();
                break;
            case DATE:
                header += ": " + labels.date();
                break;
            case ORIGIN:
                header += ": " + labels.origin();
                break;
            case TYPE:
                header += ": " + labels.type();
                break;
            case NUM_ILLUSTRATIONS:
                header += ": " + labels.numIllustrations();
                break;
            case NUM_FOLIOS:
                header += ": " + labels.numFolios();
                break;
            case TRANSCRIPTION:
                header += ": " + labels.transcription();
                break;
            case BIBLIOGRAPHY:
            case NARRATIVE_TAGGING:
            case ILLUSTRATION_TAGGING:
            case ID:
            default:
                break;
        }

        return header;
    }
}
