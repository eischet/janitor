// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.i18n;

import java.util.*;

/**
 * Loads resource bundles from properties files that are encoded in UTF-8.
 * <p>
 * Unlike {@link ResourceBundle#getBundle(String, Locale)}, the JVM's default locale is never used as a fallback: a locale
 * without a file of its own gets the bundle without a language suffix, so the result does not depend on the machine.
 * </p>
 */
public class UTF8ResourceBundle {
    /**
     * Loads a resource bundle, reading its properties file as UTF-8.
     * @param baseName the base name of the bundle
     * @param locale the locale
     * @return the bundle
     * @throws MissingResourceException if there is no bundle for the locale, not even the one without a language suffix
     */
    public static ResourceBundle getBundle(String baseName, Locale locale) {
        return getBundle(baseName, locale, null);
    }

    /**
     * Loads a resource bundle with a given class loader, reading its properties file as UTF-8.
     * @param baseName the base name of the bundle
     * @param locale the locale
     * @param classLoader the class loader that finds the files, or null for the one of this class
     * @return the bundle
     * @throws MissingResourceException if there is no bundle for the locale, not even the one without a language suffix
     */
    public static ResourceBundle getBundle(String baseName, Locale locale, ClassLoader classLoader) {
        final ClassLoader loader = classLoader != null ? classLoader : UTF8ResourceBundle.class.getClassLoader();
        return ResourceBundle.getBundle(baseName, locale, loader, new UTF8Control());
    }

    private static class UTF8Control extends ResourceBundle.Control {
        @Override
        public List<String> getFormats(String baseName) {
            return FORMAT_PROPERTIES;
        }

        @Override
        public Locale getFallbackLocale(String baseName, Locale locale) {
            return null;
        }

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws java.io.IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            try (var stream = loader.getResourceAsStream(resourceName)) {
                if (stream != null) {
                    var props = new Properties();
                    props.load(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8));
                    return new ResourceBundle() {
                        @Override
                        protected Object handleGetObject(String key) {
                            return props.get(key);
                        }

                        @Override
                        public Enumeration<String> getKeys() {
                            final Set<String> keys = new HashSet<>(props.stringPropertyNames());
                            if (parent != null) {
                                keys.addAll(Collections.list(parent.getKeys()));
                            }
                            return Collections.enumeration(keys);
                        }

                        @Override
                        public Locale getLocale() {
                            return locale;
                        }
                    };
                }
                return null;
            }
        }
    }
}
