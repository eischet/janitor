package com.eischet.janitor.toolbox.i18n;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A simple translation service, where an app can put one resource bundle per thread and one global one.
 * <p>
 * An application is supposed to fill this with its own resource bundle, possibly per thread.
 * That's better than passing bundles into library code all over the place, IMO.
 * </p>
 */
public class TranslationService {

    private static boolean hasOverrides = false;
    private static @Nullable LookupInfoReceiver lookupInfoReceiver;
    private static @NotNull final ThreadLocal<ResourceBundle> resourceBundleThreadLocal = new ThreadLocal<>();
    private static @Nullable ResourceBundle defaultResourceBundle;
    private static @NotNull final ConcurrentHashMap<String, String> overrides = new ConcurrentHashMap<>();

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
     * @param key the translation key, e.g. "foo" or "de.foo"
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
     * Returns the closest resource bundle, either for the local thread or a global one.
     * @return a resource bundle
     * @throws NullPointerException when no resource bundle has been configured
     */
    public static @NotNull ResourceBundle getResourceBundle() throws NullPointerException {
        final ResourceBundle localBundle = getThreadLocalResourceBundle();
        if (localBundle != null) {
            return localBundle;
        }
        return Objects.requireNonNull(getDefaultResourceBundle());
    }

    /**
     * Returns a translated text from the closest resource bundle, either for the local thread or a global one.
     * @param key the resource key
     * @return a text translation from the resource bundle, or a placeholder text of the key in square brackets
     * @throws NullPointerException when no resource bundle has been configured or the key is null
     */
    public static String translate(final @NotNull String key) throws NullPointerException {
        return translate(key, null);
    }

    /**
     * Returns a translated text from the closest resource bundle, either for the local thread or a global one, for the first key that can be found.
     * @param keys the resource keys
     * @return a text translation from the resource bundle, or a placeholder text of the keys (in square brackets as a list)
     * @throws NullPointerException when no resource bundle has been configured or the key is null
     */
    public static String translate(final @NotNull @Unmodifiable List<String> keys) {
        return translate(keys, null);
    }

    /**
     * Returns an override for a translation key, if any.
     * @param bundle the resource bundle
     * @param key the translation key, e.g. "foo" or "bar.baz"
     * @return the override value, or null, if none has been set
     */
    @VisibleForTesting
    public static @Nullable String getOverride(final @NotNull ResourceBundle bundle, final @NotNull String key) {
        if (!hasOverrides) {
            return null;
        }
        // try to find an override that matches the locale, by prefixing the country code, e.g. key "foo" becomes "de.foo":
        if (bundle.getLocale() != null) {
            final String countryCode = bundle.getLocale().getCountry();
            final String countryCodedKey = countryCode + "." + key;
            final String overriddenValue = overrides.get(countryCodedKey);
            if (lookupInfoReceiver != null) {
                lookupInfoReceiver.searchOverride(countryCodedKey, overriddenValue);
            }
            if (overriddenValue != null) {
                return overriddenValue;
            }
        }
        // alternatively, try to find on override that matches the key only, e.g. "foo":
        final String overriddenValue = overrides.get(key);
        if (lookupInfoReceiver != null) {
            lookupInfoReceiver.searchOverride(key, overriddenValue);
        }
        return overriddenValue;
    }

    /**
     * Returns a translated text from the closest resource bundle, either for the local thread or a global one.
     * @param key the resource key
     * @param defaultValue a default value that is returned in case the key cannot be found or points to the wrong king of entry
     * @return a text translation from the resource bundle, or a nonnull defaultValue, or a placeholder text of the key in square brackets
     * @throws NullPointerException when no resource bundle has been configured or the key is null
     */
    public static String translate(final @NotNull String key, final @Nullable String defaultValue) {
        try {
            @NotNull final ResourceBundle bundle = getResourceBundle();
            if (hasOverrides) {
                @Nullable final String overrideValue = getOverride(bundle, key);
                if (overrideValue != null) {
                    return overrideValue;
                }
            }
            return bundle.getString(key);
        } catch (MissingResourceException | ClassCastException e) {
            if (defaultValue != null) {
                return defaultValue;
            } else {
                return "[" + key + "]";
            }
        }
    }


    /**
     * Returns a translated text from the closest resource bundle, either for the local thread or a global one, for the first key that can be found.
     * @param keys the resource keys
     * @param defaultValue a default value that is returned in case the key cannot be found or points to the wrong king of entry
     * @return a text translation from the resource bundle, or a nonnull defaultValue, or a placeholder text of the keys (in square brackets as a list)
     * @throws NullPointerException when no resource bundle has been configured or the key is null
     */
    public static String translate(final @NotNull @Unmodifiable List<String> keys, final @Nullable String defaultValue) {
        if (!keys.isEmpty()) {
            @NotNull final ResourceBundle bundle = getResourceBundle();
            for (String key : keys) {
                if (hasOverrides) {
                    @Nullable final String overrideValue = getOverride(bundle, key);
                    if (overrideValue != null) {
                        return overrideValue;
                    }
                }
                if (bundle.containsKey(key)) {
                    return bundle.getString(key);
                }
            }
        }
        if (defaultValue != null) {
            return defaultValue;
        } else {
            return keys.toString();
        }
    }

}
