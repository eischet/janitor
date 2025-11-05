package com.eischet.janitor.orm.meta;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface WranglerSource {

    @NotNull EntityWrangler<?, ?> getWrangler();

}
