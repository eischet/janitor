package com.eischet.janitor.toolbox.i18n;

import java.util.*;

public class UTF8ResourceBundle {
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