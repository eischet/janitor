// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.types.JanitorTypedObject;

/**
 * Experimental: trying to establish JString as this intermediate interface.
 * The idea here is to replace JString the class with JString the interface and move the class
 * into the -lang package, leaving only an interface in the public API. I'm not sure if that's really
 * worth the effort.
 */
public interface JStringBase extends JanitorTypedObject<String> {
}
