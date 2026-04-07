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

    public @Nullable String getTitle() {
        return title;
    }

    public void setTitle(@Nullable final String title) {
        this.title = title;
    }

    public @Nullable String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(@Nullable final String subtitle) {
        this.subtitle = subtitle;
    }

    public @NotNull String getMessage() {
        return message;
    }

    public void setMessage(@NotNull final String message) {
        this.message = message;
    }

    public @Nullable String getThreadId() {
        return threadId;
    }

    public void setThreadId(@Nullable final String threadId) {
        this.threadId = threadId;
    }

    public @Nullable String getSoundAsString() {
        return sound == null ? null : sound.getStringRepresentation();
    }

    public void setSoundAsString(@Nullable final String sound) {
        this.sound = sound == null ? null : BrrrSound.fromString(sound);
    }

    public @Nullable BrrrSound getSound() {
        return sound;
    }

    public void setSound(@Nullable final BrrrSound sound) {
        this.sound = sound;
    }

    public @Nullable String getOpenUrl() {
        return openUrl;
    }

    public void setOpenUrl(@Nullable final String openUrl) {
        this.openUrl = openUrl;
    }

    public @Nullable String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(@Nullable final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public @Nullable LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(@Nullable final LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public @Nullable String getFilterCriteria() {
        return filterCriteria;
    }

    public void setFilterCriteria(@Nullable final String filterCriteria) {
        this.filterCriteria = filterCriteria;
    }

    public @Nullable String getInterruptionLevelAsString() {
        return interruptionLevel == null ? null : interruptionLevel.getStringRepresentation();
    }

    public @Nullable BrrrInterruptionLevel getInterruptionLevel() {
        return interruptionLevel;
    }

    public void setInterruptionLevelAsString(@Nullable final String interruptionLevel) {
        this.interruptionLevel = interruptionLevel == null ? null : BrrrInterruptionLevel.fromString(interruptionLevel);
    }

    public void setInterruptionLevel(@Nullable final BrrrInterruptionLevel interruptionLevel) {
        this.interruptionLevel = interruptionLevel;
    }
}
