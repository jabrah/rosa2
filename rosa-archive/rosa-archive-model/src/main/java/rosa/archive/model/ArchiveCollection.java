package rosa.archive.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A collection of books stored in the archive.
 */
public class ArchiveCollection implements IsSerializable {

    private HashMap<String, Book> books;
    private ArrayList<String> languages;
    private CharacterNames characterNames;
    private IllustrationTitles illustrationTitles;
    private NarrativeSections narrativeSections;

    public ArchiveCollection() {
        this.books = new HashMap<>();
    }

    /**
     * Retrieve a book in the archive with its ID. If the book is not present in this collection,
     * NULL is returned.
     *
     * @param id
     *          ID of the book you want
     * @return
     *          a Book. NULL if Book is not present in the collection.
     */
    public Book getBook(String id) {
        return books.get(id);
    }

    public Set<String> getAllBookIds() {
        return books.keySet();
    }

    /**
     * Get a set containing all of the books in this collection.
     *
     * @return
     *          an emtpy set will be returned if no books exist in this collection.
     */
    // Is this necessary?
    public Set<Book> getAllBooks() {
        Set<Book> allBooks = new HashSet<>();

        for (Entry<String, Book> entry : books.entrySet()) {
            allBooks.add( entry.getValue() );
        }
        return allBooks;
    }

    public void setBooks(HashMap<String, Book> books) {
        this.books = books;
    }

    public CharacterNames getCharacterNames() {
        return characterNames;
    }

    public void setCharacterNames(CharacterNames characterNames) {
        this.characterNames = characterNames;
    }

    public IllustrationTitles getIllustrationTitles() {
        return illustrationTitles;
    }

    public void setIllustrationTitles(IllustrationTitles illustrationTitles) {
        this.illustrationTitles = illustrationTitles;
    }

    public NarrativeSections getNarrativeSections() {
        return narrativeSections;
    }

    public void setNarrativeSections(NarrativeSections narrativeSections) {
        this.narrativeSections = narrativeSections;
    }

    /**
     * Get a list of all languages supported by this collection.
     *
     * @return
     *          List of languages
     */
    public List<String> getAllSupportedLanguages() {
        return languages;
    }

    public void setLanguages(ArrayList<String> languages) {
        this.languages = languages;
    }

    /**
     * Checks whether or not a language is supported by this collection.
     *
     * @param language
     * @return
     */
    public boolean isLanguageSupported(String language) {
        return languages.contains(language);
    }

    // TODO equals/hashCode

}
