package com.eischet.janitor.modules.brrr;


import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.modules.httpclient.HttpException;
import com.eischet.janitor.modules.httpclient.JanitorHttpClient;
import com.eischet.janitor.toolbox.json.api.JsonException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * Represents a message for the Brrr module.
 * <a href="https://brrr.now/docs/">docs</a>
 */
public class BrrrMessage extends JanitorComposed<BrrrMessage> {

    public static final String DUMMY_URL_DISABLED = "disabled";

    public static final DispatchTable<BrrrMessage> DISPATCHER = new DispatchTable<>();

    static {
        DISPATCHER.addStringProperty("title", BrrrMessage::getTitle, BrrrMessage::setTitle);
        DISPATCHER.addStringProperty("subtitle", BrrrMessage::getSubtitle, BrrrMessage::setSubtitle);
        DISPATCHER.addStringProperty("message", BrrrMessage::getMessage, (self, value) -> self.setMessage(value == null ? "" : value));
        DISPATCHER.addStringProperty("thread_id", BrrrMessage::getThreadId, BrrrMessage::setThreadId);
        DISPATCHER.addStringProperty("open_url", BrrrMessage::getOpenUrl, BrrrMessage::setOpenUrl);
        DISPATCHER.addStringProperty("image_url", BrrrMessage::getImageUrl, BrrrMessage::setImageUrl);
        DISPATCHER.addDateTimeProperty("expiration_date", BrrrMessage::getExpirationDate, BrrrMessage::setExpirationDate);
        DISPATCHER.addStringProperty("filter_criteria", BrrrMessage::getFilterCriteria, BrrrMessage::setFilterCriteria);
        DISPATCHER.addStringProperty("sound", BrrrMessage::getSoundAsString, BrrrMessage::setSoundAsString);
        DISPATCHER.addStringProperty("interruption_level", BrrrMessage::getInterruptionLevelAsString, BrrrMessage::setInterruptionLevelAsString);

        DISPATCHER.addMethod("send", (BrrrMessage::send));
    }

    /**
     * Sends this message.
     * @param process the running script process
     * @param arguments the call arguments; the optional first one is the URL, which defaults to the BRRR_URL environment variable
     * @return null
     * @throws JanitorRuntimeException if there is no URL or the message cannot be sent
     */
    public JanitorObject send(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final String url = arguments.getOptionalStringValue(0, System.getenv("BRRR_URL"));
        if (url == null || url.isBlank()) {
            throw new JanitorArgumentException(process, "send() needs a target URL. Either pass the URL as a string or set the BRRR_URL environment variable.");
        }
        if (DUMMY_URL_DISABLED.equals(url)) {
            process.warn("BrrrMessage.send() is 'disabled'. No message will be sent.");
            return Janitor.NULL;
        }
        try {
            final JanitorHttpClient client = new JanitorHttpClient();
            client.postJson(url, this.toJson());
            client.cleanClose();
            return Janitor.NULL;
        } catch (JsonException | HttpException e) {
            throw new JanitorNativeException(process, "error posting message", e);
        }
    }

    private @Nullable String title;
    private @Nullable String subtitle;
    private @NotNull String message = "";
    private @Nullable String threadId;
    private @Nullable BrrrSound sound;
    private @Nullable String openUrl;
    private @Nullable String imageUrl;
    private @Nullable LocalDateTime expirationDate;
    private @Nullable String filterCriteria;
    private @Nullable BrrrInterruptionLevel interruptionLevel;

    public BrrrMessage() {
        super(DISPATCHER);
    }

    /**
     * @return the title
     */
    public @Nullable String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     * @param title the new value
     */
    public void setTitle(@Nullable final String title) {
        this.title = title;
    }

    /**
     * @return the subtitle
     */
    public @Nullable String getSubtitle() {
        return subtitle;
    }

    /**
     * Sets the subtitle.
     * @param subtitle the new value
     */
    public void setSubtitle(@Nullable final String subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * @return the text of the message
     */
    public @NotNull String getMessage() {
        return message;
    }

    /**
     * Sets the text of the message.
     * @param message the new value
     */
    public void setMessage(@NotNull final String message) {
        this.message = message;
    }

    /**
     * @return the ID of the thread that the message belongs to
     */
    public @Nullable String getThreadId() {
        return threadId;
    }

    /**
     * Sets the ID of the thread that the message belongs to.
     * @param threadId the new value
     */
    public void setThreadId(@Nullable final String threadId) {
        this.threadId = threadId;
    }

    /**
     * @return the name of the sound
     */
    public @Nullable String getSoundAsString() {
        return sound == null ? null : sound.getStringRepresentation();
    }

    /**
     * Sets the name of the sound.
     * @param sound the new value
     */
    public void setSoundAsString(@Nullable final String sound) {
        this.sound = sound == null ? null : BrrrSound.fromString(sound);
    }

    /**
     * @return the sound
     */
    public @Nullable BrrrSound getSound() {
        return sound;
    }

    /**
     * Sets the sound.
     * @param sound the new value
     */
    public void setSound(@Nullable final BrrrSound sound) {
        this.sound = sound;
    }

    /**
     * @return the URL that opens when the notification is tapped
     */
    public @Nullable String getOpenUrl() {
        return openUrl;
    }

    /**
     * Sets the URL that opens when the notification is tapped.
     * @param openUrl the new value
     */
    public void setOpenUrl(@Nullable final String openUrl) {
        this.openUrl = openUrl;
    }

    /**
     * @return the URL of an image to show
     */
    public @Nullable String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the URL of an image to show.
     * @param imageUrl the new value
     */
    public void setImageUrl(@Nullable final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * @return the date and time after which the notification expires
     */
    public @Nullable LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets the date and time after which the notification expires.
     * @param expirationDate the new value
     */
    public void setExpirationDate(@Nullable final LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * @return the filter criteria
     */
    public @Nullable String getFilterCriteria() {
        return filterCriteria;
    }

    /**
     * Sets the filter criteria.
     * @param filterCriteria the new value
     */
    public void setFilterCriteria(@Nullable final String filterCriteria) {
        this.filterCriteria = filterCriteria;
    }

    /**
     * @return the name of the interruption level
     */
    public @Nullable String getInterruptionLevelAsString() {
        return interruptionLevel == null ? null : interruptionLevel.getStringRepresentation();
    }

    /**
     * @return the interruption level
     */
    public @Nullable BrrrInterruptionLevel getInterruptionLevel() {
        return interruptionLevel;
    }

    /**
     * Sets the name of the interruption level.
     * @param interruptionLevel the new value
     */
    public void setInterruptionLevelAsString(@Nullable final String interruptionLevel) {
        this.interruptionLevel = interruptionLevel == null ? null : BrrrInterruptionLevel.fromString(interruptionLevel);
    }

    /**
     * Sets the interruption level.
     * @param interruptionLevel the new value
     */
    public void setInterruptionLevel(@Nullable final BrrrInterruptionLevel interruptionLevel) {
        this.interruptionLevel = interruptionLevel;
    }
}
