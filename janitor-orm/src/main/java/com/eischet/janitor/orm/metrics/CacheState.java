// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.metrics;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * The state of a DAO's entity cache, for monitoring.
 *
 * @param size    the number of entries now; -1 if the cache cannot tell
 * @param maxSize the maximum number of entries; -1 if there is none or the cache cannot tell
 * @param ttl     how long an entry stays valid; null if entries do not expire or the cache cannot tell
 */
public record CacheState(int size, int maxSize, @Nullable Duration ttl) {
}
