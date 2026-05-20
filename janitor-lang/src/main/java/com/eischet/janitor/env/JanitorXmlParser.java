package com.eischet.janitor.env;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Reader;
import java.io.StringReader;

public final class JanitorXmlParser {

    public static JElement parseXml(String xml) throws XMLStreamException {
        return parseXml(new StringReader(xml));
    }

    public static JElement parseXml(final Reader input) throws XMLStreamException {
        final XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        final XMLStreamReader reader = factory.createXMLStreamReader(input);
        try {
            moveToFirstElement(reader);
            final JElement root = parseElement(reader);
            skipAfterRoot(reader);
            return root;
        } finally {
            reader.close();
        }
    }

    private static void moveToFirstElement(final XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {

            final int event = reader.getEventType();

            switch (event) {

                case XMLStreamConstants.START_ELEMENT -> {

                    return;

                }

                case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA -> {

                    if (!reader.isWhiteSpace()) {

                        throw new XMLStreamException(

                            "Unexpected text before root element: " + reader.getText()

                        );

                    }

                }

                default -> {

                    // XML declaration, comments, processing instructions, etc.

                }

            }

            reader.next();

        }

        throw new XMLStreamException("Unexpected end of XML document");

    }

    private static JElement parseElement(final XMLStreamReader reader) throws XMLStreamException {

        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT) {

            throw new XMLStreamException("Expected start element");

        }

        final JElement element = new JElement(reader.getLocalName());

        parseAttributes(reader, element);

        final StringBuilder text = new StringBuilder();

        while (reader.hasNext()) {

            final int event = reader.next();

            switch (event) {

                case XMLStreamConstants.START_ELEMENT -> {

                    element.getChildren().add(parseElement(reader));

                }

                case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA -> {

                    final String value = reader.getText();

                    if (!value.isBlank()) {

                        text.append(value);

                    }

                }

                case XMLStreamConstants.END_ELEMENT -> {

                    if (!text.isEmpty()) {

                        element.setText(text.toString().trim());

                    }

                    return element;

                }

                case XMLStreamConstants.COMMENT,

                     XMLStreamConstants.PROCESSING_INSTRUCTION,

                     XMLStreamConstants.SPACE -> {

                    // Ignorieren.

                }

                default -> {

                    // Für einfache XML-Files ignorieren.

                }

            }

        }

        throw new XMLStreamException("Unexpected end of XML document inside element: " + element.getName());

    }

    private static void parseAttributes(

        final XMLStreamReader reader,

        final JElement element

    ) {

        for (int i = 0; i < reader.getAttributeCount(); i++) {

            final String name = reader.getAttributeLocalName(i);

            final String value = reader.getAttributeValue(i);

            element.getAttributes().put(

                Janitor.nullableString(name),

                Janitor.nullableString(value)

            );

        }

    }

    private static void skipAfterRoot(final XMLStreamReader reader) throws XMLStreamException {

        while (reader.hasNext()) {

            final int event = reader.next();

            switch (event) {

                case XMLStreamConstants.START_ELEMENT -> throw new XMLStreamException(

                    "Unexpected second root element: " + reader.getLocalName()

                );

                case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA -> {

                    if (!reader.isWhiteSpace()) {

                        throw new XMLStreamException(

                            "Unexpected text after root element: " + reader.getText()

                        );

                    }

                }

                default -> {

                    // OK.

                }

            }

        }

    }


/* parse to JMap:
    public static JanitorObject parseXml(String xml) throws XMLStreamException {
        return parseXml(new StringReader(xml));
    }

    public static JanitorObject parseXml(Reader input) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newFactory();

        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

        XMLStreamReader reader = factory.createXMLStreamReader(input);

        try {
            moveToFirstElement(reader);
            JanitorObject root = parseElement(reader);

            skipWhitespaceAndEndDocument(reader);

            return root;
        } finally {
            reader.close();
        }
    }

    private static void moveToFirstElement(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.getEventType();

            if (event == XMLStreamConstants.START_ELEMENT) {
                return;
            }

            if (event == XMLStreamConstants.CHARACTERS && !reader.isWhiteSpace()) {
                throw new XMLStreamException("Unexpected text before root element: " + reader.getText());
            }

            reader.next();
        }

        throw new XMLStreamException("Unexpected end of XML document");
    }

    private static JanitorObject parseElement(XMLStreamReader reader) throws XMLStreamException {
        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT) {
            throw new XMLStreamException("Expected start element");
        }

        JMap element = Janitor.map();

        @NotNull final JanitorObject name = Janitor.nullableString(reader.getLocalName());
        if (name.janitorIsTrue()) {
            element.put("name", name);
        }

        final JMap attrs = parseAttributes(reader);
        if (!attrs.isEmpty()) {
            element.put("attrs", attrs);
        }

        JList children = Janitor.list();

        while (reader.hasNext()) {
            int event = reader.next();

            switch (event) {
                case XMLStreamConstants.START_ELEMENT -> {
                    children.add(parseElement(reader));
                }

                case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA -> {
                    String text = reader.getText();

                    if (!text.isBlank()) {
                        children.add(Janitor.nullableString(text));
                    }
                }

                case XMLStreamConstants.END_ELEMENT -> {
                    element.put("children", children);
                    return element;
                }

                case XMLStreamConstants.COMMENT,
                     XMLStreamConstants.PROCESSING_INSTRUCTION,
                     XMLStreamConstants.SPACE -> {
                    // Ignorieren.
                }

                default -> {
                    // Für einfache XML-Dateien reicht es meist, andere Events zu ignorieren.
                }
            }
        }

        throw new XMLStreamException("Unexpected end of XML document inside element");
    }

    private static JMap parseAttributes(XMLStreamReader reader) {
        JMap attrs = Janitor.map();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);
            attrs.put(name, Janitor.nullableString(value));
        }
        return attrs;
    }

    private static void skipWhitespaceAndEndDocument(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamConstants.CHARACTERS && !reader.isWhiteSpace()) {
                throw new XMLStreamException("Unexpected text after root element: " + reader.getText());
            }

            if (event == XMLStreamConstants.START_ELEMENT) {
                throw new XMLStreamException("Unexpected second root element: " + reader.getLocalName());
            }
        }
    }


 */
}