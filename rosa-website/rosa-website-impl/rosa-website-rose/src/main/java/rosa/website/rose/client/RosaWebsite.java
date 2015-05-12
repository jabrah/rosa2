package rosa.website.rose.client;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.web.bindery.event.shared.EventBus;
import rosa.website.core.client.ClientFactory;
import rosa.website.core.client.place.HTMLPlace;
import rosa.website.rose.client.nav.DefaultRosaHistoryMapper;
import rosa.website.rose.client.nav.RosaActivityMapper;
import rosa.website.rose.client.nav.RosaHistoryMapper;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RosaWebsite implements EntryPoint {
    private static final Logger logger = Logger.getLogger("");

    /**
     * This is the default place that will load when the application
     * first starts up. When there is no history token to read, this
     * is the place that will be displayed.
     */
    private Place default_place;

    private ScrollPanel main_content = new ScrollPanel();
    private final DockLayoutPanel main = new DockLayoutPanel(Style.Unit.PX);

    @Override
    public void onModuleLoad() {
        default_place = new HTMLPlace(WebsiteConfig.INSTANCE.defaultPage());

        // Set initial state
        ClientFactory clientFactory = new ClientFactory();
        clientFactory.context().setCollection(WebsiteConfig.INSTANCE.collection());
//        clientFactory.context().setUseFlash(clientSupportsFlash());
        clientFactory.context().setUseFlash(false);
        clientFactory.context().setLanguage("en");

        EventBus eventBus = clientFactory.eventBus();
        final PlaceController placeController = clientFactory.placeController();

        // Start ActivityManager for main widget with ActivityMapper
        ActivityMapper activity_mapper = new RosaActivityMapper(clientFactory);
        final ActivityManager activity_manager = new ActivityManager(activity_mapper, eventBus);
        activity_manager.setDisplay(main_content);

        DefaultRosaHistoryMapper history_mapper = GWT.create(DefaultRosaHistoryMapper.class);
        RosaHistoryMapper appHistoryMapper = new RosaHistoryMapper(history_mapper, clientFactory);
        final PlaceHistoryHandler history_handler = new PlaceHistoryHandler(appHistoryMapper);
        history_handler.register(placeController, eventBus, default_place);

        history_handler.handleCurrentHistory();

        main.add(main_content);
        main_content.setSize(Window.getClientWidth() + "px", Window.getClientHeight() + "px");
        RootLayoutPanel.get().add(main);

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                main_content.setSize(Window.getClientWidth() + "px", Window.getClientHeight() + "px");
            }
        });

        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable e) {
                logger.log(Level.SEVERE, "Uncaught exception.", e);
                placeController.goTo(default_place);
            }
        });
    }

    private native boolean clientSupportsFlash() /*-{
        return typeof navigator.plugins['Shockwave Flash'] !== 'undefined';
    }-*/;
}
