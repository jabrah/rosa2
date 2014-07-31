package rosa.archive.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Collection;
import java.util.List;

/**
 * A single book in the archive.
 */
public class Book implements IsSerializable {

    private String id;
    /**
     * High resolution images of the pages of this book.
     */
    private Collection<BookImage> images;
    /**
     * High resolution images of the pages of this book after cropping.
     */
    private Collection<BookImage> croppedImages;
    /**
     * Information about the cropping of the original images.
     */
    private CropInfo cropInfo;
    private BookMetadata bookMetadata;
    private BookDescription bookDescription;
    private ChecksumInfo checksumInfo;
    /**
     * Collection of all content associated with this book (ex: all file names in a directory).
     */
    private Collection<String> content;

    private BookStructure bookStructure;
    private ImageTagging imageTagging;
    private List<BookScene> manualNarrativeTagging;
    private List<BookScene> automaticNarrativeTagging;

    public Book() {  }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Collection<BookImage> getImages() {
        return images;
    }

    public void setImages(Collection<BookImage> images) {
        this.images = images;
    }

    public Collection<BookImage> getCroppedImages() {
        return croppedImages;
    }

    public void setCroppedImages(Collection<BookImage> croppedImages) {
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

    public Collection<String> getContent() {
        return content;
    }

    public void setContent(Collection<String> content) {
        this.content = content;
    }

    public BookStructure getBookStructure() {
        return bookStructure;
    }

    public void setBookStructure(BookStructure bookStructure) {
        this.bookStructure = bookStructure;
    }

    public ImageTagging getImageTagging() {
        return imageTagging;
    }

    public void setImageTagging(ImageTagging imageTagging) {
        this.imageTagging = imageTagging;
    }

    public List<BookScene> getManualNarrativeTagging() {
        return manualNarrativeTagging;
    }

    public void setManualNarrativeTagging(List<BookScene> manualNarrativeTagging) {
        this.manualNarrativeTagging = manualNarrativeTagging;
    }

    public List<BookScene> getAutomaticNarrativeTagging() {
        return automaticNarrativeTagging;
    }

    public void setAutomaticNarrativeTagging(List<BookScene> automaticNarrativeTagging) {
        this.automaticNarrativeTagging = automaticNarrativeTagging;
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
        if (content != null ? !content.equals(book.content) : book.content != null) return false;
        if (cropInfo != null ? !cropInfo.equals(book.cropInfo) : book.cropInfo != null) return false;
        if (croppedImages != null ? !croppedImages.equals(book.croppedImages) : book.croppedImages != null)
            return false;
        if (id != null ? !id.equals(book.id) : book.id != null) return false;
        if (imageTagging != null ? !imageTagging.equals(book.imageTagging) : book.imageTagging != null) return false;
        if (images != null ? !images.equals(book.images) : book.images != null) return false;
        if (manualNarrativeTagging != null ? !manualNarrativeTagging.equals(book.manualNarrativeTagging) : book.manualNarrativeTagging != null)
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
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (bookStructure != null ? bookStructure.hashCode() : 0);
        result = 31 * result + (imageTagging != null ? imageTagging.hashCode() : 0);
        result = 31 * result + (manualNarrativeTagging != null ? manualNarrativeTagging.hashCode() : 0);
        result = 31 * result + (automaticNarrativeTagging != null ? automaticNarrativeTagging.hashCode() : 0);
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
                ", content=" + content +
                ", bookStructure=" + bookStructure +
                ", imageTagging=" + imageTagging +
                ", manualNarrativeTagging=" + manualNarrativeTagging +
                ", automaticNarrativeTagging=" + automaticNarrativeTagging +
                '}';
    }
}
