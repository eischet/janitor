package com.eischet.janitor.cleanup.runtime.types;

import com.eischet.janitor.cleanup.experimental.JGetter;
import com.eischet.janitor.cleanup.experimental.JSetter;

public interface JFieldAdapter<T> extends JGetter<T>, JSetter<T> {
}
