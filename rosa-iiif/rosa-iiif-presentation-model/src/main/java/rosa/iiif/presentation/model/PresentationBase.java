package rosa.iiif.presentation.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Includes most fields described in the Metadata Requirements tables in the IIIF
 * Presentation API 2.0 specification. All fields listed in this class are at
 * minimum optional fields for all IIIF Presentation API model objects. Several
 * fields from the tables are not included, as they are not allowed in most model
 * objects. Those fields are added only to those relevant objects.
 *
 * http://iiif.io/api/presentation/2.0/#b-summary-of-metadata-requirements
 */
public abstract class PresentationBase implements IIIFNames, Serializable {
    private static final long serialVersionUID = 1L;

    protected String context;
    /**
     * URI identifying this resource.
     */
    protected String id;
    /**
     * Type of resource: Manifest, canvas, image content, etc
     */
    protected String type;
    
    protected ViewingHint viewingHint;
    
    protected ViewingDirection viewingDirection;
    
    // Descriptive Properties
    /**
     * Human readable label, name, or title. Plain text only.
     */
    protected TextValue label;
    /**
     * Long-form prose description, can include some basic HTML formatting.
     */
    protected HtmlValue description;     

    /**
     * URL that should follow the IIIF Image API syntax.
     */
    protected String thumbnailUrl;
    /**
     * Service to extend functionality of thumbnail beyond simply displaying
     * the image. RECOMMENDED: IIIF image service
     */
    protected Service thumbnailService;

    // Linked properties
    /**
     * URL to external document with further description of the resource. Can be used
     * for search.
     */
    protected String seeAlso;
    /**
     * URL to an external service that extends functionality of this resource.
     */
    protected Service service;
    /**
     * URL to an external resource intended to be displayed.
     */
    protected String relatedUri;
    /**
     * Format, or MIME type, of the related resource.
     */
    protected String relatedFormat;
    /**
     * URI of the resource that contains this resource.
     */
    protected String within;

    /**
     * A list of short descriptive entries, given as pairs of human readable label and
     * value to be displayed to the user. The value should be either simple HTML, including
     * links and text markup, or plain text, and the label should be plain text.
     *
     * This should not be used for discovery purposes. TODO HTML
     */
    protected Map<String, HtmlValue> metadata;

    /**
     * Rights and attribution: attribution, license, logo
     */
    protected Rights rights;

    protected PresentationBase() {
        metadata = new HashMap<>();
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ViewingHint getViewingHint() {
        return viewingHint;
    }

    public void setViewingHint(ViewingHint viewingHint) {
        this.viewingHint = viewingHint;
    }

    public TextValue getLabel() {
        return label;
    }

    public String getLabel(String language) {
        return getLabel() != null ? getLabel().getValue() : "";
    }

    public void setLabel(TextValue label) {
        this.label = label;
    }

    public void setLabel(String label, String language) {
        setLabel(new TextValue(label, language));
    }

    public TextValue getDescription() {
        return description;
    }

    public String getDescription(String language) {
        return getDescription() != null ? getDescription().getValue() : "";
    }

    public void setDescription(HtmlValue description) {
        this.description = description;
    }

    public void setDescription(String description, String language) {
        setDescription(new HtmlValue(description, language));
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Service getThumbnailService() {
        return thumbnailService;
    }

    public void setThumbnailService(Service thumbnailService) {
        this.thumbnailService = thumbnailService;
    }

    public String getSeeAlso() {
        return seeAlso;
    }

    public void setSeeAlso(String seeAlso) {
        this.seeAlso = seeAlso;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getRelatedUri() {
        return relatedUri;
    }

    public void setRelatedUri(String relatedUri) {
        this.relatedUri = relatedUri;
    }

    public String getRelatedFormat() {
        return relatedFormat;
    }

    public void setRelatedFormat(String relatedFormat) {
        this.relatedFormat = relatedFormat;
    }

    public String getWithin() {
        return within;
    }

    public void setWithin(String within) {
        this.within = within;
    }

    public Map<String, HtmlValue> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, HtmlValue> metadata) {
        this.metadata = metadata;
    }
    
    public ViewingDirection getViewingDirection() {
        return viewingDirection;
    }

    public void setViewingDirection(ViewingDirection viewingDirection) {
        this.viewingDirection = viewingDirection;
    }

    public Rights getRights() {
        return rights;
    }

    public void setRights(Rights rights) {
        this.rights = rights;
    }

    /**
     * Helper for subclasses.
     *
     * @return hashCode
     */
    @Override
    public int hashCode() {
        int result = context != null ? context.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (viewingHint != null ? viewingHint.hashCode() : 0);
        result = 31 * result + (viewingDirection != null ? viewingDirection.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (thumbnailUrl != null ? thumbnailUrl.hashCode() : 0);
        result = 31 * result + (thumbnailService != null ? thumbnailService.hashCode() : 0);
        result = 31 * result + (seeAlso != null ? seeAlso.hashCode() : 0);
        result = 31 * result + (service != null ? service.hashCode() : 0);
        result = 31 * result + (relatedUri != null ? relatedUri.hashCode() : 0);
        result = 31 * result + (relatedFormat != null ? relatedFormat.hashCode() : 0);
        result = 31 * result + (within != null ? within.hashCode() : 0);
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        result = 31 * result + (rights != null ? rights.hashCode() : 0);
        return result;
    }

    protected boolean canEqual(Object obj) {
        return (obj instanceof PresentationBase);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PresentationBase)) return false;

        PresentationBase that = (PresentationBase) o;

        if (!that.canEqual(this)) {
            return false;
        }

        if (context != null ? !context.equals(that.context) : that.context != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (viewingHint != that.viewingHint) return false;
        if (viewingDirection != that.viewingDirection) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (thumbnailUrl != null ? !thumbnailUrl.equals(that.thumbnailUrl) : that.thumbnailUrl != null) return false;
        if (thumbnailService != null ? !thumbnailService.equals(that.thumbnailService) : that.thumbnailService != null)
            return false;
        if (seeAlso != null ? !seeAlso.equals(that.seeAlso) : that.seeAlso != null) return false;
        if (service != null ? !service.equals(that.service) : that.service != null) return false;
        if (relatedUri != null ? !relatedUri.equals(that.relatedUri) : that.relatedUri != null) return false;
        if (relatedFormat != null ? !relatedFormat.equals(that.relatedFormat) : that.relatedFormat != null)
            return false;
        if (within != null ? !within.equals(that.within) : that.within != null) return false;
        if (metadata != null ? !metadata.equals(that.metadata) : that.metadata != null) return false;
        return !(rights != null ? !rights.equals(that.rights) : that.rights != null);

    }

    @Override
    public String toString() {
        return "PresentationBase [context=" + context + ", id=" + id + ", type=" + type + ", viewingHint="
                + viewingHint + ", viewingDirection=" + viewingDirection + ", label=" + label + ", description="
                + description + ", thumbnailUrl=" + thumbnailUrl + ", thumbnailService=" + thumbnailService
                + ", seeAlso=" + seeAlso + ", service=" + service + ", relatedUri=" + relatedUri
                + ", relatedFormat=" + relatedFormat + ", within=" + within + ", metadata=" + metadata
                + ", rights=" + rights + "]";
    }

}
