package rosa.website.core.client.activity;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import rosa.website.core.client.ArchiveDataServiceAsync;
import rosa.website.core.client.ClientFactory;
import rosa.website.core.client.place.CSVDataPlace;
import rosa.website.core.client.view.CSVDataView;
import rosa.website.model.csv.BookDataCSV;
import rosa.website.model.csv.CSVData;
import rosa.website.model.csv.CollectionCSV;
import rosa.website.model.csv.IllustrationTitleCSV;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CSVDataActivity implements Activity {
    private static final Logger logger = Logger.getLogger(CSVDataActivity.class.toString());

    private enum CsvType {
        COLLECTION_DATA("collection_data.csv"),
        COLLECTION_BOOKS("books.csv"),
        ILLUSTRATIONS("illustration_titles.csv");

        private String key;

        CsvType(String key) {
            this.key = key;
        }
    }

    private final CSVDataPlace place;
    private CSVDataView view;

    private ArchiveDataServiceAsync service;

    /**
     * Create a new CSVDataActivity.
     *
     * @param place state information
     * @param clientFactory .
     */
    public CSVDataActivity(CSVDataPlace place, ClientFactory clientFactory) {
        this.place = place;
        this.view = clientFactory.csvDataView();
        this.service = clientFactory.archiveDataService();
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
        view.clear();
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        logger.info("Starting CSVDataActivity. Current state: " + place.toString());
        panel.setWidget(view);

        CsvType type = getType(place.getName());
        switch (type) {
            case COLLECTION_DATA:
                service.loadCollectionData(place.getCollection(), null, new AsyncCallback<CollectionCSV>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        logger.log(Level.SEVERE, "Failed to load collection data.", caught);
                    }

                    @Override
                    public void onSuccess(CollectionCSV result) {
                        handleCsvData(result);
                    }
                });
                break;
            case COLLECTION_BOOKS:
                service.loadCollectionBookData(place.getCollection(), null, new AsyncCallback<BookDataCSV>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        logger.log(Level.SEVERE, "Failed to load collection book data.", caught);
                    }

                    @Override
                    public void onSuccess(BookDataCSV result) {
                        handleCsvData(result);
                    }
                });
                break;
            case ILLUSTRATIONS:
                service.loadIllustrationTitles(place.getCollection(), new AsyncCallback<IllustrationTitleCSV>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        logger.log(Level.SEVERE, "Failed to load illustration titles data.", caught);
                    }

                    @Override
                    public void onSuccess(IllustrationTitleCSV result) {
                        handleCsvData(result);
                    }
                });
                break;
            default:
                break;
        }
    }

    private <T> void handleCsvData(CSVData<T> data) {
        // TODO
        logger.fine("Done CSVDataActivity.\n" + data.toString());
        view.setData("Data found. " + data.toString());
    }

    private CsvType getType(String name) {
        for (CsvType type : CsvType.values()) {
            if (type.key.equals(name)) {
                return type;
            }
        }

        return null;
    }
}
