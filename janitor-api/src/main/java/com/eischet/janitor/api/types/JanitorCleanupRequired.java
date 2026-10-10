// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types;

/**
 * Implemented by objects that hold resources which must be released when the script process that created them ends,
 * e.g. open files or connections. The process keeps track of such objects and calls {@link #janitorCleanup()} on them.
 */
public interface JanitorCleanupRequired {

    /**
     * Releases any resources held by this object. Called by the script process when it is cleaned up.
     */
    void janitorCleanup();

}
