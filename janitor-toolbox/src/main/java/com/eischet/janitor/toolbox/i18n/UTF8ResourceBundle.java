// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.i18n;

import java.util.*;

/** Loads resource bundles from properties files that are encoded in UTF-8. */
public class UTF8ResourceBundle {
    /**
     * Loads a resource bundle, reading its properties file as UTF-8.
     * @param baseName the base name of the bundle
     * @param locale the locale
     * @return the bundle
     */
    public static ResourceBundle getBundle(String baseName, Locale locale) {
        return ResourceBundle.getBundle(baseName, locale, new UTF8Control());
    }

    private static class UTF8Control extends ResourceBundle.Control {
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
                            return Collections.enumeration(props.stringPropertyNames());
                        }
                    };
                }
                return null;
            }
        }
    }
}