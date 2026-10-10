// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.listeners;

/** The registration of a listener, which can be used to remove the listener again. */
@FunctionalInterface
public interface ListenerRegistration {
    /** Removes the listener. */
    void remove();
}
