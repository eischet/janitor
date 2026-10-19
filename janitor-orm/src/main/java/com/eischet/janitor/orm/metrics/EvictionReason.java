// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.metrics;

/**
 * Why an entry left an entity cache.
 */
public enum EvictionReason {
    /**
     * The cache was full and the least-recently-used entry had to make room. Many of these mean that the cache is too small.
     */
    LRU,
    /**
     * The entry was found expired when it was looked up, so the lookup became a miss. Many of these mean that the time to live is too short.
     */
    TTL
}
