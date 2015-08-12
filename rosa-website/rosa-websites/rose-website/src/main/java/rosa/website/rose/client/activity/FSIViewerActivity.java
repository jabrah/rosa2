package rosa.website.rose.client.activity;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import rosa.archive.model.BookImage;
import rosa.website.core.client.ArchiveDataServiceAsync;
import rosa.website.core.client.ClientFactory;
import rosa.website.core.client.Labels;
import rosa.website.core.client.event.BookSelectEvent;
import rosa.website.core.client.place.BookViewerPlace;
import rosa.website.core.client.view.FSIViewerView;
import rosa.website.core.client.widget.LoadingPanel;
import rosa.website.core.client.widget.TranscriptionViewer;
import rosa.website.viewer.client.fsiviewer.FSIViewer.FSIPagesCallback;
import rosa.website.viewer.client.fsiviewer.FSIViewer.FSIShowcaseCallback;
import rosa.website.viewer.client.fsiviewer.FSIViewerHTMLBuilder;
import rosa.website.viewer.client.fsiviewer.FSIViewerType;
import rosa.website.model.view.FSIViewerModel;
import rosa.website.rose.client.WebsiteConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FSIViewerActivity implements Activity {

    public enum DisplayCategory {   // TODO move to website model
        NONE(Labels.INSTANCE.show()),
        TRANSCRIPTION(Labels.INSTANCE.transcription()),
        LECOY(Labels.INSTANCE.transcription() + "[" + Labels.INSTANCE.lecoy() + "]"),
        ILLUSTRATION(Labels.INSTANCE.illustrationDescription()),
        NARRATIVE(Labels.INSTANCE.narrativeSections());

        DisplayCategory(String display) {
            this.display = display;
        }

        public final String display;

        public static DisplayCategory category(String label) {
            for (DisplayCategory c : DisplayCategory.values()) {
                if (c.display.equals(label)) {
                    return c;
                }
            }
            return null;
        }
    }

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

    private FSIViewerModel model;
    private int current_image_index;
    private DisplayCategory showExtraCategory;

    private final ChangeHandler showExtraChangeHandler = new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
            DisplayCategory category = DisplayCategory.category(view.getSelectedShowExtra());
            if (category == null) {
                return;
            }

            showExtraCategory = category;
            handleShowExtra();
        }
    };

    /** Callback for FSI showcase view. */
    private final FSIShowcaseCallback showcaseCallback = new FSIShowcaseCallback() {
        @Override
        public void imageSelected(int image) {
            view.setGotoLabel(getImageName(image));
            setupShowExtra(image, false);
        }
    };

    /** Callback for FSI pages view. */
    private final FSIPagesCallback pagesCallback = new FSIPagesCallback() {
        @Override
        public void pageChanged(int page) {
            // Update goto box  with label
            current_image_index = page;

            if (page == model.getImages().getImages().size()) {
                view.setGotoLabel(getImageName(page));
            } else {
                StringBuilder sb = new StringBuilder();
                if (page > 0) {
                    sb.append(getImageName(page - 1));
                    sb.append(',');
                }
                sb.append(getImageName(page));

                view.setGotoLabel(sb.toString());
            }

            // Update transcription display thingy ('show extra' labels)
            setupShowExtra(page, true);
        }

        @Override
        public void imageInfo(String info) {
            current_image_index = getImageIndexFromPagesInfo(info);
            view.setGotoLabel(getImageName(current_image_index));
            setupShowExtra(current_image_index, false);
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

        this.showExtraCategory = DisplayCategory.NONE;
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {
        this.eventBus.fireEvent(new BookSelectEvent(false, book));
        LoadingPanel.INSTANCE.hide();
    }

    @Override
    public void onStop() {
        this.eventBus.fireEvent(new BookSelectEvent(false, book));
        LoadingPanel.INSTANCE.hide();
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        LoadingPanel.INSTANCE.show();
        this.eventBus.fireEvent(new BookSelectEvent(true, book));
        panel.setWidget(view);

        service.loadFSIViewerModel(WebsiteConfig.INSTANCE.collection(), book, language,
                new AsyncCallback<FSIViewerModel>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        logger.log(Level.SEVERE, "Failed to load FSI data.");
                        LoadingPanel.INSTANCE.hide();
                    }

                    @Override
                    public void onSuccess(FSIViewerModel result) {
                        model = result;
                        view.setPermissionStatement(result.getPermission().getPermission());
                        Scheduler.get().scheduleDeferred(resizeCommand);

                        if (starterPage != null && !starterPage.isEmpty()) {
                            setupFlashViewer(getImageIndex(starterPage));
                        } else {
                            setupFlashViewer(-1);
                        }
                        LoadingPanel.INSTANCE.hide();
                    }
                });
    }

    public String[] getExtraDataLabels(int page) {
        return getExtraDataLabels(getImageName(page));
    }

    private String[] getExtraDataLabels(String page) {
        if (model == null) {
            return new String[] {Labels.INSTANCE.show()};
        }

        List<String> labels = new ArrayList<>();
        labels.add(Labels.INSTANCE.show());

        if (model.hasTranscription(page)) {
            labels.add(Labels.INSTANCE.transcription());
            labels.add(Labels.INSTANCE.transcription() + "[" + Labels.INSTANCE.lecoy() + "]");
        }
        if (model.hasIllustrationTagging(page)) {
            labels.add(Labels.INSTANCE.illustrationDescription());
        }
        if (model.hasNarrativeTagging(page)) {
            labels.add(Labels.INSTANCE.narrativeSections());
        }

        return labels.toArray(new String[labels.size()]);
    }

    public String getCurrentPage() {
        return getImageName(current_image_index);
    }

    private void setupFlashViewer(int startPage) {
        if (startPage == -1) {
            startPage = 0;
        }
        String collection = WebsiteConfig.INSTANCE.collection();
        String fsi_xml_url = FSI_URL_PREFIX + collection + "/" + book + "/" + type.getXmlId();

        String fsiHtml = new FSIViewerHTMLBuilder()
                .book(collection, book, language)
                .type(type)
                .fsiBookData(URL.encode(fsi_xml_url))
                .initialImage(startPage)
                .build();

        view.setFlashViewer(fsiHtml, type);
        view.addShowExtraChangeHandler(showExtraChangeHandler);

        if (type == FSIViewerType.SHOWCASE) {
            view.addShowcaseToolbar();
            view.setupFsiShowcaseCallback(showcaseCallback);

            // Set labels in the "show extra" dropdown
            setupShowExtra(startPage, false);

            view.addGotoKeyDownHandler(new KeyDownHandler() {
                @Override
                public void onKeyDown(KeyDownEvent event) {
                    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                        int index = getImageIndex(view.getGotoText());
                        if (index >= 0) {
                            view.fsiViewerSelectImage(index);
                        }
                    }
                }
            });
        } else if (type == FSIViewerType.PAGES) {
            view.addPagesToolbar();
            view.setupFsiPagesCallback(pagesCallback);

            // Set labels in the "show extra" dropdown
            setupShowExtra(startPage, true);

            view.addGotoKeyDownHandler(new KeyDownHandler() {
                @Override
                public void onKeyDown(KeyDownEvent event) {
                    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                        int index = getImageIndex(view.getGotoText());

                        if (index >= 0) {
                            view.fsiViewerGotoImage(index + 1);
                        }
                    }
                }
            });
        }
    }

    private void setupShowExtra(int page, boolean opening) {
        List<String> page1 = new ArrayList<>(Arrays.asList(getExtraDataLabels(page)));

        if (opening) {
            String[] page2 = getExtraDataLabels(page - 1);
            for (String label : page2) {
                if (!page1.contains(label)) {
                    page1.add(label);
                }
            }
        }
        view.setShowExtraLabels(page1.toArray(new String[page1.size()]));
        handleShowExtra();
    }

    private void handleShowExtra() {
        if (showExtraCategory == null) {
            return;
        }
        //   Generate array of String labels to label each tab
        String[] selectedPages = view.getGotoText().split(",");

        boolean lecoy = true;
        switch (showExtraCategory) {
            case TRANSCRIPTION:
                view.setSelectedShowExtra(Labels.INSTANCE.transcription());
                lecoy = false;
                // Display transcriptions for all pages/columns
                // Fall through
            case LECOY:
                view.setSelectedShowExtra(Labels.INSTANCE.transcription() + "[" + Labels.INSTANCE.lecoy() + "]");
                // Display Lecoy

                //   Generate array of Strings holding XML fragments for each relevant page
                List<String> list = new ArrayList<>();
                for (String page : selectedPages) {
                    list.add(model.getTranscription(page));
                }

                //   Create display widget and add it to view
                view.showExtra(TranscriptionViewer.createTranscriptionViewer(
                        list.toArray(new String[list.size()]), selectedPages, lecoy
                ));
                break;
            case ILLUSTRATION:
                view.setSelectedShowExtra(Labels.INSTANCE.illustrationDescription());
                // Display illustration descriptions

                view.showExtra(TranscriptionViewer.createIllustrationTaggingViewer(
                        selectedPages, model.getIllustrationTagging()));
                break;
            case NARRATIVE:
                view.setSelectedShowExtra(Labels.INSTANCE.narrativeSections());
                // Display narrative sections
                break;
            case NONE:
                // Fall through to default
            default:
                view.setSelectedShowExtra(Labels.INSTANCE.show());
                view.showExtra(null);
                break;
        }

        view.onResize();
    }

    /**
     * @param index index of page in the book
     * @return short name of a page by index, if it exists. Empty string, otherwise
     */
    private String getImageName(int index) {
        if (book == null || model.getImages() == null || model.getImages().getImages() == null
                || model.getImages().getImages().size() < index || model.getImages().getImages().get(index) == null) {
            return "";
        }

        return model.getImages().getImages().get(index).getName();
    }

    /**
     * @param name name of image
     * @return index of image in the book, if it exists. -1 otherwise.
     */
    private int getImageIndex(String name) {
        if (book != null && model.getImages() != null && model.getImages().getImages() != null) {
            for (int i = 0; i < model.getImages().getImages().size(); i++) {
                BookImage image = model.getImages().getImages().get(i);
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
