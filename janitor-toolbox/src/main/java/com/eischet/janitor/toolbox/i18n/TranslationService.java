// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.i18n;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A simple translation service, where an app can put one resource bundle per thread and one global one.
 * <p>
 * An application is supposed to fill this with its own resource bundle, possibly per thread.
 * That's better than passing bundles into library code all over the place, IMO.
 * </p>
 * <p>
 * Alternatively, or in addition, the app names the base name of its bundles ({@link #setBundleBaseName}); then
 * {@link #translate(Locale, String, String)} and {@link #translateOrNull(Locale, String)} look texts up for an explicit
 * locale, without any thread-local state. That suits code that runs outside a user's thread, e.g. a request of an API client
 * or an export.
 * </p>
 * <p>
 * Overrides replace single texts, for all languages ("foo") or for one ("de.foo", by the language code of the locale).
 * </p>
 */
public class TranslationService {

    private static volatile boolean hasOverrides = false;
    private static @Nullable LookupInfoReceiver lookupInfoReceiver;
    private static @NotNull final ThreadLocal<ResourceBundle> resourceBundleThreadLocal = new ThreadLocal<>();
    private static @Nullable ResourceBundle defaultResourceBundle;
    private static @NotNull final ConcurrentHashMap<String, String> overrides = new ConcurrentHashMap<>();
    private static volatile @Nullable String bundleBaseName;
    private static volatile @Nullable ClassLoader bundleClassLoader;
    private static @NotNull final ConcurrentHashMap<Locale, Optional<ResourceBundle>> bundlesByLocale = new ConcurrentHashMap<>();

    /**
     * Gets the lookup info receiver, an optional object that will be informed of lookups for debugging purposes.
     * @return the lookup info receiver
     */
    public static @Nullable LookupInfoReceiver getLookupInfoReceiver() {
        return lookupInfoReceiver;
    }

    /**
     * Sets the lookup info receiver, an optional object that will be informed of lookups for debugging purposes.
     * @param lookupInfoReceiver the lookup info receiver
     */
    public static void setLookupInfoReceiver(@Nullable final LookupInfoReceiver lookupInfoReceiver) {
        TranslationService.lookupInfoReceiver = lookupInfoReceiver;
    }

    /**
     * Puts an override for a translation key.
     * @param key the translation key, e.g. "foo" for all languages or "de.foo" for German only
     * @param value the translation value
     */
    public static void putOverride(final String key, final String value) {
        overrides.put(key, value);
        hasOverrides = true;
    }

    /**
     * Gets the current overrides table.
     * @return the overrides table
     */
    public static @NotNull @Unmodifiable Map<String, String> getOverrides() {
        return Map.copyOf(overrides);
    }

    /**
     * Gets the default resource bundle.
     * @return the default resource bundle or null, if none has been set
     */
    public static @Nullable ResourceBundle getDefaultResourceBundle() {
        return defaultResourceBundle;
    }

    /**
     * Sets the default resource bundle.
     * @param defaultResourceBundle the default resource bundle or null, if none should be used
     */
    public static void setDefaultResourceBundle(final @Nullable ResourceBundle defaultResourceBundle) {
        TranslationService.defaultResourceBundle = defaultResourceBundle;
    }

    /**
     * Gets the resource bundle for the current thread.
     * @return the resource bundle for the current thread, or null, if none has been set
     */
    public static @Nullable ResourceBundle getThreadLocalResourceBundle() {
        return resourceBundleThreadLocal.get();
    }

    /**
     * Sets the resource bundle for the current thread.
     * @param resourceBundle the resource bundle for the current thread, or null, if none should be used
     */
    public static void setThreadLocalResourceBundle(final @Nullable ResourceBundle resourceBundle) {
        resourceBundleThreadLocal.set(resourceBundle);
    }

    /**
     * Sets the base name of the app's resource bundles, e.g. "com.example.texts" for "com/example/texts_de.properties", for the
     * lookups with an explicit locale. The files are read as UTF-8 ({@link UTF8ResourceBundle}); the bundle without a language
     * suffix is the fallback for every locale, the JVM's default locale plays no part.
     * @param baseName the base name, or null to switch the lookups with an explicit locale off
     * @param classLoader the class loader that finds the files, or null for the one of this class
     */
    public static void setBundleBaseName(final @Nullable String baseName, final @Nullable ClassLoader classLoader) {
        bundleBaseName = baseName;
        bundleClassLoader = classLoader;
        bundlesByLocale.clear();
    }

    /**
     * Sets the base name of the app's resource bundles, found by the class loader of this class.
     * @param baseName the base name, or null to switch the lookups with an explicit locale off
     * @see #setBundleBaseName(String, ClassLoader)
     */
    public static void setBundleBaseName(final @Nullable String baseName) {
        setBundleBaseName(baseName, null);
    }

    /**
     * Gets the base name of the app's resource bundles.
     * @return the base name, or null if none has been set
     */
    public static @Nullable String getBundleBaseName() {
        return bundleBaseName;
    }

    /**
     * Returns the bundle of the base name for a locale, loaded once and then cached.
     * @param locale the locale
     * @return the bundle, or null if no base name is set or no bundle exists for the locale (not even the fallback one)
     */
    public static @Nullable ResourceBundle getBundle(final @NotNull Locale locale) {
        final String baseName = bundleBaseName;
        if (baseName == null) {
            return null;
        }
        return bundlesByLocale.computeIfAbsent(locale, l -> {
            try {
                return Optional.of(UTF8ResourceBundle.getBundle(baseName, l, bundleClassLoader));
            } catch (MissingResourceException e) {
                return Optional.empty();
            }
        }).orElse(null);
    }

    /**
     * Returns the closest resource bundle, either for the local thread or a global one.
     * @return a resource bundle
     * @throws NullPointerException when no resource bundle has been configured
     */
    public static @NotNull ResourceBundle getResourceBundle() throws NullPointerException {
        final ResourceBundle bundle = findResourceBundle();
        if (bundle == null) {
            throw new NullPointerException("no resource bundle has been configured");
        }
        return bundle;
    }

    private static @Nullable ResourceBundle findResourceBundle() {
        final ResourceBundle localBundle = getThreadLocalResourceBundle();
        if (localBundle != null) {
            return localBundle;
        }
        return getDefaultResourceBundle();
    }

    /**
     * Returns a translated text from the closest resource bundle, either for the local thread or a global one.
     * @param key the resource key
     * @return a text translation from the resource bundle, or a placeholder text of the key in square brackets
     * @throws NullPointerException when the key is null
     */
    public static String translate(final @NotNull String key) throws NullPointerException {
        return translate(key, null);
    }

    /**
     * Returns a translated text from the closest resource bundle, either for the local thread or a global one, for the first key that can be found.
     * @param keys the resource keys
     * @return a text translation from the resource bundle, or a placeholder text of the keys (in square brackets as a list)
     */
    public static String translate(final @NotNull @Unmodifiable List<String> keys) {
        return translate(keys, null);
    }

    /**
     * Returns an override for a translation key, if any.
     * @param bundle the resource bundle, whose locale picks the language-specific override
     * @param key the translation key, e.g. "foo" or "bar.baz"
     * @return the override value, or null, if none has been set
     */
    @VisibleForTesting
    public static @Nullable String getOverride(final @NotNull ResourceBundle bundle, final @NotNull String key) {
        return getOverride(bundle.getLocale(), key);
    }

    /**
     * Returns an override for a translation key, if any: first the one for the language of the locale ("de.foo"), then the one
     * for all languages ("foo").
     * @param locale the locale, or null to look for the override for all languages only
     * @param key the translation key, e.g. "foo" or "bar.baz"
     * @return the override value, or null, if none has been set
     */
    public static @Nullable String getOverride(final @Nullable Locale locale, final @NotNull String key) {
        if (!hasOverrides) {
            return null;
        }
        if (locale != null && !locale.getLanguage().isEmpty()) {
            final String languageKey = locale.getLanguage() + "." + key;
            final String overriddenValue = overrides.get(languageKey);
            if (lookupInfoReceiver != null) {
                lookupInfoReceiver.searchOverride(languageKey, overriddenValue);
            }
            if (overriddenValue != null) {
                return overriddenValue;
            }
        }
        final String overriddenValue = overrides.get(key);
        if (lookupInfoReceiver != null) {
            lookupInfoReceiver.searchOverride(key, overriddenValue);
        }
        return overriddenValue;
    }

    /**
     * Returns a translated text from the closest resource bundle, either for the local thread or a global one.
     * Without a configured bundle, only overrides for all languages are found.
     * @param key the resource key
     * @param defaultValue a default value that is returned in case the key cannot be found or points to the wrong kind of entry
     * @return a text translation from the resource bundle, or a nonnull defaultValue, or a placeholder text of the key in square brackets
     * @throws NullPointerException when the key is null
     */
    public static String translate(final @NotNull String key, final @Nullable String defaultValue) {
        final ResourceBundle bundle = findResourceBundle();
        final String overrideValue = getOverride(bundle == null ? null : bundle.getLocale(), key);
        if (overrideValue != null) {
            return overrideValue;
        }
        if (bundle != null) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException | ClassCastException e) {
                // fall through to the default
            }
        }
        return defaultValue != null ? defaultValue : "[" + key + "]";
    }

    /**
     * Returns a translated text from the closest resource bundle, either for the local thread or a global one, for the first key that can be found.
     * Without a configured bundle, only overrides for all languages are found.
     * @param keys the resource keys
     * @param defaultValue a default value that is returned in case the key cannot be found or points to the wrong kind of entry
     * @return a text translation from the resource bundle, or a nonnull defaultValue, or a placeholder text of the keys (in square brackets as a list)
     */
    public static String translate(final @NotNull @Unmodifiable List<String> keys, final @Nullable String defaultValue) {
        final ResourceBundle bundle = findResourceBundle();
        for (String key : keys) {
            final String overrideValue = getOverride(bundle == null ? null : bundle.getLocale(), key);
            if (overrideValue != null) {
                return overrideValue;
            }
            if (bundle != null && bundle.containsKey(key)) {
                return bundle.getString(key);
            }
        }
        if (defaultValue != null) {
            return defaultValue;
        } else {
            return keys.toString();
        }
    }

    /**
     * Returns a text for an explicit locale, from the overrides or the bundle of the base name ({@link #setBundleBaseName}),
     * without touching the thread-local or default bundle.
     * @param locale the locale
     * @param key the resource key
     * @return the text, or null if there is neither an override nor a bundle entry
     */
    public static @Nullable String translateOrNull(final @NotNull Locale locale, final @NotNull String key) {
        final String overrideValue = getOverride(locale, key);
        if (overrideValue != null) {
            return overrideValue;
        }
        final ResourceBundle bundle = getBundle(locale);
        if (bundle == null || !bundle.containsKey(key)) {
            return null;
        }
        try {
            return bundle.getString(key);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Returns a text for an explicit locale, like {@link #translateOrNull(Locale, String)}, or a default.
     * @param locale the locale
     * @param key the resource key
     * @param defaultValue the value if there is no text
     * @return the text or the default value
     */
    public static @Nullable String translate(final @NotNull Locale locale, final @NotNull String key, final @Nullable String defaultValue) {
        final String text = translateOrNull(locale, key);
        return text != null ? text : defaultValue;
    }

    /**
     * Forgets all configuration: bundles, base name, overrides, lookup info receiver (for tests).
     */
    @VisibleForTesting
    public static void reset() {
        overrides.clear();
        hasOverrides = false;
        lookupInfoReceiver = null;
        defaultResourceBundle = null;
        resourceBundleThreadLocal.remove();
        setBundleBaseName(null, null);
    }

}
