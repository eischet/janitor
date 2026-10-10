// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.repl;

/** The result of parsing text that might not be a complete statement yet: it is either OK, or INCOMPLETE if more input is needed. */
public enum PartialParseResult {OK, INCOMPLETE}
