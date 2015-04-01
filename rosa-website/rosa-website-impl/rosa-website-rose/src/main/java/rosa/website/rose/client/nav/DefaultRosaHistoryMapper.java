package rosa.website.rose.client.nav;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import rosa.website.core.client.place.BookSelectPlace;
import rosa.website.core.client.place.CSVDataPlace;
import rosa.website.core.client.place.HTMLPlace;

@WithTokenizers({
        BookSelectPlace.Tokenizer.class
})
public interface DefaultRosaHistoryMapper extends PlaceHistoryMapper {}
