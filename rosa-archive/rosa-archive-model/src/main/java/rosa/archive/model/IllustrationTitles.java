package rosa.archive.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Illustration titles associated with a collection, not necessarily with a single book in
 * the collection.
 */
public class IllustrationTitles implements IsSerializable {

    private Map<String, String> data;

    public IllustrationTitles() {
        this.data = new HashMap<>();
    }

    public void setData(HashMap<String, String> data) {
        this.data = data;
    }

    public String getTitleById(String id) {
        return data.get(id);
    }

    public Set<String> getAllIds() {
        return data.keySet();
    }

    public String findIdOfTitle(String title) {
        for (String id : data.keySet()) {
            if (data.get(id).equals(title)) {
                return id;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IllustrationTitles)) return false;

        IllustrationTitles that = (IllustrationTitles) o;

        if (!data.equals(that.data)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public String toString() {
        return "IllustrationTitles{" +
                "data=" + data +
                '}';
    }
}
