package rosa.archive.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A single book in the archive.
 */
public class Book implements IsSerializable {

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
    private BookMetadata bookMetadata;
    private BookDescription bookDescription;
    private ChecksumInfo checksumInfo;
    /**
     * Array of all content associated with this book (ex: all file names in a directory).
     */
    private String[] content;

    private BookStructure bookStructure;
    private IllustrationTagging illustrationTagging;
    private NarrativeTagging manualNarrativeTagging;
    private NarrativeTagging automaticNarrativeTagging;

    private Map<String, Permission> permissions;
    private Transcription transcription;

    public Book() {
        this.permissions = new HashMap<>();
    }

    public String getId() {
        return id;
    }

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

    public BookMetadata getBookMetadata() {
        return bookMetadata;
    }

    public void setBookMetadata(BookMetadata bookMetadata) {
        this.bookMetadata = bookMetadata;
    }

    public BookDescription getBookDescription() {
        return bookDescription;
    }

    public void setBookDescription(BookDescription bookDescription) {
        this.bookDescription = bookDescription;
    }

    public ChecksumInfo getChecksumInfo() {
        return checksumInfo;
    }

    public void setChecksumInfo(ChecksumInfo checksumInfo) {
        this.checksumInfo = checksumInfo;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;

        Book book = (Book) o;

        if (automaticNarrativeTagging != null ? !automaticNarrativeTagging.equals(book.automaticNarrativeTagging) : book.automaticNarrativeTagging != null)
            return false;
        if (bookDescription != null ? !bookDescription.equals(book.bookDescription) : book.bookDescription != null)
            return false;
        if (bookMetadata != null ? !bookMetadata.equals(book.bookMetadata) : book.bookMetadata != null) return false;
        if (bookStructure != null ? !bookStructure.equals(book.bookStructure) : book.bookStructure != null)
            return false;
        if (checksumInfo != null ? !checksumInfo.equals(book.checksumInfo) : book.checksumInfo != null) return false;
        if (!Arrays.equals(content, book.content)) return false;
        if (cropInfo != null ? !cropInfo.equals(book.cropInfo) : book.cropInfo != null) return false;
        if (croppedImages != null ? !croppedImages.equals(book.croppedImages) : book.croppedImages != null)
            return false;
        if (id != null ? !id.equals(book.id) : book.id != null) return false;
        if (illustrationTagging != null ? !illustrationTagging.equals(book.illustrationTagging) : book.illustrationTagging != null)
            return false;
        if (images != null ? !images.equals(book.images) : book.images != null) return false;
        if (manualNarrativeTagging != null ? !manualNarrativeTagging.equals(book.manualNarrativeTagging) : book.manualNarrativeTagging != null)
            return false;
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
        result = 31 * result + (bookMetadata != null ? bookMetadata.hashCode() : 0);
        result = 31 * result + (bookDescription != null ? bookDescription.hashCode() : 0);
        result = 31 * result + (checksumInfo != null ? checksumInfo.hashCode() : 0);
        result = 31 * result + (content != null ? Arrays.hashCode(content) : 0);
        result = 31 * result + (bookStructure != null ? bookStructure.hashCode() : 0);
        result = 31 * result + (illustrationTagging != null ? illustrationTagging.hashCode() : 0);
        result = 31 * result + (manualNarrativeTagging != null ? manualNarrativeTagging.hashCode() : 0);
        result = 31 * result + (automaticNarrativeTagging != null ? automaticNarrativeTagging.hashCode() : 0);
        result = 31 * result + (permissions != null ? permissions.hashCode() : 0);
        result = 31 * result + (transcription != null ? transcription.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", images=" + images +
                ", croppedImages=" + croppedImages +
                ", cropInfo=" + cropInfo +
                ", bookMetadata=" + bookMetadata +
                ", bookDescription=" + bookDescription +
                ", checksumInfo=" + checksumInfo +
                ", content=" + Arrays.toString(content) +
                ", bookStructure=" + bookStructure +
                ", illustrationTagging=" + illustrationTagging +
                ", manualNarrativeTagging=" + manualNarrativeTagging +
                ", automaticNarrativeTagging=" + automaticNarrativeTagging +
                ", permissions=" + permissions +
                ", transcription='" + transcription + '\'' +
                '}';
    }
}
