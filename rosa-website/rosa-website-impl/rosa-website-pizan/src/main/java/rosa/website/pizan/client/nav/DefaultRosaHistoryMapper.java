package rosa.website.pizan.client.nav;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import rosa.website.core.client.place.BookDescriptionPlace;
import rosa.website.core.client.place.BookSelectPlace;

@WithTokenizers({
        BookSelectPlace.Tokenizer.class,
        BookDescriptionPlace.Tokenizer.class
})
public interface DefaultRosaHistoryMapper extends PlaceHistoryMapper {}
