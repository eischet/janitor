package com.eischet.janitor.i18n;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.metadata.DispatcherTexts;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.toolbox.i18n.TranslationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TranslationServiceTestCase {

    private static final String BASE_NAME = "com.eischet.janitor.i18n.texts";

    @BeforeEach
    @AfterEach
    void reset() {
        TranslationService.reset();
    }

    @Test
    void languageOverridesUseTheLanguageCode() {
        TranslationService.putOverride("de.foo", "Foo auf Deutsch");
        TranslationService.putOverride("foo", "Foo");
        assertEquals("Foo auf Deutsch", TranslationService.getOverride(Locale.GERMAN, "foo"));
        assertEquals("Foo auf Deutsch", TranslationService.getOverride(Locale.GERMANY, "foo"));
        assertEquals("Foo", TranslationService.getOverride(Locale.ENGLISH, "foo"));
        assertEquals("Foo", TranslationService.getOverride((Locale) null, "foo"));
    }

    @Test
    void withoutABundleTheDefaultApplies() {
        assertEquals("default", TranslationService.translate("missing", "default"));
        assertEquals("[missing]", TranslationService.translate("missing"));
        TranslationService.putOverride("foo", "Foo");
        assertEquals("Foo", TranslationService.translate("foo", "default"));
    }

    @Test
    void explicitLocalesUseTheBaseNameAndFallBackToTheRootBundle() {
        TranslationService.setBundleBaseName(BASE_NAME);
        assertEquals("Hello", TranslationService.translateOrNull(Locale.ENGLISH, "greeting"));
        assertEquals("Hallo", TranslationService.translateOrNull(Locale.GERMAN, "greeting"));
        // no French file, and the JVM's default locale plays no part
        assertEquals("Hallo", TranslationService.translateOrNull(Locale.FRENCH, "greeting"));
        assertEquals("nur im Grund-Bundle", TranslationService.translateOrNull(Locale.ENGLISH, "only.root"));
        assertNull(TranslationService.translateOrNull(Locale.ENGLISH, "missing"));
        assertEquals("default", TranslationService.translate(Locale.ENGLISH, "missing", "default"));
    }

    @Test
    void explicitLocalesHonourOverrides() {
        TranslationService.setBundleBaseName(BASE_NAME);
        TranslationService.putOverride("en.greeting", "Hi");
        assertEquals("Hi", TranslationService.translateOrNull(Locale.ENGLISH, "greeting"));
        assertEquals("Hallo", TranslationService.translateOrNull(Locale.GERMAN, "greeting"));
    }

    @Test
    void withoutABaseNameExplicitLocalesFindNothing() {
        assertNull(TranslationService.translateOrNull(Locale.GERMAN, "greeting"));
    }

    @Test
    void dispatcherTextsComeFromTheTextKeyWithFallbacks() {
        TranslationService.setBundleBaseName(BASE_NAME);
        final DispatchTable<?> thing = new DispatchTable<>();
        thing.setMetaData(Janitor.MetaData.CLASS, "Thing");
        thing.setMetaData(Janitor.MetaData.HELP, "plain help");
        assertEquals("Thing", DispatcherTexts.title(thing, Locale.GERMAN));
        assertEquals("plain help", DispatcherTexts.help(thing, Locale.GERMAN));

        thing.setMetaData(Janitor.MetaData.TEXT_KEY, "entity.Thing");
        assertEquals("Dinge", DispatcherTexts.title(thing, Locale.GERMAN));
        assertEquals("plain help", DispatcherTexts.help(thing, Locale.GERMAN));
        assertNull(DispatcherTexts.text(thing, Locale.GERMAN, DispatcherTexts.HELP));
    }

}
