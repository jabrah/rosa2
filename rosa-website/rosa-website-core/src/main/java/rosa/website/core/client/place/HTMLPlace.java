package rosa.website.core.client.place;

import com.google.gwt.place.shared.Place;

public class HTMLPlace extends Place {

    private final String name;

    public HTMLPlace(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HTMLPlace)) return false;

        HTMLPlace htmlPlace = (HTMLPlace) o;

        return !(name != null ? !name.equals(htmlPlace.name) : htmlPlace.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "HTMLPlace{" +
                "name='" + name + '\'' +
                '}';
    }
}
