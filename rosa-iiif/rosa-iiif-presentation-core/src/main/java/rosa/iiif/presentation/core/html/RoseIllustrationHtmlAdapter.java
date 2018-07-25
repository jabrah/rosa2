package rosa.iiif.presentation.core.html;

import rosa.archive.model.Book;
import rosa.archive.model.BookCollection;
import rosa.archive.model.BookImage;
import rosa.archive.model.CharacterNames;
import rosa.archive.model.Illustration;
import rosa.archive.model.IllustrationTitles;
import rosa.iiif.presentation.core.PresentationUris;

import javax.xml.stream.XMLStreamException;

public class RoseIllustrationHtmlAdapter extends AnnotationBaseHtmlAdapter<Illustration> {
    
    RoseIllustrationHtmlAdapter(PresentationUris pres_uris) {
        super(pres_uris);
    }

    @Override
    Class<Illustration> getAnnotationType() {
        return Illustration.class;
    }

    @Override
    void annotationAsHtml(BookCollection col, Book book, BookImage page, Illustration annotation) throws XMLStreamException {
        CharacterNames names = col.getCharacterNames();
        IllustrationTitles titles = col.getIllustrationTitles();

        // Resolve character name IDs (should be done in archive layer)
        StringBuilder sb_names = new StringBuilder();
        for (String name_id : annotation.getCharacters()) {
            String name = names.getNameInLanguage(name_id, "en");

            sb_names.append(name == null ? name_id : name);
            if (!sb_names.toString().isEmpty()) {
                sb_names.append(", ");
            } else {
                sb_names.append(' ');
            }
        }

        // Resolve illustration title IDs (should be done in archive layer)
        StringBuilder sb_titles = new StringBuilder();
        for (String title_id : annotation.getTitles()) {
            String title = titles.getTitleById(title_id);

            sb_titles.append(title == null ? title_id : title);
            if (!sb_titles.toString().isEmpty()) {
                sb_titles.append(", ");
            } else {
                sb_titles.append(' ');
            }
        }

        addSimpleElement(writer, "p", "Illustration", "class", "annotation-title");

        if (isNotEmpty(annotation.getTitles())) {
            writer.writeStartElement("p");
            addSimpleElement(writer, "span", "Titles:", "class", "bold");
            writer.writeCharacters(sb_titles.toString());
            writer.writeEndElement();
        }
        if (isNotEmpty(annotation.getCharacters())) {
            writer.writeStartElement("p");
            addSimpleElement(writer, "span", "Characters:", "class", "bold");
            writer.writeCharacters(sb_names.toString());
            writer.writeEndElement();
        }
        if (isNotEmpty(annotation.getTextualElement())) {
            writer.writeStartElement("p");
            addSimpleElement(writer, "span", "Textual Elements:", "class", "bold");
            writer.writeCharacters(annotation.getTextualElement());
            writer.writeEndElement();
        }
        if (isNotEmpty(annotation.getCostume())) {
            writer.writeStartElement("p");
            addSimpleElement(writer, "span", "Costume:", "class", "bold");
            writer.writeCharacters(annotation.getCostume());
            writer.writeEndElement();
        }
        if (isNotEmpty(annotation.getInitials())) {
            writer.writeStartElement("p");
            addSimpleElement(writer, "span", "Initials:", "class", "bold");
            writer.writeCharacters(annotation.getInitials());
            writer.writeEndElement();
        }
        if (isNotEmpty(annotation.getObject())) {
            writer.writeStartElement("p");
            addSimpleElement(writer, "span", "Object:", "class", "bold");
            writer.writeCharacters(annotation.getObject());
            writer.writeEndElement();
        }
        if (isNotEmpty(annotation.getLandscape())) {
            writer.writeStartElement("p");
            addSimpleElement(writer, "span", "Landscape:", "class", "bold");
            writer.writeCharacters(annotation.getLandscape());
            writer.writeEndElement();
        }
        if (isNotEmpty(annotation.getArchitecture())) {
            writer.writeStartElement("p");
            addSimpleElement(writer, "span", "Architecture:", "class", "bold");
            writer.writeCharacters(annotation.getArchitecture());
            writer.writeEndElement();
        }
        if (isNotEmpty(annotation.getOther())) {
            writer.writeStartElement("p");
            addSimpleElement(writer, "span", "Other:", "class", "bold");
            writer.writeCharacters(annotation.getOther());
            writer.writeEndElement();
        }
    }

    private boolean isNotEmpty(String[] str) {
        return str != null && str.length > 0;
    }
}
