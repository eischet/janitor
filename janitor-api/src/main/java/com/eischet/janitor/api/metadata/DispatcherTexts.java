// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.metadata;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.toolbox.i18n.TranslationService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * The translated texts of a class, e.g. a dispatch table: its title and its description, looked up with the class's
 * {@link Janitor.MetaData#TEXT_KEY} in the host's resource bundles ({@link TranslationService#translateOrNull(Locale, String)}).
 * <p>
 * Without a translation, the title falls back to the class name ({@link Janitor.MetaData#CLASS}) and the description to the
 * plain {@link Janitor.MetaData#HELP} text.
 * </p>
 */
public final class DispatcherTexts {

    /**
     * The suffix of the title's translation key.
     */
    public static final String TITLE = "title";

    /**
     * The suffix of the description's translation key.
     */
    public static final String HELP = "help";

    private DispatcherTexts() {
    }

    /**
     * The title of a class, e.g. "Tools".
     * @param owner the class, e.g. a dispatch table
     * @param locale the language
     * @return the translated title, else the class name, else null
     */
    public static @Nullable String title(final @NotNull HasMetaData owner, final @NotNull Locale locale) {
        final String text = text(owner, locale, TITLE);
        return text != null ? text : owner.getMetaData(Janitor.MetaData.CLASS);
    }

    /**
     * The description of a class.
     * @param owner the class, e.g. a dispatch table
     * @param locale the language
     * @return the translated description, else the HELP text, else null
     */
    public static @Nullable String help(final @NotNull HasMetaData owner, final @NotNull Locale locale) {
        final String text = text(owner, locale, HELP);
        return text != null ? text : owner.getMetaData(Janitor.MetaData.HELP);
    }

    /**
     * One translated text of a class, without fallback.
     * @param owner the class, e.g. a dispatch table
     * @param locale the language
     * @param suffix the part after the text key, e.g. {@link #TITLE}
     * @return the text, or null if the class has no text key or there is no translation
     */
    public static @Nullable String text(final @NotNull HasMetaData owner, final @NotNull Locale locale, final @NotNull String suffix) {
        final String textKey = owner.getMetaData(Janitor.MetaData.TEXT_KEY);
        if (textKey == null || textKey.isBlank()) {
            return null;
        }
        final String text = TranslationService.translateOrNull(locale, textKey + "." + suffix);
        return text == null || text.isBlank() ? null : text;
    }

}
