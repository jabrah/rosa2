package rosa.website.rose.client.activity;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import rosa.archive.model.Book;
import rosa.archive.model.BookImage;
import rosa.website.core.client.ArchiveDataServiceAsync;
import rosa.website.core.client.ClientFactory;
import rosa.website.core.client.Labels;
import rosa.website.core.client.event.BookSelectEvent;
import rosa.website.core.client.place.BookViewerPlace;
import rosa.website.core.client.view.FSIViewerView;
import rosa.website.core.client.widget.FSIViewer.FSIPagesCallback;
import rosa.website.core.client.widget.FSIViewer.FSIShowcaseCallback;
import rosa.website.core.client.widget.FSIViewerHTMLBuilder;
import rosa.website.core.client.widget.FSIViewerType;
import rosa.website.rose.client.WebsiteConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FSIViewerActivity implements Activity, FSIViewerView.Presenter {
    private static final Logger logger = Logger.getLogger(FSIViewerActivity.class.toString());
    private static final String FSI_URL_PREFIX = GWT.getModuleBaseURL() + "fsi/";

    private String language;

    private String book;
    private String starterPage;
    private FSIViewerType type;

    private FSIViewerView view;
    private ArchiveDataServiceAsync service;
    private final com.google.web.bindery.event.shared.EventBus eventBus;

    private ScheduledCommand resizeCommand = new ScheduledCommand() {
        @Override
        public void execute() {
            view.onResize();
        }
    };

    private Book b;
    private int current_image_index;

    /** Callback for FSI showcase view. */
    private final FSIShowcaseCallback showcaseCallback = new FSIShowcaseCallback() {
        @Override
        public void imageSelected(int image) {
            view.setGotoLabel(getImageName(image, b));
        }
    };

    /** Callback for FSI pages view. */
    private final FSIPagesCallback pagesCallback = new FSIPagesCallback() {
        @Override
        public void pageChanged(int page) {
            // Update goto box  with label
            current_image_index = page;

            if (page == b.getImages().getImages().size()) {
                view.setGotoLabel(getImageName(page, b));
            } else {
                StringBuilder sb = new StringBuilder();
                if (page > 0) {
                    sb.append(getImageName(page - 1, b));
                    sb.append(',');
                }
                sb.append(getImageName(page, b));

                view.setGotoLabel(sb.toString());
            }

            // Update transcription display thingy ('show extra' labels)
        }

        @Override
        public void imageInfo(String info) {
            current_image_index = getImageIndexFromPagesInfo(info);
            view.setGotoLabel(getImageName(current_image_index, b));
        }
    };

    /**
     * @param place state info
     * @param clientFactory .
     */
    public FSIViewerActivity(BookViewerPlace place, ClientFactory clientFactory) {
        this.language = LocaleInfo.getCurrentLocale().getLocaleName();
        this.service = clientFactory.archiveDataService();
        this.view = clientFactory.bookViewerView();
        this.current_image_index = 0;
        this.starterPage = place.getPage();
        this.book = place.getBook();
        this.type = getViewerType(place.getType());
        this.eventBus = clientFactory.eventBus();
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {
        this.eventBus.fireEvent(new BookSelectEvent(false, book));
    }

    @Override
    public void onStop() {
        this.eventBus.fireEvent(new BookSelectEvent(false, book));
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        this.eventBus.fireEvent(new BookSelectEvent(true, book));
        panel.setWidget(view);

        service.loadBook(WebsiteConfig.INSTANCE.collection(), book, new AsyncCallback<Book>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Failed to load book.", caught);
            }

            @Override
            public void onSuccess(Book result) {
                b = result;
                view.setPermissionStatement(b.getPermission(language).getPermission());
                Scheduler.get().scheduleDeferred(resizeCommand);

                if (starterPage != null && !starterPage.isEmpty()) {
                    setupFlashViewer(getImageIndex(starterPage, b));
                } else {
                    setupFlashViewer(-1);
                }
            }
        });
    }

    @Override
    public String[] getExtraDataLabels(String page) {
        if (b == null) {
            return new String[0];
        }

        List<String> labels = new ArrayList<>();
        if (b.getTranscription() != null) { // TODO per page
            labels.add(Labels.INSTANCE.transcription());
        }
        // TODO transcription (Lecoy)
        if (b.hasIllustrationTagging(page)) {
            labels.add(Labels.INSTANCE.illustrationDescription());
        }
        if (b.hasNarrativeTagging(page)) {
            labels.add(Labels.INSTANCE.narrativeSections());
        }

        return labels.toArray(new String[labels.size()]);
    }

    public String getCurrentPage() {
        return getImageName(current_image_index, b);
    }

    private void setupFlashViewer(int startPage) {
        String collection = WebsiteConfig.INSTANCE.collection();
        String fsi_xml_url = FSI_URL_PREFIX + collection + "/" + book + "/" + type.getXmlId();

        String fsiHtml = new FSIViewerHTMLBuilder()
                .book(collection, book, language)
                .type(type)
                .fsiBookData(URL.encode(fsi_xml_url))
                .initialImage(startPage == -1 ? 0 : startPage)
                .build();

        view.setFlashViewer(fsiHtml, type);

        if (type == FSIViewerType.SHOWCASE) {
            view.addShowcaseToolbar();
            view.setupFsiShowcaseCallback(showcaseCallback);
            view.addGotoKeyDownHandler(new KeyDownHandler() {
                @Override
                public void onKeyDown(KeyDownEvent event) {
                    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                        int index = getImageIndex(view.getGotoText(), b);
                        if (index >= 0) {
                            view.fsiViewerSelectImage(index);
                        }
                    }
                }
            });
        } else if (type == FSIViewerType.PAGES) {
            view.addPagesToolbar();
            view.setupFsiPagesCallback(pagesCallback);
            view.addGotoKeyDownHandler(new KeyDownHandler() {
                @Override
                public void onKeyDown(KeyDownEvent event) {
                    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                        int index = getImageIndex(view.getGotoText(), b);

                        if (index >= 0) {
                            view.fsiViewerGotoImage(index + 1);
                        }
                    }
                }
            });
        }


    }

    /**
     * @param index index of page in the book
     * @param book .
     * @return short name of a page by index, if it exists. Empty string, otherwise
     */
    private String getImageName(int index, Book book) {
        if (book == null || book.getImages() == null || book.getImages().getImages() == null
                || book.getImages().getImages().size() < index || book.getImages().getImages().get(index) == null) {
            return "";
        }

        return book.getImages().getImages().get(index).getName();
    }

    /**
     * @param name name of image
     * @param book .
     * @return index of image in the book, if it exists. -1 otherwise.
     */
    private int getImageIndex(String name, Book book) {
        if (book != null && book.getImages() != null && book.getImages().getImages() != null) {
            for (int i = 0; i < book.getImages().getImages().size(); i++) {
                BookImage image = book.getImages().getImages().get(i);
                if (image.getName().equals(name) || image.getId().equals(name)) {
                    return i;
                }
            }
        }

        return -1;
    }

    private FSIViewerType getViewerType(String type) {
        switch (type) {
            case "browse":
                return FSIViewerType.SHOWCASE;
            case "read":
                return FSIViewerType.PAGES;
            case "pages":
                return FSIViewerType.PAGES;
            case "showcase":
                return FSIViewerType.SHOWCASE;
            default:
                return null;
        }
    }

    private int getImageIndexFromShowcaseInfo(String info) {
        String marker = "ImageIndex=";
        int i = info.indexOf(marker);

        if (i == -1) {
            return 0;
        }

        return Integer.parseInt(info.substring(i + marker.length()));
    }

    private int getImageIndexFromPagesInfo(String info) {
        int result = getImageIndexFromShowcaseInfo(info) - 1;

        if (result < 0) {
            // TODO this should not happen
            result = 0;
        }

        return result;
    }
}
