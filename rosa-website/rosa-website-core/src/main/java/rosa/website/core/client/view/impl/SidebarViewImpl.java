package rosa.website.core.client.view.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import rosa.website.core.client.Labels;
import rosa.website.core.client.view.SidebarView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SidebarViewImpl extends Composite implements SidebarView {
    private final Labels labels = Labels.INSTANCE;
    private Presenter presenter;

    private final FlowPanel content;
    private final FlowPanel navPanel;
    private final FlowPanel bookPanel;
    private final FlowPanel featuresPanel;

    private final Map<String, Label> languages;
    private final FlowPanel langPanel;

    private List<HTML> links;
    private List<HandlerRegistration> handlers;

    /**  */
    public SidebarViewImpl() {
        ScrollPanel root = new ScrollPanel();
        content = new FlowPanel();
        content.addStyleName("Sidebar");

        this.navPanel = new FlowPanel();
        this.bookPanel = new FlowPanel();
        this.featuresPanel = new FlowPanel();
        this.langPanel = new FlowPanel();
        this.links = new ArrayList<>();
        this.handlers = new ArrayList<>();
        this.languages = new HashMap<>();

        root.setWidget(content);
        content.add(bookPanel);
        content.add(navPanel);
        content.add(featuresPanel);

        bookPanel.setVisible(false);
        bookPanel.addStyleName("vspace");

        handlers.add(this.addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (!event.isAttached()) { // Remove all handlers on detach
                    for (HandlerRegistration h : handlers) {
                        h.removeHandler();
                    }
                }
            }
        }));

        featuresPanel.add(langPanel);
        addFlashSelector();

        initWidget(root);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void selectItem(String item) {
        unselectAll();

        for (HTML link : links) {
            if (link.getHTML().equals(item)) {
                link.addStyleName("SidebarSelected");
            }
        }
    }

    @Override
    public void addHelpLink(final String url) {
        Label help = new Label(Labels.INSTANCE.help());
        navPanel.add(help);

        help.setStylePrimaryName("SidebarItem");
        help.addStyleName("link");

        handlers.add(help.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.open(url, "help", "toolbar=yes,menubar=no,scrollbars=yes,resizable=yes");
            }
        }));
    }

    @Override
    public void setSiteNavigationLinks(Map<String, String> nav_links) {
        addLinks(nav_links, navPanel);
    }

    @Override
    public void addSection(String title, Map<String, String> links) {
        addHeader(title, navPanel);
        addLinks(links, navPanel);
    }

    @Override
    public void setBookLinks(String title, Map<String, String> links) {
        addHeader(title, bookPanel);
        addLinks(links, bookPanel);
        bookPanel.setVisible(true);
    }

    @Override
    public void clearBookLinks() {
        bookPanel.setVisible(false);

        for (Widget w : bookPanel) {
            if (w instanceof HTML) {
                links.remove(w);
            }
        }

        bookPanel.clear();
    }

    @Override
    public void resize(String width, String height) {
        content.setSize(width, height);
    }

    @Override
    public void addLanguageLink(String label, final String languageCode) {
        if (languages.size() == 0) {
            addHeader(labels.language(), langPanel);
        }

        final Label langLink = new Label(label);
        langLink.addStyleName("SidebarItem");

        if (!LocaleInfo.getCurrentLocale().getLocaleName().equals(languageCode)) {
            langLink.addStyleName("link");
        }

        languages.put(languageCode, langLink);
        langPanel.add(langLink);

        langLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String current = LocaleInfo.getCurrentLocale().getLocaleName();

                for (Entry<String, Label> entry : languages.entrySet()) {
                    entry.getValue().removeStyleName("link");
                    if (!current.equals(languageCode)) {
                        entry.getValue().addStyleName("link");
                    }
                }

                String token = presenter.getCurrentToken();

                StringBuilder newUrl = new StringBuilder(GWT.getHostPageBaseURL());
                if (!languageCode.equals("en")) {
                    newUrl.append("?locale=");
                    newUrl.append(languageCode);
                }
                newUrl.append((token.isEmpty() ? "" : "#" + token));

                Window.Location.assign(newUrl.toString());
            }
        });
    }

    private void addFlashSelector() {
        addHeader("Feature", featuresPanel);
        final CheckBox useFlash = new CheckBox("Flash");
        useFlash.addStyleName("SidebarItem");
        useFlash.setValue(true, false);
        featuresPanel.add(useFlash);

        handlers.add(useFlash.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (presenter != null) {
                    presenter.setUseFlash(event.getValue());
                }
            }
        }));
    }

    private void addHeader(String header, HasWidgets container) {
        if (header == null || header.isEmpty()) {
            return;
        }

        HTML h = new HTML(header);
        h.setStylePrimaryName("SidebarHeader");

        container.add(h);
    }

    private void addLinks(Map<String, String> links, HasWidgets container) {
        if (links == null) {
            return;
        }

        for (final Entry<String, String> entry : links.entrySet()) {
            HTML l = new HTML(entry.getKey());

            l.setStylePrimaryName("SidebarItem");
            l.addStyleName("link");
            l.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    unselectAll();
                    ((Widget) event.getSource()).addStyleName("SidebarSelected");

                    StringBuilder url_sb = new StringBuilder();
                    if (entry.getValue().startsWith("http")) {
                        url_sb.append(entry.getValue());
                    } else {
                        url_sb.append(GWT.getHostPageBaseURL());

                        String locale = LocaleInfo.getCurrentLocale().getLocaleName();
                        if (!locale.equalsIgnoreCase("en")) {
                            url_sb.append("?locale=");
                            url_sb.append(locale);
                        }

                        url_sb.append("#");
                        url_sb.append(entry.getValue());
                    }

                    Window.Location.assign(url_sb.toString());
                }
            });

            this.links.add(l);
            container.add(l);
        }
    }

    private void unselectAll() {
        for (HTML l : links) {
            l.removeStyleName("SidebarSelected");
        }
    }
}
