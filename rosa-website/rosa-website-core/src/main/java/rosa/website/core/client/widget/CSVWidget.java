package rosa.website.core.client.widget;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.ListDataProvider;
import rosa.website.core.client.place.AdvancedSearchPlace;
import rosa.website.core.client.place.BookDescriptionPlace;
import rosa.website.core.client.view.CSVDataView.Presenter;
import rosa.website.model.csv.CSVData;
import rosa.website.model.csv.CSVRow;

import java.util.Comparator;
import java.util.Map;
import java.util.logging.Logger;

public class CSVWidget extends Composite {
    private static final Logger logger = Logger.getLogger(CSVWidget.class.toString());
    private static final String NUM_REGEX = "^-?\\d+(\\.\\d+)?$";

    private final ScrollPanel top;

    private final CellTable<CSVRow> table;
    private final ListDataProvider<CSVRow> dataProvider;

    private Presenter presenter;

    /**
     * Create a new blank CsvWidget.
     */
    public CSVWidget() {
        SimplePanel root = new SimplePanel();

        CellTable.Resources css = GWT.create(CSVCellTableResources.class);

        this.table = new CellTable<>(Integer.MAX_VALUE, css);
        this.dataProvider = new ListDataProvider<>();

        dataProvider.addDataDisplay(table);

        root.setSize("100%", "100%");
        root.setWidget(table);

        top = new ScrollPanel(root);
        initWidget(top);
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @SuppressWarnings("unchecked")
    public void setData(CSVData data) {
        setData(data, null);
    }

    @SuppressWarnings("unchecked")
    public void setData(CSVData data, Map<Enum, String> links) {
        setData(data, links, null);
    }

    /**
     * Set the CSV data to be displayed. Also force hyperlinks in the data by specifying
     * columns and the target history token to be linked from that column. Column headers
     * can also be specified.
     *
     * Example: say you want the ID column data to link to the "book" place in the app.
     * The links map would contain the mapping ID -&gt; book. The ID column data would
     * then link out to this place, initializing it with the column data.
     *
     * @param data data
     * @param links links, force a column of data to be hyperlinked to place in the app
     * @param headers column headers
     */
    @SuppressWarnings("unchecked")
    public void setData(CSVData data, Map<Enum, String> links, String[] headers) {
        clear();

        dataProvider.setList(data.asList());
        ListHandler<CSVRow> sortHandler = new ListHandler<CSVRow>(dataProvider.getList()) {
            @Override
            public void onColumnSort(ColumnSortEvent event) {
                super.onColumnSort(event);
                dataProvider.refresh();
            }
        };
        createColumns(data, sortHandler, links, headers);

        table.addColumnSortHandler(sortHandler);
        table.setPageSize(data.size());
        table.flush();
    }

    /**
     * Clear the data table of all data.
     */
    public void clear() {
        for (int i = table.getColumnCount() - 1; i >= 0; i--) {
            table.removeColumn(i);
        }
    }

    public void resize(String width, String height) {
        top.setSize(width, height);
    }

    /**
     * Create table columns for the CSV data.
     *
     * When creating links in a column, the column FieldUpdater must be updated before
     * a link will be recognized. A 'case' in the switch should be added.
     *
     * @param data CSV data
     * @param sortHandler .
     * @param links map of column enum to history prefix
     * @param headers array of column headers
     */
    private void createColumns(CSVData  data, ColumnSortEvent.ListHandler<CSVRow> sortHandler,
                               final Map<Enum, String> links, String ... headers) {
        if (data.columns() == null) {
            logger.warning("CSV data has no columns assigned.");
            return;
        }

        // If no headers are specified, or not the right number are specified
        if (headers == null || headers.length != data.columns().length) {
            headers = new String[data.columns().length];
            for (int i = 0; i < data.columns().length; i++) {
                headers[i] = data.columns()[i].toString();
            }
        }

        for (final Enum col : data.columns()) {
            if (col == null) {
                logger.warning("NULL column detected.");
                continue;
            }

            com.google.gwt.user.cellview.client.Column<CSVRow, String> column;
            if (links != null && links.containsKey(col)) {
                column = new com.google.gwt.user.cellview.client.Column<CSVRow, String>(new ClickableTextCell()) {
                    @Override
                    public String getValue(CSVRow val) {
                        return val.getValue(col);
                    }
                };
                column.setFieldUpdater(new FieldUpdater<CSVRow, String>() {
                    @Override
                    public void update(int index, CSVRow object, String value) {
                        String token;
                        switch (links.get(col)) {       // TODO this kind of sucks, since it needs to know about site implementation
                            case "book":
                                presenter.goTo(new BookDescriptionPlace(
                                        escaped(object.getValue(col)))
                                );
                                break;
                            case "search;NARRATIVE_SECTION":
                                token = "NARRATIVE_SECTION;"
                                        + object.getValue(col).toLowerCase().replaceAll("\\s+", "")
                                        + ";0";
                                presenter.goTo(new AdvancedSearchPlace(token));
                                break;
                            case "search;ILLUSTRATION_TITLE":
                                token = "ILLUSTRATION_TITLE;"
                                        + escaped(object.getValue(col))
                                        + ";0";
                                presenter.goTo(new AdvancedSearchPlace(token));
                                break;
                            default:
                                break;
                        }
                    }
                });
                column.setCellStyleNames("link");
            } else {
                column = new TextColumn<CSVRow>() {
                    @Override
                    public String getValue(CSVRow entry) {
                        String val = entry.getValue(col);

                        // Report blank for missing or null-like values to display nicely
                        if (val == null || val.equals("-1") || val.equals("-1x-1")) {
                            return "";
                        }

                        return val;
                    }
                };
            }

            column.setSortable(true);
            table.addColumn(column, headers[col.ordinal()]);

            sortHandler.setComparator(column, new Comparator<CSVRow>() {
                @Override
                public int compare(CSVRow o1, CSVRow o2) {
                    String val1 = o1.getValue(col);
                    String val2 = o2.getValue(col);

                    if (val1.matches(NUM_REGEX) && val2.matches(NUM_REGEX)) {
                        return Integer.parseInt(val1) - Integer.parseInt(val2);
                    }

                    return o1.getValue(col).compareToIgnoreCase(o2.getValue(col));
                }
            });
        }
    }

    private String escaped(String str) {
        if (str == null) {
            return "";
        }

        str = str.replaceAll("\\\"", "\"\"");

        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains(" ")) {
            return "\"" + str + "\"";
        } else {
            return str;
        }
    }
}
