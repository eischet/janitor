package com.eischet.janitor.toolbox.i18n;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;


/**
 * A simple translation service, where an app can put one resource bundle per thread and one global one.
 * <p>
 * An application is supposed to fill this with its own resource bundle, possibly per thread.
 * That's better than passing bundles into library code all over the place, IMO.
 * </p>
 */
public class TranslationService {

    private static final ThreadLocal<ResourceBundle> resourceBundleThreadLocal = new ThreadLocal<>();
    private static ResourceBundle defaultResourceBundle;

    public static ResourceBundle getDefaultResourceBundle() {
        return defaultResourceBundle;
    }

    public static void setDefaultResourceBundle(final ResourceBundle defaultResourceBundle) {
        TranslationService.defaultResourceBundle = defaultResourceBundle;
    }

    public static ResourceBundle getThreadLocalResourceBundle() {
        return resourceBundleThreadLocal.get();
    }

    public static void setThreadLocalResourceBundle(final ResourceBundle resourceBundle) {
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
     * Returns a translated text from the closest resource bundle, either for the local thread or a global one.
     * @param key the resource key
     * @param defaultValue a default value that is returned in case the key cannot be found or points to the wrong king of entry
     * @return a text translation from the resource bundle, or a nonnull defaultValue, or a placeholder text of the key in square brackets
     * @throws NullPointerException when no resource bundle has been configured or the key is null
     */
    public static String translate(final @NotNull String key, final @Nullable String defaultValue) {
        try {
            return getResourceBundle().getString(key);
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
     * @return a text translation from the resource bundle, or a placeholder text of the keys (in square brackets as a list)
     * @throws NullPointerException when no resource bundle has been configured or the key is null
     */
    public static String translate(final @NotNull @Unmodifiable List<String> keys) {
        return translate(keys, null);
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
