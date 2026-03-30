package com.eischet.janitor.modules.brrr;

import com.eischet.janitor.api.types.StringMappedEnum;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public enum BrrrSound implements StringMappedEnum {
    DEFAULT("default"),
    SYSTEM("system"),
    BRRR("brrr"),
    BELL_RINGING("bell_ringing"),
    BUBBLE_DING("bubble_ding"),
    BUBBLY_SUCCESS_DING("bubbly_success_ding"),
    CAT_MEOW("cat_meow"),
    CALM1("calm1"),
    CALM2("calm2"),
    CHA_CHING("cha_ching")  ,
    DOG_BARKING("dog_barking"),
    DOOR_BELL("door_bell"),
    DUCK_QUACK("duck_quack"),
    SHORT_TRIPLE_BLINK("short_triple_blink"),
    UPBEAT_BELLS("upbeat_bells"),
    WARM_SOFT_ERROR("warm_soft_error");

    private final String stringRepresentation;

    BrrrSound(final String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    @Override
    public @NotNull String getStringRepresentation() {
        return stringRepresentation;
    }

    public static @NotNull @Unmodifiable List<BrrrSound> SOUNDS = List.of(values());

    public static @NotNull BrrrSound fromString(final @Nullable String stringRepresentation) {
        if (stringRepresentation == null || stringRepresentation.isBlank()) {
            return DEFAULT;
        }
        return SOUNDS.stream().filter(sound -> sound.getStringRepresentation().equals(stringRepresentation)).findFirst().orElse(DEFAULT);
    }

}
