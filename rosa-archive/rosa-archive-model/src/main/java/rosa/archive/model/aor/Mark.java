package rosa.archive.model.aor;

/**
 *
 */
public class Mark extends Annotation {

    private String name;
    private String method;
    private String place;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Mark mark = (Mark) o;

        if (method != null ? !method.equals(mark.method) : mark.method != null) return false;
        if (name != null ? !name.equals(mark.name) : mark.name != null) return false;
        if (place != null ? !place.equals(mark.place) : mark.place != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (place != null ? place.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Mark{" +
                "name='" + name + '\'' +
                ", method='" + method + '\'' +
                ", place='" + place + '\'' +
                '}';
    }
}
