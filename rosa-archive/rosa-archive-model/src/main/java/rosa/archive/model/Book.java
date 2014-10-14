package rosa.archive.model;

import rosa.archive.model.aor.AnnotatedPage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A single book in the archive.
 */
public class Book implements HasId, Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    /**
     * High resolution images of the pages of this book.
     */
    private ImageList images;
    /**
     * High resolution images of the pages of this book after cropping.
     */
    private ImageList croppedImages;
    /**
     * Information about the cropping of the original images.
     */
    private CropInfo cropInfo;

    private SHA1Checksum SHA1Checksum;
    /**
     * Array of all content associated with this book (ex: all file names in a directory).
     */
    private String[] content;

    private BookStructure bookStructure;
    private IllustrationTagging illustrationTagging;
    private NarrativeTagging manualNarrativeTagging;
    private NarrativeTagging automaticNarrativeTagging;

    private Map<String, Permission> permissions;
    private Map<String, BookMetadata> metadataMap;
    private Map<String, BookDescription> descriptionMap;
    private Transcription transcription;

    private List<AnnotatedPage> annotatedPages;

    public Book() {
        this.permissions = new HashMap<>();
        this.metadataMap = new HashMap<>();
        this.descriptionMap = new HashMap<>();
        this.annotatedPages = new ArrayList<>();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public ImageList getImages() {
        return images;
    }

    public void setImages(ImageList images) {
        this.images = images;
    }

    public ImageList getCroppedImages() {
        return croppedImages;
    }

    public void setCroppedImages(ImageList croppedImages) {
        this.croppedImages = croppedImages;
    }

    public CropInfo getCropInfo() {
        return cropInfo;
    }

    public void setCropInfo(CropInfo cropInfo) {
        this.cropInfo = cropInfo;
    }

    public BookMetadata getBookMetadata(String language) {
        return metadataMap.get(language);
    }

    public void addBookMetadata(BookMetadata metadata, String language) {
        metadataMap.put(language, metadata);
    }

    public void setBookMetadata(Map<String, BookMetadata> metadataMap) {
        this.metadataMap = metadataMap;
    }

    public BookDescription getBookDescription(String language) {
        return descriptionMap.get(language);
    }

    public void setBookDescription(BookDescription bookDescription, String language) {
        descriptionMap.put(language, bookDescription);
    }

    public SHA1Checksum getSHA1Checksum() {
        return SHA1Checksum;
    }

    public void setSHA1Checksum(SHA1Checksum SHA1Checksum) {
        this.SHA1Checksum = SHA1Checksum;
    }

    public String[] getContent() {
        return content;
    }

    /**
     * Sorts content array before setting the field.
     *
     * @param content array of content
     */
    public void setContent(String[] content) {
        Arrays.sort(content);
        this.content = content;
    }

    public BookStructure getBookStructure() {
        return bookStructure;
    }

    public void setBookStructure(BookStructure bookStructure) {
        this.bookStructure = bookStructure;
    }

    public IllustrationTagging getIllustrationTagging() {
        return illustrationTagging;
    }

    public void setIllustrationTagging(IllustrationTagging illustrationTagging) {
        this.illustrationTagging = illustrationTagging;
    }

    public NarrativeTagging getManualNarrativeTagging() {
        return manualNarrativeTagging;
    }

    public void setManualNarrativeTagging(NarrativeTagging manualNarrativeTagging) {
        this.manualNarrativeTagging = manualNarrativeTagging;
    }

    public NarrativeTagging getAutomaticNarrativeTagging() {
        return automaticNarrativeTagging;
    }

    public void setAutomaticNarrativeTagging(NarrativeTagging automaticNarrativeTagging) {
        this.automaticNarrativeTagging = automaticNarrativeTagging;
    }

    public void addPermission(Permission permission, String language) {
        permissions.put(language, permission);
    }

    public Permission getPermission(String language) {
        return permissions.get(language);
    }

    public Permission[] getPermissionsInAllLanguages() {
        List<Permission> perms = new ArrayList<>();

        for (String lang : permissions.keySet()) {
            perms.add(permissions.get(lang));
        }

        return perms.toArray(new Permission[perms.size()]);
    }

    public Transcription getTranscription() {
        return transcription;
    }

    public void setTranscription(Transcription transcription) {
        this.transcription = transcription;
    }

    public List<AnnotatedPage> getAnnotatedPages() {
        return annotatedPages;
    }

    public void setAnnotatedPages(List<AnnotatedPage> annotatedPages) {
        this.annotatedPages = annotatedPages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Book book = (Book) o;

        if (SHA1Checksum != null ? !SHA1Checksum.equals(book.SHA1Checksum) : book.SHA1Checksum != null) return false;
        if (annotatedPages != null ? !annotatedPages.equals(book.annotatedPages) : book.annotatedPages != null)
            return false;
        if (automaticNarrativeTagging != null ? !automaticNarrativeTagging.equals(book.automaticNarrativeTagging) : book.automaticNarrativeTagging != null)
            return false;
        if (bookStructure != null ? !bookStructure.equals(book.bookStructure) : book.bookStructure != null)
            return false;
        if (!Arrays.equals(content, book.content)) return false;
        if (cropInfo != null ? !cropInfo.equals(book.cropInfo) : book.cropInfo != null) return false;
        if (croppedImages != null ? !croppedImages.equals(book.croppedImages) : book.croppedImages != null)
            return false;
        if (descriptionMap != null ? !descriptionMap.equals(book.descriptionMap) : book.descriptionMap != null)
            return false;
        if (id != null ? !id.equals(book.id) : book.id != null) return false;
        if (illustrationTagging != null ? !illustrationTagging.equals(book.illustrationTagging) : book.illustrationTagging != null)
            return false;
        if (images != null ? !images.equals(book.images) : book.images != null) return false;
        if (manualNarrativeTagging != null ? !manualNarrativeTagging.equals(book.manualNarrativeTagging) : book.manualNarrativeTagging != null)
            return false;
        if (metadataMap != null ? !metadataMap.equals(book.metadataMap) : book.metadataMap != null) return false;
        if (permissions != null ? !permissions.equals(book.permissions) : book.permissions != null) return false;
        if (transcription != null ? !transcription.equals(book.transcription) : book.transcription != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (images != null ? images.hashCode() : 0);
        result = 31 * result + (croppedImages != null ? croppedImages.hashCode() : 0);
        result = 31 * result + (cropInfo != null ? cropInfo.hashCode() : 0);
        result = 31 * result + (SHA1Checksum != null ? SHA1Checksum.hashCode() : 0);
        result = 31 * result + (content != null ? Arrays.hashCode(content) : 0);
        result = 31 * result + (bookStructure != null ? bookStructure.hashCode() : 0);
        result = 31 * result + (illustrationTagging != null ? illustrationTagging.hashCode() : 0);
        result = 31 * result + (manualNarrativeTagging != null ? manualNarrativeTagging.hashCode() : 0);
        result = 31 * result + (automaticNarrativeTagging != null ? automaticNarrativeTagging.hashCode() : 0);
        result = 31 * result + (permissions != null ? permissions.hashCode() : 0);
        result = 31 * result + (metadataMap != null ? metadataMap.hashCode() : 0);
        result = 31 * result + (descriptionMap != null ? descriptionMap.hashCode() : 0);
        result = 31 * result + (transcription != null ? transcription.hashCode() : 0);
        result = 31 * result + (annotatedPages != null ? annotatedPages.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", images=" + images +
                ", croppedImages=" + croppedImages +
                ", cropInfo=" + cropInfo +
                ", SHA1Checksum=" + SHA1Checksum +
                ", content=" + Arrays.toString(content) +
                ", bookStructure=" + bookStructure +
                ", illustrationTagging=" + illustrationTagging +
                ", manualNarrativeTagging=" + manualNarrativeTagging +
                ", automaticNarrativeTagging=" + automaticNarrativeTagging +
                ", permissions=" + permissions +
                ", metadataMap=" + metadataMap +
                ", descriptionMap=" + descriptionMap +
                ", transcription=" + transcription +
                ", annotatedPages=" + annotatedPages +
                '}';
    }
}
