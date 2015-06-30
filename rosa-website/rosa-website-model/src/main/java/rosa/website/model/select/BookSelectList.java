package rosa.website.model.select;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * Similar to a BookDataCSV. More specific and tailored for use in book selection.
 */
public class BookSelectList implements Iterable<BookSelectData>, Serializable {
    public static final long serialVersionUID = 1L;

    private String collection;
    private SelectCategory category;
    private List<BookSelectData> data;

    public BookSelectList() {}

    public BookSelectList(SelectCategory category, String collection, List<BookSelectData> data) {
        this.collection = collection;
        this.category = category;
        this.data = data;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public void setCategory(SelectCategory category) {
        this.category = category;
    }

    public void setData(List<BookSelectData> data) {
        this.data = data;
    }

    public String getCollection() {
        return collection;
    }

    public SelectCategory getCategory() {
        return category;
    }

    public List<BookSelectData> asList() {
        return data;
    }

    @Override
    public Iterator<BookSelectData> iterator() {
        return data.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookSelectList)) return false;

        BookSelectList that = (BookSelectList) o;

        if (collection != null ? !collection.equals(that.collection) : that.collection != null) return false;
        if (category != that.category) return false;
        return !(data != null ? !data.equals(that.data) : that.data != null);

    }

    @Override
    public int hashCode() {
        int result = collection != null ? collection.hashCode() : 0;
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BookSelectList{" +
                "collection='" + collection + '\'' +
                ", category=" + category +
                ", data=" + data +
                '}';
    }
}
