package com.eischet.janitor.api.types;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public enum JDurationKind {
    YEARS("y", ChronoUnit.YEARS),
    MONTHS("mo", ChronoUnit.MONTHS),
    WEEKS("w", ChronoUnit.WEEKS),
    DAYS("d", ChronoUnit.DAYS),
    HOURS("h", ChronoUnit.HOURS),
    MINUTES("mi", ChronoUnit.MINUTES),
    SECONDS("s", ChronoUnit.SECONDS);

    public final String tag;
    public final TemporalUnit unit;

    JDurationKind(final String tag, final TemporalUnit unit) {
        this.tag = tag;
        this.unit = unit;
    }

}
