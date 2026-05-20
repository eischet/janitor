package com.eischet.janitor.tests.parsing.xml;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.env.JElement;
import com.eischet.janitor.env.JanitorXmlParser;
import com.eischet.janitor.toolbox.json.api.JsonException;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XmlParsingTestCase extends JanitorTest {

    @Test
    public void testXmlParsing() throws XMLStreamException, JsonException {
        JElement parsed = JanitorXmlParser.parseXml("<root><element>text</element></root>");
        log.warn("parsed: {}", parsed.toJson());
        parsed.getChildren().forEach(child -> log.warn("child: {}", child));
        assertEquals("text", Objects.requireNonNull(parsed.firstChild("element")).getText());
    }

    @Test
    void parseMavenRevisionFromProjectFragment() throws XMLStreamException, JsonException {
        final String FRAGMENT = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <!-- ... -->
                <properties>
                    <revision>0.9.56-SNAPSHOT</revision>
                    <!-- ... -->
                </properties>
                <!-- ... -->
            </project>
            """;

        JElement parsed = JanitorXmlParser.parseXml(FRAGMENT);
        final JElement properties = parsed.requireFirstChild("properties");
        final JElement revision = properties.requireFirstChild("revision");
        assertEquals("0.9.56-SNAPSHOT", revision.getText());

        final String revision2 = properties.optionalChildText("revision");
        assertEquals("0.9.56-SNAPSHOT", revision2);

        // parsed.toJson():
        // {"name":"project","children":[{"name":"properties","children":[{"name":"revision","text":"0.9.56-SNAPSHOT"}]}]}

    }

}
